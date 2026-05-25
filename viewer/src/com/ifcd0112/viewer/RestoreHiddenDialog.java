package com.ifcd0112.viewer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class RestoreHiddenDialog extends JDialog {

    private final ExerciseCatalog catalog;
    private final JList<Exercise> exerciseList;
    private boolean restored;

    public RestoreHiddenDialog(Frame owner, ExerciseCatalog catalog) {
        super(owner, "Restaurar ejercicios ocultos", true);
        this.catalog = catalog;
        setMinimumSize(new Dimension(440, 300));
        setLocationRelativeTo(owner);

        List<Exercise> hidden = catalog.getRestorableHidden();
        exerciseList = new JList<>(hidden.toArray(Exercise[]::new));
        exerciseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (!hidden.isEmpty()) {
            exerciseList.setSelectedIndex(0);
        }

        JLabel hint = new JLabel("<html>Ejercicios del curso que ocultaste con «Eliminar ejercicio…».</html>");
        hint.setFont(UiTheme.FONT_UI.deriveFont(12f));
        hint.setBorder(new EmptyBorder(10, 14, 6, 14));

        JButton restoreBtn = UiTheme.primaryButton("Restaurar en el índice", UiTheme.palette().success(), UiTheme.palette().successHover());
        restoreBtn.addActionListener(e -> restoreSelected());
        JButton cancelBtn = UiTheme.neutralButton("Cancelar");
        cancelBtn.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBorder(new EmptyBorder(8, 14, 12, 14));
        buttons.add(cancelBtn);
        buttons.add(restoreBtn);

        setLayout(new BorderLayout());
        add(hint, BorderLayout.NORTH);
        add(new JScrollPane(exerciseList), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        if (hidden.isEmpty()) {
            restoreBtn.setEnabled(false);
            hint.setText("No hay ejercicios ocultos.");
        }

        applyTheme();
    }

    public boolean wasRestored() {
        return restored;
    }

    private void restoreSelected() {
        Exercise selected = exerciseList.getSelectedValue();
        if (selected == null) {
            return;
        }
        try {
            catalog.restoreHidden(selected.id());
            restored = true;
            dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyTheme() {
        getContentPane().setBackground(UiTheme.palette().bg());
    }
}
