package com.ifcd0112.viewer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class RemoveExerciseDialog extends JDialog {

    private final ExerciseCatalog catalog;
    private final JList<Exercise> exerciseList;
    private boolean removed;

    public RemoveExerciseDialog(Frame owner, ExerciseCatalog catalog, Exercise preselected) {
        super(owner, "Eliminar ejercicio del índice", true);
        this.catalog = catalog;
        setMinimumSize(new Dimension(480, 360));
        setLocationRelativeTo(owner);

        List<Exercise> all = catalog.getAll();
        exerciseList = new JList<>(all.toArray(Exercise[]::new));
        exerciseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        exerciseList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String tag = catalog.isCustom(value.id()) ? "añadido" : "curso";
            JLabel label = new JLabel(value.title() + "  [" + value.id() + "] — " + tag);
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(4, 8, 4, 8));
            if (isSelected) {
                label.setBackground(UiTheme.palette().listSel());
                label.setForeground(UiTheme.palette().listSelText());
            } else {
                label.setBackground(UiTheme.palette().bg());
                label.setForeground(UiTheme.palette().text());
            }
            return label;
        });

        if (preselected != null) {
            exerciseList.setSelectedValue(preselected, true);
        } else if (!all.isEmpty()) {
            exerciseList.setSelectedIndex(0);
        }

        JLabel hint = new JLabel("<html>Elige el ejercicio a quitar del <b>índice</b>.<br>"
                + "· <b>Añadido</b>: se borra su registro (puedes volver a crearlo con «Añadir»).<br>"
                + "· <b>Curso</b>: solo se oculta (los .md y soluciones no se borran). "
                + "Restaura con «Restaurar ejercicios ocultos…».</html>");
        hint.setFont(UiTheme.FONT_UI.deriveFont(12f));
        hint.setBorder(new EmptyBorder(10, 14, 6, 14));

        JButton removeBtn = UiTheme.primaryButton("Eliminar del índice", new Color(0xDC, 0x26, 0x26), new Color(0xB9, 0x1C, 0x1C));
        removeBtn.addActionListener(e -> removeSelected());
        JButton cancelBtn = UiTheme.neutralButton("Cancelar");
        cancelBtn.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBorder(new EmptyBorder(8, 14, 12, 14));
        buttons.add(cancelBtn);
        buttons.add(removeBtn);

        setLayout(new BorderLayout());
        add(hint, BorderLayout.NORTH);
        add(new JScrollPane(exerciseList), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        if (all.isEmpty()) {
            removeBtn.setEnabled(false);
            hint.setText("No hay ejercicios en el índice.");
        }

        applyTheme();
        UiTheme.addThemeListener(this::applyTheme);
    }

    public boolean wasRemoved() {
        return removed;
    }

    private void removeSelected() {
        Exercise selected = exerciseList.getSelectedValue();
        if (selected == null) {
            return;
        }
        String extra = catalog.isCustom(selected.id())
                ? "Se eliminará su registro en ejercicios-personalizados/."
                : "Es un ejercicio del curso: solo se ocultará del índice (archivos intactos).";
        int ok = JOptionPane.showConfirmDialog(
                this,
                "¿Quitar «" + selected.title() + "» del índice?\n\n" + extra,
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            catalog.removeFromIndex(selected.id());
            removed = true;
            dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyTheme() {
        getContentPane().setBackground(UiTheme.palette().bg());
    }
}
