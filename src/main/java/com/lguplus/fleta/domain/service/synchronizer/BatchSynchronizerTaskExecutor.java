package com.lguplus.fleta.domain.service.synchronizer;

import com.google.gson.Gson;
import com.lguplus.fleta.adapters.persistence.exception.InvalidSyncMessageRequestException;
import com.lguplus.fleta.domain.dto.*;
import com.lguplus.fleta.domain.dto.command.TaskExecuteCommand;
import com.lguplus.fleta.domain.service.constant.Constants;
import com.lguplus.fleta.domain.service.convertor.DebeziumMessageCleaner;
import com.lguplus.fleta.domain.service.exception.DatasourceNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BatchSynchronizerTaskExecutor extends DefaultSynchronizerTaskExecutor {

	public BatchSynchronizerTaskExecutor(TaskExecuteCommand command) {
		super(command);
	}

	@Override
	protected void executeMessages(List<SyncRequestMessage> messages) throws DuplicateKeyException, SQLException, InvalidSyncMessageRequestException, DatasourceNotFoundException {
		SyncRequestBatchCountDto batchCountDto = SyncRequestBatchCountDto.of(messages);

		IDataSource dataSource = dataSourceService.findDataSourceByDatabaseName(command.getTargetDatabase());
		if (Objects.isNull(dataSource)) {
			Set<String> availableDataSources = dataSourceService.findAllAvailableDataSources();
			throw new DatasourceNotFoundException(String.format("The datasource %s not found. All available datasource: %s", command.getTargetDatabase(), new Gson().toJson(availableDataSources)));
		}
		try (Connection connection = dataSourceService.findConnectionByServerName(command.getTargetDatabase())) {
			connection.setAutoCommit(false);

			// Execute some SQL statements...
			List<String> sqlQueryHolder = new ArrayList<>();
			try (Statement statement = connection.createStatement()) {
				for (SyncRequestMessage requestMessage : messages) {
					if (Objects.isNull(requestMessage) || Objects.isNull(requestMessage.getPayload())) {
						logger.info("SyncRequestMessage or Payload block IS NULL : {}", requestMessage);
						continue;
					}
					processDebeziumMessage(sqlQueryHolder, statement, requestMessage, !CHANGE_INSERT_ON_FAILURE_UPDATE);
				}

				int batchSize = messages.size();
				batchCountDto.setBatchSize(batchSize);
				logger.info("** Send a batch of {} command(s) to execute at {}", batchSize, new Date());

				int[] affectedRecords = statement.executeBatch();
				batchCountDto.setAffectedRecords(affectedRecords);
				connection.commit();

				logger.info(">>> Found {} effected row(s).", batchCountDto.getAffectedRecordsCount());
				// e.g: [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1]
				if (batchCountDto.getAffectedRecordsCount() == batchSize) {
					logger.info("\tAll succeed: {}", affectedRecords);
				} else {
					logger.info("\tPartial succeed: {}", affectedRecords);
					this.sendNotExecutedToDlq(affectedRecords, sqlQueryHolder, messages);
					int[] generatedInsertedRows = this.changeInsertOnFailureUpdate(affectedRecords, sqlQueryHolder, messages, connection, statement);
					batchCountDto.setGeneratedInsertedRecords(generatedInsertedRows);
				}
			} catch (Exception ex) {
				// Do rollback only when the connection still alive
				if (!connection.isClosed()) { // Fix bug: java.sql.SQLException: Connection is closed
					connection.rollback();
					batchCountDto.setRollback(true);
				}
				throw ex;
			} finally {
				logger.info(">>> Batch execute summary [{}] total={}, operations={}, executed={}, not_executed={}, multiple_executed={}, generated_insert={}",
						command.getDivision(), batchCountDto.getBatchSize(), batchCountDto.getOperations(), batchCountDto.getExecutedCount(), batchCountDto.getNotExecutedCount(), batchCountDto.getMultipleExecutedCount(), batchCountDto.getGeneratedInsertedCount());
			}
		}
	}

	private void sendNotExecutedToDlq(int[] affectedRecords, List<String> sqlQueryHolder, List<SyncRequestMessage> messages) {

		List<Integer> toDlqIndexes = IntStream.range(0, affectedRecords.length)
				.boxed()
				.filter(i -> affectedRecords[i] == 0)
				.filter(i -> !messages.get(i).isTruncateOperation())
				.collect(Collectors.toList());

		toDlqIndexes.forEach(i -> {
			String sqlRedo = sqlQueryHolder.get(i);
			SyncRequestMessage unSyncMessage = messages.get(i);
			ErrorMessage errorMessage = new ErrorMessage(unSyncMessage)
					.setTopicName(command.getTopicName())
					.setSyncMessage(unSyncMessage.toJson()).setSqlRedo(sqlRedo)
					.setErrorMessage(String.format("NOT_EXECUTABLE SQL Redo: %s", sqlRedo))
					.setErrorType(ErrorType.NOT_EXECUTABLE)
					.setErrorTime(new Date());
			synchronizerReportService.report(Constants.KAFKA_ERROR_TOPIC_NAME, errorMessage);
		});
	}

	private int[] changeInsertOnFailureUpdate(int[] affectedRecords, List<String> sqlQueryHolder, List<SyncRequestMessage> messages, Connection connection, Statement statement)
			throws SQLException, InvalidSyncMessageRequestException {
		if (!command.isChangeInsertOnFailureUpdate() || command.hasNoPrimaryKeys()) {
			return null;
		}
		List<Integer> toGeneratedInsertIndexes = IntStream.range(0, affectedRecords.length)
				.boxed()
				.filter(i -> affectedRecords[i] == 0)
				.filter(i -> messages.get(i).isUpdateOperation())
				.collect(Collectors.toList());

		if (toGeneratedInsertIndexes.isEmpty()) {
			return null;
		}
		statement.clearBatch();
		for (Integer i : toGeneratedInsertIndexes) {
			processDebeziumMessage(sqlQueryHolder, statement, messages.get(i), CHANGE_INSERT_ON_FAILURE_UPDATE);
		}
		int[] generatedInsertedRows = statement.executeBatch();
		connection.commit();
		return generatedInsertedRows;
	}

	private void processDebeziumMessage(List<String> sqlQueryHolder, Statement statement, SyncRequestMessage requestMessage, boolean changeInsertOnFailureUpdate) throws InvalidSyncMessageRequestException, SQLException {
		DebeziumMessageCleaner.cleanPayload(requestMessage);
		if (changeInsertOnFailureUpdate) {
			requestMessage.getPayload().setDebeOperation(String.valueOf(DbSyncOperation.c));
		}

		final String sqlRedo = datatypeConverter.getSqlRedo(requestMessage, command);

		logger.info("** [{}] DEBEZIUM SQL Query {}: \n\t{}\n\tCommit SCN: {}, Snapshot SCN: {}, SCN: {}", command.getDivision(), changeInsertOnFailureUpdate ? "(CHANGED INSERT ON FAILURE UPDATE)" : "", sqlRedo, requestMessage.getCommitScn(), requestMessage.getProcessedTimestamp(), requestMessage.getScn());

		if (StringUtils.isNotEmpty(sqlRedo)) {
			statement.addBatch(sqlRedo);
			sqlQueryHolder.add(sqlRedo);
			requestMessage.addSqlRedo(sqlRedo);
		}
	}
}
