package com.lguplus.fleta.domain.service.synchronizer;

import ch.qos.logback.classic.Logger;
import com.lguplus.fleta.adapters.messagebroker.SynchronizerHandler;
import com.lguplus.fleta.adapters.messagebroker.SynchronizerTaskExecutor;
import com.lguplus.fleta.config.context.DbSyncContext;
import com.lguplus.fleta.domain.dto.ErrorMessage;
import com.lguplus.fleta.domain.dto.ErrorType;
import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import com.lguplus.fleta.domain.service.constant.Constants;
import com.lguplus.fleta.domain.service.constant.DivisionType;
import com.lguplus.fleta.domain.service.exception.ExceptionHelper;
import com.lguplus.fleta.domain.service.exception.InvalidTaskCreationException;
import com.lguplus.fleta.ports.service.LoggerManager;
import com.lguplus.fleta.ports.service.MessageCollectorService;

import java.util.Date;
import java.util.List;

public abstract class DefaultSynchronizerHandler implements SynchronizerHandler {
	protected final Logger logger;
	protected final String kafkaTopic;
	protected final MessageCollectorService messageCollectorService;
	protected final SynchronizerReportService synchronizerReportService;
	protected final SynchronizerTaskExecutor batchTaskExecutor;
	protected final SynchronizerTaskExecutor simpleTaskExecutor;
	protected final DivisionType division;

	public DefaultSynchronizerHandler(DivisionType division, final String kafkaTopic) throws InvalidTaskCreationException {
		this.kafkaTopic = kafkaTopic;
		this.synchronizerReportService = DbSyncContext.getBean(SynchronizerReportService.class);
		this.messageCollectorService = DbSyncContext.getBean(MessageCollectorService.class);
		this.batchTaskExecutor = DbSyncContext.getBean(SynchronizerTaskExecutorFactory.class).createForBatchSync(kafkaTopic);
		this.simpleTaskExecutor = DbSyncContext.getBean(SynchronizerTaskExecutorFactory.class).createForSimpleSync(kafkaTopic);
		LoggerManager loggerManager = DbSyncContext.getBean(LoggerManager.class);
		this.logger = loggerManager.createLogger(kafkaTopic);
		this.division = division;
	}

	@Override
	public abstract void handle(List<SyncRequestMessage> messages);

	protected void handleRequest(SyncRequestMessage message) {
		try {
			simpleTaskExecutor.execute(List.of(message));
		} catch (Exception ex) {

			String errorMessage = ex.getMessage();
			ErrorType errorType = ExceptionHelper.getCorrespondingErrorType(ex);
			Date dateTime = new Date();

			logger.error("[{}] - {} - {}\n\tKafka Topic: {}\n\tSQL Query: {}", division.getDivisionStr(), errorType, errorMessage, kafkaTopic, message.getSqlRedo());

			ErrorMessage newMessage = new ErrorMessage(message)
					.setTopicName(kafkaTopic)
					.setSyncMessage(message.toJson())
					.setSqlRedo(message.getSqlRedo())
					.setErrorMessage(errorMessage)
					.setErrorType(errorType)
					.setErrorTime(dateTime);
			synchronizerReportService.report(Constants.KAFKA_ERROR_TOPIC_NAME, newMessage);
		} finally {
			long completedTime = System.currentTimeMillis();
			messageCollectorService.saveMessages(kafkaTopic, List.of(message), completedTime);
		}
	}
}
