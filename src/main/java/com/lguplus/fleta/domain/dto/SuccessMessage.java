package com.lguplus.fleta.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SuccessMessage extends SyncRequestMessage {

	private String topicName;

	private String targetSchema;

	private String targetTable;

	private String targetDatasource;

	private Date date;

	public SuccessMessage(SyncRequestMessage message) {
		super(message);
	}
}
