package com.ifcd0112.viewer;

import javax.swing.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Editor de código para el visor. Usa {@link JTextArea} (texto plano) para que
 * el teclado español y las tecclas muertas (´ + a → á) funcionen correctamente;
 * {@link JTextPane} con estilos mezclados inserta el apóstrofo literal ('a).
 */
public class CodeHighlightPane extends JTextArea {

    private final UndoManager undo = new UndoManager();
    private boolean undoReady;

    public CodeHighlightPane() {
        setEditable(false);
        setFont(UiTheme.FONT_MONO);
        setTabSize(4);
        setLineWrap(false);
        setWrapStyleWord(false);
        PlatformSupport.configureKeyboardInput(this);
        refreshTheme();
    }

    public void enableEditing(String languageHint) {
        setEditable(true);
        if (!undoReady) {
            undoReady = true;
            getDocument().addUndoableEditListener(ev -> undo.addEdit(ev.getEdit()));
            installUndoRedo();
        }
    }

    public void setCode(String code, String languageHint) {
        setText(code != null ? code : "");
        setCaretPosition(0);
    }

    /** Sin resaltado en vivo: solo actualiza colores del tema. */
    public void refreshHighlightNow() {
        refreshTheme();
    }

    public void refreshTheme() {
        ThemePalette p = UiTheme.palette();
        setBackground(p.consoleBg());
        setForeground(p.consoleText());
        setCaretColor(p.consoleText());
        setSelectionColor(p.listSel());
        setSelectedTextColor(p.listSelText());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(p.border()),
                BorderFactory.createEmptyBorder(11, 13, 11, 13)
        ));
    }

    private void installUndoRedo() {
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, mask), "undo");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, mask), "redo");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, mask | InputEvent.SHIFT_DOWN_MASK), "redo");
        getActionMap().put("undo", new AbstractAction() {
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
        getActionMap().put("redo", new AbstractAction() {
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
}
