package io.mosip.testrig.dslrig.dataprovider.util.internalapi;

import java.time.Instant;


public final class InternalApiLogExchange {


	public static record Response(Instant responseInstant, int statusCode, String responseHeaders,
			String responseBody, String errorMessage) {
	}

	private final long sequence;
	private final String contextKey;
	private final Instant requestInstant;
	private final String method;
	private final String url;
	private final String requestHeaders;
	private final String requestBody;
	private volatile Response response;

	public InternalApiLogExchange(long sequence, String contextKey, Instant requestInstant, String method,
			String url, String requestHeaders, String requestBody) {
		this.sequence = sequence;
		this.contextKey = contextKey;
		this.requestInstant = requestInstant;
		this.method = method;
		this.url = url;
		this.requestHeaders = requestHeaders;
		this.requestBody = requestBody;
	}

	public void setResponse(Instant responseInstant, int statusCode, String responseHeaders, String responseBody) {
		this.response = new Response(responseInstant, statusCode, responseHeaders, responseBody, null);
	}

	public void setError(Instant responseInstant, String message) {
		this.response = new Response(responseInstant, 0, null, null, message);
	}

	public long getSequence() {
		return sequence;
	}

	public String getContextKey() {
		return contextKey;
	}

	public Instant getRequestInstant() {
		return requestInstant;
	}

	public Instant getResponseInstant() {
		Response r = response;
		return r == null ? null : r.responseInstant();
	}

	public String getMethod() {
		return method;
	}

	public String getUrl() {
		return url;
	}

	public String getRequestHeaders() {
		return requestHeaders;
	}

	public String getRequestBody() {
		return requestBody;
	}

	public int getStatusCode() {
		Response r = response;
		return r == null ? 0 : r.statusCode();
	}

	public String getResponseHeaders() {
		Response r = response;
		return r == null ? null : r.responseHeaders();
	}

	public String getResponseBody() {
		Response r = response;
		return r == null ? null : r.responseBody();
	}

	public String getErrorMessage() {
		Response r = response;
		return r == null ? null : r.errorMessage();
	}

	/** True when the call errored, returned HTTP 4xx/5xx, or MOSIP {@code errors} is non-empty. */
	public boolean isFailed() {
		Response r = response;
		if (r == null) {
			return true;
		}
		if (r.errorMessage() != null) {
			return true;
		}
		if (r.statusCode() >= 400) {
			return true;
		}
		return hasMosipErrors(r.responseBody());
	}

	private static boolean hasMosipErrors(String body) {
		if (body == null || body.isBlank()) {
			return false;
		}
		String t = body.trim();
		int idx = t.indexOf("\"errors\"");
		if (idx < 0) {
			return false;
		}
		int colon = t.indexOf(':', idx + 8);
		if (colon < 0) {
			return false;
		}
		int i = colon + 1;
		while (i < t.length() && Character.isWhitespace(t.charAt(i))) {
			i++;
		}
		if (i >= t.length()) {
			return false;
		}
		char c = t.charAt(i);
		if (c == 'n') { // null
			return false;
		}
		if (c == '[') {
			int j = i + 1;
			while (j < t.length() && Character.isWhitespace(t.charAt(j))) {
				j++;
			}
			return j < t.length() && t.charAt(j) != ']';
		}
		return false;
	}
}
