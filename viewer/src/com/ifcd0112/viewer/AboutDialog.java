package com.ifcd0112.viewer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class AboutDialog extends JDialog {

    private static final int LOGO_SIZE = 200;

    private final Path projectRoot;
    private final JPanel wrapper = new JPanel(new GridBagLayout());
    private final JPanel content = new JPanel();
    private final JLabel nameLabel = new JLabel(AppInfo.NAME, SwingConstants.CENTER);
    private final JLabel versionLabel = new JLabel("Versión " + AppInfo.VERSION, SwingConstants.CENTER);
    private final JTextArea description = new JTextArea(AppInfo.ABOUT_TEXT.trim());
    private final JScrollPane descScroll = new JScrollPane(description);

    public AboutDialog(Frame owner, Path projectRoot) {
        super(owner, "Acerca de " + AppInfo.NAME, true);
        this.projectRoot = projectRoot;
        setMinimumSize(new Dimension(480, 520));
        setLocationRelativeTo(owner);

        JLabel logoLabel = new JLabel("", SwingConstants.CENTER);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadLogo(logoLabel, projectRoot);

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(8, 0, 16, 0));
        logoPanel.add(logoLabel);

        nameLabel.setFont(UiTheme.FONT_TITLE.deriveFont(28f));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        versionLabel.setFont(UiTheme.FONT_SUBTITLE);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        description.setWrapStyleWord(true);
        description.setLineWrap(true);
        description.setEditable(false);
        description.setFont(UiTheme.FONT_UI);
        description.setColumns(40);
        description.setRows(6);
        description.setBorder(new EmptyBorder(12, 12, 12, 12));

        descScroll.setPreferredSize(new Dimension(380, 120));
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        descScroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        UiTheme.configureScroll(descScroll, true, false);

        JButton githubBtn = UiTheme.primaryButton("Ver en GitHub",
                UiTheme.palette().primary(), UiTheme.palette().primaryHover());
        githubBtn.addActionListener(e -> openGitHub());

        JButton closeBtn = UiTheme.primaryButton("Cerrar",
                UiTheme.palette().textMuted(), UiTheme.palette().border());
        closeBtn.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttons.setBorder(new EmptyBorder(16, 0, 4, 0));
        buttons.add(githubBtn);
        buttons.add(closeBtn);

        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 32, 24, 32));
        content.add(logoPanel);
        content.add(nameLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(versionLabel);
        content.add(Box.createVerticalStrut(12));
        content.add(descScroll);
        content.add(buttons);

        wrapper.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        wrapper.add(content, gbc);

        setLayout(new BorderLayout());
        add(wrapper, BorderLayout.CENTER);

        applyTheme();
        UiTheme.addThemeListener(this::applyTheme);
        setSize(520, 580);
    }

    private void applyTheme() {
        ThemePalette p = UiTheme.palette();
        ThemePalette read = p.darkMode() ? ThemePalette.light() : p;

        getContentPane().setBackground(p.bg());
        wrapper.setBackground(p.bg());
        content.setBackground(p.surface());
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(p.border()),
                new EmptyBorder(20, 32, 24, 32)
        ));

        nameLabel.setForeground(p.text());
        versionLabel.setForeground(p.textMuted());

        description.setOpaque(true);
        description.setBackground(read.surface());
        description.setForeground(read.text());
        description.setCaretColor(read.text());

        descScroll.setOpaque(true);
        descScroll.setBackground(read.surface());
        descScroll.setBorder(BorderFactory.createLineBorder(p.border()));
        descScroll.getViewport().setOpaque(true);
        descScroll.getViewport().setBackground(read.surface());
    }

    private void loadLogo(JLabel label, Path projectRoot) {
        Path logoPath = projectRoot.resolve("img/logo.png");
        if (Files.exists(logoPath)) {
            ImageIcon icon = PlatformSupport.loadImageIcon(logoPath);
            Image scaled = icon.getImage().getScaledInstance(LOGO_SIZE, LOGO_SIZE, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaled));
            label.setPreferredSize(new Dimension(LOGO_SIZE, LOGO_SIZE));
        } else {
            label.setIcon(AppIcons.appIcon());
            label.setText("☕");
            label.setFont(UiTheme.FONT_TITLE.deriveFont(72f));
            label.setPreferredSize(new Dimension(LOGO_SIZE, LOGO_SIZE));
        }
    }

    private void openGitHub() {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(AppInfo.GITHUB_URL));
            } else {
                JOptionPane.showMessageDialog(this,
                        "Abre manualmente:\n" + AppInfo.GITHUB_URL,
                        "GitHub", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo abrir el navegador:\n" + AppInfo.GITHUB_URL,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
