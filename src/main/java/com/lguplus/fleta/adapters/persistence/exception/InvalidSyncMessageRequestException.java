package com.lguplus.fleta.adapters.persistence.exception;

public class InvalidSyncMessageRequestException extends Exception {

	public InvalidSyncMessageRequestException(String errorMessage) {
		super(errorMessage);
	}

	public InvalidSyncMessageRequestException(String message, Throwable cause) {
		super(message, cause);
	}
}
