package com.ifcd0112.viewer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/** Árbol de esquema: diagrama completo o datos de una tabla al seleccionar en el árbol. */
public final class DatabaseSchemaViewerDialog extends JDialog {

    /** Marcador del nodo «Claves foráneas» en el árbol (no es una tabla). */
    private static final Object FK_GROUP = new Object() {
        @Override
        public String toString() {
            return "Claves foráneas";
        }
    };

    private final Path projectRoot;
    private final JLabel titleLabel = new JLabel();
    private final javax.swing.JTree schemaTree = new javax.swing.JTree(new DefaultMutableTreeNode("Cargando…"));
    private final SchemaDiagramPanel diagramPanel = new SchemaDiagramPanel();
    private final JTable dataTable = new JTable();
    private final JPanel rightCards = new JPanel(new CardLayout());
    private final JScrollPane diagramScroll;
    private final JScrollPane tableDataScroll;
    private final JLabel tableHintLabel = new JLabel(" ");

    private DatabaseSchemaService.DatabaseSchema loadedSchema;
    private String databaseName = "";

    public DatabaseSchemaViewerDialog(java.awt.Frame owner, Path projectRoot) {
        super(owner, "Esquema de la base de datos", false);
        this.projectRoot = projectRoot;
        setMinimumSize(new Dimension(920, 560));
        setLayout(new BorderLayout(8, 8));

        titleLabel.setBorder(new EmptyBorder(10, 12, 0, 12));
        titleLabel.setFont(UiTheme.FONT_UI_BOLD);

        schemaTree.setFont(UiTheme.FONT_MONO.deriveFont(12f));
        schemaTree.addTreeSelectionListener(e -> onTreeSelectionChanged());

        JScrollPane treeScroll = new JScrollPane(schemaTree);
        treeScroll.setPreferredSize(new Dimension(260, 0));
        treeScroll.setBorder(UiTheme.sectionBorder("Tablas y columnas"));

        diagramScroll = new JScrollPane(diagramPanel);
        diagramScroll.setBorder(UiTheme.sectionBorder("Diagrama (arrastre tablas para ordenar)"));
        diagramScroll.getVerticalScrollBar().setUnitIncrement(16);
        diagramScroll.getHorizontalScrollBar().setUnitIncrement(16);

        dataTable.setFont(UiTheme.FONT_MONO.deriveFont(12f));
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableDataScroll = new JScrollPane(dataTable);
        tableDataScroll.setBorder(UiTheme.sectionBorder("Datos de la tabla"));
        tableDataScroll.getVerticalScrollBar().setUnitIncrement(16);
        tableDataScroll.getHorizontalScrollBar().setUnitIncrement(16);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableHintLabel.setBorder(new EmptyBorder(6, 8, 4, 8));
        tableHintLabel.setFont(UiTheme.FONT_UI.deriveFont(12f));
        tableCard.add(tableHintLabel, BorderLayout.NORTH);
        tableCard.add(tableDataScroll, BorderLayout.CENTER);

        rightCards.add(diagramScroll, "diagram");
        rightCards.add(tableCard, "data");

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, rightCards);
        split.setResizeWeight(0.28);
        split.setDividerLocation(280);
        split.setBorder(new EmptyBorder(0, 12, 0, 12));

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Cerrar");
        closeBtn.addActionListener(e -> dispose());
        south.add(closeBtn);
        south.setBorder(new EmptyBorder(0, 12, 10, 12));

        add(titleLabel, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        applyTheme();
        UiTheme.addThemeListener(this::applyTheme);
        pack();
        setLocationRelativeTo(owner);
        reloadSchema();
    }

    public void setDatabaseName(String name) {
        this.databaseName = name != null ? name : "";
    }

