package com.ifcd0112.viewer;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExerciseTreePanel extends JPanel {

    private final JTree tree;
    private final JScrollPane scroll;
    private final Map<Exercise, DefaultMutableTreeNode> exerciseNodes = new LinkedHashMap<>();
    private final ExerciseTreeCellRenderer cellRenderer = new ExerciseTreeCellRenderer();

    public ExerciseTreePanel(ExerciseCatalog catalog) {
        setLayout(new BorderLayout());
        setOpaque(true);

        DefaultTreeModel model = buildModel(catalog);
        tree = new JTree(model);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setRowHeight(32);
        tree.setFont(UiTheme.FONT_UI);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(cellRenderer);
        installPlainTreeUi(tree);

        expandAll();

        scroll = new JScrollPane(tree);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        UiTheme.configureScroll(scroll, true, true);
        add(scroll, BorderLayout.CENTER);

        UiTheme.addThemeListener(this::applyTheme);
        applyTheme();
    }

    /**
     * Evita que el LAF GTK pinte filas con fondo blanco propio del sistema.
     */
    private static void installPlainTreeUi(JTree tree) {
        tree.setUI(new BasicTreeUI());
        tree.setOpaque(true);
    }

    public void applyTheme() {
        ThemePalette p = UiTheme.palette();
        Color indexBg = p.bg();

        setBackground(indexBg);
        setOpaque(true);

        cellRenderer.refreshTheme();

        if (tree != null) {
            tree.setBackground(indexBg);
            tree.setForeground(p.text());
            installPlainTreeUi(tree);
            tree.setCellRenderer(cellRenderer);
            tree.revalidate();
            tree.repaint();
        }

        if (scroll != null) {
            scroll.setOpaque(true);
            scroll.setBackground(indexBg);
            scroll.getViewport().setOpaque(true);
            scroll.getViewport().setBackground(indexBg);
            if (scroll.getViewport().getView() != null) {
                scroll.getViewport().getView().setBackground(indexBg);
            }
        }
    }

    /** Reconstruye el índice tras añadir o eliminar ejercicios. */
    public void reload(ExerciseCatalog catalog) {
        exerciseNodes.clear();
        DefaultTreeModel model = buildModel(catalog);
        tree.setModel(model);
        expandAll();
        tree.revalidate();
        tree.repaint();
    }

    private DefaultTreeModel buildModel(ExerciseCatalog catalog) {
        exerciseNodes.clear();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Todos los ejercicios");
        for (Exercise ex : catalog.getAll()) {
            exerciseNodes.put(ex, new DefaultMutableTreeNode(ex));
        }
        Map<String, DefaultMutableTreeNode> modules = new LinkedHashMap<>();
        for (Exercise ex : catalog.getAll()) {
            DefaultMutableTreeNode moduleNode = modules.computeIfAbsent(ex.module(), name -> {
                DefaultMutableTreeNode n = new DefaultMutableTreeNode(new ModuleItem(name));
                root.add(n);
                return n;
            });
            moduleNode.add(exerciseNodes.get(ex));
        }
        return new DefaultTreeModel(root);
    }

    public void addSelectionListener(javax.swing.event.TreeSelectionListener listener) {
        tree.addTreeSelectionListener(listener);
    }

    public Exercise getSelectedExercise() {
        Object selected = tree.getLastSelectedPathComponent();
        if (selected instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof Exercise ex) {
            return ex;
        }
        return null;
    }

    public void selectFirstExercise() {
        if (!exerciseNodes.isEmpty()) {
            Exercise first = exerciseNodes.keySet().iterator().next();
            selectExercise(first);
        }
    }

    public void selectExercise(Exercise ex) {
        DefaultMutableTreeNode node = exerciseNodes.get(ex);
        if (node != null) {
            TreePath path = new TreePath(node.getPath());
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
        }
    }

    private void expandAll() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    record ModuleItem(String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private static final class ExerciseTreeCellRenderer implements TreeCellRenderer {
        private final JPanel cell = new JPanel(new BorderLayout(8, 0));
        private final JLabel label = new JLabel();
        private Color indexBg = ThemePalette.light().bg();
        private ThemePalette palette = ThemePalette.light();

        ExerciseTreeCellRenderer() {
            cell.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 4));
            cell.add(label, BorderLayout.CENTER);
            label.setOpaque(false);
        }

        void refreshTheme() {
            palette = UiTheme.palette();
            indexBg = palette.bg();
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                                                      boolean expanded, boolean leaf, int rowIndex,
                                                      boolean hasFocus) {
            label.setOpaque(false);

            if (selected) {
                cell.setOpaque(true);
                cell.setBackground(palette.listSel());
            } else {
                cell.setOpaque(false);
                cell.setBackground(indexBg);
            }

            if (value instanceof DefaultMutableTreeNode node) {
                Object user = node.getUserObject();
                if (user instanceof ModuleItem module) {
                    label.setText(module.name());
                    label.setFont(UiTheme.FONT_UI_BOLD);
                    label.setForeground(selected ? palette.listSelText() : palette.textMuted());
                    label.setIcon(AppIcons.moduleIcon());
                    label.setToolTipText(null);
                } else if (user instanceof Exercise ex) {
                    label.setText(ex.title());
                    label.setFont(UiTheme.FONT_UI);
                    label.setForeground(selected ? palette.listSelText() : palette.text());
                    label.setIcon(AppIcons.forExercise(ex));
                    label.setToolTipText(UiTheme.typeLabel(ex) + " · " + ex.module());
                } else {
                    label.setText(String.valueOf(user));
                    label.setFont(UiTheme.FONT_UI);
                    label.setForeground(selected ? palette.listSelText() : palette.text());
                    label.setIcon(null);
                    label.setToolTipText(null);
                }
            }

            return cell;
        }
    }
}
