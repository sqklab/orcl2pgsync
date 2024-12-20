package com.lguplus.fleta.domain.service.exception;


public class DatasourceNotFoundException extends Exception {

	public DatasourceNotFoundException(String message) {
		super(message);
	}

	public DatasourceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
