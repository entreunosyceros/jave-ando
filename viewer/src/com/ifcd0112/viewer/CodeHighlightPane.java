package com.ifcd0112.viewer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.*;
import java.text.AttributedCharacterIterator;
import java.util.*;
import java.util.List;

/**
 * Editor con resaltado de sintaxis, teclado español y marcadores de error.
 */
public class CodeHighlightPane extends JPanel {

    private static final String CARD_EDIT = "edit";
    private static final String CARD_VIEW = "view";

    private static final Set<String> JAVA_KEYWORDS = Set.of(
            "abstract", "boolean", "break", "byte", "case", "catch", "char", "class", "continue",
            "default", "do", "double", "else", "extends", "final", "finally", "float", "for",
            "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "null", "package", "private", "protected", "public", "return", "short",
            "static", "super", "switch", "synchronized", "this", "throw", "throws", "try",
            "void", "volatile", "while", "true", "false", "var", "record"
    );

    private static final Set<String> SQL_KEYWORDS = Set.of(
            "SELECT", "FROM", "WHERE", "INSERT", "INTO", "UPDATE", "DELETE", "CREATE", "TABLE",
            "DROP", "ALTER", "JOIN", "INNER", "LEFT", "RIGHT", "ON", "GROUP", "BY", "ORDER",
            "HAVING", "AS", "AND", "OR", "NOT", "NULL", "VALUES", "SET", "PRIMARY", "KEY",
            "FOREIGN", "REFERENCES", "CONSTRAINT", "INDEX", "VIEW", "UNION", "COUNT", "AVG",
            "SUM", "DISTINCT", "LIMIT", "USE", "DATABASE", "IF", "EXISTS", "COMMIT", "ROLLBACK",
            "START", "TRANSACTION", "CURRENT_DATE", "DATE_SUB", "INTERVAL", "DUPLICATE"
    );

    private final JTextArea editor = new JTextArea();
    private final JTextPane view = new JTextPane();
    private final JPanel cardPanel = new JPanel(new CardLayout());
    private final JScrollPane codeScroll;
    private final ErrorGutter gutter;
    private final JList<String> errorList = new JList<>();
    private final DefaultListModel<String> errorListModel = new DefaultListModel<>();
    private final JScrollPane errorScroll;

    private SimpleAttributeSet normal;
    private SimpleAttributeSet keyword;
    private SimpleAttributeSet string;
    private SimpleAttributeSet comment;
    private SimpleAttributeSet number;
    private SimpleAttributeSet errorLine;

    private String languageHint = "plain";
    private boolean editingEnabled;
    private boolean highlighting;
    private boolean inputMethodComposing;
    private boolean undoReady;
    private final UndoManager undo = new UndoManager();
    private List<CodeDiagnostic> diagnostics = List.of();
    private Map<Integer, List<CodeDiagnostic>> diagnosticsByLine = Map.of();
    private Runnable afterEditHook;
    private Color gutterErrorColor = new Color(0xDC, 0x26, 0x26);
    private Color gutterTextColor = new Color(0x94, 0xA3, 0xB8);

