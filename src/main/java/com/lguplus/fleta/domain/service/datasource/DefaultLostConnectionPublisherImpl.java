package com.lguplus.fleta.domain.service.datasource;

import com.lguplus.fleta.domain.dto.ConnectionEvent;
import com.lguplus.fleta.ports.service.LostConnectionPublisher;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The {@link DefaultLostConnectionPublisherImpl} is a default implementation of {@link LostConnectionPublisher}
 * that will responsible for publish a lost connection event of all synchronizers which one is using disconnected
 * connection.
 * <p>
 * This implementation fire event by using Spring-Event. It could be changed to Kafka or Redis, ...etc
 *
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Sep 2021
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DefaultLostConnectionPublisherImpl implements LostConnectionPublisher {

	private final ApplicationEventPublisher eventPublisher;

	public DefaultLostConnectionPublisherImpl(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void publishEvent(ConnectionEvent event) {
		eventPublisher.publishEvent(event);
	}
}
