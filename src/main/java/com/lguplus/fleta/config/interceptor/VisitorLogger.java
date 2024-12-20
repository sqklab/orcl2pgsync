package com.lguplus.fleta.config.interceptor;

import com.lguplus.fleta.domain.util.HttpRequestResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Apr 2022
 */
@Slf4j
@Component
public class VisitorLogger implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		final String ip = HttpRequestResponseUtils.getClientIpAddress();
		final String url = HttpRequestResponseUtils.getRequestUrl();
		final String page = HttpRequestResponseUtils.getRequestUri();
		final String refererPage = HttpRequestResponseUtils.getRefererPage();
		final String queryString = HttpRequestResponseUtils.getPageQueryString();
		final String userAgent = HttpRequestResponseUtils.getUserAgent();
		final String requestMethod = HttpRequestResponseUtils.getRequestMethod();
		final LocalDateTime timestamp = LocalDateTime.now();
		String uri = request.getRequestURI();
		if (!this.isFromHeathKafkaCheck(uri)) {
			log.info("Client IP: {}, Logged Time: {}, Method: {}, Page: {}, Url: {}, Query Parameters: {}, Referer Page: {}, Unique Visit: {}\n\t-> User: {}\n\t-> User Agent: {}",
					ip, timestamp, requestMethod, page, url, queryString, refererPage, true, HttpRequestResponseUtils.getLoggedInUser(), userAgent);
		}
		return true;
	}

	private boolean isFromHeathKafkaCheck(String uri) {
		if (StringUtils.isEmpty(uri)) {
			return false;
		}
		return uri.contains("heath/kafka/start");
	}
}
