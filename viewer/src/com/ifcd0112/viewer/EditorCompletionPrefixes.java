package com.ifcd0112.viewer;

import javax.swing.text.JTextComponent;

/** Utilidades para el prefijo de autocompletado en el cursor. */
public final class EditorCompletionPrefixes {

    private EditorCompletionPrefixes() {}

    public static String prefixAt(JTextComponent field) {
        try {
            int caret = field.getCaretPosition();
            String text = field.getText(0, caret);
            int end = text.length();
            int start = end;
            while (start > 0) {
                char c = text.charAt(start - 1);
                if (Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '`') {
                    start--;
                } else {
                    break;
                }
            }
            String prefix = text.substring(start, end);
            if (prefix.startsWith("`") && prefix.endsWith("`") && prefix.length() > 2) {
                return prefix.substring(1, prefix.length() - 1);
            }
            return prefix;
        } catch (Exception e) {
            return "";
        }
    }
}