    public CodeHighlightPane() {
        setLayout(new BorderLayout(0, 0));

        editor.setEditable(false);
        editor.setFont(UiTheme.FONT_MONO);
        editor.setTabSize(4);
        editor.setLineWrap(false);
        editor.setWrapStyleWord(false);
        PlatformSupport.configureKeyboardInput(editor);

        view.setEditable(false);
        view.setFocusable(false);
        view.setFont(UiTheme.FONT_MONO);
        view.setContentType("text/plain");

        cardPanel.add(editor, CARD_EDIT);
        cardPanel.add(view, CARD_VIEW);

        codeScroll = new JScrollPane(cardPanel);
        codeScroll.setBorder(BorderFactory.createEmptyBorder());
        codeScroll.getVerticalScrollBar().setUnitIncrement(16);
        gutter = new ErrorGutter();
        codeScroll.setRowHeaderView(gutter);
        codeScroll.getVerticalScrollBar().addAdjustmentListener(e -> gutter.repaint());
        editor.addCaretListener(e -> gutter.repaint());

        view.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showEditorAt(e.getPoint());
            }
        });

        editor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!editingEnabled || highlighting || inputMethodComposing) {
                    return;
                }
                if (e.isTemporary()) {
                    return;
                }
                showHighlightedView();
            }
        });

        editor.addInputMethodListener(new InputMethodListener() {
            @Override
            public void inputMethodTextChanged(InputMethodEvent e) {
                AttributedCharacterIterator text = e.getText();
                inputMethodComposing = text != null
                        && e.getCommittedCharacterCount() < text.getEndIndex() - text.getBeginIndex();
            }

            @Override
            public void caretPositionChanged(InputMethodEvent e) {
                // no-op
            }
        });

        errorList.setModel(errorListModel);
        errorList.setFont(UiTheme.FONT_UI.deriveFont(12f));
        errorList.setVisibleRowCount(3);
        errorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        errorList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int idx = errorList.getSelectedIndex();
            if (idx >= 0 && idx < diagnostics.size()) {
                jumpToLine(diagnostics.get(idx).line());
            }
        });

        errorScroll = new JScrollPane(errorList);
        errorScroll.setBorder(BorderFactory.createTitledBorder("Errores y avisos"));
        errorScroll.setPreferredSize(new Dimension(100, 88));
        errorScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        errorScroll.setVisible(false);

        add(codeScroll, BorderLayout.CENTER);
        add(errorScroll, BorderLayout.SOUTH);

        refreshTheme();
        showCard(CARD_VIEW);
    }

    public void setAfterEditHook(Runnable hook) {
        this.afterEditHook = hook;
    }

    public void setDiagnostics(List<CodeDiagnostic> items) {
        diagnostics = items != null ? List.copyOf(items) : List.of();
        Map<Integer, List<CodeDiagnostic>> map = new TreeMap<>();
        for (CodeDiagnostic d : diagnostics) {
            map.computeIfAbsent(d.line(), k -> new ArrayList<>()).add(d);
        }
        diagnosticsByLine = map;
        refreshErrorListUi();
        applyEditorErrorHighlights();
        gutter.repaint();
        if (!editor.isFocusOwner()) {
            applyHighlightedText(editor.getText());
        }
    }

    public List<CodeDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    public void jumpToLine(int line1Based) {
        int line = Math.max(1, line1Based);
        showCard(CARD_EDIT);
        editor.requestFocusInWindow();
        try {
            int offset = editor.getLineStartOffset(line - 1);
            editor.setCaretPosition(offset);
            editor.moveCaretPosition(editor.getLineEndOffset(line - 1));
            Rectangle rect = editor.modelToView2D(offset).getBounds();
            editor.scrollRectToVisible(rect);
        } catch (BadLocationException ignored) {
            editor.setCaretPosition(0);
        }
    }

    public void enableEditing(String languageHint) {
        this.languageHint = languageHint != null ? languageHint : "plain";
        editingEnabled = true;
        editor.setEditable(true);
        if (!undoReady) {
            undoReady = true;
            editor.getDocument().addUndoableEditListener(ev -> undo.addEdit(ev.getEdit()));
            installUndoRedo();
        }
        setToolTipText("Escribe con tildes y ñ. Al salir del editor se colorean y comprueban errores.");
    }

    public void setCode(String code, String languageHint) {
        this.languageHint = languageHint != null ? languageHint : "plain";
        inputMethodComposing = false;
        String text = code != null ? code : "";
        editor.setText(text);
        editor.setCaretPosition(0);
        applyHighlightedText(text);
        applyEditorErrorHighlights();
        showCard(editingEnabled && editor.isFocusOwner() ? CARD_EDIT : CARD_VIEW);
        gutter.repaint();
    }

    public String getText() {
        return editor.getText();
    }

    public void refreshHighlightNow() {
        applyHighlightedText(editor.getText());
        applyEditorErrorHighlights();
        if (!editor.isFocusOwner()) {
            showCard(CARD_VIEW);
        }
        gutter.repaint();
    }

    public void refreshTheme() {
        ThemePalette p = UiTheme.palette();
        Border border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(p.border()),
                BorderFactory.createEmptyBorder(11, 13, 11, 13)
        );
        setBackground(p.consoleBg());
        setBorder(border);

        editor.setBackground(p.consoleBg());
        editor.setForeground(p.consoleText());
        editor.setCaretColor(p.consoleText());
        editor.setSelectionColor(p.listSel());
        editor.setSelectedTextColor(p.listSelText());
        editor.setBorder(BorderFactory.createEmptyBorder());

        view.setBackground(p.consoleBg());
        view.setCaretColor(p.consoleText());
        view.setSelectionColor(p.listSel());
        view.setSelectedTextColor(p.listSelText());
        view.setBorder(BorderFactory.createEmptyBorder());

        codeScroll.getViewport().setBackground(p.consoleBg());
        gutter.setBackground(p.consoleBg());
        gutterTextColor = p.textMuted();
        gutterErrorColor = p.darkMode() ? new Color(0xFC, 0xA5, 0xA5) : new Color(0xDC, 0x26, 0x26);

        normal = styled(p.consoleText());
        if (p.darkMode()) {
            keyword = styled(new Color(0x7D, 0xD3, 0xFC));
            string = styled(new Color(0x86, 0xEF, 0xAC));
            comment = styled(new Color(0x94, 0xA3, 0xB8));
            number = styled(new Color(0xFC, 0xA5, 0xA5));
            errorLine = styled(new Color(0xFE, 0xCA, 0xCA));
            StyleConstants.setBackground(errorLine, new Color(0x45, 0x1A, 0x1A));
        } else {
            keyword = styled(new Color(0x1D, 0x4E, 0xD8));
            string = styled(new Color(0x04, 0x78, 0x57));
            comment = styled(new Color(0x64, 0x74, 0x8B));
            number = styled(new Color(0xC2, 0x41, 0x0C));
            errorLine = styled(new Color(0x99, 0x1B, 0x1B));
            StyleConstants.setBackground(errorLine, new Color(0xFE, 0xE2, 0xE2));
        }

        errorList.setBackground(p.surface());
        errorList.setForeground(p.text());

        if (!editor.isFocusOwner()) {
            applyHighlightedText(editor.getText());
        }
        applyEditorErrorHighlights();
        gutter.repaint();
    }

    private void refreshErrorListUi() {
        errorListModel.clear();
        for (CodeDiagnostic d : diagnostics) {
            String prefix = d.severity() == CodeDiagnostic.Severity.WARNING ? "Aviso" : "Error";
            String file = d.sourceFile() != null ? " (" + d.sourceFile() + ")" : "";
            errorListModel.addElement(prefix + " línea " + d.line() + file + ": " + d.message());
        }
        errorScroll.setVisible(!diagnostics.isEmpty());
    }

    private void applyEditorErrorHighlights() {
        Highlighter hl = editor.getHighlighter();
        if (!(hl instanceof DefaultHighlighter defHl)) {
            return;
        }
        defHl.removeAllHighlights();
        Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(
                new Color(0xFE, 0xE2, 0xE2, 200)
        );
        for (CodeDiagnostic d : diagnostics) {
            try {
                int start = editor.getLineStartOffset(d.line() - 1);
                int end = editor.getLineEndOffset(d.line() - 1);
                defHl.addHighlight(start, end, painter);
            } catch (BadLocationException ignored) {
            }
        }
    }

    private void showEditorAt(Point clickInView) {
        showCard(CARD_EDIT);
        editor.requestFocusInWindow();
        int pos = view.viewToModel2D(clickInView);
        if (pos >= 0) {
            editor.setCaretPosition(Math.min(pos, editor.getDocument().getLength()));
        }
    }

    private void showHighlightedView() {
        int caret = editor.getCaretPosition();
        applyHighlightedText(editor.getText());
        showCard(CARD_VIEW);
        int len = view.getDocument().getLength();
        view.setCaretPosition(Math.min(caret, Math.max(0, len - 1)));
        gutter.repaint();
        if (afterEditHook != null) {
            afterEditHook.run();
        }
    }

    private void showCard(String name) {
        ((CardLayout) cardPanel.getLayout()).show(cardPanel, name);
        gutter.repaint();
        revalidate();
    }

    private boolean lineHasError(int line1Based) {
        return diagnosticsByLine.containsKey(line1Based);
    }

    private SimpleAttributeSet styled(Color color) {
        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setForeground(a, color);
        StyleConstants.setFontFamily(a, editor.getFont().getFamily());
        StyleConstants.setFontSize(a, editor.getFont().getSize());
        return a;
    }

    private void applyHighlightedText(String code) {
        highlighting = true;
        try {
            Language lang = detectLanguage(code, languageHint);
            StyledDocument doc = view.getStyledDocument();
            doc.remove(0, doc.getLength());
            int lineNo = 1;
            for (String line : code.split("\n", -1)) {
                int lineStart = doc.getLength();
                appendLine(doc, line, lang);
                int lineEnd = doc.getLength();
                if (lineHasError(lineNo)) {
                    doc.setCharacterAttributes(lineStart, lineEnd - lineStart, errorLine, true);
                }
                doc.insertString(doc.getLength(), "\n", normal);
                lineNo++;
            }
            if (doc.getLength() > 0) {
                doc.remove(doc.getLength() - 1, 1);
            }
        } catch (BadLocationException e) {
            view.setText(code);
        } finally {
            highlighting = false;
        }
    }

    private void installUndoRedo() {
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        editor.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, mask), "undo");
        editor.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, mask), "redo");
        editor.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, mask | InputEvent.SHIFT_DOWN_MASK), "redo");
        editor.getActionMap().put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undo.canUndo()) {
                    try {
                        undo.undo();
                    } catch (CannotUndoException ignored) {
                    }
                }
            }
        });
        editor.getActionMap().put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undo.canRedo()) {
                    try {
                        undo.redo();
                    } catch (CannotRedoException ignored) {
                    }
                }
            }
        });
    }

    private enum Language { JAVA, SQL, PLAIN }

    private Language detectLanguage(String code, String hint) {
        if (hint != null) {
            String h = hint.toLowerCase();
            if (h.contains("java")) return Language.JAVA;
            if (h.contains("sql")) return Language.SQL;
            if (h.contains("markdown")) return Language.PLAIN;
        }
        if (code.contains("public class") || code.contains("import java.")) return Language.JAVA;
        if (code.toUpperCase().contains("SELECT ") || code.toUpperCase().contains("CREATE TABLE")) {
            return Language.SQL;
        }
        return Language.PLAIN;
    }

    private void appendLine(StyledDocument doc, String line, Language lang) throws BadLocationException {
        if (lang == Language.PLAIN) {
            doc.insertString(doc.getLength(), line, normal);
            return;
        }
        if (lang == Language.JAVA && line.trim().startsWith("//")) {
            doc.insertString(doc.getLength(), line, comment);
            return;
        }
        if (lang == Language.SQL && line.trim().startsWith("--")) {
            doc.insertString(doc.getLength(), line, comment);
            return;
        }

        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (c == '"' || c == '\'') {
                int end = findClosingQuote(line, i, c);
                doc.insertString(doc.getLength(), line.substring(i, end), string);
                i = end;
                continue;
            }
            if (Character.isDigit(c)) {
                int start = i;
                while (i < line.length() && (Character.isDigit(line.charAt(i)) || line.charAt(i) == '.')) {
                    i++;
                }
                doc.insertString(doc.getLength(), line.substring(start, i), number);
                continue;
            }
            if (isWordChar(c)) {
                int start = i;
                while (i < line.length() && isWordChar(line.charAt(i))) {
                    i++;
                }
                String word = line.substring(start, i);
                boolean isKw = lang == Language.JAVA
                        ? JAVA_KEYWORDS.contains(word)
                        : SQL_KEYWORDS.contains(word.toUpperCase());
                doc.insertString(doc.getLength(), word, isKw ? keyword : normal);
                continue;
            }
            doc.insertString(doc.getLength(), String.valueOf(c), normal);
            i++;
        }
    }

    private static boolean isWordChar(char c) {
        return Character.isJavaIdentifierPart(c) || Character.isLetter(c) || c == '_';
    }

    private int findClosingQuote(String line, int start, char quote) {
        int i = start + 1;
        while (i < line.length()) {
            if (line.charAt(i) == quote && line.charAt(i - 1) != '\\') {
                return i + 1;
            }
            i++;
        }
        return line.length();
    }

    private final class ErrorGutter extends JPanel {
        ErrorGutter() {
            setFont(editor.getFont());
        }

        @Override
        public Dimension getPreferredSize() {
            int lines = Math.max(1, editor.getLineCount());
            int digits = String.valueOf(lines).length();
            return new Dimension(16 + digits * 9, editor.getHeight());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int lineHeight = editor.getFontMetrics(editor.getFont()).getHeight();
            int ascent = fm.getAscent();

            Rectangle clip = g2.getClipBounds();
            int y0 = clip.y;
            int firstLine = Math.max(0, y0 / lineHeight);
            int lastLine = Math.min(editor.getLineCount() - 1, (clip.y + clip.height) / lineHeight + 1);

            for (int line = firstLine; line <= lastLine; line++) {
                int y = line * lineHeight + ascent;
                int lineNo = line + 1;
                if (lineHasError(lineNo)) {
                    g2.setColor(gutterErrorColor);
                    g2.fillOval(2, y - ascent + 2, 8, 8);
                }
                g2.setColor(gutterTextColor);
                String num = String.valueOf(lineNo);
                int x = getWidth() - fm.stringWidth(num) - 4;
                g2.drawString(num, x, y);
            }
            g2.dispose();
        }
    }

}
