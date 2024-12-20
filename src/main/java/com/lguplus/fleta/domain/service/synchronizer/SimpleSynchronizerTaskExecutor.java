package com.lguplus.fleta.domain.service.synchronizer;

import com.google.gson.Gson;
import com.lguplus.fleta.adapters.persistence.exception.InvalidSyncMessageRequestException;
import com.lguplus.fleta.config.aop.LogExecutionTime;
import com.lguplus.fleta.domain.dto.DbSyncOperation;
import com.lguplus.fleta.domain.dto.ErrorMessage;
import com.lguplus.fleta.domain.dto.ErrorType;
import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import com.lguplus.fleta.domain.dto.command.TaskExecuteCommand;
import com.lguplus.fleta.domain.service.constant.Constants;
import com.lguplus.fleta.domain.service.convertor.DebeziumMessageCleaner;
import com.lguplus.fleta.domain.service.exception.DatasourceNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class SimpleSynchronizerTaskExecutor extends DefaultSynchronizerTaskExecutor {

	public SimpleSynchronizerTaskExecutor(TaskExecuteCommand command) {
		super(command);
	}

	@Override
	@LogExecutionTime
	public void executeMessages(List<SyncRequestMessage> messages) throws DuplicateKeyException, SQLException, InvalidSyncMessageRequestException, DatasourceNotFoundException {
		if (messages == null || messages.size() != 1) {
			throw new InvalidSyncMessageRequestException(String.format("SimpleTaskExecutor should only receive one message, but received %s", messages == null ? 0 : messages.size()));
		}
		SyncRequestMessage message = messages.get(0);

		String datasourceName = command.getTargetDatabase();
		DebeziumMessageCleaner.cleanPayload(message);

		try (Connection connection = dataSourceService.findConnectionByServerName(datasourceName)) {
			if (Objects.isNull(connection)) {
				Set<String> availableDataSources = dataSourceService.findAllAvailableDataSources();
				throw new DatasourceNotFoundException(String.format("The datasource %s not found. All available datasource: %s", datasourceName, new Gson().toJson(availableDataSources)));
			}
			this.processDebeziumMessage(message, !CHANGE_INSERT_ON_FAILURE_UPDATE);

			try (PreparedStatement stmt = connection.prepareStatement(message.getSqlRedo())) {
				int affectedCount = stmt.executeUpdate();
				if (affectedCount == 0) {
					this.sendNotExecutedToDlq(message);
					this.changeInsertOnFailureUpdate(message, connection);
				}
			}

		} catch (DataAccessException e) {
			if (e.getCause() instanceof SQLException) {
				throw (SQLException) e.getCause();
			}
			throw e;
		}
	}

	private void processDebeziumMessage(SyncRequestMessage requestMessage, boolean changeInsertOnFailureUpdate) throws InvalidSyncMessageRequestException, SQLException {
		DebeziumMessageCleaner.cleanPayload(requestMessage);
		if (changeInsertOnFailureUpdate) {
			requestMessage.getPayload().setDebeOperation(String.valueOf(DbSyncOperation.c));
		}

		final String sqlRedo = datatypeConverter.getSqlRedo(requestMessage, command);

		logger.info("** [{}] DEBEZIUM SQL Query {}: \n\t{}\n\tCommit SCN: {}, Snapshot SCN: {}, SCN: {}", command.getDivision(), changeInsertOnFailureUpdate ? "(CHANGED INSERT ON FAILURE UPDATE)" : "", sqlRedo, requestMessage.getCommitScn(), requestMessage.getProcessedTimestamp(), requestMessage.getScn());
		requestMessage.addSqlRedo(sqlRedo);
	}

	private void sendNotExecutedToDlq(SyncRequestMessage unSyncMessage) {
		if (unSyncMessage.isTruncateOperation()) {
			return;
		}
		ErrorMessage errorMessage = new ErrorMessage(unSyncMessage)
				.setTopicName(command.getTopicName())
				.setSyncMessage(unSyncMessage.toJson())
				.setSqlRedo(unSyncMessage.getSqlRedo())
				.setErrorMessage(String.format("NOT_EXECUTABLE SQL Redo: %s", unSyncMessage.getSqlRedo()))
				.setErrorType(ErrorType.NOT_EXECUTABLE)
				.setErrorTime(new Date());
		synchronizerReportService.report(Constants.KAFKA_ERROR_TOPIC_NAME, errorMessage);
	}

	private void changeInsertOnFailureUpdate(SyncRequestMessage message, Connection connection)
			throws SQLException, InvalidSyncMessageRequestException {
		if (!command.isChangeInsertOnFailureUpdate() || command.hasNoPrimaryKeys() || !message.isUpdateOperation()) {
			return;
		}
		processDebeziumMessage(message, CHANGE_INSERT_ON_FAILURE_UPDATE);
		try (PreparedStatement stmt = connection.prepareStatement(message.getSqlRedo())) {
			stmt.executeUpdate();
		}
	}
}
