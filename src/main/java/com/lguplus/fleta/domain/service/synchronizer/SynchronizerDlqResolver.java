package com.lguplus.fleta.domain.service.synchronizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lguplus.fleta.adapters.messagebroker.SynchronizerTaskExecutor;
import com.lguplus.fleta.config.context.DbSyncContext;
import com.lguplus.fleta.domain.dto.ErrorType;
import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import com.lguplus.fleta.domain.dto.Synchronizer;
import com.lguplus.fleta.domain.model.SyncErrorEntity;
import com.lguplus.fleta.domain.service.exception.ConnectionTimeoutException;
import com.lguplus.fleta.domain.service.exception.ExceptionHelper;
import com.lguplus.fleta.domain.service.exception.InvalidTaskCreationException;
import com.lguplus.fleta.domain.service.mapper.ObjectMapperFactory;
import com.lguplus.fleta.domain.util.DateUtils;
import com.lguplus.fleta.ports.repository.SyncErrorRepository;
import com.lguplus.fleta.ports.service.LoggerManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.BatchUpdateException;
import java.sql.SQLTransientConnectionException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang3.exception.ExceptionUtils.indexOfThrowable;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class SynchronizerDlqResolver {
	private static final String RETRY_LOG_FILENAME = "dlq-resolver";
	final SyncErrorRepository errorRepository;

	private final ObjectMapper objectMapper;

	Logger logger;

	public SynchronizerDlqResolver(SyncErrorRepository errorRepository,
								   LoggerManager loggerManager) {
		this.errorRepository = errorRepository;
		this.objectMapper = ObjectMapperFactory.getInstance().getObjectMapper();
		this.logger = loggerManager.getLogger(RETRY_LOG_FILENAME);
	}

	SyncRequestMessage getSynRequestMessage(String message) {
		try {
			return objectMapper.readValue(message, SyncRequestMessage.class);
		} catch (Exception ex) {
			return null;
		}
	}

	public void resolve(List<SyncErrorEntity> errors, String kafkaTopic) {
		if (null == errors || errors.isEmpty()) return;
		List<SyncRequestMessage> syncRequests = new LinkedList<>();
		for (SyncErrorEntity syncError : errors) {
			SyncRequestMessage message = validate(syncError);
			if (Synchronizer.ErrorState.RESOLVED.equals(syncError.getState())) {
				continue;
			}
			syncRequests.add(message);
		}

		// Using batch for executing for given a list of sync messages
		logger.info("Prepare retry {} errors", syncRequests.size());
		SynchronizerTaskExecutor taskExecutor;
		try {
			taskExecutor = DbSyncContext.getBean(SynchronizerTaskExecutorFactory.class).createForDlq(kafkaTopic, true, true, false, true);
		} catch (InvalidTaskCreationException e) {
			throw new IllegalArgumentException(e);
		}

		try {
			taskExecutor.execute(syncRequests);
			updateErrors(errors, kafkaTopic);
		} catch (Exception ex) {
			if (indexOfThrowable(ex, ConnectionTimeoutException.class) != -1 ||
					indexOfThrowable(ex, SQLTransientConnectionException.class) != -1) {
				logger.warn("Timeout exception occurred during executing batch ({} items) for topic {}, let wait for 15 seconds " +
						"before send messages to top of Redis pipeline", syncRequests.size(), kafkaTopic);

			} else if (indexOfThrowable(ex, BatchUpdateException.class) != -1) {
				logger.warn("The BatchUpdateException threw when an error occurs during a batch update operation " +
						"for {} message(s) for topic {}, let wait for 30 seconds before try to execute again.", syncRequests.size(), kafkaTopic);
			} else {
				logger.error(ex.getMessage(), ex);
			}
			handleException(errors, ex, kafkaTopic);
		}
	}

	private SyncRequestMessage validate(SyncErrorEntity syncError) {
		SyncRequestMessage syncRequest = getSynRequestMessage(syncError.getSyncMessage());

		if (null == syncRequest || null == syncRequest.getPayload() || null == syncRequest.getOperation()) {
			logger.warn("Skip retry invalid error info of kafka topic: {} at {}", syncError.getTopicName(), new Date());
			syncError.setUpdatedAt(DateUtils.getDateTime());
			syncError.setState(Synchronizer.ErrorState.RESOLVED);
		}
		return syncRequest;
	}

	private void handleException(List<SyncErrorEntity> errors, Throwable ex, String topic) {
		logger.info("Error occurred during retry {} errors of topic {}. Saving them to database with ERROR state", errors.size(), topic);
		String errorMessage = ex.getMessage();
		ErrorType errorType = ExceptionHelper.getCorrespondingErrorType(ex);

		for (SyncErrorEntity syncError : errors) {
			if (Synchronizer.ErrorState.RESOLVED.equals(syncError.getState())) {
				continue;
			}
			syncError.setState(Synchronizer.ErrorState.ERROR);
			syncError.setErrorType(errorType);
			syncError.setErrorMessage(errorMessage);
			syncError.setUpdatedAt(DateUtils.getDateTime());
		}
		this.errorRepository.saveAllAndFlush(errors);
	}

	private void updateErrors(List<SyncErrorEntity> errors, String topic) {
		logger.info("Update {} errors state to RESOLVED of topic {} after retry done", errors.size(), topic);
		for (SyncErrorEntity syncError : errors) {
			if (Synchronizer.ErrorState.RESOLVED.equals(syncError.getState())) {
				continue;
			}
			syncError.setState(Synchronizer.ErrorState.RESOLVED);
			syncError.setUpdatedAt(DateUtils.getDateTime());
		}
		this.errorRepository.saveAllAndFlush(errors);
	}
}
