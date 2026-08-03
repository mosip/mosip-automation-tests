package io.mosip.testrig.dslrig.packetcreator.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Gates the packet-creator endpoints that read/mutate resident data (actuator info,
 * packet create/sync, resident update, card download) behind a shared {@code X-Api-Key}
 * header. {@link SecurityConfiguration} otherwise permits all requests, so this is the
 * only application-layer control for these routes; the real deployment additionally
 * restricts the service to the internal Istio gateway (see helm/packetcreator/values.yaml).
 *
 * <p>When {@code mosip.test.packetcreator.apikey} is left blank (the default), the check
 * is skipped so existing deployments keep working until the secret is provisioned.
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
	private static final String API_KEY_HEADER = "X-Api-Key";

	@Value("${mosip.test.packetcreator.apikey:}")
	private String expectedApiKey;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		if (!isProtected(request.getServletPath())) {
			filterChain.doFilter(request, response);
			return;
		}
		if (expectedApiKey == null || expectedApiKey.isBlank()) {
			logger.warn("mosip.test.packetcreator.apikey is not configured; allowing unauthenticated access to {}",
					request.getServletPath());
			filterChain.doFilter(request, response);
			return;
		}
		if (!expectedApiKey.equals(request.getHeader(API_KEY_HEADER))) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid " + API_KEY_HEADER);
			return;
		}
		filterChain.doFilter(request, response);
	}

	private static boolean isProtected(String servletPath) {
		if (servletPath == null) {
			return false;
		}
		return servletPath.startsWith("/env/idrepoActuatorInfo/")
				|| servletPath.startsWith("/packetmanager/createPacket/")
				|| servletPath.startsWith("/sync/externalPacket/")
				|| servletPath.startsWith("/updateresident/")
				|| servletPath.startsWith("/resident/card/");
	}

}
