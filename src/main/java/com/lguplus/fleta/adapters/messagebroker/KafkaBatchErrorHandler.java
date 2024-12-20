package com.lguplus.fleta.adapters.messagebroker;

import com.lguplus.fleta.config.context.DbSyncContext;
import com.lguplus.fleta.domain.dto.ErrorMessage;
import com.lguplus.fleta.domain.dto.ErrorType;
import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import com.lguplus.fleta.domain.service.constant.Constants;
import com.lguplus.fleta.domain.service.synchronizer.SynchronizerReportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.listener.BatchErrorHandler;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;


@Slf4j
public class KafkaBatchErrorHandler implements BatchErrorHandler {

	@Override
	public void handle(Exception e, ConsumerRecords<?, ?> record) {
		log.error("Error in process with Exception {} and the record is {}", e, record);
		if (e instanceof KafkaException ||
				e instanceof RejectedExecutionException) {
			handleExecutionFailedException(e, record);
		}
	}

	private void handleExecutionFailedException(Exception e, ConsumerRecords<?, ?> records) {
		Objects.requireNonNull(records, "ConsumerRecord must not be null");

		records.forEach(record -> {
			Object value = record.value();
			if (value instanceof SyncRequestMessage) {
				SyncRequestMessage syncRequest = (SyncRequestMessage) value;

				ErrorMessage newMessage = new ErrorMessage(syncRequest)
						.setTopicName(record.topic())
						.setSyncMessage(syncRequest.toJson())
						.setSqlRedo(syncRequest.getSqlRedo())
						.setErrorMessage(e.getMessage())
						.setErrorType(ErrorType.GENERAL_ERROR)
						.setErrorTime(new Date());
				SynchronizerReportService synchronizerReportService = DbSyncContext.getBean(SynchronizerReportService.class);
				synchronizerReportService.report(Constants.KAFKA_ERROR_TOPIC_NAME, newMessage);
			}
		});
	}
}
