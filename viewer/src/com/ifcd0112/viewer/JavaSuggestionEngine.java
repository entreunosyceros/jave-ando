package com.ifcd0112.viewer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Sugerencias Java: palabras clave, plantillas, miembros y símbolos del editor. */
public final class JavaSuggestionEngine implements SuggestionEngine {

    private static final Map<String, List<String>> MEMBERS = memberMap();
    private static final Pattern CLASS_NAME = Pattern.compile(
            "\\b(?:class|interface|record|enum)\\s+(\\w+)");
    private static final Pattern FIELD = Pattern.compile(
            "\\b(?:var|boolean|byte|char|double|float|int|long|short|String|Object|\\w+)\\s+(\\w+)\\s*[=;]");
    private static final Pattern METHOD = Pattern.compile(
            "\\b(?:public|private|protected|static|\\s)+[\\w<>,\\[\\]\\s]+\\s+(\\w+)\\s*\\(");

    private final Supplier<String> sourceSupplier;

    public JavaSuggestionEngine(Supplier<String> sourceSupplier) {
        this.sourceSupplier = sourceSupplier;
    }

    @Override
    public List<String> suggest(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return List.of();
        }
        String lower = prefix.toLowerCase(Locale.ROOT);
        Set<String> matches = new LinkedHashSet<>();

        int dot = prefix.lastIndexOf('.');
        if (dot >= 0) {
            String qualifier = prefix.substring(0, dot);
            String memberPrefix = prefix.substring(dot + 1);
            addMemberMatches(matches, qualifier, memberPrefix);
        } else {
            for (String item : JavaKeywords.allForCompletion()) {
                if (item.toLowerCase(Locale.ROOT).startsWith(lower)) {
                    matches.add(item);
                }
            }
            for (String id : scanIdentifiers(sourceSupplier.get())) {
                if (id.toLowerCase(Locale.ROOT).startsWith(lower) && !JavaKeywords.isReservedOrType(id)) {
                    matches.add(id);
                }
            }
        }

        List<String> sorted = new ArrayList<>(matches);
        sorted.sort(Comparator
                .comparingInt((String s) -> score(s, prefix, lower, dot >= 0))
                .thenComparing(s -> s.toLowerCase(Locale.ROOT)));
        if (sorted.size() > MAX_RESULTS) {
            return sorted.subList(0, MAX_RESULTS);
        }
        return sorted;
    }

    private static void addMemberMatches(Set<String> matches, String qualifier, String memberPrefix) {
        String lowerMember = memberPrefix.toLowerCase(Locale.ROOT);
        List<String> members = MEMBERS.get(qualifier);
        if (members == null) {
            for (Map.Entry<String, List<String>> e : MEMBERS.entrySet()) {
                if (e.getKey().equalsIgnoreCase(qualifier)) {
                    members = e.getValue();
                    break;
                }
            }
        }
        if (members == null) {
            return;
        }
        for (String member : members) {
            if (member.toLowerCase(Locale.ROOT).startsWith(lowerMember)) {
                matches.add(qualifier + "." + formatMember(member));
            }
        }
    }

    private static String formatMember(String member) {
        if (member.endsWith("(") || member.endsWith("();")) {
            return member;
        }
        return member;
    }

    private static int score(String candidate, String prefix, String lowerPrefix, boolean memberMode) {
        String c = candidate.toLowerCase(Locale.ROOT);
        if (c.equals(lowerPrefix) || c.equals(prefix.toLowerCase(Locale.ROOT))) {
            return 0;
        }
        if (memberMode && c.startsWith(prefix.toLowerCase(Locale.ROOT))) {
            return 1;
        }
        if (c.startsWith(lowerPrefix)) {
            if (JavaKeywords.isKeyword(candidate)) {
                return 2;
            }
            return 3;
        }
        return 10;
    }

    static Set<String> scanIdentifiers(String code) {
        Set<String> ids = new LinkedHashSet<>();
        if (code == null || code.isBlank()) {
            return ids;
        }
        collectMatches(CLASS_NAME, code, ids);
        collectMatches(FIELD, code, ids);
        collectMatches(METHOD, code, ids);
        return ids;
    }

    private static void collectMatches(Pattern pattern, String code, Set<String> ids) {
        Matcher m = pattern.matcher(code);
        while (m.find()) {
            String name = m.group(1);
            if (name != null && !name.isBlank() && Character.isJavaIdentifierStart(name.charAt(0))) {
                ids.add(name);
            }
        }
    }

    private static Map<String, List<String>> memberMap() {
        Map<String, List<String>> m = new LinkedHashMap<>();
        m.put("System", List.of("out", "in", "err"));
        m.put("System.out", List.of("println(", "print(", "printf("));
        m.put("System.in", List.of());
        m.put("String", List.of(
                "length()", "charAt(", "equals(", "equalsIgnoreCase(", "substring(",
                "toLowerCase()", "toUpperCase()", "trim()", "isEmpty()", "isBlank()",
                "split(", "valueOf(", "formatted(", "contains(", "startsWith(", "endsWith("));
        m.put("List", List.of("add(", "get(", "set(", "remove(", "size()", "isEmpty()", "clear()", "contains("));
        m.put("ArrayList", List.of("add(", "get(", "size()", "isEmpty()", "clear()"));
        m.put("Map", List.of("put(", "get(", "remove(", "containsKey(", "keySet()", "values()", "size()", "isEmpty()"));
        m.put("HashMap", List.of("put(", "get(", "remove(", "size()", "isEmpty()"));
        m.put("Optional", List.of("of(", "ofNullable(", "empty()", "isPresent()", "orElse(", "orElseGet("));
        m.put("Scanner", List.of("next()", "nextLine()", "nextInt()", "nextDouble()", "hasNext()", "close()"));
        m.put("Connection", List.of(
                "createStatement()", "prepareStatement(", "close()", "setAutoCommit(",
                "commit()", "rollback()"));
        m.put("Statement", List.of("executeQuery(", "executeUpdate(", "execute(", "close()"));
        m.put("PreparedStatement", List.of(
                "executeQuery()", "executeUpdate()", "setString(", "setInt(", "setDouble(", "close()"));
        m.put("ResultSet", List.of(
                "next()", "getString(", "getInt(", "getDouble(", "getBoolean(", "close()"));
        m.put("Arrays", List.of("asList(", "sort(", "toString(", "equals("));
        m.put("Collections", List.of("sort(", "reverse(", "shuffle(", "max(", "min("));
        m.put("Math", List.of("max(", "min(", "abs(", "sqrt(", "pow(", "round("));
        m.put("Integer", List.of("parseInt(", "valueOf(", "toString("));
        m.put("Double", List.of("parseDouble(", "valueOf("));
        m.put("Objects", List.of("equals(", "requireNonNull("));
        return Map.copyOf(m);
    }
}
