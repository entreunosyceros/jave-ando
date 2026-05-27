package com.ifcd0112.viewer;

import java.awt.*;

public record ThemePalette(
        Color bg,
        Color surface,
        Color header,
        Color headerAccent,
        Color headerHint,
        Color primary,
        Color primaryHover,
        Color success,
        Color successHover,
        Color text,
        Color textMuted,
        Color border,
        Color listSel,
        Color listSelText,
        Color consoleBg,
        Color consoleText,
        Color badgePoo,
        Color badgeBbdd,
        Color badgeJdbc,
        Color badgeMd,
        Color menuBg,
        Color menuFg,
        boolean darkMode
) {
    /** Tema claro suave: fondo gris, tarjetas blancas, acentos azul y verde. */
    public static ThemePalette light() {
        return new ThemePalette(
                new Color(0xE8, 0xEA, 0xED),
                Color.WHITE,
                new Color(0x1E, 0x29, 0x3B),
                new Color(0xBF, 0xDB, 0xFE),
                new Color(0x94, 0xA3, 0xB8),
                new Color(0x3B, 0x82, 0xF6),
                new Color(0x60, 0xA5, 0xFA),
                new Color(0x05, 0x96, 0x69),
                new Color(0x10, 0xB9, 0x81),
                new Color(0x1E, 0x29, 0x3B),
                new Color(0x64, 0x74, 0x8B),
                new Color(0xD1, 0xD5, 0xDB),
                new Color(0xDB, 0xEA, 0xFE),
                new Color(0x1E, 0x40, 0xAF),
                Color.WHITE,
                new Color(0x1E, 0x29, 0x3B),
                new Color(0x7C, 0x3A, 0xED),
                new Color(0xEA, 0x58, 0x0C),
                new Color(0x0D, 0x94, 0x88),
                new Color(0x65, 0x65, 0x70),
                Color.WHITE,
                new Color(0x1E, 0x29, 0x3B),
                false
        );
    }

    public static ThemePalette dark() {
        return new ThemePalette(
                new Color(0x0B, 0x12, 0x20),
                new Color(0x15, 0x23, 0x42),
                new Color(0x02, 0x06, 0x17),
                new Color(0x38, 0xBD, 0xF8),
                new Color(0x94, 0xA3, 0xB8),
                new Color(0x3B, 0x82, 0xF6),
                new Color(0x60, 0xA5, 0xFA),
                new Color(0x10, 0xB9, 0x81),
                new Color(0x34, 0xD3, 0x99),
                new Color(0xF1, 0xF5, 0xF9),
                new Color(0x94, 0xA3, 0xB8),
                new Color(0x33, 0x41, 0x55),
                new Color(0x1E, 0x3A, 0x8A),
                new Color(0xBF, 0xDB, 0xFE),
                new Color(0x02, 0x06, 0x17),
                new Color(0xCB, 0xD5, 0xE1),
                new Color(0xA7, 0x8B, 0xFA),
                new Color(0xFB, 0x92, 0x3C),
                new Color(0x2D, 0xD4, 0xBF),
                new Color(0x94, 0xA3, 0xB8),
                new Color(0x15, 0x23, 0x42),
                new Color(0xE2, 0xE8, 0xF0),
                true
        );
    }
}
