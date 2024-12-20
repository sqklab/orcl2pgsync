package com.lguplus.fleta.config.security;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

public class AuditorAwareImpl implements AuditorAware<String> {
	@Override
	public Optional<String> getCurrentAuditor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (isEmpty(authentication)) {
			return Optional.empty();
		} else if (authentication instanceof KeycloakAuthenticationToken) {
			KeycloakPrincipal principal = (KeycloakPrincipal) authentication.getPrincipal();
			RefreshableKeycloakSecurityContext securityContext = (RefreshableKeycloakSecurityContext) principal.getKeycloakSecurityContext();
			AccessToken token = securityContext.getToken();
			return Optional.of(token.getPreferredUsername());
		} else {
			return Optional.of(authentication.getName());
		}
	}
}