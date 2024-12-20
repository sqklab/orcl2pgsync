package com.lguplus.fleta.ports.service;

import com.lguplus.fleta.domain.dto.ConnectionEvent;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Sep 2021
 */
public interface LostConnectionPublisher {

	void publishEvent(ConnectionEvent event);
}
