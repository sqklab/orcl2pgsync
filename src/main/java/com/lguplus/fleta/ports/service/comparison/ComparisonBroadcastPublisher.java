package com.lguplus.fleta.ports.service.comparison;

import com.lguplus.fleta.domain.dto.event.SyncComparisonBroadcastEventData;

public interface ComparisonBroadcastPublisher {
	void broadcast(SyncComparisonBroadcastEventData eventData);
}
