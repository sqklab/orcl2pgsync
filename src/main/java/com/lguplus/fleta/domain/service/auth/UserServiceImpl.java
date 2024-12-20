package com.lguplus.fleta.domain.service.auth;

import com.lguplus.fleta.domain.model.UserEntity;
import com.lguplus.fleta.ports.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.ArrayList;

@Service("UserServiceImpl")
public class UserServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserEntity dbSyncUser = userRepository.findByUsername(username).orElseThrow(() -> new ResourceAccessException("User not found"));

		return new User(dbSyncUser.getUsername(), dbSyncUser.getPassword(), new ArrayList<>());
	}
}