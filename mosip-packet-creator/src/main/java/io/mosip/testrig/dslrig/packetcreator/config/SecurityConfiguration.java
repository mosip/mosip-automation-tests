package io.mosip.testrig.dslrig.packetcreator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfiguration {

	@Bean
	public HttpFirewall defaultHttpFirewall() {
		return new DefaultHttpFirewall();
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.httpBasic(httpEntry -> httpEntry.disable());
		httpSecurity.csrf(httpEntry -> httpEntry.disable());
		httpSecurity.authorizeHttpRequests(http -> http.anyRequest().permitAll());
		
		return httpSecurity.build();
	}
	
	@Bean
	public AuthenticationEntryPoint unauthorizedEntryPoint() {
		return (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}
}