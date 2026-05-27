package com.ifcd0112.viewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Diagrama simple de tablas y relaciones FK; las tablas se pueden reordenar arrastrándolas. */
public final class SchemaDiagramPanel extends javax.swing.JPanel {

    private static final int PAD = 14;
    private static final int COL_GAP = 48;
    private static final int ROW_GAP = 36;

    private DatabaseSchemaService.DatabaseSchema schema;
    private Path projectRoot;
    private String databaseName = "";
    private Map<String, Point> savedPositions = Map.of();
    private final Map<String, java.awt.Rectangle> tableBounds = new LinkedHashMap<>();
    private String dragTable;
    private Point dragOffset;
    public SchemaDiagramPanel() {
        setOpaque(true);
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (schema == null) {
                    return;
                }
                ensureTableBounds();
                dragTable = tableAt(e.getX(), e.getY());
                if (dragTable != null) {
                    java.awt.Rectangle r = tableBounds.get(dragTable);
                    dragOffset = new Point(e.getX() - r.x, e.getY() - r.y);
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragTable != null) {
                    persistLayout();
                }
                dragTable = null;
                dragOffset = null;
                updateHoverCursor(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragTable == null || dragOffset == null) {
                    return;
                }
                java.awt.Rectangle r = tableBounds.get(dragTable);
                if (r == null) {
                    return;
                }
                r.x = Math.max(0, e.getX() - dragOffset.x);
                r.y = Math.max(0, e.getY() - dragOffset.y);
                applyContentSize();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (dragTable != null) {
                    return;
                }
                updateHoverCursor(e.getX(), e.getY());
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void setLayoutContext(Path projectRoot, String databaseName) {
        this.projectRoot = projectRoot;
        this.databaseName = databaseName != null ? databaseName : "";
        loadSavedPositions();
    }

    public void setSchema(DatabaseSchemaService.DatabaseSchema schema) {
        this.schema = schema;
        tableBounds.clear();
        dragTable = null;
        dragOffset = null;
        loadSavedPositions();
        revalidate();
        repaint();
    }

    private void loadSavedPositions() {
        if (projectRoot == null || databaseName.isBlank()) {
            savedPositions = Map.of();
            return;
        }
        try {
            savedPositions = SchemaDiagramLayoutStore.load(projectRoot, databaseName);
        } catch (IOException e) {
            savedPositions = Map.of();
        }
    }

    private void persistLayout() {
        if (projectRoot == null || databaseName.isBlank() || tableBounds.isEmpty()) {
            return;
        }
        Map<String, Point> positions = new LinkedHashMap<>();
        for (Map.Entry<String, java.awt.Rectangle> e : tableBounds.entrySet()) {
            java.awt.Rectangle r = e.getValue();
            positions.put(e.getKey(), new Point(r.x, r.y));
        }
        try {
            SchemaDiagramLayoutStore.save(projectRoot, databaseName, positions);
            savedPositions = positions;
        } catch (IOException ignored) {
            // posiciones solo en memoria si no se puede escribir
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (schema == null || schema.tables().isEmpty()) {
            return new Dimension(400, 200);
        }
        ensureTableBounds();
        return contentSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ThemePalette p = UiTheme.palette();
        g2.setColor(p.consoleBg());
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (schema == null || schema.tables().isEmpty()) {
            g2.setColor(p.textMuted());
            g2.setFont(UiTheme.FONT_UI);
            g2.drawString("Sin tablas en esta base de datos.", PAD, PAD + 16);
            g2.dispose();
            return;
        }

        ensureTableBounds();
        Color boxFill = p.darkMode() ? new Color(0x1E, 0x29, 0x3B) : new Color(0xF8, 0xFA, 0xFC);
        Color boxBorder = p.darkMode() ? new Color(0x47, 0x55, 0x69) : new Color(0xCB, 0xD5, 0xE1);
        Color titleColor = p.darkMode() ? new Color(0x7D, 0xD3, 0xFC) : new Color(0x1D, 0x4E, 0xD8);
        Color textColor = p.consoleText();
        Color pkColor = p.darkMode() ? new Color(0xFC, 0xA5, 0xA5) : new Color(0xB9, 0x1C, 0x1C);
        Color fkLine = p.darkMode() ? new Color(0x94, 0xA3, 0xB8) : new Color(0x64, 0x74, 0x8B);

        for (DatabaseSchemaService.TableSchema table : schema.tables()) {
            java.awt.Rectangle r = tableBounds.get(table.name());
            g2.setColor(boxFill);
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g2.setColor(boxBorder);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g2.setFont(UiTheme.FONT_UI_BOLD.deriveFont(13f));
            g2.setColor(titleColor);
            g2.drawString(table.name(), r.x + 10, r.y + 20);
            g2.setFont(UiTheme.FONT_MONO.deriveFont(12f));
            int y = r.y + 38;
            for (DatabaseSchemaService.ColumnSchema col : table.columns()) {
                g2.setColor(col.primaryKey() ? pkColor : textColor);
                String line = (col.primaryKey() ? "• " : "  ") + col.name() + " : " + col.sqlType();
                g2.drawString(line, r.x + 10, y);
                y += 16;
            }
        }

        g2.setColor(fkLine);
        g2.setStroke(new BasicStroke(1.5f));
        for (DatabaseSchemaService.ForeignKeySchema fk : schema.foreignKeys()) {
            java.awt.Rectangle from = tableBounds.get(fk.fromTable());
            java.awt.Rectangle to = tableBounds.get(fk.toTable());
            if (from == null || to == null) {
                continue;
            }
            Point p1 = new Point(from.x + from.width, from.y + from.height / 2);
            Point p2 = new Point(to.x, to.y + to.height / 2);
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            drawArrowHead(g2, p1, p2);
        }
        g2.dispose();
    }

    private void updateHoverCursor(int x, int y) {
        if (schema == null) {
            setCursor(Cursor.getDefaultCursor());
            return;
        }
        ensureTableBounds();
        String hit = tableAt(x, y);
        setCursor(hit != null
                ? Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
                : Cursor.getDefaultCursor());
    }

    private String tableAt(int x, int y) {
        List<DatabaseSchemaService.TableSchema> tables = schema.tables();
        for (int i = tables.size() - 1; i >= 0; i--) {
            String name = tables.get(i).name();
            java.awt.Rectangle r = tableBounds.get(name);
            if (r != null && r.contains(x, y)) {
                return name;
            }
        }
        return null;
    }

    private void drawArrowHead(Graphics2D g2, Point from, Point to) {
        double angle = Math.atan2(to.y - from.y, to.x - from.x);
        int size = 8;
        int x1 = (int) (to.x - size * Math.cos(angle - Math.PI / 6));
        int y1 = (int) (to.y - size * Math.sin(angle - Math.PI / 6));
        int x2 = (int) (to.x - size * Math.cos(angle + Math.PI / 6));
        int y2 = (int) (to.y - size * Math.sin(angle + Math.PI / 6));
        g2.fillPolygon(new int[] {to.x, x1, x2}, new int[] {to.y, y1, y2}, 3);
    }

    private void ensureTableBounds() {
        if (schema == null) {
            return;
        }
        FontMetrics fm = getFontMetrics(UiTheme.FONT_MONO.deriveFont(12f));
        boolean needsInitialLayout = tableBounds.isEmpty();
        for (DatabaseSchemaService.TableSchema table : schema.tables()) {
            java.awt.Dimension size = tableSize(table, fm);
            java.awt.Rectangle existing = tableBounds.get(table.name());
            if (existing == null) {
                if (needsInitialLayout) {
                    continue;
                }
                int x = PAD;
                int y = PAD;
                for (java.awt.Rectangle r : tableBounds.values()) {
                    x = Math.max(x, r.x + r.width + COL_GAP);
                    y = Math.max(y, r.y);
                }
                tableBounds.put(table.name(), new java.awt.Rectangle(x, y, size.width, size.height));
            } else {
                existing.width = size.width;
                existing.height = size.height;
            }
        }
        tableBounds.keySet().removeIf(name ->
                schema.tables().stream().noneMatch(t -> t.name().equals(name)));
        if (needsInitialLayout) {
            applyInitialLayout(fm);
        }
    }

    private void applyInitialLayout(FontMetrics fm) {
        List<DatabaseSchemaService.TableSchema> tables = schema.tables();
        boolean anySaved = false;
        for (DatabaseSchemaService.TableSchema table : tables) {
            if (savedPositions.containsKey(table.name())) {
                anySaved = true;
                break;
            }
        }
        if (!anySaved) {
            autoLayoutGrid(fm);
            return;
        }
        for (DatabaseSchemaService.TableSchema table : tables) {
            java.awt.Dimension size = tableSize(table, fm);
            Point pos = savedPositions.get(table.name());
            if (pos != null) {
                tableBounds.put(table.name(),
                        new java.awt.Rectangle(Math.max(0, pos.x), Math.max(0, pos.y), size.width, size.height));
            }
        }
        for (DatabaseSchemaService.TableSchema table : tables) {
            if (!tableBounds.containsKey(table.name())) {
                int x = PAD;
                int y = PAD;
                for (java.awt.Rectangle r : tableBounds.values()) {
                    x = Math.max(x, r.x + r.width + COL_GAP);
                    y = Math.max(y, r.y);
                }
                java.awt.Dimension size = tableSize(table, fm);
                tableBounds.put(table.name(), new java.awt.Rectangle(x, y, size.width, size.height));
            }
        }
    }

    private static java.awt.Dimension tableSize(DatabaseSchemaService.TableSchema table, FontMetrics fm) {
        int w = 220;
        for (DatabaseSchemaService.ColumnSchema c : table.columns()) {
            w = Math.max(w, fm.stringWidth(c.name() + " : " + c.sqlType()) + 28);
        }
        int h = 32 + table.columns().size() * 16 + 12;
        return new java.awt.Dimension(w, h);
    }

    private void autoLayoutGrid(FontMetrics fm) {
        List<DatabaseSchemaService.TableSchema> tables = schema.tables();
        int cols = Math.max(1, (int) Math.ceil(Math.sqrt(tables.size())));
        int x = PAD;
        int y = PAD;
        int rowHeight = 0;
        int col = 0;
        for (DatabaseSchemaService.TableSchema table : tables) {
            java.awt.Dimension size = tableSize(table, fm);
            tableBounds.put(table.name(), new java.awt.Rectangle(x, y, size.width, size.height));
            rowHeight = Math.max(rowHeight, size.height);
            col++;
            if (col >= cols) {
                col = 0;
                x = PAD;
                y += rowHeight + ROW_GAP;
                rowHeight = 0;
            } else {
                x += size.width + COL_GAP;
            }
        }
    }

    private Dimension contentSize() {
        int maxX = 0;
        int maxY = 0;
        for (java.awt.Rectangle r : tableBounds.values()) {
            maxX = Math.max(maxX, r.x + r.width);
            maxY = Math.max(maxY, r.y + r.height);
        }
        return new Dimension(maxX + PAD * 2, maxY + PAD * 2);
    }

    private void applyContentSize() {
        Dimension d = contentSize();
        if (!d.equals(getPreferredSize())) {
            setPreferredSize(d);
            revalidate();
        }
    }
}
