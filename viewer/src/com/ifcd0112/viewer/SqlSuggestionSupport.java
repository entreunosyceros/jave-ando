package com.ifcd0112.viewer;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.text.JTextComponent;

/** Conecta autocompletado SQL al editor de código. */
public final class SqlSuggestionSupport {

    private SqlSuggestionSupport() {}

    public static Handle attach(JTextComponent textArea, Path projectRoot, Supplier<List<String>> historySupplier) {
        SqlSchemaSuggestProvider schema = new SqlSchemaSuggestProvider(projectRoot);
        schema.refresh();
        SqlSuggestionEngine engine = new SqlSuggestionEngine(
                historySupplier != null ? historySupplier : List::of,
                schema::suggestions);
        TextAreaSuggestionPopup popup = new TextAreaSuggestionPopup(textArea, engine);
        return new Handle(schema, popup);
    }

    public record Handle(SqlSchemaSuggestProvider schema, TextAreaSuggestionPopup popup) {

        public void refreshSchema() {
            schema.refresh();
        }

        public boolean isPopupVisible() {
            return popup.isVisible();
        }

        public void applyTheme(ThemePalette palette) {
            popup.applyTheme(palette);
        }

        public void dispose() {
            schema.cancel();
            popup.dispose();
        }
    }
}
