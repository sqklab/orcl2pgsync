package com.lguplus.fleta.config.initializer;

import com.lguplus.fleta.domain.dto.UserDto;
import com.lguplus.fleta.ports.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DefaultUserInitializer implements ApplicationRunner {

	private final AuthService authService;

	@Value("${app.default-user.username:}")
	public String username;

	@Value("${app.default-user.password:}")
	public String password;

	public DefaultUserInitializer(AuthService authService) {
		this.authService = authService;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
			log.warn("There is no default user");
		} else {
			authService.createDefaultUser(new UserDto(username, password));
		}
	}
}