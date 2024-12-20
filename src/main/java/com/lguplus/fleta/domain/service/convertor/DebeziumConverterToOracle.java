package com.lguplus.fleta.domain.service.convertor;

import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import com.lguplus.fleta.domain.dto.command.TaskExecuteCommand;
import lombok.extern.slf4j.Slf4j;

/**
 * https://debezium.io/documentation/reference/1.8/connectors/postgresql.html#postgresql-data-types
 * This class build sql to update data in oracle database
 */
@Slf4j
public class DebeziumConverterToOracle extends DebeziumConverterToDB {

	private static final String MERGE_INTO_ALIAS = "x";

	public DebeziumConverterToOracle(DebeziumDataReceiver receiver) {
		super(receiver);
	}

	private static void debugLogging(TaskExecuteCommand command, DebeziumPayloadValues debeziumPayloadValues, String insertSql) {
		if (log.isDebugEnabled()) {
			log.debug("** [{}] SQL Query: \n\t{}", command.getDivision(), insertSql);
			log.debug("\t-> SQL Params: {}", debeziumPayloadValues);
		}
	}

	@Override
	public String createInsertSqlRedo(SyncRequestMessage syncRequestMessage, TaskExecuteCommand command) {
		DebeziumPayloadValues payload = new DebeziumPayloadValues(syncRequestMessage, receiver, command.listPrimaryKeys(), command.listUniqueKeys(), command.isAllColumnConditionsOnUpdate());

		final String insertSql;
		if (command.isUpsert() && payload.isConstraint()) {
			insertSql = new StringBuilder()
					.append("MERGE INTO ")
					.append(command.getTargetTableFullName().toUpperCase() + " " + MERGE_INTO_ALIAS)
					.append(" " + indent + "USING (SELECT 1 FROM dual) i " + indent + "ON (")
					.append(payload.sqlBuild().getMergeIntoOnConditionSqlPart(MERGE_INTO_ALIAS))
					.append(") " + indent + "WHEN MATCHED THEN UPDATE SET ")
					.append(payload.sqlBuild().getUpdateSetSqlPart())
					.append(" " + indent + "WHERE ")
					.append(payload.sqlBuild().getMergeIntoWhenMatchThenUpdateWhereSqlPart())
					.append(" " + indent + "WHEN NOT MATCHED THEN INSERT (")
					.append(payload.getAllColumnNameStrList())
					.append(") " + indent + "VALUES (")
					.append(payload.sqlBuild().getInsertValuesPart())
					.append(")")
					.toString();
		} else {
			insertSql = new StringBuilder()
					.append("INSERT INTO ")
					.append(command.getTargetTableFullName().toUpperCase()) // SCHEMA.TABLE_NAME
					.append(" (")
					.append(payload.getAllColumnNameStrList().toUpperCase()) // (COL1, COL2, COL3)
					.append(") " + indent + "VALUES (")
					.append(payload.sqlBuild().getInsertValuesPart())
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
				.append("UPDATE ")
				.append(command.getTargetTableFullName().toUpperCase()) // SCHEMA.TABLE_NAME
				.append(" " + indent + "SET ")
				.append(payload.sqlBuild().getUpdateSetSqlPart()) // col1=?, col2=?
				.append(" " + indent + "WHERE ")
				.append(payload.sqlBuild().getWhereSqlPart()) // col3=?
				.toString();

		debugLogging(command, payload, updateSql);
		return updateSql;
	}

	@Override
	protected String createDeleteSqlRedo(SyncRequestMessage message, TaskExecuteCommand command) {
		DebeziumPayloadValues payload = new DebeziumPayloadValues(message, receiver, command.listPrimaryKeys(), command.listUniqueKeys(), command.isAllColumnConditionsOnUpdate());

		String deleteSql = new StringBuilder()
				.append("DELETE FROM ")
				.append(command.getTargetTableFullName().toUpperCase()) // SCHEMA.TABLE_NAME
				.append(" " + indent + "WHERE ")
				.append(payload.sqlBuild().getWhereSqlPart()) // col3=? and col4 is null
				.toString();

		debugLogging(command, payload, deleteSql);
		return deleteSql;
	}

	@Override
	protected String createTruncateSqlRedo(TaskExecuteCommand command) {
		String sql = "TRUNCATE TABLE " +
				command.getTargetTableFullName().toUpperCase() // SCHEMA.TABLE_NAME
				;

		debugLogging(command, null, sql);
		return sql;
	}
}
