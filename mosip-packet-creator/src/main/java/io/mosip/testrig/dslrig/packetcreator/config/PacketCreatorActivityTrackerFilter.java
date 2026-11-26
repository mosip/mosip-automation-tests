package io.mosip.testrig.dslrig.packetcreator.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.mosip.testrig.dslrig.packetcreator.service.PacketCreatorActivityTracker;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PacketCreatorActivityTrackerFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		boolean track = isBusinessRequest(request);
		if (track) {
			PacketCreatorActivityTracker.enter();
		}
		try {
			filterChain.doFilter(request, response);
		} finally {
			if (track) {
				PacketCreatorActivityTracker.leave();
			}
		}
	}

	/**
	 * Health/actuator/swagger/purge traffic must not keep the process "busy", otherwise
	 * idle maintenance never runs under Kubernetes probes.
	 */
	private static boolean isBusinessRequest(HttpServletRequest request) {
		String uri = request.getRequestURI();
		if (uri == null || uri.isBlank()) {
			return false;
		}
		String path = uri.toLowerCase();
		if (path.contains("/workdir/purgeall")
				|| path.contains("/actuator/")
				|| path.contains("/swagger")
				|| path.contains("/v3/api-docs")
				|| path.contains("/api-docs")) {
			return false;
		}
		return true;
	}
}
