package com.lguplus.fleta.domain.dto.command;

import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.domain.service.convertor.DebeziumPayloadValues;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskExecuteCommand {
	private String targetDatabase;
	private String targetSchema;
	private String targetTable;
	private String topicName;
	private String division;
	private String primaryKeys;
	private String uniqueKeys;
	private boolean isUpsert;
	private boolean isChangeInsertOnFailureUpdate;
	private boolean isAllColumnConditionsOnUpdate;

	private boolean enableTruncate;

	private int retryTime;

	private int delayInMillis;

	private static int DEFAULT_RETRY_TIME = 1;
	private static int DEFAULT_DELAY_IN_MILLIS = 3000;

	public List<String> listPrimaryKeys() {
		return StringUtils.isBlank(primaryKeys) ? null :
				Arrays.stream(primaryKeys.split(","))
						.map(String::trim)
						.map(String::toUpperCase)
						.collect(Collectors.toList());
	}

	public boolean hasNoPrimaryKeys() {
		return StringUtils.isBlank(primaryKeys);
	}

	public boolean hasPrimaryKeys() {
		return Objects.nonNull(getPrimaryKeys()) && !getPrimaryKeys().isEmpty();
	}

	public List<String> listUniqueKeys() {
		return StringUtils.isBlank(uniqueKeys) ? null :
				Arrays.stream(uniqueKeys.split(","))
						.map(String::trim)
						.map(String::toUpperCase)
						.collect(Collectors.toList());
	}

	public static TaskExecuteCommand of(SyncRequestEntity syncRequest, int retryTime, int delayInMillis) {
		TaskExecuteCommand command = new TaskExecuteCommand();
		command.targetDatabase = syncRequest.getTargetDatabase();
		command.targetSchema = syncRequest.getTargetSchema();
		command.targetTable = syncRequest.getTargetTable();
		command.topicName = syncRequest.getTopicName();
		command.division = syncRequest.getDivision();
		command.primaryKeys = syncRequest.getPrimaryKeys();
		command.uniqueKeys = syncRequest.getUniqueKeys();
		command.isUpsert = syncRequest.isUpsert();
		command.isChangeInsertOnFailureUpdate = syncRequest.isChangeInsertOnFailureUpdate();
		command.isAllColumnConditionsOnUpdate = syncRequest.isAllColumnConditionsOnUpdate();
		command.enableTruncate = syncRequest.isEnableTruncate();
		command.retryTime = retryTime;
		command.delayInMillis = delayInMillis;
		return command;
	}

	public static TaskExecuteCommand of(SyncRequestEntity syncRequest) {
		return TaskExecuteCommand.of(syncRequest, DEFAULT_RETRY_TIME, DEFAULT_DELAY_IN_MILLIS);
	}


	public String getTargetTableFullName(){
		return this.targetSchema + "." + this.targetTable;
	}

	public String getSourceTableFullName(){
		return this.targetSchema + "." + this.targetTable;
	}
}
