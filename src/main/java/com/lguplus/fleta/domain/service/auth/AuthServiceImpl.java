package com.lguplus.fleta.domain.service.auth;

import com.lguplus.fleta.domain.dto.UserDto;
import com.lguplus.fleta.domain.model.UserEntity;
import com.lguplus.fleta.ports.repository.UserRepository;
import com.lguplus.fleta.ports.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class AuthServiceImpl implements AuthService {

	private final PasswordEncoder passwordEncoder;

	private final ModelMapper modelMapper;

	private final UserRepository userRepository;

	public AuthServiceImpl(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.modelMapper = modelMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void createDefaultUser(UserDto dto) throws Exception {
		if (Objects.isNull(dto) || StringUtils.isBlank(dto.getUsername()) || StringUtils.isBlank(dto.getPassword())) {
			throw new Exception("user param is invalid");
		}
		Optional<UserEntity> userOptional = userRepository.findByUsername(dto.getUsername());
		if (userOptional.isPresent()) {
			UserEntity user = userOptional.get();
			user.setPassword(passwordEncoder.encode(dto.getPassword()));
			user.setUpdatedAt(new Date());
			log.info("Update default user {}", user.getUsername());
			userRepository.save(user);

		} else {
			registerNewUser(dto);
		}
	}

	@Override
	public UserEntity registerNewUser(UserDto dto) {
		log.info("registering... user {}", dto.getUsername());
		UserEntity user = modelMapper.map(dto, UserEntity.class);
		user.setId(null);
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setCreatedAt(new Date());
		return userRepository.save(user);
	}

	@Override
	public UserEntity changePassword(String username, String newPassword) {
		Optional<UserEntity> userOptional = userRepository.findByUsername(username);
		if (userOptional.isPresent()) {
			UserEntity user = userOptional.get();
			user.setPassword(passwordEncoder.encode(newPassword.trim()));
			user.setUpdatedAt(new Date());
			userRepository.save(user);
			return user;
		}
		return null;
	}

	@Override
	public boolean isExisted(@NotBlank String username) {
		return userRepository.existsByUsername(username);
	}
}