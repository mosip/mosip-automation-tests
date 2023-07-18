package io.mosip.testrig.dslrig.dataprovider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public class CSVHelper {
	private static final Logger logger = LoggerFactory.getLogger(CSVHelper.class);

	String fileName;
	CSVReader csvReader;
	int recCount;

	public int getRecordCount() {
		return recCount;
	}

	public CSVHelper(String csvFile) throws IOException {
		fileName = csvFile;

		open();

		// get line count
		recCount = 0;
		while (csvReader.readNext() != null) {
			recCount++;
		}
		csvReader.close();
	}

	public void open() throws FileNotFoundException, UnsupportedEncodingException {

		try (FileInputStream inStream = new FileInputStream(fileName);
				InputStreamReader filereader = new InputStreamReader(inStream, "UTF-8");) {
			csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

	}

	// pass an array of record numbers to read
	List<String[]> readRecords(int[] recnos) throws IOException {

		List<String[]> outList = new ArrayList<String[]>();
		Arrays.sort(recnos);

		String[] nextRecord;
		int recno = 0;

		int i = 0;

		while ((nextRecord = csvReader.readNext()) != null) {

			if (i >= recnos.length)
				break;

			if (recno == recnos[i]) {
				i++;

				if (nextRecord != null) {
					outList.add(nextRecord);
				}
			}
			recno++;

		}
		return outList;
	}

	public String[] readRecord() throws IOException {
		return csvReader.readNext();

	}

	public void close() throws IOException {
		csvReader.close();
	}

	public List<String> readAttribute(int col, int[] recnos) throws IOException {
		List<String> retCols = new ArrayList<String>();
		List<String[]> recs = readRecords(recnos);
		for (String[] r : recs) {
			String val = r[(col >= r.length) ? 0 : col];
			if (val != null)
				val = CommonUtil.toCaptialize(r[0]);
			retCols.add(val);
		}
		return retCols;
	}

	public static void main(String[] args) {

		try {
			CSVHelper helper = new CSVHelper(VariableManager.getVariableValue("contextKey", "mountPath").toString()
					+ VariableManager.getVariableValue("contextKey", "mosip.test.persona.namesdatapath").toString()
					+ "/en/surnames.csv");
			System.out.println(helper.getRecordCount());
			helper.open();
			List<String[]> recs = helper.readRecords(new int[] { 0, 15, 10, 20, 12 });
			for (String[] r : recs) {

				System.out.println(CommonUtil.toCaptialize(r[0]));
			}
			helper.close();

			helper = new CSVHelper(VariableManager.getVariableValue("contextKey", "mountPath").toString()
					+ VariableManager.getVariableValue("contextKey", "mosip.test.persona.namesdatapath").toString()
					+ "/ara/boy_names.csv");
			System.out.println(helper.getRecordCount());
			helper.open();
			recs = helper.readRecords(new int[] { 1, 15, 10, 20, 12 });
			for (String[] r : recs) {

				System.out.println(r[1]);
			}
			helper.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

	}
}
