package com.ifcd0112.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

/** Consola SQL interactiva contra la base activa. */
public final class SqlConsoleDialog extends JDialog {

    private static final int TIMEOUT = 15;

    private final Path projectRoot;
    private final JTextArea inputArea = new JTextArea(8, 60);
    private final JTextArea outputArea = new JTextArea(14, 60);
    private final JLabel dbLabel = new JLabel();
    private final List<String> history = new ArrayList<>();
    private int historyBrowseIndex = -1;
    private String historyDraft;
    private SqlSuggestionSupport.Handle sqlSuggestions;

    public SqlConsoleDialog(java.awt.Frame owner, Path projectRoot) {
        super(owner, "Consola SQL", false);
        this.projectRoot = projectRoot;
        setMinimumSize(new Dimension(720, 520));
        setLayout(new BorderLayout(8, 8));

        dbLabel.setBorder(new EmptyBorder(8, 12, 0, 12));
        dbLabel.setFont(UiTheme.FONT_UI);
        refreshDbLabel();

        inputArea.setFont(UiTheme.FONT_MONO);
        inputArea.setLineWrap(false);
        inputArea.setTabSize(4);
        PlatformSupport.configureKeyboardInput(inputArea);
        inputArea.setText("SELECT 1;\n");

        outputArea.setFont(UiTheme.FONT_MONO);
        outputArea.setEditable(false);
        outputArea.setLineWrap(false);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(0, 12, 0, 12));
        inputPanel.add(new JLabel(
                "Consulta SQL (Ctrl+Enter ejecutar; Ctrl+Espacio sugerencias; ↑↓ historial en 1.ª/última línea):"),
                BorderLayout.NORTH);
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);

        JPanel outPanel = new JPanel(new BorderLayout());
        outPanel.setBorder(new EmptyBorder(0, 12, 0, 12));
        outPanel.add(new JLabel("Resultado:"), BorderLayout.NORTH);
        outPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, outPanel);
        split.setResizeWeight(0.35);
        split.setDividerLocation(0.35);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton runBtn = new JButton("Ejecutar");
        JButton clearOutBtn = new JButton("Limpiar salida");
        JButton clearInBtn = new JButton("Limpiar consulta");
        JButton closeBtn = new JButton("Cerrar");
        runBtn.addActionListener(e -> executeQuery());
        clearOutBtn.addActionListener(e -> outputArea.setText(""));
        clearInBtn.addActionListener(e -> inputArea.setText(""));
        closeBtn.addActionListener(e -> dispose());
        buttons.add(runBtn);
        buttons.add(clearOutBtn);
        buttons.add(clearInBtn);
        buttons.add(closeBtn);
        JPanel south = new JPanel(new BorderLayout());
        south.add(buttons, BorderLayout.CENTER);
        south.setBorder(new EmptyBorder(0, 4, 8, 4));

        add(dbLabel, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(runBtn);
        inputArea.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, java.awt.event.InputEvent.CTRL_DOWN_MASK),
                "execute");
        inputArea.getActionMap().put("execute", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                executeQuery();
            }
        });
        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (sqlSuggestions != null && sqlSuggestions.isPopupVisible()) {
                    return;
                }
                if (e.getModifiersEx() != 0) {
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_UP && historyStepOlder()) {
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN && historyStepNewer()) {
                    e.consume();
                }
            }
        });
        sqlSuggestions = SqlSuggestionSupport.attach(inputArea, projectRoot, () -> history);
        loadHistory();

        applyTheme();
        UiTheme.addThemeListener(this::applyTheme);
        pack();
        setLocationRelativeTo(owner);
    }

    public void refreshDbLabel() {
        try {
            if (DatabaseConfig.configExists(projectRoot)) {
                String db = DatabaseConfig.activeDatabaseName(DatabaseConfig.load(projectRoot));
                dbLabel.setText("Base activa: " + (db.isBlank() ? "(ninguna en la URL)" : db));
            } else {
                dbLabel.setText("Sin database.properties — configura la conexión en Herramientas.");
            }
        } catch (IOException e) {
            dbLabel.setText("Error al leer configuración.");
        }
        if (sqlSuggestions != null) {
            sqlSuggestions.refreshSchema();
        }
    }

    @Override
    public void dispose() {
        if (sqlSuggestions != null) {
            sqlSuggestions.dispose();
            sqlSuggestions = null;
        }
        super.dispose();
    }

    private void loadHistory() {
        try {
            history.clear();
            history.addAll(SqlHistoryStore.load(projectRoot));
        } catch (IOException ignored) {
            history.clear();
        }
        resetHistoryBrowse();
    }

    private void resetHistoryBrowse() {
        historyBrowseIndex = -1;
        historyDraft = null;
    }

    private boolean historyStepOlder() {
        if (history.isEmpty() || !caretOnFirstLine()) {
            return false;
        }
        if (historyBrowseIndex < 0) {
            historyDraft = inputArea.getText();
            historyBrowseIndex = history.size() - 1;
            showHistoryEntry(history.get(historyBrowseIndex));
            return true;
        }
        if (historyBrowseIndex <= 0) {
            return true;
        }
        historyBrowseIndex--;
        showHistoryEntry(history.get(historyBrowseIndex));
        return true;
    }

    private boolean historyStepNewer() {
        if (history.isEmpty() || historyBrowseIndex < 0 || !caretOnLastLine()) {
            return false;
        }
        if (historyBrowseIndex >= history.size() - 1) {
            historyBrowseIndex = -1;
            inputArea.setText(historyDraft != null ? historyDraft : "");
            inputArea.setCaretPosition(inputArea.getDocument().getLength());
            historyDraft = null;
            return true;
        }
        historyBrowseIndex++;
        showHistoryEntry(history.get(historyBrowseIndex));
        return true;
    }

    private void showHistoryEntry(String sql) {
        inputArea.setText(sql);
        inputArea.setCaretPosition(0);
    }

    private boolean caretOnFirstLine() {
        try {
            return inputArea.getLineOfOffset(inputArea.getCaretPosition()) == 0;
        } catch (javax.swing.text.BadLocationException e) {
            return true;
        }
    }

    private boolean caretOnLastLine() {
        try {
            int line = inputArea.getLineOfOffset(inputArea.getCaretPosition());
            return line >= inputArea.getLineCount() - 1;
        } catch (javax.swing.text.BadLocationException e) {
            return true;
        }
    }

    private void rememberQuery(String sql) {
        if (sql.isEmpty()) {
            return;
        }
        if (!history.isEmpty() && history.get(history.size() - 1).equals(sql)) {
            return;
        }
        history.add(sql);
        if (history.size() > SqlHistoryStore.MAX_ENTRIES) {
            history.remove(0);
        }
        resetHistoryBrowse();
        try {
            SqlHistoryStore.save(projectRoot, history);
        } catch (IOException ignored) {
            // historial solo en memoria si no se puede escribir
        }
    }

    private void executeQuery() {
        String sql = inputArea.getText().trim();
        if (sql.isEmpty()) {
            return;
        }
        rememberQuery(sql);
        if (!DatabaseConfig.configExists(projectRoot)) {
            JOptionPane.showMessageDialog(this,
                    "Configura la conexión en Herramientas → Configurar conexión SQL…",
                    "Consola SQL", JOptionPane.WARNING_MESSAGE);
            return;
        }
        outputArea.append("\n>>> " + sql.replace('\n', ' ') + "\n");
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                StringBuilder out = new StringBuilder();
                List<String> statements = SqlExecutionService.splitStatements(sql);
                if (statements.isEmpty()) {
                    statements = List.of(sql);
                }
                try (Connection conn = DatabaseConfig.openConnectionToActiveDatabase(projectRoot)) {
                    conn.setAutoCommit(true);
                    SqlExecutionService.executeOnConnection(conn, statements, out, TIMEOUT);
                }
                return out.toString();
            }

            @Override
            protected void done() {
                try {
                    outputArea.append(get());
                } catch (Exception e) {
                    Throwable c = e.getCause() != null ? e.getCause() : e;
                    if (c instanceof SqlScriptException sse) {
                        outputArea.append(sse.consoleOutput());
                    } else {
                        outputArea.append("ERROR: " + c.getMessage() + "\n");
                    }
                }
                outputArea.setCaretPosition(outputArea.getDocument().getLength());
            }
        };
        worker.execute();
    }

    private void applyTheme() {
        ThemePalette p = UiTheme.palette();
        getContentPane().setBackground(p.bg());
        inputArea.setBackground(p.consoleBg());
        inputArea.setForeground(p.consoleText());
        inputArea.setCaretColor(p.consoleText());
        outputArea.setBackground(p.consoleBg());
        outputArea.setForeground(p.consoleText());
        dbLabel.setForeground(p.text());
        if (sqlSuggestions != null) {
            sqlSuggestions.applyTheme(p);
        }
    }
}