    public void reloadSchema() {
        if (!DatabaseConfig.configExists(projectRoot)) {
            titleLabel.setText("Configura la conexión SQL primero.");
            return;
        }
        try {
            if (databaseName.isBlank()) {
                databaseName = DatabaseConfig.activeDatabaseName(DatabaseConfig.load(projectRoot));
            }
        } catch (IOException ignored) {
            // handled below
        }
        if (databaseName == null || databaseName.isBlank()) {
            titleLabel.setText("Selecciona una base en el menú Base de datos.");
            return;
        }
        titleLabel.setText("Base de datos: " + databaseName + " — pulsa el nombre de la base para el diagrama");
        final String db = databaseName;
        new SwingWorker<DatabaseSchemaService.DatabaseSchema, Void>() {
            @Override
            protected DatabaseSchemaService.DatabaseSchema doInBackground() throws Exception {
                return DatabaseSchemaService.load(projectRoot, db);
            }

            @Override
            protected void done() {
                try {
                    loadedSchema = get();
                    fillTree(loadedSchema);
                    diagramPanel.setLayoutContext(projectRoot, db);
                    diagramPanel.setSchema(loadedSchema);
                    diagramPanel.revalidate();
                    showDiagramCard();
                    schemaTree.setSelectionRow(0);
                } catch (Exception e) {
                    Throwable c = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(DatabaseSchemaViewerDialog.this,
                            c.getMessage(), "Error al cargar esquema", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void onTreeSelectionChanged() {
        TreePath path = schemaTree.getSelectionPath();
        if (path == null || loadedSchema == null) {
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        String table = tableNameFromNode(node);
        if (table == null) {
            showDiagramCard();
        } else {
            loadAndShowTable(table);
        }
    }

    private static String tableNameFromNode(DefaultMutableTreeNode node) {
        if (node == null || node.isRoot()) {
            return null;
        }
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if (parent == null) {
            return null;
        }
        if (parent.isRoot()) {
            Object obj = node.getUserObject();
            if (FK_GROUP.equals(obj)) {
                return null;
            }
            return obj.toString();
        }
        if (parent.getParent() instanceof DefaultMutableTreeNode grand && grand.isRoot()) {
            Object obj = parent.getUserObject();
            if (FK_GROUP.equals(obj)) {
                return null;
            }
            return obj.toString();
        }
        return null;
    }

    private void showDiagramCard() {
        setRightCardTitle(diagramScroll, "Diagrama (arrastre tablas para ordenar)");
        getCardLayout().show(rightCards, "diagram");
        titleLabel.setText("Base de datos: " + databaseName + " — diagrama completo");
    }

    private void loadAndShowTable(String tableName) {
        setRightCardTitle(tableDataScroll, "Datos: " + tableName);
        getCardLayout().show(rightCards, "data");
        titleLabel.setText("Base de datos: " + databaseName + " — tabla " + tableName);
        tableHintLabel.setText("Cargando…");
        dataTable.setModel(new DefaultTableModel());

        final String db = databaseName;
        new SwingWorker<DatabaseSchemaService.TablePreview, Void>() {
            @Override
            protected DatabaseSchemaService.TablePreview doInBackground() throws Exception {
                return DatabaseSchemaService.loadTablePreview(projectRoot, db, tableName);
            }

            @Override
            protected void done() {
                try {
                    DatabaseSchemaService.TablePreview preview = get();
                    applyTablePreview(preview);
                } catch (Exception e) {
                    Throwable c = e.getCause() != null ? e.getCause() : e;
                    tableHintLabel.setText("Error: " + c.getMessage());
                    JOptionPane.showMessageDialog(DatabaseSchemaViewerDialog.this,
                            c.getMessage(), "Error al leer tabla", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void applyTablePreview(DatabaseSchemaService.TablePreview preview) {
        DefaultTableModel model = new DefaultTableModel(
                preview.rows().toArray(new Object[0][]),
                preview.columnNames().toArray()
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        dataTable.setModel(model);
        String hint = preview.rowCount() + " fila(s)";
        if (preview.rowCount() >= preview.maxRows()) {
            hint += " (máximo " + preview.maxRows() + " mostradas)";
        }
        tableHintLabel.setText("SELECT * FROM " + preview.tableName() + " — " + hint);
    }

    private CardLayout getCardLayout() {
        return (CardLayout) rightCards.getLayout();
    }

    private static void setRightCardTitle(JScrollPane scroll, String title) {
        if (scroll.getBorder() instanceof javax.swing.border.CompoundBorder compound) {
            if (compound.getOutsideBorder() instanceof TitledBorder titled) {
                titled.setTitle(title);
                scroll.repaint();
            }
        }
    }

    private void fillTree(DatabaseSchemaService.DatabaseSchema schema) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(schema.databaseName());
        for (DatabaseSchemaService.TableSchema table : schema.tables()) {
            DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(table.name());
            for (DatabaseSchemaService.ColumnSchema col : table.columns()) {
                String label = (col.primaryKey() ? "PK " : "") + col.name() + " : " + col.sqlType();
                tableNode.add(new DefaultMutableTreeNode(label));
            }
            root.add(tableNode);
        }
        if (!schema.foreignKeys().isEmpty()) {
            DefaultMutableTreeNode fkRoot = new DefaultMutableTreeNode(FK_GROUP);
            for (DatabaseSchemaService.ForeignKeySchema fk : schema.foreignKeys()) {
                fkRoot.add(new DefaultMutableTreeNode(
                        fk.fromTable() + "." + fk.fromColumn() + " → "
                                + fk.toTable() + "." + fk.toColumn()));
            }
            root.add(fkRoot);
        }
        schemaTree.setModel(new DefaultTreeModel(root));
        for (int i = 0; i < schemaTree.getRowCount(); i++) {
            schemaTree.expandRow(i);
        }
    }

    private void applyTheme() {
        ThemePalette p = UiTheme.palette();
        getContentPane().setBackground(p.bg());
        titleLabel.setForeground(p.text());
        tableHintLabel.setForeground(p.textMuted());
        dataTable.setBackground(p.consoleBg());
        dataTable.setForeground(p.consoleText());
        dataTable.setGridColor(p.border());
        dataTable.getTableHeader().setBackground(p.surface());
        dataTable.getTableHeader().setForeground(p.text());
    }
}
