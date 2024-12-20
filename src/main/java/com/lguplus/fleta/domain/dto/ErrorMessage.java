package com.lguplus.fleta.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMessage extends SyncRequestMessage {

	private Long id;

	private String topicName;

	private String syncMessage;

	private String sqlRedo;

	private String errorMessage;

	private ErrorType errorType;

	private Date errorTime;

	// Additional fields
	private String errorEnvironment;

	private String errorVersion;

	public ErrorMessage(SyncRequestMessage message) {
		super(message);
	}

	public ErrorMessage setId(Long id) {
		this.id = id;
		return this;
	}

	public ErrorMessage setTopicName(String topicName) {
		this.topicName = topicName;
		return this;
	}

	public ErrorMessage setSyncMessage(String syncMessage) {
		this.syncMessage = syncMessage;
		return this;
	}

	public ErrorMessage setSqlRedo(String sqlRedo) {
		this.sqlRedo = sqlRedo;
		return this;
	}

	public ErrorMessage setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
		return this;
	}

	public ErrorMessage setErrorType(ErrorType errorType) {
		this.errorType = errorType;
		return this;
	}

	public ErrorMessage setErrorTime(Date errorTime) {
		this.errorTime = errorTime;
		return this;
	}
}
