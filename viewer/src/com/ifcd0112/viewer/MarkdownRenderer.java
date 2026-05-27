package com.ifcd0112.viewer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MarkdownRenderer {

    private static final Pattern FENCE = Pattern.compile("```(\\w*)");

    private MarkdownRenderer() {}

    public static String toHtml(String markdown) {
        return toHtml(markdown, UiTheme.isDark());
    }

    public static String toHtml(String markdown, boolean dark) {
        if (markdown == null || markdown.isBlank()) {
            return wrapBody("<p><em>Sin contenido.</em></p>", dark);
        }

        String[] lines = markdown.replace("\r\n", "\n").split("\n", -1);
        StringBuilder html = new StringBuilder();
        boolean inCode = false;
        boolean inUl = false;
        boolean inTable = false;
        String codeLang = "";

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.startsWith("```")) {
                if (!inCode) {
                    closeUl(html, inUl);
                    inUl = false;
                    closeTable(html, inTable);
                    inTable = false;
                    Matcher m = FENCE.matcher(line.trim());
                    codeLang = m.find() && m.group(1) != null ? m.group(1) : "";
                    html.append("<pre class=\"code\"><code>");
                    inCode = true;
                } else {
                    html.append("</code></pre>");
                    inCode = false;
                }
                continue;
            }

            if (inCode) {
                html.append(escape(line)).append('\n');
                continue;
            }

            if (line.trim().startsWith("|") && line.trim().endsWith("|")) {
                closeUl(html, inUl);
                inUl = false;
                if (isTableSeparator(line)) {
                    continue;
                }
                if (!inTable) {
                    html.append("<table>");
                    inTable = true;
                }
                html.append("<tr>");
                for (String cell : line.trim().split("\\|")) {
                    String c = cell.trim();
                    if (!c.isEmpty()) {
                        html.append("<td>").append(inline(c)).append("</td>");
                    }
                }
                html.append("</tr>");
                continue;
            } else if (inTable) {
                html.append("</table>");
                inTable = false;
            }

            if (line.startsWith("# ")) {
                closeUl(html, inUl);
                inUl = false;
                html.append("<h1>").append(inline(line.substring(2).trim())).append("</h1>");
            } else if (line.startsWith("## ")) {
                closeUl(html, inUl);
                inUl = false;
                html.append("<h2>").append(inline(line.substring(3).trim())).append("</h2>");
            } else if (line.startsWith("### ")) {
                closeUl(html, inUl);
                inUl = false;
                html.append("<h3>").append(inline(line.substring(4).trim())).append("</h3>");
            } else if (line.startsWith("- ") || line.startsWith("* ")) {
                if (!inUl) {
                    html.append("<ul>");
                    inUl = true;
                }
                html.append("<li>").append(inline(line.substring(2).trim())).append("</li>");
            } else if (line.matches("^\\d+\\.\\s+.*")) {
                if (!inUl) {
                    html.append("<ul>");
                    inUl = true;
                }
                html.append("<li>").append(inline(line.replaceFirst("^\\d+\\.\\s+", ""))).append("</li>");
            } else if (line.isBlank()) {
                closeUl(html, inUl);
                inUl = false;
                html.append("<br/>");
            } else if (line.startsWith("> ")) {
                html.append("<blockquote>").append(inline(line.substring(2))).append("</blockquote>");
            } else {
                closeUl(html, inUl);
                inUl = false;
                html.append("<p>").append(inline(line)).append("</p>");
            }
        }

        closeUl(html, inUl);
        closeTable(html, inTable);
        if (inCode) {
            html.append("</code></pre>");
        }

        return wrapBody(html.toString(), dark);
    }

    private static void closeUl(StringBuilder html, boolean open) {
        if (open) {
            html.append("</ul>");
        }
    }

    private static void closeTable(StringBuilder html, boolean open) {
        if (open) {
            html.append("</table>");
        }
    }

    private static boolean isTableSeparator(String line) {
        return line.replace("|", "").replace("-", "").replace(":", "").trim().isEmpty();
    }

    private static String inline(String text) {
        text = escape(text);
        text = text.replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>");
        text = text.replaceAll("`([^`]+)`", "<code class=\"inline\">$1</code>");
        return text;
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String wrapBody(String body, boolean dark) {
        if (dark) {
            return """
                    <html><head><style>
                    body { font-family: 'Segoe UI', sans-serif; font-size: 14px; color: #E2E8F0;
                      background: #152238; margin: 0; padding: 4px 8px 16px 8px; line-height: 1.55; }
                    h1 { font-size: 22px; color: #F8FAFC; margin: 0 0 12px 0; border-bottom: 2px solid #334155; padding-bottom: 8px; }
                    h2 { font-size: 17px; color: #93C5FD; margin: 18px 0 8px 0; }
                    h3 { font-size: 15px; color: #CBD5E1; margin: 14px 0 6px 0; }
                    p  { margin: 6px 0 8px 0; }
                    ul { margin: 6px 0 10px 0; padding-left: 22px; }
                    li { margin-bottom: 4px; }
                    table { border-collapse: collapse; margin: 10px 0; width: 100%%; }
                    td, th { border: 1px solid #334155; padding: 6px 10px; text-align: left; }
                    tr:nth-child(even) { background: #1E293B; }
                    code.inline { font-family: Consolas, monospace; font-size: 12px;
                      background: #334155; color: #F9A8D4; padding: 1px 5px; border-radius: 4px; }
                    pre.code { background: #020617; color: #E2E8F0; font-family: Consolas, monospace;
                      font-size: 12px; padding: 12px 14px; border-radius: 8px; margin: 10px 0;
                      white-space: pre-wrap; border: 1px solid #475569; }
                    blockquote { border-left: 4px solid #3B82F6; margin: 8px 0; padding: 6px 12px;
                      background: #1E3A5F; color: #BFDBFE; }
                    strong { color: #F8FAFC; }
                    </style></head><body>
                    """ + body + "</body></html>";
        }
        return """
                <html><head><style>
                body { font-family: 'Segoe UI', sans-serif; font-size: 14px; color: #1E293B;
                  background: #FFFFFF; margin: 0; padding: 4px 8px 16px 8px; line-height: 1.55; }
                h1 { font-size: 22px; color: #1E293B; margin: 0 0 12px 0;
                  border-bottom: 2px solid #D1D5DB; padding-bottom: 8px; }
                h2 { font-size: 17px; color: #3B82F6; margin: 18px 0 8px 0; }
                h3 { font-size: 15px; color: #475569; margin: 14px 0 6px 0; }
                p  { margin: 6px 0 8px 0; }
                ul { margin: 6px 0 10px 0; padding-left: 22px; }
                li { margin-bottom: 4px; }
                table { border-collapse: collapse; margin: 10px 0; width: 100%%; }
                td, th { border: 1px solid #D1D5DB; padding: 6px 10px; text-align: left; }
                tr:nth-child(even) { background: #F1F5F9; }
                code.inline { font-family: Consolas, monospace; font-size: 12px;
                  background: #F1F5F9; color: #7C3AED; padding: 1px 5px; border-radius: 4px; }
                pre.code { background: #F8FAFC; color: #0F172A; font-family: Consolas, monospace;
                  font-size: 12px; padding: 12px 14px; border-radius: 8px; margin: 10px 0;
                  white-space: pre-wrap; border: 1px solid #D1D5DB; }
                blockquote { border-left: 4px solid #3B82F6; margin: 8px 0; padding: 6px 12px;
                  background: #EFF6FF; color: #334155; }
                strong { color: #0F172A; }
                </style></head><body>
                """ + body + "</body></html>";
    }
}
