package com.lguplus.fleta.domain.service.synchronizer;

import com.lguplus.fleta.domain.dto.event.SyncBroadcastEventData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;

@Slf4j
public class SynchronizerBroadcastPublisher {
	private final StreamBridge streamBridge;
	private final String broadcastGroup;

	public SynchronizerBroadcastPublisher(StreamBridge streamBridge, String broadcastGroup) {
		this.streamBridge = streamBridge;
		this.broadcastGroup = broadcastGroup;
	}

	public void broadcast(SyncBroadcastEventData eventData) {
		log.info("** Broadcast event topic={}, action={} to group {}",
				eventData.getTopic(), eventData.getAction(), broadcastGroup);
		streamBridge.send(broadcastGroup, eventData);
	}
}
