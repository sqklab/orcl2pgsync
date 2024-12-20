package com.lguplus.fleta.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {
	private Integer id;
	@NotBlank
	private String username;
	@NotBlank
	private String password;
	private String email;
	private Integer status;

	public UserDto(@NotBlank String username, @NotBlank String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public String toString() {
		return "UserDto{" +
				"id=" + id +
				", username='" + username + '\'' +
				", password='" + "not show here" + '\'' +
				", email='" + email + '\'' +
				", status=" + status +
				'}';
	}
}
