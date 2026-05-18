package io.mosip.testrig.dslrig.dataprovider.util.internalapi;

import java.time.Instant;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;


public final class InternalApiRestAssuredFilter implements Filter {

	private final String storageKey;

	public InternalApiRestAssuredFilter(String contextKey) {
		this.storageKey = (contextKey == null || contextKey.isBlank()) ? InternalApiLogCollector.GLOBAL_LOG_KEY
				: contextKey;
	}

	@Override
	public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec,
			FilterContext ctx) {
		String uri = safeUri(requestSpec);
		if (uri.contains("/context/internalApiLogs")) {
			return ctx.next(requestSpec, responseSpec);
		}

		long seq = InternalApiLogCollector.nextSequence();
		String method = requestSpec.getMethod() != null ? requestSpec.getMethod() : "";
		String reqHeaders = headersToString(requestSpec);
		String reqBody = bodyToString(requestSpec);

		InternalApiLogExchange ex = new InternalApiLogExchange(seq, storageKey, Instant.now(), method, uri, reqHeaders,
				reqBody);
		InternalApiLogCollector.record(storageKey, ex);

		try {
			Response response = ctx.next(requestSpec, responseSpec);
			String respHeaders = response.getHeaders() != null ? response.getHeaders().toString() : "";
			String respBody = response.getBody() != null ? response.getBody().asString() : "";
			ex.setResponse(Instant.now(), response.getStatusCode(), respHeaders, respBody);
			return response;
		} catch (RuntimeException e) {
			ex.setError(Instant.now(), String.valueOf(e.getMessage()));
			throw e;
		}
	}

	private static String safeUri(FilterableRequestSpecification requestSpec) {
		try {
			String u = requestSpec.getURI();
			return u != null ? u : "";
		} catch (Exception e) {
			return "";
		}
	}

	private static String headersToString(FilterableRequestSpecification req) {
		try {
			if (req.getHeaders() == null) {
				return "";
			}
			return req.getHeaders().toString();
		} catch (Exception e) {
			return "";
		}
	}

	private static String bodyToString(FilterableRequestSpecification req) {
		try {
			if (req.getFormParams() != null && !req.getFormParams().isEmpty()) {
				return "(x-www-form-urlencoded / form) " + req.getFormParams();
			}
		} catch (Exception ignored) {

		}
		try {
			Object body = req.getBody();
			return body != null ? body.toString() : "";
		} catch (Exception e) {
			return "";
		}
	}
}
