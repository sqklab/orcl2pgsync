package com.lguplus.fleta.domain.service.convertor;

import com.lguplus.fleta.adapters.persistence.exception.InvalidSyncMessageRequestException;
import com.lguplus.fleta.domain.dto.DbSyncOperation;
import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import com.lguplus.fleta.domain.dto.command.TaskExecuteCommand;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * https://debezium.io/documentation/reference/1.8/connectors/postgresql.html#postgresql-data-types
 */
@Slf4j
public abstract class DebeziumConverterToDB implements IDebeziumConverter {
	final String indent = log.isDebugEnabled() ? "\n\t" : "";
	protected final DebeziumDataReceiver receiver;

	public DebeziumConverterToDB(DebeziumDataReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public String getSqlRedo(SyncRequestMessage syncRequestMessage, TaskExecuteCommand command) throws InvalidSyncMessageRequestException {
		clearDebeziumUnavailableValue(syncRequestMessage);
		DbSyncOperation operation = DbSyncOperation.valueOf(syncRequestMessage.getPayload().getOperation());
		final String sqlRedo;
		switch (operation) {
			case c:
				sqlRedo = createInsertSqlRedo(syncRequestMessage, command);
				break;
			case u:
				sqlRedo = createUpdateSqlRedo(syncRequestMessage, command);
				break;
			case d:
				sqlRedo = createDeleteSqlRedo(syncRequestMessage, command);
				break;
			case t:
				if (!command.isEnableTruncate()) {
					throw new InvalidSyncMessageRequestException("Invalid operation " + operation);
				}
				sqlRedo = createTruncateSqlRedo(command);
				break;
			default:
				throw new InvalidSyncMessageRequestException("Invalid operation " + operation);
		}
		return sqlRedo;
	}

	private void clearDebeziumUnavailableValue(SyncRequestMessage syncRequestMessage) {
		Map<String, Object> before = syncRequestMessage.getPayload().getBefore();
		Map<String, Object> after = syncRequestMessage.getPayload().getAfter();
		if (null != before) {
			before.entrySet().removeIf(entry -> __debezium_unavailable_value.equals(entry.getValue()));
		}
		if (null != after) {
			after.entrySet().removeIf(entry -> __debezium_unavailable_value.equals(entry.getValue()));
		}
	}

	protected abstract String createInsertSqlRedo(SyncRequestMessage syncRequestMessage, TaskExecuteCommand command);

	protected abstract String createUpdateSqlRedo(SyncRequestMessage syncRequestMessage, TaskExecuteCommand command);

	protected abstract String createDeleteSqlRedo(SyncRequestMessage syncRequestMessage, TaskExecuteCommand command);

	protected abstract String createTruncateSqlRedo(TaskExecuteCommand command) throws InvalidSyncMessageRequestException;
}
