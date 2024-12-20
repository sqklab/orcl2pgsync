package com.lguplus.fleta.ports.service;

import com.lguplus.fleta.domain.dto.UserDto;
import com.lguplus.fleta.domain.model.UserEntity;

public interface AuthService {

	void createDefaultUser(UserDto user) throws Exception;

	UserEntity registerNewUser(UserDto user);

	UserEntity changePassword(String username, String newPassword);

	boolean isExisted(String username);
}