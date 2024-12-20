package com.lguplus.fleta.config;

import com.lguplus.fleta.config.interceptor.VisitorLogger;
import com.slack.api.Slack;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
			"classpath:/META-INF/resources/",
			"classpath:/resources/",
			"classpath:/static/",
			"classpath:/public/"
	};
	private static final List<String> EXCLUDE_URL = Arrays.asList("/favicon.ico", "/*.js", "/*.css", "/*/*/*.json", "/*/*/*.png", "/*/*/*.jpg", "/*/*/*.jpeg", "/*.woff2");
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	private final VisitorLogger visitorLogger;
	@Value("${logging.file.path:logs}")
	private String logPath;

	public WebMvcConfig(VisitorLogger visitorLogger) {
		this.visitorLogger = visitorLogger;
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**")
				.addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("forward:/index.html");
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(visitorLogger);
	}

	@Bean
	@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Bean
	public SpringTemplateEngine springTemplateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.addTemplateResolver(htmlTemplateResolver());
		return templateEngine;
	}

	@Bean
	public SpringResourceTemplateResolver htmlTemplateResolver() {
		SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
		templateResolver.setPrefix("classpath:/templates/email/");
		templateResolver.setSuffix(".html");
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
		return templateResolver;
	}

	@Bean
	@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON) // TODO: important
	public IDefaultLogPath createLogPath() {
		return () -> logPath;
	}

	@Bean
	public Slack createSlackInstance() {
		return Slack.getInstance();
	}

	@Bean
	public CommonsRequestLoggingFilter requestLoggingFilter() {
		CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter() {
			@Override
			protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
				return EXCLUDE_URL.stream().anyMatch(p -> pathMatcher.match(p, request.getServletPath()));
			}
		};
		filter.setIncludeHeaders(true);
		filter.setIncludeQueryString(true);
		filter.setIncludePayload(true);
		filter.setIncludeClientInfo(true);
		filter.setMaxPayloadLength(100000);
		return filter;
	}
}