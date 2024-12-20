package com.lguplus.fleta.domain.service.operation;

import com.lguplus.fleta.ports.service.operation.OperationBroadcastPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class OperationBroadcastPublisherImpl implements OperationBroadcastPublisher {
	private static final Logger logger = LoggerFactory.getLogger(OperationBroadcastPublisherImpl.class);

	private static final String BROADCAST_GROUP = "operation-out-0";
	private final StreamBridge streamBridge;

	public OperationBroadcastPublisherImpl(StreamBridge streamBridge) {
		this.streamBridge = streamBridge;
	}

	@Override
	public void broadcast(OperationBroadcastPublisher.BroadcastEventData eventData) {
		logger.info("** Broadcast event cancel operation request session={}, where={}, table={} to group {}",
				eventData.getSession(), eventData.getWhere(), eventData.getTable(), BROADCAST_GROUP);
		streamBridge.send(BROADCAST_GROUP, eventData);
	}
}
