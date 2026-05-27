package com.ifcd0112.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/** Diálogo modal: tu código frente a la solución oficial (diff por líneas). */
public final class CodeCompareDialog extends JDialog {

    private final JTextPane studentPane = new JTextPane();
    private final JTextPane solutionPane = new JTextPane();
    private final JScrollPane studentScroll;
    private final JScrollPane solutionScroll;
    private final JLabel summaryLabel = new JLabel(" ");
    private final JLabel legendLabel = new JLabel();

    private List<LineDiffEngine.DiffRow> rows = List.of();
    private boolean scrollSync;

    private SimpleAttributeSet normal;
    private SimpleAttributeSet addedLine;
    private SimpleAttributeSet missingLine;
    private SimpleAttributeSet placeholder;

    public static void show(
            java.awt.Window owner,
            String studentText,
            String solutionText,
            String exerciseTitle) {
        CodeCompareDialog dialog = new CodeCompareDialog(owner, studentText, solutionText, exerciseTitle);
        dialog.setVisible(true);
    }

    private CodeCompareDialog(
            java.awt.Window owner,
            String studentText,
            String solutionText,
            String exerciseTitle) {
        super(owner, "Comparar con solución — " + exerciseTitle, ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(900, 520));
        setSize(1000, 620);
        setLocationRelativeTo(owner);

        rows = LineDiffEngine.compare(studentText, solutionText);
        LineDiffEngine.DiffSummary summary = LineDiffEngine.summarize(rows);

        configurePane(studentPane);
        configurePane(solutionPane);

        studentScroll = new JScrollPane(studentPane);
        solutionScroll = new JScrollPane(solutionPane);
        UiTheme.configureScroll(studentScroll, true, true);
        UiTheme.configureScroll(solutionScroll, true, true);
        studentScroll.setBorder(UiTheme.sectionBorder("Tu código (trabajo/)"));
        solutionScroll.setBorder(UiTheme.sectionBorder("Solución oficial"));

        linkVerticalScroll(studentScroll, solutionScroll);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, studentScroll, solutionScroll);
        split.setResizeWeight(0.5);
        split.setDividerLocation(0.5);

        summaryLabel.setFont(UiTheme.FONT_UI.deriveFont(12f));
        updateSummaryText(summary);

        legendLabel.setFont(UiTheme.FONT_UI.deriveFont(12f));
        legendLabel.setText(
                "<html><span style='background:#DCFCE7;padding:2px 6px;'>Verde</span> línea que tú tienes y no está en la solución &nbsp;|&nbsp; "
                        + "<span style='background:#FEE2E2;padding:2px 6px;'>Rojo</span> línea de la solución que te falta</html>");

        JPanel north = new JPanel(new BorderLayout(0, 6));
        north.setBorder(new EmptyBorder(12, 14, 8, 14));
        north.add(legendLabel, BorderLayout.NORTH);
        north.add(summaryLabel, BorderLayout.SOUTH);

        JButton closeBtn = UiTheme.primaryButton("Cerrar",
                UiTheme.palette().primary(), UiTheme.palette().primaryHover());
        closeBtn.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBorder(new EmptyBorder(0, 14, 12, 14));
        south.add(closeBtn);

        add(north, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        applyTheme();
        renderDiff();
        UiTheme.addThemeListener(this::applyTheme);
    }

    private static void configurePane(JTextPane pane) {
        pane.setEditable(false);
        pane.setFont(UiTheme.FONT_MONO);
        pane.setContentType("text/plain");
    }

    private void linkVerticalScroll(JScrollPane left, JScrollPane right) {
        left.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (scrollSync) {
                return;
            }
            scrollSync = true;
            right.getVerticalScrollBar().setValue(e.getValue());
            scrollSync = false;
        });
        right.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (scrollSync) {
                return;
            }
            scrollSync = true;
            left.getVerticalScrollBar().setValue(e.getValue());
            scrollSync = false;
        });
    }

    private void updateSummaryText(LineDiffEngine.DiffSummary summary) {
        if (summary.identical()) {
            summaryLabel.setText("Tu código coincide línea a línea con la solución oficial.");
            return;
        }
        summaryLabel.setText(String.format(
                "%d línea(s) iguales · %d añadida(s) por ti · %d que te faltan respecto a la solución.",
                summary.equalLines(), summary.addedLines(), summary.missingLines()));
    }

    private void applyTheme() {
        ThemePalette p = UiTheme.palette();
        getContentPane().setBackground(p.bg());
        summaryLabel.setForeground(p.text());
        legendLabel.setForeground(p.text());

        Color addedBg = p.darkMode() ? new Color(0x14, 0x53, 0x32) : new Color(0xDC, 0xFC, 0xE7);
        Color addedFg = p.darkMode() ? new Color(0x86, 0xEF, 0xAC) : new Color(0x16, 0x65, 0x34);
        Color missingBg = p.darkMode() ? new Color(0x45, 0x1A, 0x1A) : new Color(0xFE, 0xE2, 0xE2);
        Color missingFg = p.darkMode() ? new Color(0xFC, 0xA5, 0xA5) : new Color(0x99, 0x1B, 0x1B);

        normal = styled(p.consoleText(), p.consoleBg(), false);
        addedLine = styled(addedFg, addedBg, true);
        missingLine = styled(missingFg, missingBg, true);
        placeholder = styled(p.textMuted(), p.surface(), false);

        studentPane.setBackground(p.consoleBg());
        solutionPane.setBackground(p.consoleBg());
        studentScroll.getViewport().setBackground(p.consoleBg());
        solutionScroll.getViewport().setBackground(p.consoleBg());

        renderDiff();
    }

    private void renderDiff() {
        fillPane(studentPane, true);
        fillPane(solutionPane, false);
        SwingUtilities.invokeLater(() -> studentScroll.getVerticalScrollBar().setValue(0));
    }

    private void fillPane(JTextPane pane, boolean studentSide) {
        StyledDocument doc = pane.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());
            for (LineDiffEngine.DiffRow row : rows) {
                String line = studentSide ? row.studentLine() : row.solutionLine();
                SimpleAttributeSet style = styleForRow(row, studentSide);
                if (line.isEmpty()) {
                    line = " ";
                    style = placeholder;
                }
                doc.insertString(doc.getLength(), line, style);
                doc.insertString(doc.getLength(), "\n", style);
            }
        } catch (BadLocationException ignored) {
            pane.setText("");
        }
    }

    private SimpleAttributeSet styleForRow(LineDiffEngine.DiffRow row, boolean studentSide) {
        if (studentSide && row.kind() == LineDiffEngine.RowKind.ONLY_STUDENT) {
            return addedLine;
        }
        if (!studentSide && row.kind() == LineDiffEngine.RowKind.ONLY_SOLUTION) {
            return missingLine;
        }
        return normal;
    }

    private static SimpleAttributeSet styled(Color fg, Color bg, boolean bold) {
        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setForeground(a, fg);
        StyleConstants.setBackground(a, bg);
        StyleConstants.setFontFamily(a, UiTheme.FONT_MONO.getFamily());
        StyleConstants.setFontSize(a, UiTheme.FONT_MONO.getSize());
        StyleConstants.setBold(a, bold);
        return a;
    }
}
