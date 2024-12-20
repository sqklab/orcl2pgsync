package com.lguplus.fleta.config.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Apr 2022
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestResponseLoggingFilter implements Filter {

	private static final List<String> EXCLUDE_URL = Arrays.asList("/favicon.ico", "/*.js", "/*.css", "/*/*/*.json", "/*/*/*.png", "/*/*/*.jpg", "/*/*/*.jpeg", "/*.woff2");

	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.info("########## Initiating RequestResponseLoggingFilter ##########");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		if (EXCLUDE_URL.stream().anyMatch(p -> pathMatcher.match(p, req.getServletPath()))) {
			// Call next filter in the filter chain
			filterChain.doFilter(request, response);
		} else {
			log.info("Logging Request  {} : {}", req.getMethod(), req.getRequestURI());

			// Call next filter in the filter chain
			filterChain.doFilter(request, response);

			log.info("Logging Response :{}", res.getContentType());
		}
	}

	@Override
	public void destroy() {

	}
}
