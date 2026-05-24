package com.ifcd0112.viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.EnumMap;
import java.util.Map;

public final class AppIcons {

    private static final Map<Exercise.SolutionType, Icon> TYPE_ICONS = new EnumMap<>(Exercise.SolutionType.class);
    private static Icon appIcon;

    static {
        TYPE_ICONS.put(Exercise.SolutionType.JAVA, iconJava(18));
        TYPE_ICONS.put(Exercise.SolutionType.SQL, iconSql(18));
        TYPE_ICONS.put(Exercise.SolutionType.MARKDOWN, iconDoc(18));
        TYPE_ICONS.put(Exercise.SolutionType.NONE, iconProject(18));
    }

    private AppIcons() {}

    public static Icon forExercise(Exercise ex) {
        return TYPE_ICONS.getOrDefault(ex.solutionType(), TYPE_ICONS.get(Exercise.SolutionType.MARKDOWN));
    }

    public static Icon moduleIcon() {
        return createModuleIcon();
    }

    public static Icon appIcon() {
        if (appIcon == null) {
            appIcon = loadAppIcon();
        }
        return appIcon;
    }

    private static Icon loadAppIcon() {
        java.nio.file.Path logo = MainFrame.findProjectRoot().resolve("img/logo.png");
        if (java.nio.file.Files.exists(logo)) {
            Image img = PlatformSupport.loadImageIcon(logo).getImage()
                    .getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }
        return iconJava(24);
    }

    private static Icon iconJava(int size) {
        return new IconPainter(size, g -> {
            g.setColor(new Color(0xF5, 0x9E, 0x0B));
            g.fillRoundRect(2, 4, size - 6, size - 8, 4, 4);
            g.setColor(new Color(0x78, 0x35, 0x0F));
            g.fillRect(size / 2 - 2, 2, 4, 4);
            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size - 10));
            g.drawString("J", size / 2 - 3, size - 6);
        });
    }

    private static Icon iconSql(int size) {
        return new IconPainter(size, g -> {
            int w = size - 6;
            int h = size - 10;
            g.setColor(new Color(0x0D, 0x94, 0x88));
            g.fillRoundRect(3, 6, w, h - 4, 6, 6);
            g.setColor(new Color(0x14, 0xB8, 0xA6));
            g.fillRoundRect(3, 3, w, h / 2, 6, 6);
            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size - 11));
            g.drawString("SQL", 4, size - 7);
        });
    }

    private static Icon iconDoc(int size) {
        return new IconPainter(size, g -> {
            g.setColor(new Color(0x64, 0x74, 0x8B));
            Path2D doc = new Path2D.Float();
            doc.moveTo(4, 2);
            doc.lineTo(size - 6, 2);
            doc.lineTo(size - 3, 6);
            doc.lineTo(size - 3, size - 3);
            doc.lineTo(4, size - 3);
            doc.closePath();
            g.fill(doc);
            g.setColor(Color.WHITE);
            g.fillRect(6, 8, size - 12, 2);
            g.fillRect(6, 12, size - 14, 2);
            g.fillRect(6, 16, size - 10, 2);
        });
    }

    private static Icon iconProject(int size) {
        return new IconPainter(size, g -> {
            g.setColor(new Color(0x8B, 0x5C, 0xF6));
            g.fill(new RoundRectangle2D.Float(2, 5, size - 4, size - 8, 4, 4));
            g.setColor(new Color(0xC4, 0xB5, 0xFD));
            g.fillRect(4, 3, size - 8, 5);
        });
    }

    private static Icon createModuleIcon() {
        return new IconPainter(18, g -> {
            ThemePalette p = UiTheme.palette();
            g.setColor(p.textMuted());
            g.fillRoundRect(2, 4, 14, 11, 3, 3);
            g.setColor(p.bg());
            g.fillRect(5, 7, 8, 2);
            g.fillRect(5, 10, 6, 2);
        });
    }

    @FunctionalInterface
    private interface Painter {
        void paint(Graphics2D g);
    }

    private static class IconPainter implements Icon {
        private final int size;
        private final Painter painter;

        IconPainter(int size, Painter painter) {
            this.size = size;
            this.painter = painter;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            painter.paint(g2);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return size; }

        @Override
        public int getIconHeight() { return size; }
    }
}
