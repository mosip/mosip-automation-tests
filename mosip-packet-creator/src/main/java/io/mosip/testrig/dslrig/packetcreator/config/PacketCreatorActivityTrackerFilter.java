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
		boolean track = !isPurgeRequest(request);
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

	private static boolean isPurgeRequest(HttpServletRequest request) {
		String uri = request.getRequestURI();
		return uri != null && uri.contains("/workDir/purgeAll");
	}
}
