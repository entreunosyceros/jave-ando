package com.ifcd0112.viewer;

import java.util.function.Supplier;
import javax.swing.text.JTextComponent;

/** Conecta autocompletado Java al editor de código. */
public final class JavaSuggestionSupport {

    private JavaSuggestionSupport() {}

    public static Handle attach(JTextComponent textArea, Supplier<String> sourceSupplier) {
        JavaSuggestionEngine engine = new JavaSuggestionEngine(sourceSupplier);
        TextAreaSuggestionPopup popup = new TextAreaSuggestionPopup(textArea, engine);
        return new Handle(popup);
    }

    public record Handle(TextAreaSuggestionPopup popup) {

        public boolean isPopupVisible() {
            return popup.isVisible();
        }

        public void applyTheme(ThemePalette palette) {
            popup.applyTheme(palette);
        }

        public void dispose() {
            popup.dispose();
        }
    }
}
