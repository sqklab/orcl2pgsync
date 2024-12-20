package com.lguplus.fleta.domain.service.datasource;

import com.lguplus.fleta.domain.dto.event.DatasourceBroadcastEventData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatasourceBroadcastPublisher {
	private static final String BROADCAST_GROUP = "datasource-out-0";
	private final StreamBridge streamBridge;

	public DatasourceBroadcastPublisher(StreamBridge streamBridge) {
		this.streamBridge = streamBridge;
	}

	public void broadcast(DatasourceBroadcastEventData eventData) {
		log.info("** Broadcast event serverName={}, datasource={}, action={} to group {}",
				eventData.getCurrentServerName(), eventData.getDataSource(), eventData.getAction(), BROADCAST_GROUP);
		streamBridge.send(BROADCAST_GROUP, eventData);
	}
}
