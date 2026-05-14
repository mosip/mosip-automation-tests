package io.mosip.testrig.dslrig.dataprovider.util.internalapi;

import java.time.Instant;

/**
 * Records outbound HTTP that does not go through Rest Assured (Apache HttpClient,
 * {@link java.net.HttpURLConnection}, etc.).
 */
public final class InternalApiManualRecorder {

	private InternalApiManualRecorder() {
	}

	public static void record(String contextKey, String method, String url, String requestHeaders, String requestBody,
			int statusCode, String responseHeaders, String responseBody) {
		if (!InternalApiLogging.isEnabled(contextKey)) {
			return;
		}
		String key = (contextKey == null || contextKey.isBlank()) ? InternalApiLogCollector.GLOBAL_LOG_KEY
				: contextKey;
		long seq = InternalApiLogCollector.nextSequence();
		InternalApiLogExchange ex = new InternalApiLogExchange(seq, key, Instant.now(), method, url,
				requestHeaders != null ? requestHeaders : "",
				requestBody != null ? requestBody : "");
		ex.setResponse(Instant.now(), statusCode, responseHeaders != null ? responseHeaders : "",
				responseBody != null ? responseBody : "");
		InternalApiLogCollector.record(key, ex);
	}

	public static void recordFailure(String contextKey, String method, String url, String requestHeaders,
			String requestBody, String message) {
		if (!InternalApiLogging.isEnabled(contextKey)) {
			return;
		}
		String key = (contextKey == null || contextKey.isBlank()) ? InternalApiLogCollector.GLOBAL_LOG_KEY
				: contextKey;
		long seq = InternalApiLogCollector.nextSequence();
		InternalApiLogExchange ex = new InternalApiLogExchange(seq, key, Instant.now(), method, url,
				requestHeaders != null ? requestHeaders : "",
				requestBody != null ? requestBody : "");
		ex.setError(Instant.now(), message);
		InternalApiLogCollector.record(key, ex);
	}
}
