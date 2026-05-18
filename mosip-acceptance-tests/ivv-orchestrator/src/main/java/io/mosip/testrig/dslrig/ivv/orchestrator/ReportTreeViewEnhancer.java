package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


public final class ReportTreeViewEnhancer {

    private static final String REPORT_THEME_SCRIPT =
            "<script>(function(){function s(){var t=document.documentElement.getAttribute('data-theme')||'light',"
                    + "b=document.getElementById('report-theme-toggle');if(!b)return;"
                    + "b.textContent=t==='light'?'Dark mode':'Light mode';"
                    + "b.setAttribute('aria-pressed',t==='light'?'true':'false')}"
                    + "s();document.getElementById('report-theme-toggle')?.addEventListener('click',function(){"
                    + "var t=document.documentElement.getAttribute('data-theme')||'light',n=t==='light'?'dark':'light';"
                    + "document.documentElement.setAttribute('data-theme',n);try{localStorage.setItem('report-theme',n)}catch(e){}"
                    + "s()})})();</script>";

    private static final String REPORT_TREE_ACCORDION_SCRIPT =
            "<script>(function(){var nav=document.getElementById('report-scenario-tree');"
                    + "if(!nav)return;"
                    + "nav.querySelectorAll('details.tree-steps-details').forEach(function(d){"
                    + "d.addEventListener('toggle',function(){"
                    + "if(!d.open)return;"
                    + "nav.querySelectorAll('details.tree-steps-details').forEach(function(o){"
                    + "if(o!==d)o.open=false;});});});})();</script>";

    private static final String REPORT_TREE_FILTER_SCRIPT =
            "<script>(function(){var nav=document.getElementById('report-scenario-tree');"
                    + "if(!nav)return;var bar=nav.querySelector('.tree-filter-toolbar');"
                    + "if(!bar)return;"
                    + "bar.addEventListener('click',function(e){"
                    + "var t=e.target;if(!t||!t.closest)return;var btn=t.closest('.tree-filter-chip');"
                    + "if(!btn)return;var f=btn.getAttribute('data-tree-filter')||'all';"
                    + "bar.querySelectorAll('.tree-filter-chip').forEach(function(b){"
                    + "var on=b===btn;b.classList.toggle('is-active',on);b.setAttribute('aria-pressed',on?'true':'false');});"
                    + "nav.querySelectorAll('li.tree-scenario-node').forEach(function(li){"
                    + "var s=li.getAttribute('data-status')||'';var show=f==='all'||s===f;"
                    + "if(!show){var det=li.querySelector('details.tree-steps-details');if(det)det.open=false;}"
                    + "li.style.display=show?'':'none';});});})();</script>";


    private static final String REPORT_MAIN_SCROLL_SCRIPT =
            "<script>(function(){function m(){return document.querySelector('.report-main')}"
                    + "function s(el,sm){var p=m();if(!p||!el)return;"
                    + "var t=el.getBoundingClientRect().top-p.getBoundingClientRect().top+p.scrollTop-10;"
                    + "p.scrollTo({top:Math.max(0,t),behavior:sm?'smooth':'auto'})}"
                    + "function h(hash,sm){if(!hash||hash.charAt(0)!=='#')return;"
                    + "var el=document.getElementById(hash.slice(1));if(el)s(el,sm)}"
                    + "function navClick(e){var a=e.target.closest&&e.target.closest('a[href^=\"#\"]');"
                    + "if(!a||!a.closest('.report-sidebar'))return;"
                    + "var id=a.getAttribute('href');if(!id||id==='#')return;"
                    + "e.preventDefault();h(id,true);try{history.replaceState(null,'',id)}catch(x){}}"
                    + "function mainClick(e){var a=e.target.closest&&e.target.closest('a[href^=\"#\"]');"
                    + "if(!a||!a.closest('.report-main'))return;"
                    + "var id=a.getAttribute('href');if(!id||id==='#')return;"
                    + "var el=document.getElementById(id.slice(1));if(!el||!el.closest('.report-main'))return;"
                    + "e.preventDefault();h(id,true)}"
                    + "function init(){document.querySelector('.report-sidebar')?.addEventListener('click',navClick);"
                    + "m()?.addEventListener('click',mainClick);"
                    + "h(location.hash||'#report-exec-summary',false);"
                    + "if(!document.getElementById('report-exec-summary'))h('#summary',false)}"
                    + "if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',init);"
                    + "else init()})();</script>";

