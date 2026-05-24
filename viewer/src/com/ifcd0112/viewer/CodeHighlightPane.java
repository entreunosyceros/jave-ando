package com.ifcd0112.viewer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.util.Set;

public class CodeHighlightPane extends JTextPane {

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

    private SimpleAttributeSet normal;
    private SimpleAttributeSet keyword;
    private SimpleAttributeSet string;
    private SimpleAttributeSet comment;
    private SimpleAttributeSet number;

    private String languageHint = "plain";
    private boolean highlighting;
    private boolean editingEnabled;
    private final Timer highlightTimer;
    private final UndoManager undo = new UndoManager();

    public CodeHighlightPane() {
        setEditable(false);
        setFont(UiTheme.FONT_MONO);
        refreshTheme();

        highlightTimer = new Timer(350, e -> refreshHighlight());
        highlightTimer.setRepeats(false);

        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onDocumentChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onDocumentChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onDocumentChanged();
            }
        });
    }

    /** Activa edición con resaltado de sintaxis (actualización tras una breve pausa al escribir). */
    public void enableEditing(String languageHint) {
        this.languageHint = languageHint != null ? languageHint : "plain";
        editingEnabled = true;
        setEditable(true);
        getDocument().addUndoableEditListener(ev -> undo.addEdit(ev.getEdit()));
        installUndoRedo();
    }

    public void refreshTheme() {
        ThemePalette p = UiTheme.palette();
        setBackground(p.consoleBg());
        setCaretColor(p.consoleText());
        setSelectionColor(p.listSel());
        setSelectedTextColor(p.listSelText());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(p.border()),
                BorderFactory.createEmptyBorder(11, 13, 11, 13)
        ));
        normal = styled(p.consoleText());
        if (p.darkMode()) {
            keyword = styled(new Color(0x7D, 0xD3, 0xFC));
            string = styled(new Color(0x86, 0xEF, 0xAC));
            comment = styled(new Color(0x94, 0xA3, 0xB8));
            number = styled(new Color(0xFC, 0xA5, 0xA5));
        } else {
            keyword = styled(new Color(0x1D, 0x4E, 0xD8));
            string = styled(new Color(0x04, 0x78, 0x57));
            comment = styled(new Color(0x64, 0x74, 0x8B));
            number = styled(new Color(0xC2, 0x41, 0x0C));
        }
    }

    private SimpleAttributeSet styled(Color color) {
        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setForeground(a, color);
        StyleConstants.setFontFamily(a, "Consolas");
        StyleConstants.setFontSize(a, 13);
        return a;
    }

    public void setCode(String code, String languageHint) {
        this.languageHint = languageHint != null ? languageHint : "plain";
        applyHighlightedText(code);
    }

    private void onDocumentChanged() {
        if (!editingEnabled || highlighting) {
            return;
        }
        highlightTimer.restart();
    }

    private void refreshHighlight() {
        if (!editingEnabled || highlighting) {
            return;
        }
        int caret = getCaretPosition();
        String plain = getText();
        applyHighlightedText(plain);
        int len = getDocument().getLength();
        setCaretPosition(Math.min(caret, Math.max(0, len - 1)));
    }

    private void applyHighlightedText(String code) {
        highlighting = true;
        try {
            refreshTheme();
            Language lang = detectLanguage(code, languageHint);
            StyledDocument doc = getStyledDocument();
            doc.remove(0, doc.getLength());
            for (String line : code.split("\n", -1)) {
                appendLine(doc, line, lang);
                doc.insertString(doc.getLength(), "\n", normal);
            }
            if (doc.getLength() > 0) {
                doc.remove(doc.getLength() - 1, 1);
            }
        } catch (BadLocationException e) {
            setText(code);
        } finally {
            highlighting = false;
        }
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

    private enum Language { JAVA, SQL, PLAIN }

    private Language detectLanguage(String code, String hint) {
        if (hint != null) {
            String h = hint.toLowerCase();
            if (h.contains("java")) return Language.JAVA;
            if (h.contains("sql")) return Language.SQL;
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
            if (Character.isJavaIdentifierStart(c)) {
                int start = i;
                while (i < line.length() && Character.isJavaIdentifierPart(line.charAt(i))) {
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
}
