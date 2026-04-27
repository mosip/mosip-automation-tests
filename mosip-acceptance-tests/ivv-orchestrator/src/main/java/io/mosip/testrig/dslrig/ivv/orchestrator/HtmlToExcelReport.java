package io.mosip.testrig.dslrig.ivv.orchestrator;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class HtmlToExcelReport {

    static class Scenario {
        String description;
        String status;

        Scenario(String description, String status) {
            this.description = description;
            this.status = status;
        }
    }

    public static Map<String, Scenario> extractScenarios(File htmlFile) throws IOException {
        Document doc = Jsoup.parse(htmlFile, "UTF-8");
        Element table = doc.selectFirst("table#summary");
        if (table == null)
            return Collections.emptyMap();

        Map<String, Scenario> scenarios = new LinkedHashMap<>();
        String currentStatus = "";

        for (Element row : table.select("tr")) {
            Elements headers = row.select("th");
            if (headers.size() == 1) {
                String headerText = headers.get(0).text();
                String[] statusParts = headerText.split("—");
                if (statusParts.length >= 2) {
                    currentStatus = statusParts[1].trim();
                } else {
                    currentStatus = "Unknown";
                }
            } else {
                Elements cols = row.select("td");
                if (cols.size() == 3) {
                    String id = cols.get(0).text().trim();
                    String desc = cols.get(1).text().trim();
                    scenarios.put(id, new Scenario(desc, currentStatus));
                }
            }
        }
        return scenarios;
    }

    public static void writeExcel(
            Map<String, Scenario> baseline,
            Map<String, Map<String, Scenario>> inputMap,
            String outputFilePath) throws IOException {

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet comparison = wb.createSheet("Comparison");

            // ================== CREATE STYLES ==================

            // Header Style
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Passed - Green
            CellStyle passStyle = wb.createCellStyle();
            passStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            passStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Failed - Red
            CellStyle failStyle = wb.createCellStyle();
            failStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            failStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Skipped - Orange
            CellStyle skipStyle = wb.createCellStyle();
            skipStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
            skipStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Ignored - Grey
            CellStyle ignoreStyle = wb.createCellStyle();
            ignoreStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
            ignoreStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Known Issue - Yellow
            CellStyle knownStyle = wb.createCellStyle();
            knownStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            knownStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // ================== HEADER ROW ==================

            Row header = comparison.createRow(0);

            String[] baseHeaders = { "Scenario ID", "Description", "BASE_LINE Status" };

            int colIndex = 0;
            for (String h : baseHeaders) {
                Cell cell = header.createCell(colIndex++);
                cell.setCellValue(h);
                cell.setCellStyle(headerStyle);
            }

            List<String> labels = new ArrayList<>(inputMap.keySet());
            for (String label : labels) {
                Cell cell = header.createCell(colIndex++);
                cell.setCellValue(label + " Status");
                cell.setCellStyle(headerStyle);
            }

            // Freeze header row
            comparison.createFreezePane(0, 1);

            // ================== DATA ==================

            Set<String> allIds = new TreeSet<>(baseline.keySet());
            for (Map<String, Scenario> input : inputMap.values()) {
                allIds.addAll(input.keySet());
            }

            int rowNum = 1;

            for (String id : allIds) {
                Row row = comparison.createRow(rowNum++);

                row.createCell(0).setCellValue(id);

                String description = baseline.containsKey(id)
                        ? baseline.get(id).description
                        : inputMap.values().stream()
                                .filter(m -> m.containsKey(id))
                                .map(m -> m.get(id).description)
                                .findFirst()
                                .orElse("");

                row.createCell(1).setCellValue(description);

                // Baseline status
                String baselineStatus = baseline.containsKey(id)
                        ? baseline.get(id).status
                        : "Not Found";

                Cell baseCell = row.createCell(2);
                baseCell.setCellValue(baselineStatus);
                applyStyle(baseCell, baselineStatus, passStyle, failStyle, skipStyle, ignoreStyle, knownStyle);

                int dynamicCol = 3;

                for (String label : labels) {
                    Map<String, Scenario> input = inputMap.get(label);

                    String status = input.containsKey(id)
                            ? input.get(id).status
                            : "Not Found";

                    Cell statusCell = row.createCell(dynamicCol++);
                    statusCell.setCellValue(status);

                    applyStyle(statusCell, status, passStyle, failStyle, skipStyle, ignoreStyle, knownStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < colIndex; i++) {
                comparison.autoSizeColumn(i);
            }

            // Write file
            try (FileOutputStream fileOut = new FileOutputStream(outputFilePath)) {
                wb.write(fileOut);
            }

            wb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void applyStyle(
            Cell cell,
            String status,
            CellStyle passStyle,
            CellStyle failStyle,
            CellStyle skipStyle,
            CellStyle ignoreStyle,
            CellStyle knownStyle) {

        if (status == null)
            return;

        if (status.equalsIgnoreCase("Passed")) {
            cell.setCellStyle(passStyle);
        } else if (status.equalsIgnoreCase("Failed")) {
            cell.setCellStyle(failStyle);
        } else if (status.equalsIgnoreCase("Skipped")) {
            cell.setCellStyle(skipStyle);
        } else if (status.equalsIgnoreCase("Ignored")) {
            cell.setCellStyle(ignoreStyle);
        } else if (status.equalsIgnoreCase("Known Issues")) {
            cell.setCellStyle(knownStyle);
        }
    }

    public static String CreateExcelReport(String newReportPath, String fileName) throws Exception {

        String outputFile = "comparison_vs_BASE_LINE.xlsx";

        File dir = new File(TestRunner.getGlobalResourcePath() + "/config");
        File[] htmlFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".html"));

        if (htmlFiles == null || htmlFiles.length == 0) {
            throw new RuntimeException("No .html file found in: " + dir.getAbsolutePath());
        }

        File baselineFile = htmlFiles[0];
        Map<String, Scenario> baseline = extractScenarios(baselineFile);

        Map<String, Map<String, Scenario>> inputMap = new LinkedHashMap<>();
        File inputFile = new File(newReportPath + fileName);

        String label = inputFile.getName().replace(".html", "");
        inputMap.put(label, extractScenarios(inputFile));

        new File(newReportPath).mkdirs();
        String outputPath = newReportPath + File.separator + outputFile;

        writeExcel(baseline, inputMap, outputPath);

        System.out.println("✅ Excel Report generated at: " + outputPath);

        return outputPath;
    }
}
