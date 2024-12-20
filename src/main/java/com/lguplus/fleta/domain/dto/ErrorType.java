package com.lguplus.fleta.domain.dto;

import java.util.List;
import java.util.stream.Collectors;

public enum ErrorType {
	GENERAL_ERROR,
	CONNECTION_TIMEOUT,
	DUPLICATED_KEY,
	SQL_ERROR,
	DATASOURCE_NOT_FOUND,
	NOT_EXECUTABLE;

	public static boolean isResolvable(ErrorType errorType) {
		return resolvableErrorTypes().contains(errorType);
	}

	public static List<String> resolvableErrorTypeNames() {
		return resolvableErrorTypes().stream().map(Enum::name).collect(Collectors.toList());
	}

	private static List<ErrorType> resolvableErrorTypes() {
		return List.of(ErrorType.CONNECTION_TIMEOUT, ErrorType.GENERAL_ERROR);
	}
}
