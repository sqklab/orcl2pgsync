package com.lguplus.fleta.adapters.messagebroker;

import com.lguplus.fleta.domain.dto.SyncRequestMessage;

import java.util.List;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Sep 2021
 */
public class DataSyncMessageConsumer implements MessageConsumer<SyncRequestMessage> {

	private final ConsumerProperties properties;

	private final SynchronizerHandler handler;

	public DataSyncMessageConsumer(ConsumerProperties properties, SynchronizerHandler handler) {
		this.properties = properties;
		this.handler = handler;
	}

	@Override
	public ConsumerProperties getConsumerProperties() {
		return properties;
	}

	@Override
	public void consume(List<SyncRequestMessage> messages) {
		handler.handle(messages);
	}
}
