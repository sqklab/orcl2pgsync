package com.lguplus.fleta.domain.service.operation;

import com.lguplus.fleta.ports.service.operation.OperationBroadcastPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.function.Consumer;


@Configuration
public class OperationEventListener {
	private static final Logger log = LoggerFactory.getLogger(OperationEventListener.class);
	OperationManager operationManager;

	public OperationEventListener(OperationManager operationManager) {
		this.operationManager = operationManager;
	}

	/**
	 * This is operationSink-in-0 in application-local.yml / DbSyncServiceConfig
	 * subscribe from operation-out-0
	 *
	 * @return Consumer
	 */
	@Bean
	public Consumer<OperationBroadcastPublisher.BroadcastEventData> operationSink() {
		return eventData -> {
			log.info("At operationSink. Received message {}", eventData);
			if (Objects.isNull(eventData) || eventData.inValid()) {
				log.error("Error when handle broadcast message from operationSink. Event data is invalid");
				return;
			}
			operationManager.doCancel(eventData.getSession(), eventData.getWhere(), eventData.getTable());
		};
	}

}
