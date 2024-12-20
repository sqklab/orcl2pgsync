package com.lguplus.fleta.ports.service;

import com.lguplus.fleta.domain.dto.SyncErrorCountOperationsDto;
import com.lguplus.fleta.domain.dto.Synchronizer;
import com.lguplus.fleta.domain.dto.ui.SyncErrorDto;
import com.lguplus.fleta.domain.service.exception.ErrorNotFoundException;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

public interface SynchronizerDlqService {

	SyncErrorDto findAllSyncErrorsByTopicName(String topicName, String errorState, Pageable pageable, String kidOfErrorType, LocalDateTime from, LocalDateTime to, List<String> operationState);

	int resolveAllErrorsByTopic(String topicName);

	int resolveAllErrorsByErrorIds(List<Integer> errorIds) throws ErrorNotFoundException;

	void handleError(Consumer<Long, String> message);

	void deleteAllByTopic(String topicName);

	ByteArrayInputStream exportPrimaryKeys(String topicName, LocalDateTime dateFrom, LocalDateTime dateTo, String errorState,  String errorType, List<String> operationState);

	void deleteAllByErrorIds(List<Long> ids);

	int retryToSolveErrorsByErrorIds(List<Long> ids, String topic);

	int retryToSolveErrorByTopicName(String topicName);

	SyncErrorCountOperationsDto countOperations(String topic, List<Synchronizer.ErrorState> states);

	int deleteBeforeTime(LocalDateTime time);
}
