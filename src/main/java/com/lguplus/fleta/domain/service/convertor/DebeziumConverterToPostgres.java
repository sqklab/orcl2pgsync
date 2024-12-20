package com.lguplus.fleta.domain.service.convertor;

import com.lguplus.fleta.adapters.persistence.exception.InvalidSyncMessageRequestException;
import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import com.lguplus.fleta.domain.dto.command.TaskExecuteCommand;
import lombok.extern.slf4j.Slf4j;

/**
 * https://debezium.io/documentation/reference/1.8/connectors/postgresql.html#postgresql-data-types
 */
@Slf4j
public class DebeziumConverterToPostgres extends DebeziumConverterToDB {
	public DebeziumConverterToPostgres(DebeziumDataReceiver receiver) {
		super(receiver);
	}

	private static void debugLogging(TaskExecuteCommand command, DebeziumPayloadValues debeziumPayloadValues, String sql) {
		if (log.isDebugEnabled()) {
			log.debug("** [{}] SQL Query: \n\t{}", command.getDivision(), sql);
			log.debug("\t-> SQL Params: {}", debeziumPayloadValues);
		}
	}

	public String createInsertSqlRedo(SyncRequestMessage message, TaskExecuteCommand command) {
		DebeziumPayloadValues payload = new DebeziumPayloadValues(message, receiver, command.listPrimaryKeys(), command.listUniqueKeys(), command.isAllColumnConditionsOnUpdate());

		final String insertSql;
		if (command.isUpsert() && payload.isConstraint()) {
			insertSql = new StringBuilder()
					.append("insert into ")
					.append(command.getTargetTableFullName().toLowerCase()) // SCHEMA.TABLE_NAME
					.append(" (")
					.append(String.join(", ", payload.getAllColumnNameStrList())) // col1, col2, col3, col4
					.append(") " + indent + "values (")
					.append(payload.sqlBuild().getInsertValuesPart()) // null, v2, null, v4
					.append(") " + indent + "on conflict (")
					.append(payload.getConstraintColumnNameStrList())
					.append(") " + indent + "do update set ")
					.append(payload.sqlBuild().getUpdateSetSqlPart())
					.toString();
		} else {
			insertSql = new StringBuilder()
					.append("insert into ")
					.append(command.getTargetTableFullName().toLowerCase()) // SCHEMA.TABLE_NAME
					.append(" (")
					.append(String.join(", ", payload.getAllColumnNameStrList()).toLowerCase()) // col1, col2, col3, col4
					.append(") " + indent + "values (")
					.append(payload.sqlBuild().getInsertValuesPart()) // null, v2, null, v4
					.append(")")
					.toString();
		}

		debugLogging(command, payload, insertSql);
		return insertSql;
	}

	@Override
	protected String createUpdateSqlRedo(SyncRequestMessage message, TaskExecuteCommand command) {
		DebeziumPayloadValues payload = new DebeziumPayloadValues(message, receiver, command.listPrimaryKeys(), command.listUniqueKeys(), command.isAllColumnConditionsOnUpdate());

		String updateSql = new StringBuilder()
				.append("update " + command.getTargetTableFullName().toLowerCase()) // SCHEMA.TABLE_NAME
				.append(" " + indent + "set ")
				.append(payload.sqlBuild().getUpdateSetSqlPart()) // col3=null, col4=v4
				.append(" " + indent + "where ")
				.append(payload.sqlBuild().getWhereSqlPart()) // col1 is null and col2=v2
				.toString();

		debugLogging(command, payload, updateSql);
		return updateSql;
	}

	@Override
	protected String createDeleteSqlRedo(SyncRequestMessage message, TaskExecuteCommand command) {
		DebeziumPayloadValues payload = new DebeziumPayloadValues(message, receiver, command.listPrimaryKeys(), command.listUniqueKeys(), command.isAllColumnConditionsOnUpdate());

		String deleteSql = new StringBuilder()
				.append("delete from ")
				.append(command.getTargetTableFullName().toLowerCase()) // SCHEMA.TABLE_NAME
				.append(" " + indent + "where ")
				.append(payload.sqlBuild().getWhereSqlPart()) // col1 is null and col2=v2
				.toString();

		debugLogging(command, payload, deleteSql);
		return deleteSql;
	}

	@Override
	protected String createTruncateSqlRedo(TaskExecuteCommand command) throws InvalidSyncMessageRequestException {
		String sql = "truncate table " +
				command.getTargetTableFullName().toLowerCase() // SCHEMA.TABLE_NAME
				;

		debugLogging(command, null, sql);
		return sql;
	}
}
