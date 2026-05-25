package com.ifcd0112.viewer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AddExerciseDialog extends JDialog {

    private final Path projectRoot;
    private final ExerciseCatalog catalog;
    private final JTextField idField = new JTextField(16);
    private final JTextField titleField = new JTextField(28);
    private final JComboBox<String> moduleCombo = new JComboBox<>();
    private final JComboBox<Exercise.SolutionType> typeCombo = new JComboBox<>(Exercise.SolutionType.values());
    private final JTextField mainClassField = new JTextField("Main", 14);
    private final JLabel enunciadoLabel = new JLabel("No seleccionado");
    private final JLabel solutionLabel = new JLabel("No seleccionado");

    private Path enunciadoPath;
    private Path solutionPath;
    private Exercise created;

    public AddExerciseDialog(Frame owner, Path projectRoot, ExerciseCatalog catalog) {
        super(owner, "Añadir ejercicio", true);
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
        this.catalog = catalog;
        setMinimumSize(new Dimension(520, 420));
        setLocationRelativeTo(owner);
        buildUi();
        applyTheme();
        UiTheme.addThemeListener(this::applyTheme);
    }

    public Exercise getCreatedExercise() {
        return created;
    }

    private void buildUi() {
        catalog.getAll().stream()
                .map(Exercise::module)
                .distinct()
                .forEach(moduleCombo::addItem);
        moduleCombo.addItem("Ejercicios añadidos");
        moduleCombo.setSelectedItem("Ejercicios añadidos");
        moduleCombo.setEditable(true);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 14, 8, 14));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;

        int row = 0;
        addRow(form, c, row++, "Id (único)", idField);
        addRow(form, c, row++, "Título", titleField);
        addRow(form, c, row++, "Módulo en el índice", moduleCombo);
        addRow(form, c, row++, "Tipo de solución", typeCombo);
        addRow(form, c, row++, "Clase principal (Java)", mainClassField);

        c.gridy = row++;
        c.gridwidth = 2;
        form.add(new JLabel("Enunciado (.md):"), c);
        c.gridy = row;
        JPanel enunciadoRow = filePickerRow(enunciadoLabel, this::pickEnunciado);
        form.add(enunciadoRow, c);

        c.gridy = ++row;
        form.add(new JLabel("Solución (archivo o carpeta .java):"), c);
        c.gridy = ++row;
        JPanel solutionRow = filePickerRow(solutionLabel, this::pickSolution);
        form.add(solutionRow, c);

        typeCombo.addActionListener(e -> updateTypeFields());
        updateTypeFields();

        JButton saveBtn = UiTheme.primaryButton("Añadir al índice", UiTheme.palette().success(), UiTheme.palette().successHover());
        saveBtn.addActionListener(e -> saveExercise());
        JButton cancelBtn = UiTheme.neutralButton("Cancelar");
        cancelBtn.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBorder(new EmptyBorder(8, 14, 12, 14));
        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridy = row;
        c.gridwidth = 1;
        c.weightx = 0;
        form.add(new JLabel(label + ":"), c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(field, c);
        c.gridx = 0;
        c.weightx = 0;
    }

    private JPanel filePickerRow(JLabel pathLabel, Runnable onPick) {
        JButton pick = UiTheme.neutralButton("Elegir…");
        pick.addActionListener(e -> onPick.run());
        JPanel row = new JPanel(new BorderLayout(8, 0));
        pathLabel.setFont(UiTheme.FONT_UI.deriveFont(12f));
        row.add(pathLabel, BorderLayout.CENTER);
        row.add(pick, BorderLayout.EAST);
        return row;
    }

    private void updateTypeFields() {
        boolean java = typeCombo.getSelectedItem() == Exercise.SolutionType.JAVA;
        mainClassField.setEnabled(java);
    }

    private void pickEnunciado() {
        JFileChooser chooser = fileChooser();
        chooser.setDialogTitle("Seleccionar enunciado (.md)");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Markdown", "md"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            enunciadoPath = chooser.getSelectedFile().toPath().toAbsolutePath().normalize();
            enunciadoLabel.setText(relativize(enunciadoPath));
        }
    }

    private void pickSolution() {
        JFileChooser chooser = fileChooser();
        chooser.setDialogTitle("Seleccionar solución (archivo o carpeta Java)");
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            solutionPath = chooser.getSelectedFile().toPath().toAbsolutePath().normalize();
            solutionLabel.setText(relativize(solutionPath));
            inferTypeFromSolution(solutionPath);
        }
    }

    private JFileChooser fileChooser() {
        JFileChooser chooser = new JFileChooser(projectRoot.toFile());
        chooser.setMultiSelectionEnabled(false);
        return chooser;
    }

    private void inferTypeFromSolution(Path path) {
        if (path == null) {
            return;
        }
        if (Files.isDirectory(path)) {
            typeCombo.setSelectedItem(Exercise.SolutionType.JAVA);
            return;
        }
        String name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(".sql")) {
            typeCombo.setSelectedItem(Exercise.SolutionType.SQL);
        } else if (name.endsWith(".md")) {
            typeCombo.setSelectedItem(Exercise.SolutionType.MARKDOWN);
        }
    }

    private String relativize(Path absolute) {
        if (!PlatformSupport.isSubPath(absolute, projectRoot)) {
            return absolute.toString();
        }
        return projectRoot.relativize(absolute).toString().replace('\\', '/');
    }

    private void saveExercise() {
        try {
            if (enunciadoPath == null) {
                throw new IOException("Selecciona el archivo de enunciado (.md).");
            }
            if (!PlatformSupport.isSubPath(enunciadoPath, projectRoot)) {
                throw new IOException("El enunciado debe estar dentro del repositorio:\n" + projectRoot);
            }
            String solRel = "";
            if (solutionPath != null) {
                if (!PlatformSupport.isSubPath(solutionPath, projectRoot)) {
                    throw new IOException("La solución debe estar dentro del repositorio:\n" + projectRoot);
                }
                solRel = relativize(solutionPath);
            }

            Exercise.SolutionType type = (Exercise.SolutionType) typeCombo.getSelectedItem();
            String mainClass = type == Exercise.SolutionType.JAVA ? mainClassField.getText().trim() : "";

            CustomExerciseRecord record = new CustomExerciseRecord(
                    idField.getText().trim(),
                    titleField.getText().trim(),
                    String.valueOf(moduleCombo.getSelectedItem()).trim(),
                    relativize(enunciadoPath),
                    solRel,
                    type,
                    mainClass
            );

            catalog.addCustom(record);
            created = catalog.getAll().stream()
                    .filter(ex -> ex.id().equals(record.id()))
                    .findFirst()
                    .orElseThrow();
            dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo añadir", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyTheme() {
        ThemePalette p = UiTheme.palette();
        getContentPane().setBackground(p.bg());
        enunciadoLabel.setForeground(p.textMuted());
        solutionLabel.setForeground(p.textMuted());
    }
}
