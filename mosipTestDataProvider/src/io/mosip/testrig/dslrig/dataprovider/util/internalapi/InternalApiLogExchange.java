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
}
