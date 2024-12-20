package com.lguplus.fleta.adapters.rest;

import com.lguplus.fleta.domain.dto.rest.HttpResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.keycloak.OAuth2Constants;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


@RequestMapping(value = "/auth")
@RestController
@CrossOrigin
@ConditionalOnProperty(
		value = "spring.keycloak-enabled",
		havingValue = "true")
public class KeyCloakAuthController {

	private static final Logger log = LoggerFactory.getLogger(KeyCloakAuthController.class);

	@Value("${keycloak.auth-server-url}")
	private String authServerUrl;
	@Value("${keycloak.realm}")
	private String realm;
	@Value("${keycloak.resource}")
	private String clientId;
	@Value("${keycloak.credentials.secret}")
	private String clientSecret;

	@PostMapping(path = "/login")
	public ResponseEntity<?> login(@RequestBody UserDTO userDTO) {
		try {
			if (userDTO == null
					|| userDTO.getUsername() == null
					|| userDTO.getPassword() == null
					|| userDTO.getPassword().isEmpty()
					|| userDTO.getUsername().isEmpty()) {
				return ResponseEntity.badRequest().build();
			}
			Map<String, Object> clientCredentials = new HashMap<>();
			clientCredentials.put("secret", clientSecret);
			clientCredentials.put("grant_type", OAuth2Constants.PASSWORD);

			Configuration configuration =
					new Configuration(authServerUrl, realm, clientId, clientCredentials, null);
			AuthzClient authzClient = AuthzClient.create(configuration);

			AccessTokenResponse response =
					authzClient.obtainAccessToken(userDTO.getUsername(), userDTO.getPassword());
			final String token = response.getToken();
			return ResponseEntity.ok(new LoginResponse(token));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class UserDTO implements Serializable {
		private String username;
		private String email;
		private String password;
		private String firstname;
		private String lastname;
		private int statusCode;
		private String status;

	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class LoginRequest implements Serializable {
		private String username;
		private String password;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class LoginResponse {
		private String token;

		public LoginResponse(String token) {
			this.token = token;
		}
	}
}
