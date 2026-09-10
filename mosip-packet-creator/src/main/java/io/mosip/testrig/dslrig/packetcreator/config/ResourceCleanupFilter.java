package io.mosip.testrig.dslrig.packetcreator.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * {@link DataProviderConstants#setResource(String)} stores the persona config path in a
 * thread-local, but servlet worker threads are pooled and reused across requests: without this
 * cleanup, a thread that served a request with a request-specific persona config path would
 * silently reuse that path for the next request it handles, regardless of that request's own
 * configuration.
 */
@Component
public class ResourceCleanupFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} finally {
			DataProviderConstants.clearResource();
		}
	}

}
