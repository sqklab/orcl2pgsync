package com.lguplus.fleta.domain.service.exception;

public class InvalidKafkaOffsetTimestampException extends Exception {

	public InvalidKafkaOffsetTimestampException(String message) {
		super(message);
	}
}
