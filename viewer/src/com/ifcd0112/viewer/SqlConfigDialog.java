package com.ifcd0112.viewer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingWorker;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/** Diálogo para editar y probar database.properties. */
public final class SqlConfigDialog extends JDialog {

    private final Path projectRoot;
    private final JTextField driverField = new JTextField(40);
    private final JTextField urlField = new JTextField(40);
    private final JTextField userField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JTextArea statusArea = new JTextArea(4, 50);
    private final JButton testBtn = new JButton("Probar conexión");

    public SqlConfigDialog(java.awt.Frame owner, Path projectRoot) {
        super(owner, "Configurar conexión SQL", true);
        this.projectRoot = projectRoot;
        setLayout(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 12, 0, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;
        addRow(form, gbc, row++, "Driver JDBC:", driverField);
        addRow(form, gbc, row++, "URL:", urlField);
        addRow(form, gbc, row++, "Usuario:", userField);
        addRow(form, gbc, row++, "Contraseña:", passwordField);

        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new EmptyBorder(0, 12, 0, 12));
        statusPanel.add(new JLabel("Estado:"), BorderLayout.NORTH);
        statusPanel.add(statusArea, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Guardar");
        JButton closeBtn = new JButton("Cerrar");
        testBtn.addActionListener(e -> testConnection());
        saveBtn.addActionListener(e -> saveConfig());
        closeBtn.addActionListener(e -> dispose());
        buttons.add(testBtn);
        buttons.add(saveBtn);
        buttons.add(closeBtn);

        add(form, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        add(buttons, BorderLayout.PAGE_END);

        loadFields();
        pack();
        setLocationRelativeTo(owner);
    }

    private static void addRow(JPanel form, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(field, gbc);
    }

    private void loadFields() {
        try {
            Properties props = DatabaseConfig.loadOrExample(projectRoot);
            driverField.setText(props.getProperty("jdbc.driver", "com.mysql.cj.jdbc.Driver"));
            urlField.setText(props.getProperty("jdbc.url", ""));
            userField.setText(props.getProperty("jdbc.user", "root"));
            passwordField.setText(props.getProperty("jdbc.password", ""));
            if (DatabaseConfig.configExists(projectRoot)) {
                statusArea.setText("Configuración cargada desde:\n" + DatabaseConfig.configFile(projectRoot));
            } else {
                statusArea.setText(
                        "Aún no hay database.properties guardado.\n"
                                + "Ajusta los valores y pulsa Guardar.");
            }
        } catch (IOException ex) {
            statusArea.setText("Error al cargar: " + ex.getMessage());
        }
    }

    private Properties collectProperties() {
        Properties props = new Properties();
        props.setProperty("jdbc.driver", driverField.getText().trim());
        props.setProperty("jdbc.url", urlField.getText().trim());
        props.setProperty("jdbc.user", userField.getText().trim());
        props.setProperty("jdbc.password", new String(passwordField.getPassword()));
        return props;
    }

    private void saveConfig() {
        try {
            DatabaseConfig.save(projectRoot, collectProperties());
            statusArea.setText("Guardado en:\n" + DatabaseConfig.configFile(projectRoot));
            JOptionPane.showMessageDialog(this, "Configuración guardada.", "SQL", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void testConnection() {
        try {
            DatabaseConfig.save(projectRoot, collectProperties());
        } catch (IOException ex) {
            statusArea.setText(ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        statusArea.setText("Comprobando conexión (si falta el conector en lib/, se descargará automáticamente)…");
        testBtn.setEnabled(false);
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return DatabaseConfig.testConnection(projectRoot);
            }

            @Override
            protected void done() {
                testBtn.setEnabled(true);
                try {
                    String msg = get();
                    statusArea.setText(msg);
                    JOptionPane.showMessageDialog(
                            SqlConfigDialog.this,
                            msg,
                            "Probar conexión",
                            msg.contains("correcta") && !msg.startsWith("Error")
                                    ? JOptionPane.INFORMATION_MESSAGE
                                    : JOptionPane.WARNING_MESSAGE
                    );
                } catch (Exception e) {
                    String err = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    statusArea.setText(err);
                    JOptionPane.showMessageDialog(SqlConfigDialog.this, err, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
