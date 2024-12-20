package com.lguplus.fleta.config.security;

import com.lguplus.fleta.config.Profile;
import com.lguplus.fleta.config.security.jwt.AuthEntryPointJwt;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticatedActionsFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakSecurityContextRequestFilter;
import org.keycloak.adapters.springsecurity.management.HttpSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
		securedEnabled = true,
		jsr250Enabled = true,
		prePostEnabled = true)
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
@ConditionalOnProperty(
		value = "spring.keycloak-enabled",
		havingValue = "true")
public class KeyCloakSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

	private final AuthEntryPointJwt unauthorizedHandler;
	private final SecurityAccessDeniedHandler accessDeniedHandler;
	@Value("${spring.profiles.active}")
	private String ACTIVE_PROFILE;

	public KeyCloakSecurityConfig(AuthEntryPointJwt unauthorizedHandler,
								  SecurityAccessDeniedHandler accessDeniedHandler) {
		this.unauthorizedHandler = unauthorizedHandler;
		this.accessDeniedHandler = accessDeniedHandler;
	}

	private boolean isProduction() {
		return Profile.isProduction(this.ACTIVE_PROFILE);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		if (isProduction()) {
			web.ignoring()
					.antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**", "/fonts/**", "/scss/**", "/assets/**",
							"/*.css",
							"/*.css.map",
							"/*.js",
							"/*.js.map",
							"/*bootstrap-icons*",
							"/*woff*",
							"/logs/**",
							"/logs/log/**",
							"/*.svg"
					);
		} else {
			web.ignoring()
					.antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**", "/fonts/**", "/scss/**", "/assets/**",
							"/*.css",
							"/*.css.map",
							"/*.js",
							"/*.js.map",
							"/*bootstrap-icons*",
							"/*woff*",
							"/*/synchronizers/status/*",
							"/*/synchronizers/status",
							"/*/kafka/offset**",
							"/*/kafka/messages-behind/*",
							"/*/kafka/consumer-groups",
							"/logs/**",
							"/logs/log/**",
							"/heath/memory/stats",
							"/*.svg"

					);
		}
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
		http.cors()
				.and()
				.csrf().disable()
				.exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
				.and()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
				.authorizeRequests()
				.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
				.antMatchers("/i18n/**", "/content/**", "/swagger-ui.html**", "/swagger-ui/**", "/doc.*", "/swagger-resources/**", "/v2/api-docs", "/v3/api-docs", "/test/**",
						"/resources/**", "/static/**", "/css/**", "/js/**", "/images/**", "/fonts/**", "/scss/**").permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.antMatchers("/auth/**").permitAll()
				.antMatchers("/").permitAll()
				.antMatchers("/#/**").permitAll()
				.antMatchers("/dbsync/logs/**", "/socket.io/**").permitAll()
				.antMatchers(HttpMethod.GET, "/**").hasAuthority("ROLE_VIEWER")
				.antMatchers(HttpMethod.POST, "/**").hasAuthority("ROLE_ADMIN")
				.antMatchers(HttpMethod.PUT, "/**").hasAuthority("ROLE_ADMIN")
				.antMatchers(HttpMethod.DELETE, "/**").hasAuthority("ROLE_ADMIN")
				.anyRequest().authenticated();

		http.exceptionHandling().accessDeniedHandler((request, response, accessDeniedException) -> accessDeniedHandler.send(response));
	}

	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {

		/*
		 * Returning NullAuthenticatedSessionStrategy means app will not remember session
		 */

		return new NullAuthenticatedSessionStrategy();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		KeycloakAuthenticationProvider keycloakAuthenticationProvider =
				keycloakAuthenticationProvider();
		SimpleAuthorityMapper mapper = new SimpleAuthorityMapper();
		mapper.setConvertToUpperCase(true);
		mapper.setPrefix("");
		keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(mapper);
		auth.authenticationProvider(keycloakAuthenticationProvider);
	}

	@Bean
	public FilterRegistrationBean<?> keycloakAuthenticationProcessingFilterRegistrationBean(
			KeycloakAuthenticationProcessingFilter filter) {

		FilterRegistrationBean<?> registrationBean = new FilterRegistrationBean<>(filter);

		registrationBean.setEnabled(false);
		return registrationBean;
	}

	@Bean
	public FilterRegistrationBean<?> keycloakPreAuthActionsFilterRegistrationBean(
			KeycloakPreAuthActionsFilter filter) {

		FilterRegistrationBean<?> registrationBean = new FilterRegistrationBean<>(filter);
		registrationBean.setEnabled(false);
		return registrationBean;
	}

	@Bean
	public FilterRegistrationBean<?> keycloakAuthenticatedActionsFilterBean(
			KeycloakAuthenticatedActionsFilter filter) {

		FilterRegistrationBean<?> registrationBean = new FilterRegistrationBean<>(filter);

		registrationBean.setEnabled(false);
		return registrationBean;
	}

	@Bean
	public FilterRegistrationBean<?> keycloakSecurityContextRequestFilterBean(
			KeycloakSecurityContextRequestFilter filter) {

		FilterRegistrationBean<?> registrationBean = new FilterRegistrationBean<>(filter);

		registrationBean.setEnabled(false);

		return registrationBean;
	}

	@Bean
	@Override
	@ConditionalOnMissingBean(HttpSessionManager.class)
	protected HttpSessionManager httpSessionManager() {
		return new HttpSessionManager();
	}
}