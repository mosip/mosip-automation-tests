package io.mosip.testrig.dslrig.dataprovider.util.internalapi;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Pretty-prints collected exchanges: one section, one block per outbound call
 * with request and response together (execution order).
 */
public final class InternalApiLogFormatter {

	private static final String ENTRY_SEP = "\n--------------------------------------------------------------------------------\n";

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private InternalApiLogFormatter() {
	}

	public static String format(List<InternalApiLogExchange> exchanges) {
		if (exchanges == null || exchanges.isEmpty()) {
			return "OUTBOUND INTERNAL API CALLS\n(no internal API calls recorded for this context)\n";
		}
		StringBuilder out = new StringBuilder();
		out.append("OUTBOUND INTERNAL API CALLS (execution order: request + response per call)\n");

		int n = 1;
		for (InternalApiLogExchange ex : exchanges) {
			out.append(ENTRY_SEP);
			out.append("Call #").append(n).append(" | sequence=").append(ex.getSequence()).append('\n');
			out.append("URL           : ").append(safe(ex.getUrl())).append('\n');
			out.append("HTTP Method   : ").append(safe(ex.getMethod())).append('\n');
			out.append("Timestamp     : ").append(formatInstant(ex.getRequestInstant())).append('\n');
			out.append("Headers       :\n").append(indent(safe(ex.getRequestHeaders()))).append('\n');
			out.append("Request Body  :\n").append(indent(prettyIfJson(safe(ex.getRequestBody())))).append('\n');
			out.append("--- Response ---\n");
			if (ex.getErrorMessage() != null) {
				out.append("Status Code   : (no HTTP response — error before completion)\n");
				out.append("Headers       :\n  (n/a)\n");
				out.append("Response Body :\n").append(indent(safe(ex.getErrorMessage()))).append('\n');
			} else {
				out.append("Status Code   : ").append(ex.getStatusCode()).append('\n');
				out.append("Timestamp     : ").append(formatInstant(ex.getResponseInstant())).append('\n');
				out.append("Headers       :\n").append(indent(safe(ex.getResponseHeaders()))).append('\n');
				out.append("Response Body :\n").append(indent(prettyIfJson(safe(ex.getResponseBody())))).append('\n');
			}
			n++;
		}
		return out.toString();
	}

	private static String formatInstant(Instant i) {
		return i == null ? "N/A" : i.toString();
	}

	private static String safe(String s) {
		return s == null ? "" : s;
	}

	private static String indent(String block) {
		if (block.isEmpty()) {
			return "  (empty)";
		}
		return "  " + block.replace("\n", "\n  ");
	}

	private static String prettyIfJson(String raw) {
		if (raw == null || raw.isBlank()) {
			return "";
		}
		String t = raw.trim();
		if (!(t.startsWith("{") || t.startsWith("["))) {
			return raw;
		}
		try {
			Object tree = MAPPER.readValue(t, Object.class);
			return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
		} catch (Exception e) {
			return raw;
		}
	}
}
