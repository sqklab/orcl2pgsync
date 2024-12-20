package com.lguplus.fleta.domain.dto;

import lombok.Getter;

@Getter
public enum DataSourceState {
	ACTIVE(0), DISCONNECTED(1), INACTIVE(2), IN_USE(3), PENDING(4);

	private final int state;

	DataSourceState(int state) {
		this.state = state;
	}
}
