package com.lguplus.fleta.domain.service.exception;

public class InvalidKafkaConsumerGroupStateException extends Exception {

	public InvalidKafkaConsumerGroupStateException(String message) {
		super(message);
	}

	public InvalidKafkaConsumerGroupStateException(String message, Throwable cause) {
		super(message, cause);
	}
}
