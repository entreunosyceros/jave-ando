package com.ifcd0112.viewer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class UiTheme {

    public enum Mode { LIGHT, DARK }

    private static Mode mode = Mode.LIGHT;
    private static ThemePalette palette = ThemePalette.light();
    private static final List<Runnable> listeners = new ArrayList<>();

    public static final Font FONT_UI = uiFont(Font.PLAIN, 14);
    public static final Font FONT_UI_BOLD = uiFont(Font.BOLD, 14);
    public static final Font FONT_TITLE = uiFont(Font.PLAIN, 28);
    public static final Font FONT_SUBTITLE = uiFont(Font.PLAIN, 13);
    public static final Font FONT_MONO = monoFont(13);
    public static final Font FONT_BADGE = uiFont(Font.BOLD, 11);

    private static Font uiFont(int style, int size) {
        return PlatformSupport.pickFont(PlatformSupport.uiFontFamilies(), style, size);
    }

    private static Font monoFont(int size) {
        return PlatformSupport.pickFont(PlatformSupport.monoFontFamilies(), Font.PLAIN, size);
    }

    private UiTheme() {}

    public static ThemePalette palette() {
        return palette;
    }

    public static Mode mode() {
        return mode;
    }

    public static boolean isDark() {
        return mode == Mode.DARK;
    }

    public static void addThemeListener(Runnable listener) {
        listeners.add(listener);
    }

    public static void toggleMode() {
        setMode(mode == Mode.LIGHT ? Mode.DARK : Mode.LIGHT);
    }

    public static void setMode(Mode newMode) {
        mode = newMode;
        palette = newMode == Mode.DARK ? ThemePalette.dark() : ThemePalette.light();
        installUIManager();
        listeners.forEach(Runnable::run);
    }

    public static void install() {
        installUIManager();
    }

    private static void installUIManager() {
        ThemePalette p = palette;
        UIManager.put("control", p.bg());
        UIManager.put("Panel.background", p.bg());
        UIManager.put("Viewport.background", p.surface());
        UIManager.put("ScrollPane.background", p.bg());
        Color treeBg = p.darkMode() ? p.surface() : p.bg();
        UIManager.put("Tree.background", treeBg);
        UIManager.put("Tree.foreground", p.text());
        UIManager.put("Tree.textBackground", treeBg);
        UIManager.put("Tree.textForeground", p.text());
        UIManager.put("Tree.selectionBackground", p.listSel());
        UIManager.put("Tree.selectionForeground", p.listSelText());
        UIManager.put("Tree.selectionBorderColor", treeBg);
        UIManager.put("Tree.drawsFocusBorderAroundIcon", Boolean.FALSE);
        UIManager.put("Tree.drawDashedFocusIndicator", Boolean.FALSE);
        UIManager.put("Tree.changeSelectionOnFocusLost", Boolean.TRUE);
        UIManager.put("MenuBar.background", p.menuBg());
        UIManager.put("MenuBar.foreground", p.menuFg());
        UIManager.put("Menu.background", p.menuBg());
        UIManager.put("Menu.foreground", p.menuFg());
        UIManager.put("MenuItem.background", p.menuBg());
        UIManager.put("MenuItem.foreground", p.menuFg());
        UIManager.put("MenuItem.selectionBackground", p.listSel());
        UIManager.put("MenuItem.selectionForeground", p.listSelText());
        UIManager.put("PopupMenu.background", p.surface());
        UIManager.put("PopupMenu.foreground", p.text());
        UIManager.put("ToolTip.background", p.header());
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("SplitPane.background", p.bg());
        UIManager.put("SplitPane.dividerFocusColor", p.border());
    }

    public static Border cardBorder() {
        return new CompoundBorder(
                new LineBorder(palette.border(), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        );
    }

    public static Border sectionBorder(String title) {
        var titled = BorderFactory.createTitledBorder(
                new LineBorder(palette.border(), 1, true), title,
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                FONT_UI_BOLD, palette.textMuted()
        );
        return new CompoundBorder(titled, new EmptyBorder(4, 4, 4, 4));
    }

    public static JButton primaryButton(String text, Color bg, Color hover) {
        JButton btn = new JButton(text);
        styleActionButton(btn, bg, hover);
        return btn;
    }

    private static final String PROP_ACTION_BG = "uiTheme.actionBg";
    private static final String PROP_ACTION_HOVER = "uiTheme.actionHover";
    private static final String PROP_ACTION_LISTENER = "uiTheme.actionListener";

    public static void styleActionButton(JButton btn, Color bg, Color hover) {
        btn.putClientProperty(PROP_ACTION_BG, bg);
        btn.putClientProperty(PROP_ACTION_HOVER, hover);
        // Evita que GTK/metal en Linux ignore fondo y texto en tema claro
        btn.setUI(new BasicButtonUI());
        btn.setFont(FONT_UI);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (btn.getClientProperty(PROP_ACTION_LISTENER) == null) {
            btn.putClientProperty(PROP_ACTION_LISTENER, Boolean.TRUE);
            btn.addChangeListener(e -> applyActionButtonColors(btn));
        }
        applyActionButtonColors(btn);
    }

    private static void applyActionButtonColors(JButton btn) {
        Color bg = (Color) btn.getClientProperty(PROP_ACTION_BG);
        Color hover = (Color) btn.getClientProperty(PROP_ACTION_HOVER);
        if (bg == null) {
            bg = palette.primary();
        }
        if (hover == null) {
            hover = palette.primaryHover();
        }

        if (!btn.isEnabled()) {
            btn.setBackground(palette.border());
            btn.setForeground(palette.textMuted());
            btn.setBorder(new CompoundBorder(
                    new LineBorder(palette.border(), 1, false),
                    new EmptyBorder(10, 16, 10, 16)
            ));
            return;
        }

        Color fill = bg;
        if (btn.getModel().isPressed()) {
            fill = hover.darker();
        } else if (btn.getModel().isRollover()) {
            fill = hover;
        }
        btn.setForeground(Color.WHITE);
        btn.setBackground(fill);
        btn.setBorder(new CompoundBorder(
                new LineBorder(fill.darker(), 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));
    }

    public static void refreshButton(JButton btn, Color bg, Color hover) {
        styleActionButton(btn, bg, hover);
    }

    /** Botón secundario (borde visible, texto oscuro en tema claro). */
    public static void styleSecondaryButton(JButton btn) {
        ThemePalette p = palette;
        btn.setFont(FONT_UI);
        btn.setForeground(p.text());
        btn.setBackground(p.bg());
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);
        btn.setBorder(new CompoundBorder(
                new LineBorder(p.border(), 1, false),
                new EmptyBorder(9, 14, 9, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text);
        styleSecondaryButton(btn);
        return btn;
    }

    /** Botón gris (p. ej. Restaurar): texto blanco siempre; fondo más oscuro al pulsar o pasar el ratón. */
    public static Color[] neutralButtonColors() {
        ThemePalette p = palette;
        if (p.darkMode()) {
            return new Color[] {new Color(0x47, 0x55, 0x69), new Color(0x33, 0x41, 0x55)};
        }
        return new Color[] {new Color(0x64, 0x74, 0x8B), new Color(0x47, 0x55, 0x69)};
    }

    public static JButton neutralButton(String text) {
        Color[] colors = neutralButtonColors();
        return primaryButton(text, colors[0], colors[1]);
    }

    public static JLabel badge(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BADGE);
        Color fg = palette.darkMode() ? color.brighter() : color.darker();
        label.setForeground(fg);
        Color bg = palette.darkMode()
                ? blend(color, palette.surface(), 0.35f)
                : blend(color, Color.WHITE, 0.88f);
        label.setBackground(bg);
        label.setOpaque(true);
        label.setBorder(new CompoundBorder(
                new LineBorder(blend(color, palette.border(), 0.5f), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));
        return label;
    }

    public static Color badgeColor(Exercise ex) {
        if (ex.id().startsWith("poo")) return palette.badgePoo();
        if (ex.id().startsWith("jdbc")) return palette.badgeJdbc();
        if (ex.id().startsWith("proj")) return palette.badgeMd();
        if (ex.solutionType() == Exercise.SolutionType.MARKDOWN) return palette.badgeMd();
        return palette.badgeBbdd();
    }

    public static String typeLabel(Exercise ex) {
        return switch (ex.solutionType()) {
            case JAVA -> "Java";
            case SQL -> "SQL";
            case MARKDOWN -> "Teoría";
            case NONE -> "Proyecto";
        };
    }

    private static Color blend(Color a, Color b, float ratio) {
        float ir = 1f - ratio;
        return new Color(
                clamp((int) (a.getRed() * ratio + b.getRed() * ir)),
                clamp((int) (a.getGreen() * ratio + b.getGreen() * ir)),
                clamp((int) (a.getBlue() * ratio + b.getBlue() * ir))
        );
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    public static JPanel createHeader() {
        ThemePalette p = palette;
        JPanel header = new JPanel(new BorderLayout());
        header.setName("appHeader");
        header.setBackground(p.header());
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel(AppInfo.NAME);
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(AppInfo.SUBTITLE);
        subtitle.setFont(FONT_SUBTITLE);
        subtitle.setForeground(p.headerAccent());

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        text.add(title);
        text.add(Box.createVerticalStrut(4));
        text.add(subtitle);

        JLabel hint = new JLabel(
                "<html><span style='color:#E2E8F0;font-size:13px'>"
                        + "Índice &gt; Enunciado &gt; Solución &gt; Ejecutar"
                        + "</span></html>"
        );
        hint.setName("headerHint");

        header.add(text, BorderLayout.WEST);
        header.add(hint, BorderLayout.EAST);
        return header;
    }

    public static JScrollPane wrap(JComponent inner, String title) {
        inner.setBackground(palette.surface());
        inner.setForeground(palette.text());
        JScrollPane scroll = new JScrollPane(inner);
        scroll.setName("scroll:" + title);
        scroll.getViewport().setBackground(palette.surface());
        scroll.setBorder(sectionBorder(title));
        configureScroll(scroll, true, false);
        return scroll;
    }

    /**
     * Configura barras de desplazamiento visibles cuando el contenido desborda.
     *
     * @param vertical   barra vertical
     * @param horizontal barra horizontal (p. ej. árbol con textos largos)
     */
    public static void configureScroll(JScrollPane scroll, boolean vertical, boolean horizontal) {
        scroll.setVerticalScrollBarPolicy(vertical
                ? ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
                : ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(horizontal
                ? ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
                : ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        scroll.setWheelScrollingEnabled(true);
    }

    public static void applyPanel(JPanel panel) {
        panel.setBackground(palette.bg());
    }

    public static void applySurface(JComponent c) {
        c.setBackground(palette.surface());
        c.setForeground(palette.text());
    }
}
