package com.lguplus.fleta.domain.service.synchronizer;

import com.lguplus.fleta.adapters.messagebroker.SynchronizerTaskExecutor;
import com.lguplus.fleta.domain.dto.command.TaskExecuteCommand;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.domain.service.exception.InvalidTaskCreationException;
import com.lguplus.fleta.ports.service.SyncRequestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Dec 2021
 */
@Component
public final class SynchronizerTaskExecutorFactory {
	private final SyncRequestService syncRequestService;


	@Value("${app.executor.retry:3}")
	private int retryTime;

	@Value("${app.executor.delay:3000}")
	private int delayInMillis;

	public SynchronizerTaskExecutorFactory(SyncRequestService syncRequestService) {
		this.syncRequestService = syncRequestService;
	}

	public SynchronizerTaskExecutor createForBatchSync(final String kafkaTopic) throws InvalidTaskCreationException {
		if (Objects.isNull(kafkaTopic)) throw new InvalidTaskCreationException("kafka topic required");
		SyncRequestEntity syncRequest = syncRequestService.findByTopicName(kafkaTopic);
		if (Objects.isNull(syncRequest))
			throw new InvalidTaskCreationException(String.format("Not found SyncRequestEntity of topic %s", kafkaTopic));
		TaskExecuteCommand command = TaskExecuteCommand.of(syncRequest, retryTime, delayInMillis);
		return this.create(command, true);
	}

	public SynchronizerTaskExecutor createForSimpleSync(final String kafkaTopic) throws InvalidTaskCreationException {
		if (Objects.isNull(kafkaTopic)) throw new InvalidTaskCreationException("kafka topic required");
		SyncRequestEntity syncRequest = syncRequestService.findByTopicName(kafkaTopic);
		if (Objects.isNull(syncRequest))
			throw new InvalidTaskCreationException(String.format("Not found SyncRequestEntity of topic %s", kafkaTopic));
		TaskExecuteCommand command = TaskExecuteCommand.of(syncRequest, retryTime, delayInMillis);
		return this.create(command, false);
	}

	public SynchronizerTaskExecutor createForDlq(final String kafkaTopic, final boolean isBatch,
												 final boolean isUpsert,
												 final boolean isChangeInsertOnFailureUpdate,
												 final boolean isAllColumnConditionsOnUpdate) throws InvalidTaskCreationException {
		if (Objects.isNull(kafkaTopic)) throw new InvalidTaskCreationException("kafka topic required");
		SyncRequestEntity syncRequest = syncRequestService.findByTopicName(kafkaTopic);
		if (Objects.isNull(syncRequest))
			throw new InvalidTaskCreationException(String.format("Not found SyncRequestEntity of topic %s", kafkaTopic));
		TaskExecuteCommand command = TaskExecuteCommand.of(syncRequest, retryTime, delayInMillis);
		command.setUpsert(isUpsert);
		command.setChangeInsertOnFailureUpdate(isChangeInsertOnFailureUpdate);
		command.setAllColumnConditionsOnUpdate(isAllColumnConditionsOnUpdate);
		return this.create(command, isBatch);
	}

	private SynchronizerTaskExecutor create(TaskExecuteCommand command, boolean isBatch) {
		if (isBatch) {
			return new BatchSynchronizerTaskExecutor(command);
		} else
			return new SimpleSynchronizerTaskExecutor(command);
	}
}
