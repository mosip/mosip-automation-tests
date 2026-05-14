package io.mosip.testrig.dslrig.dataprovider.util.internalapi;

import java.time.Instant;

/**
 * One outbound HTTP exchange (request + response) in execution order.
 */
public final class InternalApiLogExchange {

	private final long sequence;
	private final String contextKey;
	private final Instant requestInstant;
	private final String method;
	private final String url;
	private final String requestHeaders;
	private final String requestBody;
	private volatile Instant responseInstant;
	private volatile int statusCode;
	private volatile String responseHeaders;
	private volatile String responseBody;
	private volatile String errorMessage;

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
		this.responseInstant = responseInstant;
		this.statusCode = statusCode;
		this.responseHeaders = responseHeaders;
		this.responseBody = responseBody;
	}

	public void setError(Instant responseInstant, String message) {
		this.responseInstant = responseInstant;
		this.errorMessage = message;
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
		return responseInstant;
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
		return statusCode;
	}

	public String getResponseHeaders() {
		return responseHeaders;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}
