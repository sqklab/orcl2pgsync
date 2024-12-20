package com.lguplus.fleta.domain.service.synchronizer;

import com.lguplus.fleta.domain.dto.event.SyncBroadcastEventData;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.ports.repository.SyncRequestRepository;
import com.lguplus.fleta.ports.service.SynchronizerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
@Configuration
public class SynchronizerEventListener {

	private final SynchronizerService synchronizerService;

	private final SyncRequestRepository syncRequestRepository;

	public SynchronizerEventListener(SynchronizerService synchronizerService,
									 SyncRequestRepository syncRequestRepository) {
		this.synchronizerService = synchronizerService;
		this.syncRequestRepository = syncRequestRepository;
	}

	/**
	 * This is synchronizerSink-in-0 in application-local.yml
	 * subscribe from synchronizer-out-0
	 *
	 * @return Consumer
	 */
	@Bean
	public Consumer<SyncBroadcastEventData> synchronizerSink() {
		return message -> {
			log.info("At synchronizerSink. Received message {}", message);
			handleMessage(message, synchronizerService);
		};
	}

	private void handleMessage(SyncBroadcastEventData eventData, SynchronizerService synchronizerService) {
		if (Objects.isNull(eventData) || eventData.inValid()) {
			log.error("An error occurred while handling broadcast message. Event data is invalid");
			return;
		}
		final String kafkaTopic = eventData.getTopic();
		SyncRequestEntity synchronizer = syncRequestRepository.findByTopicName(kafkaTopic);
		if (Objects.isNull(synchronizer)) {
			log.warn("There is no synchronizer found for given kafka topic {}", kafkaTopic);
			return;
		}

		switch (eventData.getAction()) {
			case START:
				synchronizerService.startSynchronizer(kafkaTopic, eventData.getSynchronizerName(), 1, false);
				synchronizerService.addToMapDataSource(synchronizer);
				break;
			case STOP:
				synchronizerService.doStop(kafkaTopic, eventData.getSynchronizerName(), false);
				break;
			default:
				log.warn("Operation not support");
		}
	}
}
