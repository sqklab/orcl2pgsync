package com.lguplus.fleta.adapters.rest;

import com.lguplus.fleta.config.security.jwt.TokenProvider;
import com.lguplus.fleta.domain.dto.UserDto;
import com.lguplus.fleta.domain.model.UserEntity;
import com.lguplus.fleta.ports.service.AuthService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.Serializable;

@RestController
@CrossOrigin
@RequestMapping("/auth")
@ConditionalOnProperty(
		value = "spring.keycloak-enabled",
		havingValue = "false")
public class JWTAuthController {

	private final AuthenticationManager authenticationManager;

	private final TokenProvider tokenProvider;

	private final UserDetailsService userDetailsService;

	private final AuthService authService;

	public JWTAuthController(AuthenticationManager authenticationManager,
							 TokenProvider tokenProvider,
							 @Qualifier("UserServiceImpl") UserDetailsService userDetailsService,
							 AuthService authService) {
		this.authenticationManager = authenticationManager;
		this.tokenProvider = tokenProvider;
		this.userDetailsService = userDetailsService;
		this.authService = authService;
	}


	@PostMapping(path = "/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) throws Exception {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							loginRequest.getUsername(),
							loginRequest.getPassword()));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}
		final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
		final String token = tokenProvider.createToken(userDetails, false);
		return ResponseEntity.ok(new LoginResponse(token));
	}

	@PostMapping(value = "/register")
	public UserEntity register(@RequestBody @Valid UserDto userDto) {
		if (authService.isExisted(userDto.getUsername())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "user is already existed");
		}
		return authService.registerNewUser(userDto);
	}

	@PostMapping(value = "/changePassword")
	public UserEntity changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordReq) {
		if (!authService.isExisted(changePasswordReq.getUsername())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found");
		}
		return authService.changePassword(changePasswordReq.getUsername(), changePasswordReq.getNewPassword());
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

	@Getter
	@Setter
	static class ChangePasswordRequest implements Serializable {
		private String username;
		private String newPassword;
	}
}