package com.ifcd0112.viewer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

/** Genera sugerencias SQL a partir del prefijo en el cursor. */
public final class SqlSuggestionEngine implements SuggestionEngine {

    private final Supplier<List<String>> historySupplier;
    private final Supplier<List<String>> schemaSupplier;

    public SqlSuggestionEngine(
            Supplier<List<String>> historySupplier,
            Supplier<List<String>> schemaSupplier) {
        this.historySupplier = historySupplier;
        this.schemaSupplier = schemaSupplier;
    }

    @Override
    public List<String> suggest(String prefix) {
        if (prefix == null) {
            return List.of();
        }
        String p = prefix.trim();
        if (p.isEmpty()) {
            return List.of();
        }
        String lower = p.toLowerCase(Locale.ROOT);
        Set<String> matches = new LinkedHashSet<>();

        for (String kw : SqlKeywords.allForCompletion()) {
            if (kw.toLowerCase(Locale.ROOT).startsWith(lower)) {
                matches.add(kw);
            }
        }

        for (String item : schemaSupplier.get()) {
            if (item != null && item.toLowerCase(Locale.ROOT).startsWith(lower)) {
                matches.add(item);
            }
        }

        for (String entry : historySupplier.get()) {
            addHistoryMatches(matches, entry, lower);
        }

        List<String> sorted = new ArrayList<>(matches);
        sorted.sort(Comparator
                .comparingInt((String s) -> score(s, p, lower))
                .thenComparing(s -> s.toLowerCase(Locale.ROOT)));
        if (sorted.size() > MAX_RESULTS) {
            return sorted.subList(0, MAX_RESULTS);
        }
        return sorted;
    }

    private static int score(String candidate, String prefix, String lowerPrefix) {
        String c = candidate.toLowerCase(Locale.ROOT);
        if (c.equals(lowerPrefix)) {
            return 0;
        }
        if (c.startsWith(lowerPrefix)) {
            if (SqlKeywords.isKeyword(candidate)) {
                return 1;
            }
            if (candidate.contains(".")) {
                return 2;
            }
            return 3;
        }
        return 10;
    }

    private static void addHistoryMatches(Set<String> matches, String entry, String lowerPrefix) {
        if (entry == null || entry.isBlank()) {
            return;
        }
        String compact = entry.replace('\n', ' ').trim();
        if (compact.toLowerCase(Locale.ROOT).startsWith(lowerPrefix)) {
            matches.add(compact);
            return;
        }
        for (String token : compact.split("[\\s,();]+")) {
            if (token.length() >= 2 && token.toLowerCase(Locale.ROOT).startsWith(lowerPrefix)) {
                matches.add(token);
            }
        }
    }
}
