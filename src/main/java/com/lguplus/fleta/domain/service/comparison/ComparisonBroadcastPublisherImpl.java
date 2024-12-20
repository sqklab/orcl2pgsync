package com.lguplus.fleta.domain.service.comparison;

import com.lguplus.fleta.domain.dto.event.SyncComparisonBroadcastEventData;
import com.lguplus.fleta.ports.service.comparison.ComparisonBroadcastPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;

@Slf4j
public class ComparisonBroadcastPublisherImpl implements ComparisonBroadcastPublisher {

	private static final String COMPARISON_BROADCAST_GROUP = "syncComparison-out-0";

	private final StreamBridge streamBridge;

	public ComparisonBroadcastPublisherImpl(StreamBridge streamBridge) {
		this.streamBridge = streamBridge;
	}

	@Override
	public void broadcast(SyncComparisonBroadcastEventData eventData) {
		log.info("** Broadcast event time={}, action={} to group {}",
				eventData.getTime(), eventData.getAction(), COMPARISON_BROADCAST_GROUP);
		streamBridge.send(COMPARISON_BROADCAST_GROUP, eventData);
	}
}
