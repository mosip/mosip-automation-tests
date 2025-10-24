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
				String[] statusParts = headerText.split("—"); // en dash
				if (statusParts.length >= 2) {
					currentStatus = statusParts[1].trim();
				} else {
					System.out.println("⚠️ Unexpected header format: " + headerText);
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

	public static Map<String, Integer> extractSummaryCounts(String filename) {
		Map<String, Integer> counts = new LinkedHashMap<>();
		counts.put("Total", extractInt(filename, "T-(\\d+)"));
		counts.put("Passed", extractInt(filename, "P-(\\d+)"));
		counts.put("Skipped", extractInt(filename, "S-(\\d+)"));
		counts.put("Failed", extractInt(filename, "F-(\\d+)"));
		counts.put("Known Issues", extractInt(filename, "KI-(\\d+)"));
		return counts;
	}

	public static int extractInt(String text, String regex) {
		Matcher matcher = Pattern.compile(regex).matcher(text);
		return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
	}

	public static void writeExcel(Map<String, Scenario> baseline, Map<String, Map<String, Scenario>> inputMap,
			List<Map<String, Integer>> summaryList, String outputFilePath) throws IOException {
		Workbook wb = new XSSFWorkbook();
		Sheet comparison = wb.createSheet("Comparison");

		// Header row
		Row header = comparison.createRow(0);
		header.createCell(0).setCellValue("Scenario ID");
		header.createCell(1).setCellValue("Description");
		header.createCell(2).setCellValue("BASE_LINE Status");

		int col = 3;
		List<String> labels = new ArrayList<>(inputMap.keySet());
		for (String label : labels) {
			header.createCell(col++).setCellValue(label + " Status");
		}

		// Build data
		Set<String> allIds = new TreeSet<>(baseline.keySet());
		for (Map<String, Scenario> input : inputMap.values()) {
			allIds.addAll(input.keySet());
		}

		int rowNum = 1;
		for (String id : allIds) {
			Row row = comparison.createRow(rowNum++);
			row.createCell(0).setCellValue(id);
			row.createCell(1)
					.setCellValue(baseline.containsKey(id) ? baseline.get(id).description
							: inputMap.values().stream().filter(m -> m.containsKey(id)).map(m -> m.get(id).description)
									.findFirst().orElse(""));
			row.createCell(2).setCellValue(baseline.containsKey(id) ? baseline.get(id).status : "Not Found");

			int colIndex = 3;
			for (String label : labels) {
				Map<String, Scenario> input = inputMap.get(label);
				row.createCell(colIndex++).setCellValue(input.containsKey(id) ? input.get(id).status : "Not Found");
			}
		}

		// Create formatting
		Sheet legend = wb.createSheet("Legend");
		legend.createRow(0).createCell(0).setCellValue("Color Conditions:");
		legend.createRow(1).createCell(0).setCellValue("✅ Green: Same as baseline and passed");
		legend.createRow(2).createCell(0).setCellValue("🔵 Blue: Different and passed");
		legend.createRow(3).createCell(0).setCellValue("🔴 Red: Different and failed");
		legend.createRow(4).createCell(0).setCellValue("🟤 Brown: Baseline missing, input failed");

		try (FileOutputStream fileOut = new FileOutputStream(outputFilePath)) {
			wb.write(fileOut);
		}
		wb.close();
	}

	public static String CreateExcelReport(String newReportPath , String fileName) throws Exception {
		String outputFile = "comparison_vs_BASE_LINE.xlsx";

		File dir = new File(TestRunner.getGlobalResourcePath()+"/"+"config");
		File[] htmlFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".html"));

		if (htmlFiles == null || htmlFiles.length == 0) {
		    throw new RuntimeException("No .html file found in: " + dir.getAbsolutePath());
		}

		// Pick the first .html file (you can also sort if needed)
		File baselineFile = htmlFiles[0];
		Map<String, Scenario> baseline = extractScenarios(baselineFile);
		List<Map<String, Integer>> summaryList = new ArrayList<>();

		summaryList.add(extractSummaryCounts(baselineFile.getName()));

		Map<String, Map<String, Scenario>> inputMap = new LinkedHashMap<>();
		File inputFile = new File(newReportPath+fileName);

		String label = inputFile.getName().replace(".html", "");
		inputMap.put(label, extractScenarios(inputFile));
		summaryList.add(extractSummaryCounts(inputFile.getName()));

		new File(newReportPath).mkdirs();
		String outputPath = newReportPath + File.separator + outputFile;
		writeExcel(baseline, inputMap, summaryList, outputPath);
		System.out.println("✅ Report generated at: " + outputPath);
		return outputPath;
	}
}
