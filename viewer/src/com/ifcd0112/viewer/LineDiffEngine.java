package com.ifcd0112.viewer;

import java.util.ArrayList;
import java.util.List;

/** Comparación línea a línea (LCS) entre el código del alumno y la solución oficial. */
public final class LineDiffEngine {

    public enum RowKind {
        /** Misma línea en ambos textos. */
        EQUAL,
        /** Solo en el texto del alumno (añadida). */
        ONLY_STUDENT,
        /** Solo en la solución (falta en el alumno). */
        ONLY_SOLUTION
    }

    public record DiffRow(String studentLine, String solutionLine, RowKind kind) {}

    private LineDiffEngine() {}

    public static List<DiffRow> compare(String studentText, String solutionText) {
        String[] student = toLines(studentText);
        String[] solution = toLines(solutionText);
        int n = student.length;
        int m = solution.length;
        int[][] lcs = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (student[i - 1].equals(solution[j - 1])) {
                    lcs[i][j] = lcs[i - 1][j - 1] + 1;
                } else {
                    lcs[i][j] = Math.max(lcs[i - 1][j], lcs[i][j - 1]);
                }
            }
        }
        List<DiffRow> reversed = new ArrayList<>();
        int i = n;
        int j = m;
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && student[i - 1].equals(solution[j - 1])) {
                reversed.add(new DiffRow(student[i - 1], solution[j - 1], RowKind.EQUAL));
                i--;
                j--;
            } else if (j > 0 && (i == 0 || lcs[i][j - 1] >= lcs[i - 1][j])) {
                reversed.add(new DiffRow("", solution[j - 1], RowKind.ONLY_SOLUTION));
                j--;
            } else {
                reversed.add(new DiffRow(student[i - 1], "", RowKind.ONLY_STUDENT));
                i--;
            }
        }
        List<DiffRow> rows = new ArrayList<>(reversed.size());
        for (int k = reversed.size() - 1; k >= 0; k--) {
            rows.add(reversed.get(k));
        }
        return rows;
    }

    public static DiffSummary summarize(List<DiffRow> rows) {
        int added = 0;
        int missing = 0;
        int equal = 0;
        for (DiffRow row : rows) {
            switch (row.kind()) {
                case ONLY_STUDENT -> added++;
                case ONLY_SOLUTION -> missing++;
                case EQUAL -> equal++;
            }
        }
        return new DiffSummary(equal, added, missing);
    }

    private static String[] toLines(String text) {
        if (text == null || text.isEmpty()) {
            return new String[0];
        }
        return text.split("\n", -1);
    }

    public record DiffSummary(int equalLines, int addedLines, int missingLines) {
        boolean identical() {
            return addedLines == 0 && missingLines == 0;
        }
    }
}
