package com.lguplus.fleta.config.security;

import com.lguplus.fleta.domain.dto.rest.HttpResponse;
import com.lguplus.fleta.domain.service.mapper.ObjectMapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class SecurityAccessDeniedHandler {

	public void send(HttpServletResponse response) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		HttpResponse<String> deny = new HttpResponse<>(HttpStatus.UNAUTHORIZED.value(), "RESOURCE_ACCESS_DENY", "");
		response.getWriter().write(
				ObjectMapperFactory.getInstance().getObjectMapper().writeValueAsString(deny)
		);
	}
}
