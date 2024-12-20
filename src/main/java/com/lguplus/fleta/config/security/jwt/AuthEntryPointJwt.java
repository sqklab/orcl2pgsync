package com.lguplus.fleta.config.security.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
						 AuthenticationException authException) throws IOException, ServletException {
		log.error("Unauthorized Error on path {}.\n\nDetails: {}", request.getRequestURI(), authException.getMessage());
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not Authorized.\n\tHTTP Error 401. The requested resource requires user authentication");
	}

}