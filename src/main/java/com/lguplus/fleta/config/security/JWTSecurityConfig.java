package com.lguplus.fleta.config.security;

import com.lguplus.fleta.config.Profile;
import com.lguplus.fleta.config.security.jwt.AuthEntryPointJwt;
import com.lguplus.fleta.config.security.jwt.JWTFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
		securedEnabled = true,
		jsr250Enabled = true,
		prePostEnabled = true)
@ConditionalOnProperty(
		value = "spring.keycloak-enabled",
		havingValue = "false")
public class JWTSecurityConfig extends WebSecurityConfigurerAdapter {

	private final UserDetailsService jwtUserDetailsService;

	private final AuthEntryPointJwt unauthorizedHandler;

	@Value("${spring.profiles.active}")
	private String ACTIVE_PROFILE;

	public JWTSecurityConfig(AuthEntryPointJwt unauthorizedHandler, @Qualifier("UserServiceImpl") UserDetailsService jwtUserDetailsService) {
		this.unauthorizedHandler = unauthorizedHandler;
		this.jwtUserDetailsService = jwtUserDetailsService;
	}

	private boolean isNotProduction() {
		return !Profile.isProduction(this.ACTIVE_PROFILE);
	}

	private boolean isProduction() {
		return Profile.isProduction(this.ACTIVE_PROFILE);
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		// configure AuthenticationManager so that it knows from where to load
		// user for matching credentials
		// Use BCryptPasswordEncoder
		auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder());
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public JWTFilter authenticationJwtTokenFilter() {
		return new JWTFilter();
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
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
							"/logs/log/**"
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
							"/heath/memory/stats"
					);
		}
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
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
				.anyRequest().authenticated();

		http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
	}
}