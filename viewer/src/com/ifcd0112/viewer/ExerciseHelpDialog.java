package com.ifcd0112.viewer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ExerciseHelpDialog extends JDialog {

    private final JEditorPane contentPane = new JEditorPane();
    private final JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    public ExerciseHelpDialog(Frame owner, java.nio.file.Path projectRoot) {
        super(owner, "Cómo añadir ejercicios", true);
        setMinimumSize(new Dimension(720, 560));
        setLocationRelativeTo(owner);

        contentPane.setEditable(false);
        contentPane.setContentType("text/html");
        contentPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        contentPane.setFont(UiTheme.FONT_UI);

        reloadContent(projectRoot);

        JScrollPane scroll = new JScrollPane(contentPane);
        scroll.setBorder(BorderFactory.createLineBorder(UiTheme.palette().border()));
        UiTheme.configureScroll(scroll, true, false);

        JButton closeBtn = UiTheme.primaryButton("Cerrar", UiTheme.palette().primary(), UiTheme.palette().primaryHover());
        closeBtn.addActionListener(e -> dispose());

        southPanel.setBorder(new EmptyBorder(8, 14, 12, 14));
        southPanel.add(closeBtn);

        setLayout(new BorderLayout(0, 0));
        add(wrapWithPadding(scroll), BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        applyTheme(projectRoot);
        UiTheme.addThemeListener(() -> applyTheme(projectRoot));
        setSize(800, 620);
    }

    private JPanel wrapWithPadding(JComponent inner) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(12, 14, 0, 14));
        panel.add(inner, BorderLayout.CENTER);
        return panel;
    }

    private void reloadContent(java.nio.file.Path projectRoot) {
        String markdown = HelpContent.loadAddExerciseGuide(projectRoot);
        contentPane.setText(MarkdownRenderer.toHtml(markdown, UiTheme.isDark()));
        contentPane.setCaretPosition(0);
    }

    private void applyTheme(java.nio.file.Path projectRoot) {
        ThemePalette p = UiTheme.palette();
        getContentPane().setBackground(p.bg());
        southPanel.setBackground(p.bg());
        contentPane.setBackground(p.surface());
        reloadContent(projectRoot);
        repaint();
    }
}
