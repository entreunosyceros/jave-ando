package com.ifcd0112.viewer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/** Elige la base de datos activa (actualiza jdbc.url en database.properties). */
public final class SelectDatabaseDialog extends JDialog {

    private final Path projectRoot;
    private final Runnable onSelected;
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> dbList = new JList<>(listModel);
    private final JLabel statusLabel = new JLabel(" ");

    public SelectDatabaseDialog(java.awt.Frame owner, Path projectRoot, Runnable onSelected) {
        super(owner, "Seleccionar base de datos", true);
        this.projectRoot = projectRoot;
        this.onSelected = onSelected;
        setLayout(new BorderLayout(10, 10));
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(12, 12, 0, 12));
        top.add(new JLabel("<html>Bases disponibles en el servidor (sin esquemas de sistema).</html>"),
                BorderLayout.NORTH);
        top.add(statusLabel, BorderLayout.SOUTH);

        dbList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                statusLabel.setText(selectedOrEmpty());
            }
        });
        dbList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && dbList.getSelectedValue() != null) {
                    applySelection();
                }
            }
        });

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(dbList), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("Actualizar lista");
        JButton okBtn = new JButton("Usar esta base");
        JButton cancelBtn = new JButton("Cancelar");
        refreshBtn.addActionListener(e -> refreshList());
        okBtn.addActionListener(e -> applySelection());
        cancelBtn.addActionListener(e -> dispose());
        buttons.add(refreshBtn);
        buttons.add(okBtn);
        buttons.add(cancelBtn);
        JPanel south = new JPanel(new BorderLayout());
        south.setBorder(new EmptyBorder(0, 12, 12, 12));
        south.add(buttons, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        setMinimumSize(new java.awt.Dimension(380, 320));
        pack();
        setLocationRelativeTo(owner);
        refreshList();
    }

    private void refreshList() {
        listModel.clear();
        try {
            if (!DatabaseConfig.configExists(projectRoot)) {
                statusLabel.setText("Configura la conexión SQL primero.");
                return;
            }
            String current = DatabaseConfig.activeDatabaseName(DatabaseConfig.load(projectRoot));
            for (String db : DatabaseConfig.listUserDatabases(projectRoot)) {
                listModel.addElement(db);
            }
            if (current != null && !current.isBlank()) {
                dbList.setSelectedValue(current, true);
                dbList.ensureIndexIsVisible(dbList.getSelectedIndex());
            }
            statusLabel.setText(listModel.isEmpty() ? "No hay bases de usuario." : selectedOrEmpty());
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String selectedOrEmpty() {
        String sel = dbList.getSelectedValue();
        return sel == null ? "Selecciona una base." : "Base seleccionada: " + sel;
    }

    private void applySelection() {
        String sel = dbList.getSelectedValue();
        if (sel == null || sel.isBlank()) {
            JOptionPane.showMessageDialog(this, "Selecciona una base de la lista.", "Base de datos",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            DatabaseConfig.setActiveDatabase(projectRoot, sel);
            if (onSelected != null) {
                onSelected.run();
            }
            dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
