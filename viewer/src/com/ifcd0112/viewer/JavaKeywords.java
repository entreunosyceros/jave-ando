package com.ifcd0112.viewer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Palabras reservadas, tipos habituales y plantillas Java para resaltado y autocompletado. */
public final class JavaKeywords {

    public static final Set<String> RESERVED = Set.of(
            "abstract", "boolean", "break", "byte", "case", "catch", "char", "class", "continue",
            "default", "do", "double", "else", "extends", "final", "finally", "float", "for",
            "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "null", "package", "private", "protected", "public", "return", "short",
            "static", "super", "switch", "synchronized", "this", "throw", "throws", "try",
            "void", "volatile", "while", "true", "false", "var", "record", "enum", "permits",
            "sealed", "yield", "assert"
    );

    private static final List<String> PRIMITIVES_AND_TYPES = List.of(
            "boolean", "byte", "char", "double", "float", "int", "long", "short", "void",
            "String", "Object", "Integer", "Double", "Boolean", "Long", "Character",
            "List", "ArrayList", "LinkedList", "Set", "HashSet", "Map", "HashMap",
            "Optional", "Scanner", "Exception", "RuntimeException", "IOException",
            "SQLException", "Connection", "Statement", "PreparedStatement", "ResultSet",
            "DriverManager", "Arrays", "Collections", "Math", "Objects", "System"
    );

    private static final List<String> SNIPPETS = List.of(
            "public class ",
            "public static void main(String[] args) {\n    \n}",
            "public static void ",
            "private ",
            "protected ",
            "public ",
            "import java.util.",
            "import java.util.List;",
            "import java.util.ArrayList;",
            "import java.util.Scanner;",
            "import java.sql.",
            "import java.io.",
            "System.out.println();",
            "System.out.print();",
            "new ArrayList<>()",
            "new HashMap<>()",
            "for (int i = 0; i < ; i++) {\n    \n}",
            "for ( : ) {\n    \n}",
            "while () {\n    \n}",
            "if () {\n    \n} else {\n    \n}",
            "try {\n    \n} catch (Exception e) {\n    \n}",
            "try ( ) {\n    \n}",
            "@Override\n"
    );

    private JavaKeywords() {}

    public static List<String> allForCompletion() {
        LinkedHashSet<String> out = new LinkedHashSet<>(RESERVED);
        out.addAll(PRIMITIVES_AND_TYPES);
        out.addAll(SNIPPETS);
        return List.copyOf(out);
    }

    public static boolean isKeyword(String word) {
        return word != null && RESERVED.contains(word);
    }

    public static boolean isReservedOrType(String word) {
        if (word == null) {
            return false;
        }
        String w = word;
        return RESERVED.contains(w) || PRIMITIVES_AND_TYPES.contains(w);
    }
}
