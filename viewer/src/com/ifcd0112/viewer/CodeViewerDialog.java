package com.ifcd0112.viewer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CodeViewerDialog extends JDialog {

    public enum EditorMode {
        OFFICIAL_SOLUTION,
        STUDENT_WORK
    }

    private final Exercise exercise;
    private final EditorMode editorMode;
    private final Path projectRoot;
    private final CodeHighlightPane codePane;
    private final String language;
    private String savedBaseline;

    private JLabel titleLabel;
    private JLabel hintLabel;
    private JPanel topBar;
    private JButton saveBtn;
    private JButton copyBtn;
    private JButton restoreBtn;
    private JButton checkBtn;
    private JButton formatBtn;
    private JButton compareBtn;
    private JButton closeBtn;
    private JLabel diagnosticsLabel;
    private SwingWorker<CodeDiagnosticsService.AnalysisResult, Void> diagnosticsWorker;

    public static CodeViewerDialog forOfficialSolution(Frame owner, Exercise exercise, String code) {
        return new CodeViewerDialog(owner, exercise, code, EditorMode.OFFICIAL_SOLUTION, null);
    }

    public static CodeViewerDialog forStudentWork(Frame owner, Exercise exercise, String code, Path projectRoot) {
        return new CodeViewerDialog(owner, exercise, code, EditorMode.STUDENT_WORK, projectRoot);
    }

    private CodeViewerDialog(Frame owner, Exercise exercise, String code, EditorMode mode, Path projectRoot) {
        super(owner, dialogTitle(exercise, mode), true);
        this.exercise = exercise;
        this.editorMode = mode;
        this.projectRoot = projectRoot;
        this.savedBaseline = code;
        this.language = languageFor(exercise);

        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(owner);

        codePane = new CodeHighlightPane();
        codePane.setCode(code, language);
        codePane.enableEditing(language);
        Path root = projectRoot != null ? projectRoot : PlatformSupport.findProjectRoot();
        if ("sql".equals(language)) {
            codePane.enableSqlSuggestions(root, () -> List.of());
        } else if ("java".equals(language)) {
            codePane.enableJavaSuggestions();
        }
        codePane.setAfterEditHook(this::runDiagnosticsAsync);

        buildLayout(exercise, codePane);
        applyTheme();
        runDiagnosticsAsync();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeWithConfirm();
            }
        });

        UiTheme.addThemeListener(this::applyTheme);
    }

    public String getEditedCode() {
        return codePane.getText();
    }

    private static String languageFor(Exercise exercise) {
        return switch (exercise.solutionType()) {
            case JAVA -> "java";
            case SQL -> "sql";
            case MARKDOWN -> "markdown";
            case NONE -> "plain";
        };
    }

    private void buildLayout(Exercise exercise, JComponent center) {
        ThemePalette p = UiTheme.palette();

        titleLabel = new JLabel(exercise.title());
        titleLabel.setFont(UiTheme.FONT_UI_BOLD);

        hintLabel = new JLabel(hintHtml(exercise));
        hintLabel.setFont(UiTheme.FONT_UI.deriveFont(12f));

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badges.setOpaque(false);
        badges.add(UiTheme.badge(UiTheme.typeLabel(exercise), UiTheme.badgeColor(exercise)));
        badges.add(UiTheme.badge(exercise.module(), p.textMuted()));

        JPanel titleCol = new JPanel();
        titleCol.setLayout(new BoxLayout(titleCol, BoxLayout.Y_AXIS));
        titleCol.setOpaque(false);
        titleCol.add(titleLabel);
        titleCol.add(Box.createVerticalStrut(4));
        titleCol.add(hintLabel);
        titleCol.add(Box.createVerticalStrut(6));
        titleCol.add(badges);

        saveBtn = UiTheme.primaryButton("Guardar", p.success(), p.successHover());
        saveBtn.setToolTipText(saveTargetDescription());
        saveBtn.addActionListener(e -> saveChanges(saveBtn));

        copyBtn = UiTheme.primaryButton("Copiar", p.primary(), p.primaryHover());
        copyBtn.setToolTipText("Copiar todo el código al portapapeles");
        copyBtn.addActionListener(e -> copyToClipboard(copyBtn));

        restoreBtn = UiTheme.neutralButton("Restaurar");
        restoreBtn.setToolTipText("Descartar cambios no guardados y recargar desde disco");
        restoreBtn.addActionListener(e -> restoreFromDisk(restoreBtn));

        checkBtn = UiTheme.primaryButton("Comprobar errores", p.primary(), p.primaryHover());
        checkBtn.setToolTipText("Compila (Java) o revisa sintaxis básica y marca líneas con error");
        checkBtn.addActionListener(e -> runDiagnosticsAsync());

        formatBtn = UiTheme.neutralButton("Formatear código");
        formatBtn.setToolTipText("Ajusta la sangría (identación) del código Java o SQL");
        formatBtn.addActionListener(e -> formatCode());
        formatBtn.setVisible("java".equals(language) || "sql".equals(language));

        boolean canCompare = editorMode == EditorMode.STUDENT_WORK
                && SolutionReader.solutionExists(exercise)
                && !"plain".equals(language)
                && exercise.solutionType() != Exercise.SolutionType.NONE;
        if (canCompare) {
            compareBtn = UiTheme.neutralButton("Comparar con solución");
            compareBtn.setToolTipText(
                    "Muestra tu código junto a la solución oficial (verde = añadido, rojo = te falta)");
            compareBtn.addActionListener(e -> openCompareDialog());
        }

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(restoreBtn);
        if (canCompare) {
            actions.add(compareBtn);
        }
        if (formatBtn.isVisible()) {
            actions.add(formatBtn);
        }
        actions.add(checkBtn);
        actions.add(copyBtn);
        actions.add(saveBtn);

        topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setName("codeTopBar");
        topBar.setBorder(new EmptyBorder(12, 14, 8, 14));
        topBar.add(titleCol, BorderLayout.CENTER);
        topBar.add(actions, BorderLayout.EAST);

        diagnosticsLabel = new JLabel(" ");
        diagnosticsLabel.setFont(UiTheme.FONT_UI.deriveFont(12f));

        closeBtn = UiTheme.primaryButton("Cerrar", p.primary(), p.primaryHover());
        closeBtn.addActionListener(e -> closeWithConfirm());

        JPanel south = new JPanel(new BorderLayout(8, 0));
        south.setName("codeSouth");
        south.setBorder(new EmptyBorder(8, 14, 12, 14));
        south.add(diagnosticsLabel, BorderLayout.CENTER);
        JPanel southActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        southActions.setOpaque(false);
        southActions.add(closeBtn);
        south.add(southActions, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setName("codeContent");
        content.setBorder(new EmptyBorder(0, 14, 0, 14));
        content.add(topBar, BorderLayout.NORTH);
        content.add(center, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(content, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        setSize(860, 600);
    }

    private static String dialogTitle(Exercise exercise, EditorMode mode) {
        return (mode == EditorMode.STUDENT_WORK ? "Mi código — " : "Solución — ") + exercise.title();
    }

    private String hintHtml(Exercise exercise) {
        if (editorMode == EditorMode.STUDENT_WORK) {
            return "<html>Edita <b>tu código</b> en <code>trabajo/</code>. "
                    + "<b>Comparar con solución</b> muestra diferencias línea a línea. "
                    + "<b>Formatear código</b>, <b>Ctrl+Espacio</b>, <b>Comprobar errores</b> (<code>javac</code>). "
                    + "En varios <code>.java</code>, conserva <code>// ===== archivo.java =====</code>.</html>";
        }
        return "<html><b>Formatear código</b> ajusta la sangría. <b>Ctrl+Espacio</b> sugerencias. "
                + "<b>Comprobar errores</b> (javac en Java). <b>Guardar</b> escribe en la solución oficial. "
                + "En varios <code>.java</code>, conserva <code>// ===== archivo.java =====</code>.</html>";
    }

    private String saveTargetDescription() {
        if (editorMode == EditorMode.STUDENT_WORK && projectRoot != null) {
            return "Guardar en " + StudentWorkspace.directory(exercise, projectRoot);
        }
        return "Guardar en " + exercise.solutionPath();
    }

    private void saveChanges(JButton saveBtn) {
        String text = codePane.getText();
        int ok = JOptionPane.showConfirmDialog(
                this,
                "¿Guardar los cambios?\n" + saveTargetDescription(),
                editorMode == EditorMode.STUDENT_WORK ? "Guardar mi código" : "Guardar solución",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (editorMode == EditorMode.STUDENT_WORK) {
                StudentWorkspace.save(exercise, projectRoot, text);
            } else {
                SolutionWriter.save(exercise, text);
            }
            savedBaseline = text;
            saveBtn.setText("¡Guardado!");
            Timer timer = new Timer(1500, ev -> saveBtn.setText("Guardar"));
            timer.setRepeats(false);
            timer.start();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo guardar:\n" + ex.getMessage(),
                    "Error al guardar",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void restoreFromDisk(JButton restoreBtn) {
        if (isDirty()) {
            int ok = JOptionPane.showConfirmDialog(
                    this,
                    "¿Descartar los cambios y recargar desde disco?",
                    "Restaurar",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (ok != JOptionPane.YES_OPTION) {
                return;
            }
        }
        try {
            String code = editorMode == EditorMode.STUDENT_WORK
                    ? StudentWorkspace.readContent(exercise, projectRoot)
                    : SolutionReader.readSolutionContent(exercise);
            savedBaseline = code;
            codePane.setCode(code, language);
            runDiagnosticsAsync();
            restoreBtn.setText("Recargado");
            Timer timer = new Timer(1200, ev -> restoreBtn.setText("Restaurar"));
            timer.setRepeats(false);
            timer.start();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error al leer: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void closeWithConfirm() {
        if (isDirty()) {
            int ok = JOptionPane.showConfirmDialog(
                    this,
                    "Hay cambios sin guardar. ¿Cerrar igualmente?",
                    "Cerrar",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (ok != JOptionPane.YES_OPTION) {
                return;
            }
        }
        codePane.disableSuggestions();
        dispose();
    }

    private boolean isDirty() {
        return !codePane.getText().equals(savedBaseline);
    }

    private void openCompareDialog() {
        try {
            String solution = SolutionReader.readSolutionContent(exercise);
            CodeCompareDialog.show(this, codePane.getText(), solution, exercise.title());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo leer la solución:\n" + ex.getMessage(),
                    "Comparar con solución",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void formatCode() {
        String before = codePane.getText();
        String formatted = switch (language) {
            case "java" -> CodeFormatter.formatJava(before);
            case "sql" -> CodeFormatter.formatSql(before);
            default -> before;
        };
        if (formatted.equals(before)) {
            diagnosticsLabel.setText("El código ya estaba bien indentado.");
            return;
        }
        codePane.setCode(formatted, language);
        codePane.focusEditor();
        diagnosticsLabel.setText("Código formateado. Revisa y guarda si te convence.");
        runDiagnosticsAsync();
    }

    private void copyToClipboard(JButton copyBtn) {
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(codePane.getText()), null);
        copyBtn.setText("¡Copiado!");
        Timer timer = new Timer(1500, ev -> copyBtn.setText("Copiar"));
        timer.setRepeats(false);
        timer.start();
    }

    private void applyTheme() {
        ThemePalette p = UiTheme.palette();
        getContentPane().setBackground(p.bg());

        titleLabel.setForeground(p.text());
        hintLabel.setForeground(p.textMuted());
        topBar.setBackground(p.bg());

        UiTheme.refreshButton(saveBtn, p.success(), p.successHover());
        UiTheme.refreshButton(copyBtn, p.primary(), p.primaryHover());
        UiTheme.refreshButton(closeBtn, p.primary(), p.primaryHover());
        Color[] neutral = UiTheme.neutralButtonColors();
        UiTheme.refreshButton(restoreBtn, neutral[0], neutral[1]);
        UiTheme.refreshButton(checkBtn, p.primary(), p.primaryHover());
        if (compareBtn != null) {
            UiTheme.refreshButton(compareBtn, neutral[0], neutral[1]);
        }
        diagnosticsLabel.setForeground(p.textMuted());

        Component south = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.SOUTH);
        if (south != null) {
            south.setBackground(p.bg());
        }

        codePane.refreshTheme();
        codePane.refreshHighlightNow();

        repaint();
    }

    private void runDiagnosticsAsync() {
        if (diagnosticsWorker != null && !diagnosticsWorker.isDone()) {
            diagnosticsWorker.cancel(true);
        }
        checkBtn.setEnabled(false);
        diagnosticsLabel.setText("Comprobando errores…");

        String content = codePane.getText();
        String defaultFile = defaultJavaFileName();

        diagnosticsWorker = new SwingWorker<>() {
            @Override
            protected CodeDiagnosticsService.AnalysisResult doInBackground() {
                Path root = projectRoot != null ? projectRoot : PlatformSupport.findProjectRoot();
                return CodeDiagnosticsService.analyze(content, language, root, defaultFile);
            }

            @Override
            protected void done() {
                checkBtn.setEnabled(true);
                if (isCancelled()) {
                    return;
                }
                try {
                    CodeDiagnosticsService.AnalysisResult result = get();
                    codePane.setDiagnostics(result.diagnostics());
                    updateDiagnosticsLabel(result.diagnostics());
                } catch (Exception ex) {
                    diagnosticsLabel.setText("No se pudo analizar el código.");
                    codePane.setDiagnostics(List.of());
                }
            }
        };
        diagnosticsWorker.execute();
    }

    private void updateDiagnosticsLabel(List<CodeDiagnostic> items) {
        long errors = items.stream()
                .filter(d -> d.severity() == CodeDiagnostic.Severity.ERROR)
                .count();
        long warnings = items.stream()
                .filter(d -> d.severity() == CodeDiagnostic.Severity.WARNING)
                .count();
        if (items.isEmpty()) {
            diagnosticsLabel.setText(
                    language.contains("java")
                            ? "Sin errores de compilación ni avisos de sintaxis."
                            : "Sin avisos de sintaxis detectados."
            );
            return;
        }
        diagnosticsLabel.setText(
                errors + " error(es), " + warnings + " aviso(s). Haz clic en la lista para ir a la línea."
        );
    }

    private String defaultJavaFileName() {
        Path path = exercise.solutionPath();
        if (path != null && path.toString().endsWith(".java")) {
            return path.getFileName().toString();
        }
        return "Main.java";
    }
}
