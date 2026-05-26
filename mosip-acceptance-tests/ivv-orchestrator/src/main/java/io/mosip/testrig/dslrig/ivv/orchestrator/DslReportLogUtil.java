package io.mosip.testrig.dslrig.ivv.orchestrator;

import io.mosip.testrig.apirig.utils.GlobalMethods;
import io.mosip.testrig.dslrig.ivv.orchestrator.util.SensitiveLogMasker;
import io.restassured.response.Response;

/**
 * TestNG report logging with secrets, keys, and tokens masked before they reach the HTML report.
 */
public final class DslReportLogUtil {

	private DslReportLogUtil() {
	}

	public static void reportRequestAndResponse(String operation, String headers, String url, String request,
			String response) {
		GlobalMethods.ReportRequestAndResponse(operation, mask(headers), url, mask(request), mask(response));
	}

	public static void reportRequestAndResponse(String operation, String headers, String url, String request,
			String response, boolean flag) {
		GlobalMethods.ReportRequestAndResponse(operation, mask(headers), url, mask(request), mask(response), flag);
	}

	public static void reportRequest(String label, String request, String url) {
		GlobalMethods.reportRequest(label, mask(request), url);
	}

	public static void reportRequest(String label, String request) {
		GlobalMethods.reportRequest(label, mask(request));
	}

	public static void reportResponse(String headers, String url, Response response) {
		String body = "";
		if (response != null && response.getBody() != null) {
			body = response.getBody().asString();
		}
		String headerStr = response != null && response.getHeaders() != null
				? response.getHeaders().asList().toString()
				: headers;
		GlobalMethods.ReportRequestAndResponse(null, mask(headerStr), url, null, mask(body));
	}

	private static String mask(String value) {
		return value == null ? null : SensitiveLogMasker.mask(value);
	}
}