    private static final Pattern SUMMARY_TABLE =
            Pattern.compile("<table\\b[^>]*\\bid\\s*=\\s*['\"]summary['\"][^>]*>(.*?)</table>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);


    private static final Pattern SCENARIO_ROW_TWO_COL =
            Pattern.compile(
                    "<tr class=\"([^\"]+)\"><td><a href=\"#(m\\d+)\">([^<]+)</a></td>"
                            + "<td[^>]*\\bclass=['\"]bug-column['\"][^>]*>(.*?)</td></tr>",
                    Pattern.DOTALL);


    private static final Pattern SCENARIO_ROW_THREE_COL =
            Pattern.compile(
                    "<tr class=\"([^\"]+)\"><td><a href=\"#(m\\d+)\">([^<]+)</a></td>"
                            + "<td[^>]*>(.*?)</td><td[^>]*class=['\"]bug-column['\"]>(.*?)</td></tr>",
                    Pattern.DOTALL);

    private static final Pattern H3_ANCHOR = Pattern.compile("<h3[^>]*\\bid=['\"](m\\d+)['\"][^>]*>", Pattern.CASE_INSENSITIVE);


    private static final Pattern STEP_CARD =
            Pattern.compile(
                    "<div class=['\"]step-capture-card['\"]\\s+id=['\"](m\\d+-s\\d+)['\"][^>]*>[\\s\\S]*?"
                            + "<textarea[^>]*\\bstep-capture-raw\\b[^>]*name=['\"]headers['\"][^>]*>([^<]*)</textarea>\\s*</div>",
                    Pattern.CASE_INSENSITIVE);


    private static final Pattern STEP_CAPTURE_LEGACY_BLOCK =
            Pattern.compile(
                    "<div style='padding: 0; margin: 0;'><textarea style='border: solid 1px gray; background-color: lightgray; width: 100%; padding: 0; margin: 0;' name='headers' rows='3' readonly='true'>(.*?)</textarea></div>",
                    Pattern.DOTALL);

    private static final Pattern LEGEND_BR = Pattern.compile("<br\\s*/?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern LEGEND_ICON = Pattern.compile("^(&#(?:\\d+|x[0-9a-fA-F]+);)\\s*(.*)$", Pattern.DOTALL);
    private static final Pattern EXEC_LEGEND_TD =
            Pattern.compile(
                    "(<td[^>]*\\bclass=['\"]exec-legend['\"][^>]*>)(.*?)(</td>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern STATS_HEADER_ROW =
            Pattern.compile("</td></tr>\\s*(<th\\s+style=['\"]text-align:center;['\"]>)", Pattern.CASE_INSENSITIVE);

    private static final Pattern TC_MARKING_BANNER =
            Pattern.compile(
                    "<b\\s+style=\"background-color:\\s*(#[0-9a-fA-F]{3,8});?\">\\s*(Marking\\s+test\\s+case[^<]*?)</b>",
                    Pattern.CASE_INSENSITIVE);


    private static final Pattern HTTP_REQ =
            Pattern.compile(
                    "(?i)<b><u>Request:\\s*</u></b>(\\(End Point URL:[^<]*?\\)\\s*)<pre><div style='padding: 0; margin: 0;'><textarea style='border: solid 1px gray");

    private static final Pattern HTTP_RESP =
            Pattern.compile(
                    "(?i)<b><u>Response:\\s*</u></b>\\s*<pre><div style='padding: 0; margin: 0;'><textarea style='border: solid 1px gray",
                    Pattern.DOTALL);

    private static final Pattern VALIDATION_PANEL =
            Pattern.compile(
                    "(?:<br/>\\s*){1,2}<b>\\s*Output validation:\\s*</b>\\s*EXPECTED vs ACTUAL\\s*"
                            + "<table width='100%' charset='UTF8'>(.*?)</table>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern HEAD = Pattern.compile("<head>.*?</head>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern TITLE_H2 = Pattern.compile("<h2[^>]*>([^<]+)</h2>", Pattern.CASE_INSENSITIVE);
    private static final Pattern TITLE_TAG = Pattern.compile("<title>([^<]*)</title>", Pattern.CASE_INSENSITIVE);

    private static final Pattern NAV_BY_ID =
            Pattern.compile(
                    "<nav\\b[^>]*\\bid\\s*=\\s*['\"]report-scenario-tree['\"][^>]*>.*?</nav>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern NAV_LEGACY =
            Pattern.compile(
                    "<nav\\b[^>]*class\\s*=\\s*['\"]tree-nav['\"][^>]*\\baria-label\\s*=\\s*['\"]Scenario outline['\"][^>]*>.*?</nav>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern STEP_CAPTURE_DIV =
            Pattern.compile("<div class=['\"]step-capture-card['\"]>", Pattern.CASE_INSENSITIVE);


    private static final Pattern SCENARIO_DETAIL_SECTION_OPEN =
            Pattern.compile(
                    "<section\\s+[^>]*\\bclass\\s*=\\s*['\"][^'\"]*\\bscenario-detail-card\\b",
                    Pattern.CASE_INSENSITIVE);


    private static final Pattern SCENARIO_DETAIL_CARD_CHROME =
            Pattern.compile(
                    "<div\\s+class\\s*=\\s*['\"]scenario-detail-card__chrome['\"][^>]*>[\\s\\S]*?</div>\\s*</div>\\s*",
                    Pattern.CASE_INSENSITIVE);

    private static volatile String headTemplate;

    private ReportTreeViewEnhancer() {}


    public static boolean enhanceReportFileInPlace(File reportFile, Logger log) {
        Objects.requireNonNull(log, "log");
        if (reportFile == null) {
            log.error("ReportTreeViewEnhancer: report file is null; skip.");
            return false;
        }
        if (!reportFile.isFile()) {
            log.warn("ReportTreeViewEnhancer: not a regular file, skip: " + reportFile.getAbsolutePath());
            return false;
        }
        Path target = reportFile.toPath().toAbsolutePath().normalize();
        log.info("ReportTreeViewEnhancer: detected report file for enhancement at " + target);
        try {
            String html = Files.readString(target, StandardCharsets.UTF_8);
            if (html.contains("report-shell")) {
                String patched = patchExistingTreeViewReport(html);
                if (!patched.equals(html)) {
                    Path parent = target.getParent();
                    if (parent == null) {
                        log.error("ReportTreeViewEnhancer: cannot resolve parent directory for " + target);
                        return false;
                    }
                    Path tmp = Files.createTempFile(parent, "treeview-patch-", ".html");
                    try {
                        Files.writeString(tmp, patched, StandardCharsets.UTF_8);
                        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
                        log.info("ReportTreeViewEnhancer: patched existing tree-view report at " + target);
                    } catch (IOException e) {
                        try {
                            Files.deleteIfExists(tmp);
                        } catch (IOException ignored) {

                        }
                        throw e;
                    }
                } else {
                    log.info("ReportTreeViewEnhancer: file already has tree-view layout; skip transformation.");
                }
                return true;
            }
            log.info("ReportTreeViewEnhancer: enhancement starting (" + html.length() + " characters).");
            String enhanced = enhance(html);
            Path parent = target.getParent();
            if (parent == null) {
                log.error("ReportTreeViewEnhancer: cannot resolve parent directory for " + target);
                return false;
            }
            Path tmp = Files.createTempFile(parent, "treeview-enh-", ".html");
            try {
                Files.writeString(tmp, enhanced, StandardCharsets.UTF_8);
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                try {
                    Files.deleteIfExists(tmp);
                } catch (IOException ignored) {

                }
                throw e;
            }
            log.info(
                    "ReportTreeViewEnhancer: enhanced report written in place; previous plain HTML removed/replaced: "
                            + target);
            return true;
        } catch (Exception e) {
            log.error("ReportTreeViewEnhancer: enhancement failed; leaving existing report unchanged: " + target, e);
            return false;
        }
    }


    public static String enhance(String html) throws IOException {
        if (html != null && html.contains("report-shell")) {
            return patchExistingTreeViewReport(html);
        }
        String title = resolveTitle(html);
        if (!title.toLowerCase().contains("tree view")) {
            title = title + " (tree view)";
        }

        String summaryBlock = extractSummaryTable(html);
        List<ScenarioRow> rows = summaryBlock != null ? parseScenarioRows(summaryBlock) : List.of();

        String treeHtml = buildTreeNav(rows);
        String themeBar =
                "<div class=\"theme-toolbar\" role=\"region\" aria-label=\"Theme\">"
                        + "<button type=\"button\" id=\"report-theme-toggle\" class=\"theme-toggle\" "
                        + "aria-pressed=\"false\" title=\"Switch color theme\">Dark mode</button>"
                        + "</div>";

        String out = HEAD.matcher(html).replaceFirst(Matcher.quoteReplacement(headBlock(title)));

        out =
                replaceFirstLiteral(
                        out,
                        "<body>",
                        "<body class=\"report-document report-document--readonly\"><div class=\"report-shell\"><div class=\"report-sidebar sidebar\">"
                                + themeBar
                                + treeHtml
                                + "</div><div class=\"report-main\">");
        out =
                replaceFirstLiteral(
                        out,
                        "</body>",
                        "</div></div>"
                                + REPORT_THEME_SCRIPT
                                + REPORT_TREE_ACCORDION_SCRIPT
                                + REPORT_TREE_FILTER_SCRIPT
                                + REPORT_MAIN_SCROLL_SCRIPT
                                + "\n</body>");

        out = annotateReportBody(out);

        if (summaryBlock != null && !rows.isEmpty()) {
            rows = attachStepsToRows(out, rows);
            String newNav = buildTreeNav(rows);
            Matcher m = NAV_BY_ID.matcher(out);
            if (m.find()) {
                out = NAV_BY_ID.matcher(out).replaceFirst(Matcher.quoteReplacement(newNav));
            } else {
                out = NAV_LEGACY.matcher(out).replaceFirst(Matcher.quoteReplacement(newNav));
            }
        }
        out = wrapScenarioDetailCards(out);
        return stripScenarioDetailCardChrome(out);
    }


    private static String patchExistingTreeViewReport(String html) {
        String out = ensureReportExecSummaryAnchor(html);
        out =
                out.replace(
                        "href=\"#summary\" role=\"treeitem\"><span class=\"tree-icon\"",
                        "href=\"#report-exec-summary\" role=\"treeitem\"><span class=\"tree-icon\"");
        if (!out.contains("h(location.hash||'#report-exec-summary'")) {
            String inject = REPORT_MAIN_SCROLL_SCRIPT + "\n";
            if (out.contains("</body>")) {
                out = replaceFirstLiteral(out, "</body>", inject + "</body>");
            }
        }
        return stripScenarioDetailCardChrome(out);
    }


    private static String stripScenarioDetailCardChrome(String html) {
        if (html == null || html.isEmpty() || !html.contains("scenario-detail-card__chrome")) {
            return html;
        }
        String out = SCENARIO_DETAIL_CARD_CHROME.matcher(html).replaceAll("");
        return out.replace("<motion class=\"scenario-detail-card__inner\">", "<div class=\"scenario-detail-card__inner\">");
    }


    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println(
                    "Usage: java report.treeview.ReportTreeViewEnhancer <input.html> [output.html]\n"
                            + "  Default output: <stem>-tree-view-java.html");
            System.exit(2);
        }
        Path in = Paths.get(args[0]);
        if (!Files.isRegularFile(in)) {
            System.err.println("Not found: " + in);
            System.exit(1);
        }
        Path out;
        if (args.length >= 2) {
            out = Paths.get(args[1]);
        } else {
            String name = in.getFileName().toString();
            String stem = name.endsWith(".html") ? name.substring(0, name.length() - 5) : name;
            out = in.resolveSibling(stem + "-tree-view-java.html");
        }
        String html = Files.readString(in, StandardCharsets.UTF_8);
        Files.writeString(out, enhance(html), StandardCharsets.UTF_8);
        System.out.println("Wrote " + out);
    }

    private static String resolveTitle(String html) {
        Matcher h2 = TITLE_H2.matcher(html);
        if (h2.find()) {
            return h2.group(1).trim();
        }
        Matcher t = TITLE_TAG.matcher(html);
        if (t.find() && !t.group(1).trim().isEmpty()) {
            return t.group(1).trim();
        }
        return "Test report";
    }

    private static String headBlock(String title) throws IOException {
        return getHeadTemplate().replace("__REPORT_TITLE__", escapeHtml(title));
    }

    private static String getHeadTemplate() throws IOException {
        String cached = headTemplate;
        if (cached != null) {
            return cached;
        }
        synchronized (ReportTreeViewEnhancer.class) {
            if (headTemplate != null) {
                return headTemplate;
            }
            try (InputStream in =
                    ReportTreeViewEnhancer.class.getResourceAsStream("/config/head-template.html")) {
                if (in == null) {
                    throw new IOException(
                            "Missing classpath resource /config/head-template.html (expected next to dsl.properties).");
                }
                headTemplate = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                return headTemplate;
            }
        }
    }

    static String escapeHtml(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }


    private static String escapeForTextareaText(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;");
    }


    private static String decodeMinimalHtmlEntities(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return s.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
    }


    private static String replaceFirstLiteral(String s, String from, String to) {
        int i = s.indexOf(from);
        if (i < 0) {
            return s;
        }
        return s.substring(0, i) + to + s.substring(i + from.length());
    }

    private static String extractSummaryTable(String html) {
        Matcher m = SUMMARY_TABLE.matcher(html);
        return m.find() ? m.group(0) : null;
    }

    private static List<ScenarioRow> parseScenarioRows(String summaryHtml) {
        List<ScenarioRow> rows = new ArrayList<>();
        Matcher m3 = SCENARIO_ROW_THREE_COL.matcher(summaryHtml);
        while (m3.find()) {
            String cls = m3.group(1);
            String anchor = m3.group(2);
            String name = m3.group(3).trim();
            String descHtml = m3.group(4);
            String timingHtml = m3.group(5);
            String timing = timingHtml.replaceAll("<[^>]+>", "").trim();
            String desc = descHtml.replaceAll("<[^>]+>", "").trim();
            rows.add(new ScenarioRow(cls, anchor, name, desc, timing, statusFromRowClass(cls)));
        }
        if (!rows.isEmpty()) {
            return rows;
        }
        Matcher m2 = SCENARIO_ROW_TWO_COL.matcher(summaryHtml);
        while (m2.find()) {
            String cls = m2.group(1);
            String anchor = m2.group(2);
            String name = m2.group(3).trim();
            String timingHtml = m2.group(4);
            String timing = timingHtml.replaceAll("<[^>]+>", "").trim();
            rows.add(new ScenarioRow(cls, anchor, name, "", timing, statusFromRowClass(cls)));
        }
        return rows;
    }

    private static String statusFromRowClass(String cls) {
        String c = cls.toLowerCase();
        if (c.contains("passed")) return "pass";
        if (c.contains("failed")) return "fail";
        if (c.contains("skipped")) return "skip";
        if (c.contains("ignored")) return "ignore";
        if (c.contains("knownissue")) return "known";
        return "unknown";
    }


    private static String scenarioStatusLabel(String status) {
        return switch (status) {
            case "pass" -> "Passed";
            case "ignore" -> "Ignored";
            case "known" -> "Known issues";
            case "skip" -> "Skipped";
            case "fail" -> "Failed";
            default -> "Unknown";
        };
    }

    private static String buildTreeFilterToolbar(List<ScenarioRow> rows) {
        if (rows.isEmpty()) {
            return "";
        }
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String k : List.of("pass", "ignore", "known", "skip", "fail", "unknown")) {
            counts.put(k, 0);
        }
        for (ScenarioRow r : rows) {
            counts.merge(r.status, 1, Integer::sum);
        }
        int nAll = rows.size();
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"tree-filter-toolbar\" role=\"toolbar\" aria-label=\"Filter scenarios by outcome\">");
        sb.append("<div class=\"tree-filter-chips\">");
        sb.append(
                "<button type=\"button\" class=\"tree-filter-chip is-active\" data-tree-filter=\"all\" aria-pressed=\"true\">All<span class=\"tree-filter-chip-n\" aria-hidden=\"true\">(");
        sb.append(nAll).append(")</span></button>");

        List<String> filterOrder = List.of("pass", "ignore", "known", "skip", "fail", "unknown");
        for (String key : filterOrder) {
            int cnt = counts.getOrDefault(key, 0);
            if (cnt == 0) continue;
            sb.append("<button type=\"button\" class=\"tree-filter-chip tree-filter-chip--")
                    .append(key)
                    .append("\" data-tree-filter=\"")
                    .append(key)
                    .append("\" aria-pressed=\"false\">")
                    .append(scenarioStatusLabel(key))
                    .append("<span class=\"tree-filter-chip-n\" aria-hidden=\"true\">(")
                    .append(cnt)
                    .append(")</span></button>");
        }
        sb.append("</div></div>");
        return sb.toString();
    }


    private static String suiteLifecycleTreeClass(String scenarioName) {
        if (scenarioName == null) {
            return "";
        }
        String n = scenarioName.trim();
        if ("Scenario_0".equals(n) || "Scenario_AFTER_SUITE".equals(n)) {
            return " tree-scenario-node--suite-lifecycle";
        }
        return "";
    }

    private static String buildTreeNav(List<ScenarioRow> rows) {
        StringBuilder parts = new StringBuilder();
        parts.append("<nav id=\"report-scenario-tree\" class=\"tree-nav\" aria-label=\"Scenario outline\">");
        parts.append("<div class=\"tree-nav-title\">Scenarios</div>");
        parts.append(buildTreeFilterToolbar(rows));
        parts.append("<ul class=\"tree tree-root\" role=\"tree\">");
        parts.append("<li class=\"tree-item depth-0\" role=\"none\">");
        parts.append("<a class=\"tree-link root-link\" href=\"#report-exec-summary\" role=\"treeitem\">");
        parts.append("<span class=\"tree-icon\" aria-hidden=\"true\">◆</span>");
        parts.append("<span class=\"tree-label\">Summary &amp; metrics</span>");
        parts.append("</a></li>");
        for (ScenarioRow r : rows) {
            String st = r.status;
            String stLbl = scenarioStatusLabel(st);
            String tip = escapeHtml(r.name + " — " + stLbl);
            parts.append("<li class=\"tree-item depth-1 tree-scenario-node scenario-card status-")
                    .append(st)
                    .append(suiteLifecycleTreeClass(r.name))
                    .append("\" data-status=\"")
                    .append(st)
                    .append("\" role=\"none\">");
            parts.append("<a class=\"tree-link tree-link--scenario\" href=\"#")
                    .append(r.anchor)
                    .append("\" role=\"treeitem\" title=\"")
                    .append(tip)
                    .append("\">");
            parts.append("<span class=\"tree-scenario-row\">");
            parts.append("<span class=\"tree-scenario-left\">");
            parts.append("<span class=\"status-dot ").append(st).append("\" aria-hidden=\"true\"></span>");
            parts.append("<span class=\"tree-name scenario-title\">").append(escapeHtml(r.name)).append("</span>");
            parts.append("</span>");
            parts.append("<span class=\"tree-scenario-right\">");
            parts.append("<span class=\"tree-scenario-status status-badge tree-scenario-status--")
                    .append(st)
                    .append("\">")
                    .append(escapeHtml(stLbl))
                    .append("</span>");
            if (!r.timing.isEmpty()) {
                parts.append("<span class=\"tree-time scenario-duration\">").append(escapeHtml(r.timing)).append("</span>");
            }
            parts.append("</span></span>");
            parts.append("</a>");
            String descTrim = r.desc == null ? "" : r.desc.trim();
            if (!descTrim.isEmpty()) {
                parts.append("<div class=\"tree-scenario-desc scenario-description\">").append(escapeHtml(descTrim)).append("</div>");
            }
            List<StepEntry> steps = r.steps != null ? r.steps : List.of();
            if (!steps.isEmpty()) {
                int n = steps.size();
                parts.append("<details class=\"tree-steps-details\" aria-label=\"Steps for this scenario\">");
                parts.append("<summary class=\"tree-steps-summary\">");
                parts.append("<span class=\"tree-steps-chevron\" aria-hidden=\"true\"></span>");
                parts.append("<span class=\"tree-steps-summary-label\">");
                parts.append("<span class=\"tree-steps-lbl-closed\">Show steps</span>");
                parts.append("<span class=\"tree-steps-lbl-open\">Hide steps</span>");
                parts.append("</span>");
                parts.append("<span class=\"tree-steps-count\">(").append(n).append(")</span>");
                parts.append("</summary>");
                parts.append("<ul class=\"tree tree-steps\" role=\"group\">");
                for (int j = 0; j < steps.size(); j++) {
                    StepEntry stp = steps.get(j);
                    String label = stp.label;
                    String desc = stp.desc == null ? "" : stp.desc.trim();
                    String tipS = desc.isEmpty() ? label : escapeHtml(label + " — " + desc);
                    parts.append("<li class=\"tree-item depth-2\" role=\"none\">");
                    parts.append("<a class=\"tree-link tree-step-link\" href=\"#")
                            .append(stp.id)
                            .append("\" role=\"treeitem\" title=\"")
                            .append(tipS)
                            .append("\">");
                    parts.append("<span class=\"tree-step-head\">");
                    parts.append("<span class=\"tree-step-idx\" aria-hidden=\"true\">").append(j + 1).append(".</span>");
                    parts.append("<span class=\"tree-step-name\">").append(escapeHtml(label)).append("</span>");
                    parts.append("</span>");
                    if (!desc.isEmpty()) {
                        parts.append("<span class=\"tree-step-desc\">").append(escapeHtml(desc)).append("</span>");
                    }
                    parts.append("</a></li>");
                }
                parts.append("</ul></details>");
            }
            parts.append("</li>");
        }
        parts.append("</ul></nav>");
        return parts.toString();
    }

    private static List<ScenarioRow> attachStepsToRows(String fullHtml, List<ScenarioRow> rows) {
        if (rows.isEmpty()) {
            return rows;
        }
        List<Integer> starts = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();
        List<String> aids = new ArrayList<>();
        Matcher hm = H3_ANCHOR.matcher(fullHtml);
        while (hm.find()) {
            aids.add(hm.group(1));
            starts.add(hm.start());
            ends.add(hm.end());
        }
        if (aids.isEmpty()) {
            for (ScenarioRow r : rows) {
                r.steps = List.of();
            }
            return rows;
        }
        Map<String, String> segs = new LinkedHashMap<>();
        for (int i = 0; i < aids.size(); i++) {
            String aid = aids.get(i);
            int segEnd = i + 1 < aids.size() ? starts.get(i + 1) : fullHtml.length();
            segs.put(aid, fullHtml.substring(ends.get(i), segEnd));
        }
        for (ScenarioRow r : rows) {
            String chunk = segs.getOrDefault(r.anchor, "");
            List<StepEntry> stepsOut = new ArrayList<>();
            Matcher sm = STEP_CARD.matcher(chunk);
            while (sm.find()) {
                String sid = sm.group(1);
                if (!sid.startsWith(r.anchor + "-")) {
                    continue;
                }
                String raw = decodeMinimalHtmlEntities(sm.group(2) == null ? "" : sm.group(2));
                String[] parsed = parseStepCaptureText(raw);
                stepsOut.add(new StepEntry(sid, parsed[0], parsed[1]));
            }
            r.steps = stepsOut;
        }
        return rows;
    }


    private static String[] parseStepCaptureFields(String raw) {
        String name = "Step";
        List<String> descParts = new ArrayList<>();
        String params = "";
        boolean inDesc = false;
        String pendingLabel = null;
        for (String rawLine : raw.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            String low = line.toLowerCase();
            if (low.startsWith("step name:")) {
                inDesc = false;
                pendingLabel = null;
                int idx = line.indexOf(':');
                name = idx >= 0 ? line.substring(idx + 1).trim() : name;
            } else if (low.equals("step name")) {
                inDesc = false;
                pendingLabel = "name";
            } else if (low.startsWith("step description:")) {
                inDesc = true;
                pendingLabel = null;
                int idx = line.indexOf(':');
                String rest = idx >= 0 ? line.substring(idx + 1).trim() : "";
                if (!rest.isEmpty()) {
                    descParts.add(rest);
                }
            } else if (low.equals("step description")) {
                inDesc = false;
                pendingLabel = "description";
            } else if (low.startsWith("step parameters:") || low.startsWith("step parameter:")) {
                inDesc = false;
                pendingLabel = null;
                int idx = line.indexOf(':');
                params = idx >= 0 ? line.substring(idx + 1).trim() : "";
            } else if (low.equals("step parameters") || low.equals("step parameter")) {
                inDesc = false;
                pendingLabel = "parameters";
            } else if ("name".equals(pendingLabel)) {
                name = line;
                pendingLabel = null;
            } else if ("description".equals(pendingLabel)) {
                descParts.add(line);
                pendingLabel = null;
            } else if ("parameters".equals(pendingLabel)) {
                params = line;
                pendingLabel = null;
            } else if (inDesc) {
                descParts.add(line);
            }
        }
        String desc = String.join(" ", descParts).trim().replaceAll("\\s+", " ");
        if (name.length() > 56) {
            name = name.substring(0, 53) + "...";
        }
        return new String[] {name, desc, params};
    }


    private static String[] parseStepCaptureText(String raw) {
        String[] f = parseStepCaptureFields(raw);
        return new String[] {f[0], f[1]};
    }

    private static String annotateReportBody(String html) {
        html = annotateTcOutcomeBanners(html);
        html = annotateExecDashboard(html);
        html = annotateHttpBlocks(html);
        html = annotateValidationPanels(html);
        html = annotateStepCaptureCards(html);
        html = injectStepNavIds(html);
        return html;
    }


    private static String wrapScenarioDetailCards(String html) {
        if (html == null || html.isEmpty() || SCENARIO_DETAIL_SECTION_OPEN.matcher(html).find()) {
            return html;
        }
        final String h3Head = "<h3";
        StringBuilder out = new StringBuilder();
        int cursor = 0;
        while (cursor < html.length()) {
            int h3 = indexOfIgnoreCase(html, h3Head, cursor);
            if (h3 < 0) {
                out.append(html, cursor, html.length());
                break;
            }
            int gt = html.indexOf('>', h3);
            if (gt < 0) {
                out.append(html, cursor, html.length());
                break;
            }
            String h3Tag = html.substring(h3, gt + 1);
            int h3End = gt + 1;
            if (!containsIgnoreCase(h3Tag, "scenario-anchor")) {
                out.append(html, cursor, h3End);
                cursor = h3End;
                continue;
            }
            String anchorId = extractScenarioAnchorId(h3Tag);
            if (anchorId == null) {
                out.append(html, cursor, h3End);
                cursor = h3End;
                continue;
            }
            int pos = h3End;
            while (pos < html.length() && Character.isWhitespace(html.charAt(pos))) {
                pos++;
            }
            int tableStart = indexOfIgnoreCase(html, "<table", pos);
            if (tableStart < 0 || tableStart > pos + 24) {
                out.append(html, cursor, h3End);
                cursor = h3End;
                continue;
            }
            int tableGt = html.indexOf('>', tableStart);
            if (tableGt < 0) {
                out.append(html, cursor, h3End);
                cursor = h3End;
                continue;
            }
            String openTable = html.substring(tableStart, tableGt + 1);
            if (!tableOpensWithClassResult(openTable)) {
                out.append(html, cursor, h3End);
                cursor = h3End;
                continue;
            }
            int inner = tableGt + 1;
            int closeTableIdx = findClosingTableStartIndex(html, inner);
            if (closeTableIdx < 0) {
                out.append(html, cursor, h3End);
                cursor = h3End;
                continue;
            }
            int afterTable = closeTableIdx + 8;
            int pScan = afterTable;
            while (pScan < html.length() && Character.isWhitespace(html.charAt(pScan))) {
                pScan++;
            }
            int pOpen = indexOfIgnoreCase(html, "<p", pScan);
            if (pOpen < 0 || pOpen > pScan + 120) {
                out.append(html, cursor, h3End);
                cursor = h3End;
                continue;
            }
            int pGt = html.indexOf('>', pOpen);
            if (pGt < 0) {
                out.append(html, cursor, h3End);
                cursor = h3End;
                continue;
            }
            String pTag = html.substring(pOpen, pGt + 1);
            if (!containsIgnoreCase(pTag, "totop")) {
                out.append(html, cursor, h3End);
                cursor = h3End;
                continue;
            }
            int pClose = indexOfIgnoreCase(html, "</p>", pGt + 1);
            if (pClose < 0) {
                out.append(html, cursor, h3End);
                cursor = h3End;
                continue;
            }
            int segmentEnd = pClose + 4;
            String core = html.substring(h3, segmentEnd);
            String suiteClass = "";
            if (containsIgnoreCase(h3Tag, "scenario-anchor--before-suite")) {
                suiteClass = " scenario-detail-card--before-suite";
            } else if (containsIgnoreCase(h3Tag, "scenario-anchor--after-suite")) {
                suiteClass = " scenario-detail-card--after-suite";
            }
            String wrapped =
                    "<section class=\"scenario-detail-card"
                            + suiteClass
                            + "\" data-scenario-anchor=\""
                            + escapeHtml(anchorId)
                            + "\" aria-label=\"Scenario detail "
                            + escapeHtml(anchorId)
                            + "\">"
                            + "<div class=\"scenario-detail-card__inner\">"
                            + core
                            + "</div></section>";
            out.append(html, cursor, h3);
            out.append(wrapped);
            cursor = segmentEnd;
        }
        return out.toString();
    }

    private static int indexOfIgnoreCase(String haystack, String needle, int from) {
        if (needle.isEmpty()) {
            return from;
        }
        int max = haystack.length() - needle.length();
        for (int i = Math.max(0, from); i <= max; i++) {
            if (haystack.regionMatches(true, i, needle, 0, needle.length())) {
                return i;
            }
        }
        return -1;
    }

    private static boolean containsIgnoreCase(String s, String sub) {
        return indexOfIgnoreCase(s, sub, 0) >= 0;
    }

    private static String extractScenarioAnchorId(String h3OpenTag) {
        Matcher m = Pattern.compile("(?i)\\bid\\s*=\\s*(['\"])(m\\d+)\\1").matcher(h3OpenTag);
        return m.find() ? m.group(2) : null;
    }

    private static boolean tableOpensWithClassResult(String openTableTag) {
        Matcher m = Pattern.compile("(?i)\\bclass\\s*=\\s*(['\"])([^'\"]*)\\1").matcher(openTableTag);
        while (m.find()) {
            String cls = m.group(2);
            if (Pattern.compile("(^|\\s)result(\\s|$)").matcher(cls).find()) {
                return true;
            }
        }
        return false;
    }


    private static int findClosingTableStartIndex(String html, int innerStart) {
        int depth = 1;
        int pos = innerStart;
        while (depth > 0 && pos < html.length()) {
            int nextOpen = indexOfIgnoreCase(html, "<table", pos);
            int nextClose = indexOfIgnoreCase(html, "</table>", pos);
            if (nextClose < 0) {
                return -1;
            }
            if (nextOpen >= 0 && nextOpen < nextClose) {
                depth++;
                pos = nextOpen + 6;
            } else {
                depth--;
                if (depth == 0) {
                    return nextClose;
                }
                pos = nextClose + 8;
            }
        }
        return -1;
    }

    private static String annotateTcOutcomeBanners(String html) {
        Matcher m = TC_MARKING_BANNER.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String inner = m.group(2).trim();
            String kind = tcOutcomeKind(inner);
            String[] td = tcSplitTitleDetail(inner);
            String title = td[0];
            String detail = td[1];
            String label = tcPillLabel(kind);
            String detailHtml =
                    detail.isEmpty()
                            ? ""
                            : "<div class=\"tc-outcome-detail\">" + escapeHtml(detail) + "</div>";
            String repl =
                    "<div class=\"tc-outcome-banner tc-outcome-banner--"
                            + kind
                            + "\" role=\"status\">"
                            + "<span class=\"tc-outcome-pill\">"
                            + escapeHtml(label)
                            + "</span>"
                            + "<div class=\"tc-outcome-content\">"
                            + "<div class=\"tc-outcome-title\">"
                            + escapeHtml(title)
                            + "</div>"
                            + detailHtml
                            + "</div></div>";
            m.appendReplacement(sb, Matcher.quoteReplacement(repl));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String tcOutcomeKind(String body) {
        String low = body.toLowerCase();
        if (low.contains(" as passed")) return "pass";
        if (low.contains(" as failed")) return "fail";
        if (low.contains(" as skipped") || low.contains("skipped")) return "skip";
        if (low.contains(" as ignored") || low.contains("ignored")) return "ignore";
        return "info";
    }

    private static String tcPillLabel(String kind) {
        return switch (kind) {
            case "pass" -> "Passed";
            case "fail" -> "Failed";
            case "skip" -> "Skipped";
            case "ignore" -> "Ignored";
            default -> "Result";
        };
    }

    private static String[] tcSplitTitleDetail(String body) {
        body = body.trim();
        int i = body.indexOf(". As ");
        if (i >= 0) {
            return new String[] {body.substring(0, i + 1).trim(), body.substring(i + 5).trim()};
        }
        i = body.indexOf(" As ");
        if (i >= 0) {
            return new String[] {body.substring(0, i).trim(), body.substring(i + 4).trim()};
        }
        return new String[] {body, ""};
    }

    private static String annotateExecDashboard(String html) {
        String oldTb = "<table style='width:100%; table-layout:fixed;'>";
        if (!html.contains(oldTb)) {
            return html;
        }
        html = STATS_HEADER_ROW.matcher(html).replaceFirst("</td></tr><tr>$1");
        html = replaceFirstLiteral(html, oldTb, "<table class='report-exec-table' style='width:100%; table-layout:fixed;'>");
        html =
                replaceFirstLiteral(
                        html,
                        "<table class='report-exec-table' style='width:100%; table-layout:fixed;'><tr><th colspan='7'>",
                        "<table class='report-exec-table' style='width:100%; table-layout:fixed;'><tr class='exec-title-row'><th colspan='7'>");
        html =
                replaceFirstLiteral(
                        html,
                        "<tr style='background:#f9fbfd;' style='border-bottom:2px solid #ccc;'><td colspan='3'>",
                        "<tr class='exec-kv-row exec-kv-row--footer'><td colspan='3'>");
        html =
                html.replace(
                        "<tr style='background:#f9fbfd;'><td colspan='3'>",
                        "<tr class='exec-kv-row'><td colspan='3'>");
        html =
                html.replace(
                        "<tr><td colspan='7' style='background:#eef1f5; font-size:18px; font-weight:bold; padding:12px; border-top:2px solid #ccc; text-align:center; color:#2c3e50;'>",
                        "<tr id='report-exec-summary'><td colspan='7' class='exec-section-title' style='background:#D6EAF8; font-size:13px; font-weight:700; padding:8px 10px; border:1px solid #AED6F1; text-align:center; color:#000000; text-transform:uppercase; letter-spacing:0.06em;'>");
        html =
                replaceFirstLiteral(
                        html,
                        "<tr><td colspan='7' style='background:#e0e6ed; height:2px;'></td></tr>",
                        "<tr><td colspan='7' class='exec-separator' style='background:#AED6F1; height:2px; padding:0;'></td></tr>");
        html =
                replaceFirstLiteral(
                        html,
                        "<tr><td colspan='7' style='background:#f9fafc; padding:10px; font-size:13px; text-align:left;'>",
                        "<tr><td colspan='7' class='exec-legend' style='background:#FFFFFF; padding:10px 12px; font-size:12px; text-align:left; border:1px solid #AED6F1;'>");
        html = ensureReportExecSummaryAnchor(html);
        return reflowExecLegendCell(html);
    }


    private static String ensureReportExecSummaryAnchor(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        if (html.contains("id=\"report-exec-summary\"") || html.contains("id='report-exec-summary'")) {
            return html;
        }
        Matcher m =
                Pattern.compile(
                                "(?is)(<tr)(\\s*)(>\\s*<td[^>]*>\\s*Summary of Test Results\\s*</td>\\s*</tr>)")
                        .matcher(html);
        if (m.find()) {
            return m.replaceFirst("$1 id=\"report-exec-summary\"$2$3");
        }
        return html;
    }

    private static String reflowExecLegendCell(String html) {
        Matcher m = EXEC_LEGEND_TD.matcher(html);
        if (!m.find()) {
            return html;
        }
        String openTd = m.group(1);
        String inner = m.group(2);
        String closeTd = m.group(3);
        String[] parts = LEGEND_BR.split(inner);
        List<String> cleaned = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) cleaned.add(t);
        }
        if (cleaned.size() < 2) {
            return html;
        }
        StringBuilder chunks = new StringBuilder();
        chunks.append("<div class=\"exec-legend-inner\">");
        chunks.append("<div class=\"exec-legend-title\">").append(cleaned.get(0)).append("</div>");
        chunks.append("<ul class=\"exec-legend-list\">");
        for (int i = 1; i < cleaned.size(); i++) {
            String seg = cleaned.get(i).trim();
            Matcher ic = LEGEND_ICON.matcher(seg);
            if (ic.matches()) {
                String ico = ic.group(1);
                String txt = ic.group(2).replaceFirst("<b>\\s+", "<b>");
                chunks.append(
                        "<li class=\"exec-legend-item\"><span class=\"exec-legend-ico\" aria-hidden=\"true\">");
                chunks.append(ico).append("</span><span class=\"exec-legend-txt\">").append(txt).append("</span></li>");
            } else {
                chunks.append("<li class=\"exec-legend-item exec-legend-item--plain\"><span class=\"exec-legend-txt\">");
                chunks.append(seg).append("</span></li>");
            }
        }
        chunks.append("</ul></div>");
        String rebuilt = openTd + chunks + closeTd;
        return m.replaceFirst(Matcher.quoteReplacement(rebuilt));
    }

    private static String annotateValidationPanels(String html) {
        Matcher m = VALIDATION_PANEL.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String inner = m.group(1);
            String repl =
                    "<div class=\"validation-panel\"><div class=\"validation-heading\">"
                            + "<span class=\"val-h-icon\" aria-hidden=\"true\">&#9889;</span>"
                            + "<span class=\"val-h-title\">Output validation</span>"
                            + "<span class=\"val-h-pills\">"
                            + "<span class=\"val-pill val-pill-exp\">Expected</span>"
                            + "<span class=\"val-h-vs\">vs</span>"
                            + "<span class=\"val-pill val-pill-act\">Actual</span>"
                            + "</span></div><div class='validation-table-wrap'>"
                            + "<table class='validation-table' width='100%' charset='UTF8'>"
                            + inner
                            + "</table></div></div>";
            m.appendReplacement(sb, Matcher.quoteReplacement(repl));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String annotateStepCaptureCards(String html) {
        Matcher m = STEP_CAPTURE_LEGACY_BLOCK.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String raw = m.group(1) == null ? "" : m.group(1);
            String repl = buildStepCaptureCardMarkup(raw);
            m.appendReplacement(sb, Matcher.quoteReplacement(repl));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static void appendStepCaptureLine(StringBuilder b, String label, String value, boolean mono) {
        String valueClass = mono ? " step-capture-value--mono" : "";
        b.append("<p class='step-capture-line'>");
        b.append("<span class='step-capture-label'>").append(escapeHtml(label)).append("</span>");
        b.append(" : ");
        b.append("<span class='step-capture-value").append(valueClass).append("'>");
        b.append(escapeHtml(value));
        b.append("</span></p>");
    }


    private static String buildStepCaptureCardMarkup(String rawContent) {
        String[] f = parseStepCaptureFields(rawContent);
        String name = f[0];
        String desc = f[1];
        String params = f[2];
        String safeRaw = escapeForTextareaText(rawContent);
        StringBuilder b = new StringBuilder();
        b.append("<div class='step-capture-card'>");
        b.append("<div class='step-capture-banner'>STEP · <strong>")
                .append(escapeHtml(name))
                .append("</strong></div>");
        b.append("<div class='step-capture-panel'>");
        appendStepCaptureLine(b, "Step name", name, false);
        appendStepCaptureLine(b, "Step description", desc, false);
        appendStepCaptureLine(b, "Step parameters", params, true);
        b.append("</div>");
        b.append(
                "<textarea class='step-capture-raw' name='headers' rows='1' readonly tabindex='-1' "
                        + "aria-hidden='true'>");
        b.append(safeRaw);
        b.append("</textarea></div>");
        return b.toString();
    }

    private static String injectStepNavIds(String html) {
        List<Integer> starts = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();
        List<String> aids = new ArrayList<>();
        Matcher hm = H3_ANCHOR.matcher(html);
        while (hm.find()) {
            aids.add(hm.group(1));
            starts.add(hm.start());
            ends.add(hm.end());
        }
        if (aids.isEmpty()) {
            return html;
        }
        StringBuilder out = new StringBuilder();
        int last = 0;
        for (int i = 0; i < aids.size(); i++) {
            int s = starts.get(i);
            int e = ends.get(i);
            out.append(html, last, s);
            out.append(html, s, e);
            int segEnd = i + 1 < aids.size() ? starts.get(i + 1) : html.length();
            String chunk = html.substring(e, segEnd);
            String aid = aids.get(i);
            int counter = 0;
            Matcher dm = STEP_CAPTURE_DIV.matcher(chunk);
            StringBuffer cbuf = new StringBuffer();
            while (dm.find()) {
                String repl = "<div class='step-capture-card' id='" + aid + "-s" + counter++ + "'>";
                dm.appendReplacement(cbuf, Matcher.quoteReplacement(repl));
            }
            dm.appendTail(cbuf);
            out.append(cbuf);
            last = segEnd;
        }
        out.append(html, last, html.length());
        return out.toString();
    }

    private static String annotateHttpBlocks(String html) {
        html =
                HTTP_REQ.matcher(html)
                        .replaceAll(
                                "<div class=\"http-callline http-callline-req\">"
                                        + "<span class=\"http-badge http-badge-req\">Request</span>"
                                        + "<span class=\"http-endpoint-chip\"><code class=\"http-endpoint\">$1</code></span></div>"
                                        + "<pre class=\"http-payload http-payload-request\"><div class=\"http-field http-headers-field\">"
                                        + "<textarea class='http-ta http-headers-ta' style='border: solid 1px gray");
        html =
                HTTP_RESP.matcher(html)
                        .replaceAll(
                                "<div class=\"http-callline http-callline-res\">"
                                        + "<span class=\"http-badge http-badge-res\">Response</span></div>"
                                        + "<pre class=\"http-payload http-payload-response\"><div class=\"http-field http-headers-field\">"
                                        + "<textarea class='http-ta http-headers-ta' style='border: solid 1px gray");
        String msgOld =
                "<div style='width: 100%; overflow: hidden;'><textarea style='border:solid 1px black; width: 100%; "
                        + "box-sizing: border-box;' name='message' rows='6' readonly='true'>";
        String msgNew =
                "<div class='http-field http-body-field'><textarea class='http-ta http-body-ta' style='border:solid 1px black; width: 100%; "
                        + "box-sizing: border-box;' name='message' rows='6' readonly='true'>";
        html = html.replace(msgOld, msgNew);

        html =
                html.replace(
                        "<div style='width:100%; overflow: hidden;'><textarea style='border:solid 1px black; width: 100%; "
                                + "box-sizing: border-box;' name='message' rows='6' readonly='true'>",
                        msgNew);
        return html;
    }

    static final class ScenarioRow {
        final String clazz;
        final String anchor;
        final String name;
        final String desc;
        final String timing;
        final String status;

        List<StepEntry> steps;

        ScenarioRow(String clazz, String anchor, String name, String desc, String timing, String status) {
            this.clazz = clazz;
            this.anchor = anchor;
            this.name = name;
            this.desc = desc;
            this.timing = timing;
            this.status = status;
        }
    }

    static final class StepEntry {
        final String id;
        final String label;
        final String desc;

        StepEntry(String id, String label, String desc) {
            this.id = id;
            this.label = label;
            this.desc = desc;
        }
    }
}
