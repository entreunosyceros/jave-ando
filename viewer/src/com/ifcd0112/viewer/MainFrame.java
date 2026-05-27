package com.ifcd0112.viewer;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainFrame extends JFrame {

    private final Path projectRoot;
    private final ExerciseCatalog catalog;
    private final ExecutionService executionService = new ExecutionService();
    private final SqlExecutionService sqlExecutionService = new SqlExecutionService();

    private final ExerciseTreePanel treePanel;
    private final JEditorPane enunciadoPane = new JEditorPane();
    private final JTextArea outputArea = new JTextArea();
    private final JButton viewSolutionBtn;
    private final JButton editMyCodeBtn;
    private final JButton runMyCodeBtn;
    private final JButton runBtn;
    private final JLabel statusLabel = new JLabel(" ");
    private final JLabel helpLabel = new JLabel(" ");
    private final JLabel exerciseTitleLabel = new JLabel(" ");
    private final JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final JLabel listTitle = new JLabel("Índice de ejercicios");
    private final JLabel actionsTitle = new JLabel("Acciones");
    private final JLabel hintLabel = new JLabel();

    private JPanel headerPanel;
    private JPanel leftCard;
    private JPanel statusBar;
    private JPanel rightPanel;
    private JPanel centerTop;
    private JPanel actionPanel;
    private JScrollPane enunciadoScroll;
    private JScrollPane outputScroll;
    private JSplitPane split;
    private JSplitPane consoleSplit;
    private JMenuItem themeMenuItem;
    private JLabel databaseStatusLabel;

    private SqlConsoleDialog sqlConsoleDialog;
    private DatabaseSchemaViewerDialog schemaViewerDialog;

    private Exercise lastSelected;

    public MainFrame(Path projectRoot) {
        super(AppInfo.NAME);
        this.projectRoot = projectRoot;
        this.catalog = new ExerciseCatalog(projectRoot);
        this.treePanel = new ExerciseTreePanel(catalog);

        ThemePalette p = UiTheme.palette();
        viewSolutionBtn = UiTheme.primaryButton("Ver solución", p.primary(), p.primaryHover());
        editMyCodeBtn = UiTheme.primaryButton("Mi código", p.primary(), p.primaryHover());
        runMyCodeBtn = UiTheme.primaryButton("Ejecutar mi código", p.success(), p.successHover());
        runBtn = UiTheme.neutralButton("Ejecutar solución");

        setIconImage(iconToImage(AppIcons.appIcon()));

        initUi();
        setJMenuBar(buildMenuBar());
        UiTheme.addThemeListener(this::applyTheme);

        treePanel.selectFirstExercise();
        configureWindowAndTray();
    }

    private void configureWindowAndTray() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (SystemTrayManager.isInstalled() && !SystemTrayManager.isExiting()) {
                    setVisible(false);
                } else {
                    SystemTrayManager.shutdown();
                    dispose();
                }
            }
        });
        SystemTrayManager.install(this, projectRoot);
    }

    public void showMainWindow() {
        setVisible(true);
        setExtendedState(java.awt.Frame.NORMAL);
        toFront();
        requestFocus();
    }

    public void openSqlConfigDialog() {
        new SqlConfigDialog(this, projectRoot).setVisible(true);
    }

    public void toggleThemeFromTray() {
        UiTheme.toggleMode();
        if (themeMenuItem != null) {
            themeMenuItem.setText(themeToggleLabel());
        }
    }

    public void openExerciseHelp() {
        new ExerciseHelpDialog(this, projectRoot).setVisible(true);
    }

    public void openAbout() {
        new AboutDialog(this, projectRoot).setVisible(true);
    }

    private Image iconToImage(Icon icon) {
        if (icon instanceof ImageIcon ii && ii.getImage() != null) {
            return ii.getImage();
        }
        int w = Math.max(icon.getIconWidth(), 32);
        int h = Math.max(icon.getIconHeight(), 32);
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(
                w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        icon.paintIcon(null, g2, 0, 0);
        g2.dispose();
        return bi;
    }

    private void initUi() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(960, 600));

        treePanel.addSelectionListener(e -> showSelectedExercise());
        viewSolutionBtn.addActionListener(e -> viewSolution());
        editMyCodeBtn.addActionListener(e -> editMyCode());
        runMyCodeBtn.addActionListener(e -> runMyCode());
        runBtn.addActionListener(e -> runSolution());

        configureEnunciadoPane();
        configureOutputArea();

        headerPanel = UiTheme.createHeader();

        leftCard = new JPanel(new BorderLayout());
        leftCard.add(listTitle, BorderLayout.NORTH);
        leftCard.add(treePanel, BorderLayout.CENTER);
        leftCard.setPreferredSize(new Dimension(280, 0));
        treePanel.setMinimumSize(new Dimension(200, 120));

        actionPanel = buildActionPanel();
        centerTop = buildCenterHeader();
        enunciadoScroll = UiTheme.wrap(enunciadoPane, "Enunciado del ejercicio");

        JPanel centerColumn = new JPanel(new BorderLayout(0, 10));
        centerColumn.setOpaque(false);
        centerColumn.add(centerTop, BorderLayout.NORTH);
        centerColumn.add(enunciadoScroll, BorderLayout.CENTER);

        JPanel centerRow = new JPanel(new BorderLayout(12, 0));
        centerRow.setOpaque(false);
        centerRow.add(centerColumn, BorderLayout.CENTER);
        centerRow.add(actionPanel, BorderLayout.EAST);

        outputScroll = UiTheme.wrap(outputArea, "Salida de la ejecución");
        outputScroll.setMinimumSize(new Dimension(0, 90));

        centerRow.setMinimumSize(new Dimension(200, 160));

        consoleSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centerRow, outputScroll);
        consoleSplit.setResizeWeight(0.72);
        consoleSplit.setDividerLocation(0.72);
        consoleSplit.setContinuousLayout(true);
        consoleSplit.setOneTouchExpandable(true);
        consoleSplit.setBorder(null);

        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(0, 4, 0, 0));
        rightPanel.add(consoleSplit, BorderLayout.CENTER);

        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftCard, rightPanel);
        split.setResizeWeight(0.22);
        split.setDividerLocation(290);
        split.setBorder(null);

        statusBar = new JPanel(new BorderLayout(12, 0));
        statusBar.add(statusLabel, BorderLayout.WEST);
        databaseStatusLabel = new JLabel(" ");
        databaseStatusLabel.setFont(UiTheme.FONT_UI.deriveFont(12f));
        statusBar.add(databaseStatusLabel, BorderLayout.CENTER);
        statusBar.add(helpLabel, BorderLayout.EAST);
        refreshDatabaseStatusLabel();

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        applyTheme();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("Archivo");
        JMenuItem exit = new JMenuItem("Cerrar");
        exit.setAccelerator(KeyStroke.getKeyStroke("alt F4"));
        exit.addActionListener(e -> SystemTrayManager.exitApplication(this));
        file.add(exit);

        JMenu tools = new JMenu("Herramientas");
        JMenuItem openEnunciadoFolder = new JMenuItem("Abrir carpeta del enunciado");
        openEnunciadoFolder.addActionListener(e -> openEnunciadoFolder());
        JMenuItem openWorkFolder = new JMenuItem("Abrir carpeta «mi código»");
        openWorkFolder.addActionListener(e -> openStudentWorkFolder());
        JMenuItem openEnunciadoTerminal = new JMenuItem("Terminal en carpeta del enunciado");
        openEnunciadoTerminal.addActionListener(e -> openEnunciadoTerminal());
        JMenuItem openWorkTerminal = new JMenuItem("Terminal en «mi código»");
        openWorkTerminal.addActionListener(e -> openStudentWorkTerminal());
        tools.add(openEnunciadoFolder);
        tools.add(openWorkFolder);
        tools.addSeparator();
        tools.add(openEnunciadoTerminal);
        tools.add(openWorkTerminal);
        tools.addSeparator();
        JMenuItem sqlConfig = new JMenuItem("Configurar conexión SQL…");
        sqlConfig.addActionListener(e -> openSqlConfigDialog());
        JMenuItem initDb = new JMenuItem("Inicializar base de datos (script 01)…");
        initDb.addActionListener(e -> runInitDatabaseScript());
        tools.add(sqlConfig);
        tools.add(initDb);

        JMenu database = new JMenu("Base de datos");
        JMenuItem selectDb = new JMenuItem("Seleccionar base de datos…");
        selectDb.addActionListener(e -> openSelectDatabaseDialog());
        JMenuItem sqlConsole = new JMenuItem("Consola SQL…");
        sqlConsole.setAccelerator(KeyStroke.getKeyStroke("control alt S"));
        sqlConsole.addActionListener(e -> openSqlConsole());
        JMenuItem schemaView = new JMenuItem("Ver esquema gráfico…");
        schemaView.addActionListener(e -> openSchemaViewer());
        database.add(selectDb);
        database.add(sqlConsole);
        database.add(schemaView);

        JMenu exercises = new JMenu("Ejercicios");
        JMenuItem addExercise = new JMenuItem("Añadir ejercicio…");
        addExercise.addActionListener(e -> addCustomExercise());
        JMenuItem removeExercise = new JMenuItem("Eliminar ejercicio…");
        removeExercise.addActionListener(e -> removeCustomExercise());
        exercises.add(addExercise);
        exercises.add(removeExercise);
        JMenuItem restoreHidden = new JMenuItem("Restaurar ejercicios ocultos…");
        restoreHidden.addActionListener(e -> restoreHiddenExercises());
        exercises.addSeparator();
        exercises.add(restoreHidden);

        JMenu view = new JMenu("Ver");
        themeMenuItem = new JMenuItem(themeToggleLabel());
        themeMenuItem.addActionListener(e -> {
            UiTheme.toggleMode();
            themeMenuItem.setText(themeToggleLabel());
        });
        view.add(themeMenuItem);

        JMenu help = new JMenu("Ayuda");
        JMenuItem addExercises = new JMenuItem("Cómo añadir y gestionar ejercicios…");
        addExercises.addActionListener(e -> new ExerciseHelpDialog(this, projectRoot).setVisible(true));
        JMenuItem about = new JMenuItem("Acerca de " + AppInfo.NAME + "…");
        about.addActionListener(e -> new AboutDialog(this, projectRoot).setVisible(true));
        help.add(addExercises);
        help.addSeparator();
        help.add(about);

        bar.add(file);
        bar.add(tools);
        bar.add(database);
        bar.add(exercises);
        bar.add(view);
        bar.add(help);
        return bar;
    }

    private void refreshDatabaseStatusLabel() {
        if (databaseStatusLabel == null) {
            return;
        }
        try {
            if (!DatabaseConfig.configExists(projectRoot)) {
                databaseStatusLabel.setText("BD: (sin configurar)");
                return;
            }
            DatabaseConfig.normalizeJdbcUrl(projectRoot);
            String db = DatabaseConfig.activeDatabaseName(DatabaseConfig.load(projectRoot));
            databaseStatusLabel.setText(db.isBlank() ? "BD: (servidor)" : "BD: " + db);
        } catch (IOException e) {
            databaseStatusLabel.setText("BD: ?");
        }
    }

    void openSelectDatabaseDialog() {
        if (!ensureSqlConfigured()) {
            return;
        }
        SelectDatabaseDialog dialog = new SelectDatabaseDialog(this, projectRoot, () -> {
            refreshDatabaseStatusLabel();
            if (sqlConsoleDialog != null) {
                sqlConsoleDialog.refreshDbLabel();
            }
            if (schemaViewerDialog != null) {
                try {
                    schemaViewerDialog.setDatabaseName(
                            DatabaseConfig.activeDatabaseName(DatabaseConfig.load(projectRoot)));
                    schemaViewerDialog.reloadSchema();
                } catch (IOException ignored) {
                    // refresh on next open
                }
            }
        });
        dialog.setVisible(true);
    }

    void openSqlConsole() {
        if (!ensureSqlConfigured()) {
            return;
        }
        if (sqlConsoleDialog == null) {
            sqlConsoleDialog = new SqlConsoleDialog(this, projectRoot);
        } else {
            sqlConsoleDialog.refreshDbLabel();
        }
        sqlConsoleDialog.setVisible(true);
        sqlConsoleDialog.toFront();
    }

    void openSchemaViewer() {
        if (!ensureSqlConfigured()) {
            return;
        }
        if (schemaViewerDialog == null) {
            schemaViewerDialog = new DatabaseSchemaViewerDialog(this, projectRoot);
        }
        try {
            schemaViewerDialog.setDatabaseName(
                    DatabaseConfig.activeDatabaseName(DatabaseConfig.load(projectRoot)));
        } catch (IOException ignored) {
            // dialog shows hint
        }
        schemaViewerDialog.setVisible(true);
        schemaViewerDialog.toFront();
        schemaViewerDialog.reloadSchema();
    }

    void addCustomExercise() {
        AddExerciseDialog dialog = new AddExerciseDialog(this, projectRoot, catalog);
        dialog.setVisible(true);
        Exercise created = dialog.getCreatedExercise();
        if (created != null) {
            refreshExerciseIndex(created);
            statusLabel.setText("Ejercicio añadido: " + created.title());
        }
    }

    void removeCustomExercise() {
        RemoveExerciseDialog dialog = new RemoveExerciseDialog(this, catalog, lastSelected);
        dialog.setVisible(true);
        if (dialog.wasRemoved()) {
            refreshExerciseIndex(null);
            statusLabel.setText("Ejercicio eliminado del índice.");
        }
    }

    void restoreHiddenExercises() {
        RestoreHiddenDialog dialog = new RestoreHiddenDialog(this, catalog);
        dialog.setVisible(true);
        if (dialog.wasRestored()) {
            refreshExerciseIndex(null);
            statusLabel.setText("Ejercicio restaurado en el índice.");
        }
    }

    private void refreshExerciseIndex(Exercise selectAfter) {
        treePanel.reload(catalog);
        if (selectAfter != null) {
            treePanel.selectExercise(selectAfter);
        } else {
            treePanel.selectFirstExercise();
        }
        showSelectedExercise();
    }

    private String themeToggleLabel() {
        return UiTheme.isDark() ? "Modo claro" : "Modo oscuro";
    }

    private void applyTheme() {
        ThemePalette p = UiTheme.palette();
        getContentPane().setBackground(p.bg());

        headerPanel = UiTheme.createHeader();
        JPanel content = (JPanel) getContentPane();
        content.removeAll();
        content.add(headerPanel, BorderLayout.NORTH);
        content.add(split, BorderLayout.CENTER);
        content.add(statusBar, BorderLayout.SOUTH);

        leftCard.setBackground(p.bg());
        leftCard.setOpaque(true);
        leftCard.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 0, 1, p.border()),
                new EmptyBorder(12, 14, 12, 14)
        ));
        listTitle.setFont(UiTheme.FONT_UI_BOLD);
        listTitle.setForeground(p.text());
        listTitle.setOpaque(false);
        listTitle.setBorder(new EmptyBorder(0, 0, 8, 0));

        rightPanel.setBackground(p.bg());
        centerTop.setOpaque(false);
        exerciseTitleLabel.setForeground(p.text());

        actionsTitle.setForeground(p.textMuted());
        hintLabel.setText("<html><body style='width:200px;font-size:12px;color:" + hex(p.textMuted()) +
                "'>Tu trabajo va en <code>trabajo/</code>. Usa <b>Ejecutar mi código</b> para probar.</body></html>");

        statusBar.setBackground(p.surface());
        statusBar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, p.border()),
                new EmptyBorder(8, 16, 8, 16)
        ));
        statusLabel.setForeground(p.textMuted());
        if (databaseStatusLabel != null) {
            databaseStatusLabel.setForeground(p.textMuted());
        }
        helpLabel.setForeground(p.textMuted());

        enunciadoPane.setBackground(p.surface());
        outputArea.setBackground(p.consoleBg());
        outputArea.setForeground(p.consoleText());
        outputArea.setCaretColor(p.consoleText());
        outputArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(p.border()),
                outputArea.getBorder()
        ));

        UiTheme.refreshButton(viewSolutionBtn, p.primary(), p.primaryHover());
        UiTheme.refreshButton(editMyCodeBtn, p.primary(), p.primaryHover());
        UiTheme.refreshButton(runMyCodeBtn, p.success(), p.successHover());
        Color[] neutral = UiTheme.neutralButtonColors();
        UiTheme.refreshButton(runBtn, neutral[0], neutral[1]);

        split.setBackground(p.bg());
        if (consoleSplit != null) {
            consoleSplit.setBackground(p.bg());
        }
        enunciadoScroll.setBorder(UiTheme.sectionBorder("Enunciado del ejercicio"));
        enunciadoScroll.getViewport().setBackground(p.surface());
        UiTheme.configureScroll(enunciadoScroll, true, false);

        outputScroll.setBorder(UiTheme.sectionBorder("Salida de la ejecución"));
        outputScroll.getViewport().setBackground(p.consoleBg());
        UiTheme.configureScroll(outputScroll, true, true);

        if (themeMenuItem != null) {
            themeMenuItem.setText(themeToggleLabel());
        }

        treePanel.applyTheme();
        if (lastSelected != null) {
            showSelectedExercise();
        }
        revalidate();
        repaint();
    }

    private static String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    private void configureEnunciadoPane() {
        enunciadoPane.setEditable(false);
        enunciadoPane.setContentType("text/html");
        enunciadoPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        enunciadoPane.setFont(UiTheme.FONT_UI);
    }

    private void configureOutputArea() {
        outputArea.setEditable(false);
        outputArea.setFont(UiTheme.FONT_MONO);
        outputArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        outputArea.setText("La salida del programa aparecerá aquí al ejecutar tu código o la solución.");
    }

    private JPanel buildCenterHeader() {
        exerciseTitleLabel.setFont(UiTheme.FONT_TITLE.deriveFont(18f));
        badgePanel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 4, 0));
        header.add(exerciseTitleLabel, BorderLayout.NORTH);
        header.add(badgePanel, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(28, 0, 0, 0));
        panel.setPreferredSize(new Dimension(220, 0));

        actionsTitle.setFont(UiTheme.FONT_UI_BOLD);
        actionsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        hintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        viewSolutionBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        editMyCodeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        runMyCodeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        runBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JButton b : new JButton[] {viewSolutionBtn, editMyCodeBtn, runMyCodeBtn, runBtn}) {
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        }

        panel.add(actionsTitle);
        panel.add(Box.createVerticalStrut(12));
        panel.add(editMyCodeBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(runMyCodeBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(viewSolutionBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(runBtn);
        panel.add(Box.createVerticalStrut(16));
        panel.add(hintLabel);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private void showSelectedExercise() {
        Exercise selected = treePanel.getSelectedExercise();
        boolean exerciseChanged = selected == null
                ? lastSelected != null
                : !selected.equals(lastSelected);
        lastSelected = selected;
        badgePanel.removeAll();

        if (selected == null) {
            enunciadoPane.setText(MarkdownRenderer.toHtml(
                    "<p>Selecciona un ejercicio del índice.</p>"));
            exerciseTitleLabel.setText("Ningún ejercicio seleccionado");
            viewSolutionBtn.setEnabled(false);
            editMyCodeBtn.setEnabled(false);
            runMyCodeBtn.setEnabled(false);
            runBtn.setEnabled(false);
            statusLabel.setText("Listo");
            badgePanel.revalidate();
            return;
        }

        exerciseTitleLabel.setText(selected.title());
        badgePanel.add(UiTheme.badge(UiTheme.typeLabel(selected), UiTheme.badgeColor(selected)));
        badgePanel.add(UiTheme.badge(selected.module(), UiTheme.palette().textMuted()));
        if (selected.isRunnable()) {
            badgePanel.add(UiTheme.badge("Ejecutable", UiTheme.palette().success()));
        }
        badgePanel.revalidate();
        badgePanel.repaint();

        try {
            if (Files.exists(selected.enunciadoPath())) {
                String md = Files.readString(selected.enunciadoPath(), StandardCharsets.UTF_8);
                enunciadoPane.setText(MarkdownRenderer.toHtml(md));
            } else {
                enunciadoPane.setText(MarkdownRenderer.toHtml(
                        "<p><strong>No se encontró el enunciado.</strong></p><p>" +
                                selected.enunciadoPath() + "</p>"));
            }
            enunciadoPane.setCaretPosition(0);
        } catch (IOException e) {
            enunciadoPane.setText(MarkdownRenderer.toHtml(
                    "<p>Error al leer el enunciado: " + e.getMessage() + "</p>"));
        }

        boolean hasSolution = SolutionReader.solutionExists(selected);
        viewSolutionBtn.setEnabled(hasSolution && selected.solutionType() != Exercise.SolutionType.NONE);

        boolean canEditWork = selected.solutionType() != Exercise.SolutionType.NONE;
        editMyCodeBtn.setEnabled(canEditWork);
        editMyCodeBtn.setToolTipText("Editar tu código en " + StudentWorkspace.directory(selected, projectRoot));

        if (selected.isJavaRunnable()) {
            runMyCodeBtn.setEnabled(true);
            runMyCodeBtn.setToolTipText("Compila y ejecuta los .java de tu carpeta trabajo/");
            runBtn.setEnabled(hasSolution);
            runBtn.setToolTipText("Compila y ejecuta la solución oficial");
        } else if (selected.isSqlRunnable()) {
            runMyCodeBtn.setEnabled(true);
            runMyCodeBtn.setToolTipText("Ejecuta tu .sql en trabajo/ contra la base configurada (JDBC)");
            runBtn.setEnabled(hasSolution);
            runBtn.setToolTipText("Ejecuta el script .sql de la solución oficial");
        } else if (selected.hasSqlRunScript()) {
            runMyCodeBtn.setEnabled(false);
            runMyCodeBtn.setToolTipText("Ejercicio teórico: usa «Mi código» para tu documento (.md)");
            runBtn.setEnabled(true);
            runBtn.setToolTipText("Ejecuta el script de creación de la base (sql/01_crear_base_datos.sql)");
        } else {
            runMyCodeBtn.setEnabled(false);
            runMyCodeBtn.setToolTipText("Solo disponible en ejercicios Java o SQL");
            runBtn.setEnabled(false);
            runBtn.setToolTipText("Sin ejecución automática en el visor");
        }

        if (exerciseChanged) {
            outputArea.setText("Pulsa «Ejecutar mi código» o «Ejecutar solución» para ver la salida.");
        }
        statusLabel.setText(selected.module() + "  ·  " + selected.title());
        helpLabel.setText(UiTheme.typeLabel(selected) + " · " + AppInfo.NAME);
    }

    private void refreshActionButtons() {
        Exercise selected = lastSelected != null ? lastSelected : treePanel.getSelectedExercise();
        if (selected == null) {
            viewSolutionBtn.setEnabled(false);
            editMyCodeBtn.setEnabled(false);
            runMyCodeBtn.setEnabled(false);
            runBtn.setEnabled(false);
            return;
        }
        boolean hasSolution = SolutionReader.solutionExists(selected);
        viewSolutionBtn.setEnabled(hasSolution && selected.solutionType() != Exercise.SolutionType.NONE);
        editMyCodeBtn.setEnabled(selected.solutionType() != Exercise.SolutionType.NONE);
        runMyCodeBtn.setEnabled(selected.isJavaRunnable() || selected.isSqlRunnable());
        runBtn.setEnabled(selected.isJavaRunnable() || selected.isSqlRunnable()
                || (selected.hasSqlRunScript() && hasSolution));
    }

    private Exercise getSelectedOrWarn() {
        Exercise selected = treePanel.getSelectedExercise();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un ejercicio en el índice de la izquierda.",
                    AppInfo.NAME, JOptionPane.INFORMATION_MESSAGE);
        }
        return selected;
    }

    private void viewSolution() {
        Exercise selected = getSelectedOrWarn();
        if (selected == null) {
            return;
        }
        try {
            String code = SolutionReader.readSolutionContent(selected);
            CodeViewerDialog.forOfficialSolution(this, selected, code).setVisible(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al leer la solución: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editMyCode() {
        Exercise selected = getSelectedOrWarn();
        if (selected == null || selected.solutionType() == Exercise.SolutionType.NONE) {
            return;
        }
        try {
            prepareStudentWorkspace(selected);
            String code = StudentWorkspace.readContent(selected, projectRoot);
            if (code.isBlank()) {
                code = emptyWorkPlaceholder(selected);
            }
            CodeViewerDialog.forStudentWork(this, selected, code, projectRoot).setVisible(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al preparar tu carpeta de trabajo:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void prepareStudentWorkspace(Exercise selected) throws IOException {
        StudentWorkspace.ensureDirectory(selected, projectRoot);
        if (!StudentWorkspace.isEmpty(selected, projectRoot)) {
            return;
        }
        if (selected.solutionType() == Exercise.SolutionType.JAVA) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Tu carpeta de trabajo está vacía.\n¿Copiar la solución oficial como punto de partida?\n"
                            + "(Elige «No» para crear solo un esqueleto " + selected.mainClassName().orElse("Main") + ".java)",
                    "Iniciar mi código",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (choice == JOptionPane.CANCEL_OPTION) {
                throw new IOException("Operación cancelada.");
            }
            if (choice == JOptionPane.YES_OPTION && SolutionReader.solutionExists(selected)) {
                StudentWorkspace.copyFromOfficialSolution(selected, projectRoot);
            } else {
                StudentWorkspace.createJavaStub(selected, projectRoot);
            }
        } else if (SolutionReader.solutionExists(selected)) {
            StudentWorkspace.copyFromOfficialSolution(selected, projectRoot);
        }
    }

    private static String emptyWorkPlaceholder(Exercise selected) {
        return switch (selected.solutionType()) {
            case JAVA -> "// Escribe tu código Java aquí\n";
            case SQL -> "-- Escribe tus consultas SQL aquí\n";
            case MARKDOWN -> "# Mi trabajo\n\n";
            case NONE -> "";
        };
    }

    private void runMyCode() {
        Exercise selected = getSelectedOrWarn();
        if (selected == null || !selected.isRunnable()) {
            return;
        }
        if (selected.isSqlRunnable()) {
            if (!ensureSqlConfigured()) {
                return;
            }
            if (!StudentWorkspace.exists(selected, projectRoot)) {
                JOptionPane.showMessageDialog(this,
                        "Aún no tienes un script en «mi código».\nPulsa «Mi código» para empezar.",
                        AppInfo.NAME, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            runSqlFile(selected, StudentWorkspace.primaryFile(selected, projectRoot), "mi código");
            return;
        }
        if (!selected.isJavaRunnable()) {
            JOptionPane.showMessageDialog(this,
                    "Este ejercicio no ejecuta código desde «Ejecutar mi código».\n"
                            + "Usa «Mi código» para tu documento y «Ejecutar solución» si hay script SQL asociado.",
                    AppInfo.NAME, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Path workDir = StudentWorkspace.directory(selected, projectRoot);
        if (!StudentWorkspace.exists(selected, projectRoot)) {
            JOptionPane.showMessageDialog(this,
                    "Aún no tienes código en «mi código».\nPulsa «Mi código» para empezar.",
                    AppInfo.NAME, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        runCode(selected, workDir, "mi código");
    }

    private void runSolution() {
        Exercise selected = getSelectedOrWarn();
        if (selected == null || !selected.isRunnable()) {
            return;
        }
        if (selected.hasSqlRunScript()) {
            if (!ensureSqlConfigured()) {
                return;
            }
            Path sqlPath = selected.sqlExecutionPath().orElse(null);
            if (sqlPath == null) {
                JOptionPane.showMessageDialog(this, "No hay script SQL para ejecutar.",
                        AppInfo.NAME, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (!selected.isSqlRunnable()) {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Se ejecutará el script de creación de la base del curso:\n"
                                + sqlPath + "\n\n"
                                + "Puede borrar y recrear la base academia_idiomas.\n¿Continuar?",
                        "Crear base de datos",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
                runSqlFile(selected, sqlPath, "creación de base de datos");
                return;
            }
            if (!SolutionReader.solutionExists(selected)) {
                JOptionPane.showMessageDialog(this, "No hay solución SQL para este ejercicio.",
                        AppInfo.NAME, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            runSqlFile(selected, sqlPath, "solución oficial");
            return;
        }
        runCode(selected, selected.solutionPath(), "solución oficial");
    }

    private boolean ensureSqlConfigured() {
        if (DatabaseConfig.configExists(projectRoot)) {
            return true;
        }
        int choice = JOptionPane.showConfirmDialog(
                this,
                "No existe database.properties.\n¿Abrir el asistente de conexión SQL ahora?",
                "Conexión SQL",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            new SqlConfigDialog(this, projectRoot).setVisible(true);
        }
        return DatabaseConfig.configExists(projectRoot);
    }

    void runInitDatabaseScript() {
        if (!ensureSqlConfigured()) {
            return;
        }
        Path script = projectRoot.resolve("sql/01_crear_base_datos.sql");
        if (!Files.isRegularFile(script)) {
            JOptionPane.showMessageDialog(this,
                    "No se encontró:\n" + script,
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Se ejecutará el script de creación de la base (puede borrar datos existentes):\n"
                        + script + "\n\n¿Continuar?",
                "Inicializar base de datos",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        runSqlFile(null, script, "inicialización BBDD");
    }

    private void runSqlFile(Exercise selected, Path sqlFile, String label) {
        setExecutionButtonsEnabled(false);
        String title = selected != null ? selected.title() : sqlFile.getFileName().toString();
        outputArea.setText("Ejecutando SQL (" + label + "): " + sqlFile.getFileName() + "\n");
        statusLabel.setText("Ejecutando SQL: " + title);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                StringBuilder out = new StringBuilder();
                out.append("Archivo: ").append(sqlFile).append("\n");
                if (!DatabaseConfig.configExists(projectRoot)) {
                    throw new IOException("Configura la conexión en Herramientas → Configurar conexión SQL…");
                }
                out.append("Conexión: ").append(DatabaseConfig.configFile(projectRoot)).append("\n\n");
                sqlExecutionService.executeSqlFile(sqlFile, projectRoot, out);
                return out.toString();
            }

            @Override
            protected void done() {
                try {
                    outputArea.setText(get());
                    outputArea.setCaretPosition(0);
                    statusLabel.setText("SQL completado — " + title);
                    refreshDatabaseStatusLabel();
                } catch (Exception e) {
                    Throwable cause = e;
                    if (e instanceof java.util.concurrent.ExecutionException ex && ex.getCause() != null) {
                        cause = ex.getCause();
                    }
                    if (cause instanceof SqlScriptException sqlEx) {
                        outputArea.setText(sqlEx.consoleOutput());
                        outputArea.setCaretPosition(0);
                        statusLabel.setText(sqlEx.isPartial()
                                ? "Script SQL a medias — " + title
                                : "Error SQL — " + title);
                    } else {
                        String msg = cause.getMessage() != null ? cause.getMessage() : e.getMessage();
                        outputArea.setText("Error SQL:\n" + msg);
                        statusLabel.setText("Error SQL — " + title);
                    }
                }
                setExecutionButtonsEnabled(true);
                refreshActionButtons();
                outputArea.repaint();
            }
        };
        worker.execute();
    }

    private void runCode(Exercise selected, Path sourceDir, String label) {
        setExecutionButtonsEnabled(false);
        outputArea.setText("Compilando y ejecutando (" + label + ")...\n");
        statusLabel.setText("Ejecutando: " + selected.title());

        SwingWorker<ExecutionService.ExecutionResult, Void> worker = new SwingWorker<>() {
            @Override
            protected ExecutionService.ExecutionResult doInBackground() {
                return executionService.compileAndRun(
                        sourceDir,
                        selected.mainClassName().orElse("Main"),
                        projectRoot
                );
            }

            @Override
            protected void done() {
                try {
                    ExecutionService.ExecutionResult result = get();
                    outputArea.setText(result.output());
                    outputArea.setCaretPosition(0);
                    statusLabel.setText(result.success()
                            ? "Completado — " + selected.title()
                            : "Con errores — " + selected.title());
                } catch (Exception e) {
                    outputArea.setText("Error inesperado:\n" + e.getMessage());
                    statusLabel.setText("Error al ejecutar");
                }
                setExecutionButtonsEnabled(true);
                refreshActionButtons();
                outputArea.repaint();
            }
        };
        worker.execute();
    }

    private void setExecutionButtonsEnabled(boolean enabled) {
        runMyCodeBtn.setEnabled(enabled);
        runBtn.setEnabled(enabled);
        viewSolutionBtn.setEnabled(enabled);
        editMyCodeBtn.setEnabled(enabled);
    }

    void openEnunciadoFolder() {
        Exercise selected = getSelectedOrWarn();
        if (selected == null) {
            return;
        }
        Path folder = selected.enunciadoPath().getParent();
        openFolderAt(folder, "enunciado");
    }

    void openStudentWorkFolder() {
        Exercise selected = getSelectedOrWarn();
        if (selected == null) {
            return;
        }
        try {
            StudentWorkspace.ensureDirectory(selected, projectRoot);
            openFolderAt(StudentWorkspace.directory(selected, projectRoot), "mi código");
        } catch (IOException e) {
            showIoError(e);
        }
    }

    void openEnunciadoTerminal() {
        Exercise selected = getSelectedOrWarn();
        if (selected == null) {
            return;
        }
        openTerminalAt(selected.enunciadoPath().getParent());
    }

    void openStudentWorkTerminal() {
        Exercise selected = getSelectedOrWarn();
        if (selected == null) {
            return;
        }
        try {
            StudentWorkspace.ensureDirectory(selected, projectRoot);
            openTerminalAt(StudentWorkspace.directory(selected, projectRoot));
        } catch (IOException e) {
            showIoError(e);
        }
    }

    private void openFolderAt(Path folder, String label) {
        try {
            DesktopActions.openFolder(folder.toAbsolutePath().normalize());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo abrir la carpeta del " + label + ":\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openTerminalAt(Path folder) {
        try {
            DesktopActions.openTerminal(folder.toAbsolutePath().normalize());
        } catch (IOException e) {
            showIoError(e);
        }
    }

    private void showIoError(IOException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static Path findProjectRoot() {
        return PlatformSupport.findProjectRoot();
    }
}
