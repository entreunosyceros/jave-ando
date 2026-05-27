package com.ifcd0112.viewer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

/**
 * Popup de sugerencias para el editor: Ctrl+Espacio o escritura con prefijo.
 */
public final class TextAreaSuggestionPopup {

    private static final int POPUP_ROWS = 8;
    private static final int AUTO_DELAY_MS = 180;

    private final JTextComponent textArea;
    private final SuggestionEngine engine;
    private final JPopupMenu popup = new JPopupMenu();
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> list = new JList<>(listModel);
    private final JScrollPane listScroll;
    private final Timer autoTimer;
    private String activePrefix = "";

    private final KeyAdapter keyAdapter = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!popup.isVisible()) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE
                        && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
                    e.consume();
                    showForCaret(true);
                }
                return;
            }
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ESCAPE -> {
                    hide();
                    e.consume();
                }
                case KeyEvent.VK_UP -> {
                    moveSelection(-1);
                    e.consume();
                }
                case KeyEvent.VK_DOWN -> {
                    moveSelection(1);
                    e.consume();
                }
                case KeyEvent.VK_TAB, KeyEvent.VK_ENTER -> {
                    acceptSelected();
                    e.consume();
                }
                default -> { }
            }
        }
    };

    private final DocumentListener documentListener = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            scheduleAuto();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            scheduleAuto();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            scheduleAuto();
        }
    };

    public TextAreaSuggestionPopup(JTextComponent textArea, SuggestionEngine engine) {
        this.textArea = textArea;
        this.engine = engine;

        list.setFont(UiTheme.FONT_MONO.deriveFont(12f));
        list.setFocusable(false);
        list.setCellRenderer(new DefaultListCellRenderer());
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    acceptSelected();
                }
            }
        });

        listScroll = new JScrollPane(list);
        listScroll.setBorder(null);
        listScroll.setPreferredSize(new Dimension(320, 22 * POPUP_ROWS));
        popup.add(listScroll);
        popup.setFocusable(false);

        autoTimer = new Timer(AUTO_DELAY_MS, e -> showForCaret(false));
        autoTimer.setRepeats(false);

        textArea.addKeyListener(keyAdapter);
        textArea.getDocument().addDocumentListener(documentListener);
    }

    public boolean isVisible() {
        return popup.isVisible();
    }

    public void dispose() {
        autoTimer.stop();
        hide();
        textArea.removeKeyListener(keyAdapter);
        textArea.getDocument().removeDocumentListener(documentListener);
    }

    public void hide() {
        popup.setVisible(false);
        activePrefix = "";
    }

    public void applyTheme(ThemePalette p) {
        list.setBackground(p.consoleBg());
        list.setForeground(p.consoleText());
        list.setSelectionBackground(p.listSel());
        list.setSelectionForeground(p.listSelText());
        listScroll.getViewport().setBackground(p.consoleBg());
        popup.setBackground(p.consoleBg());
    }

    private void scheduleAuto() {
        if (popup.isVisible()) {
            SwingUtilities.invokeLater(() -> showForCaret(false));
            return;
        }
        autoTimer.restart();
    }

    private void showForCaret(boolean force) {
        String prefix = EditorCompletionPrefixes.prefixAt(textArea);
        if (!force && prefix.length() < SuggestionEngine.MIN_PREFIX_AUTO) {
            hide();
            return;
        }
        List<String> items = engine.suggest(prefix);
        if (items.isEmpty()) {
            hide();
            return;
        }
        if (!force && items.size() == 1 && items.get(0).equalsIgnoreCase(prefix)) {
            hide();
            return;
        }

        activePrefix = prefix;
        listModel.clear();
        for (String item : items) {
            listModel.addElement(item);
        }
        list.setSelectedIndex(0);
        list.ensureIndexIsVisible(0);

        Point location = caretScreenLocation();
        popup.pack();
        popup.show(textArea, location.x, location.y);
    }

    private Point caretScreenLocation() {
        try {
            int caret = textArea.getCaretPosition();
            Rectangle view = textArea.modelToView2D(caret).getBounds();
            int y = view.y + view.height;
            int x = view.x;
            if (y + popup.getPreferredSize().height > textArea.getHeight()) {
                y = Math.max(0, view.y - popup.getPreferredSize().height);
            }
            return new Point(Math.max(0, x), y);
        } catch (BadLocationException e) {
            return new Point(0, textArea.getFontMetrics(textArea.getFont()).getHeight());
        }
    }

    private void moveSelection(int delta) {
        int idx = list.getSelectedIndex();
        if (idx < 0) {
            idx = 0;
        }
        idx = Math.max(0, Math.min(listModel.getSize() - 1, idx + delta));
        list.setSelectedIndex(idx);
        list.ensureIndexIsVisible(idx);
    }

    private void acceptSelected() {
        String selected = list.getSelectedValue();
        if (selected == null && !listModel.isEmpty()) {
            selected = listModel.getElementAt(0);
        }
        if (selected == null) {
            hide();
            return;
        }
        insertCompletion(selected);
        hide();
    }

    private void insertCompletion(String completion) {
        int caret = textArea.getCaretPosition();
        String prefix = EditorCompletionPrefixes.prefixAt(textArea);
        int start = Math.max(0, caret - prefix.length());
        try {
            if (caret > start) {
                String typed = textArea.getText(start, caret - start);
                if (!typed.equals(prefix) && !typed.equalsIgnoreCase(prefix)) {
                    start = caret - typed.length();
                    prefix = typed;
                }
            }
        } catch (BadLocationException ignored) {
            // usar start calculado con prefixAt
        }
        try {
            if (caret > start) {
                textArea.getDocument().remove(start, caret - start);
            }
            textArea.getDocument().insertString(start, completion, null);
            textArea.setCaretPosition(start + completion.length());
        } catch (BadLocationException ignored) {
            // sin cambios
        }
    }
}
