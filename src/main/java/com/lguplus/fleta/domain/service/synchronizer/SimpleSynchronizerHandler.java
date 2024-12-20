package com.lguplus.fleta.domain.service.synchronizer;

import com.google.gson.Gson;
import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import com.lguplus.fleta.domain.service.constant.DivisionType;
import com.lguplus.fleta.domain.service.exception.InvalidTaskCreationException;

import java.util.Date;
import java.util.List;

public class SimpleSynchronizerHandler extends DefaultSynchronizerHandler {
	public SimpleSynchronizerHandler(DivisionType division, String kafkaTopic) throws InvalidTaskCreationException {
		super(division, kafkaTopic);
	}

	@Override
	public void handle(List<SyncRequestMessage> messages) {
		if (messages.isEmpty()) {
			return;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("** Handling message topic: {}, at {}. JSON message: \n\t{}", kafkaTopic, new Date(), new Gson().toJson(messages));
		} else {
			logger.info("** Handling {} message topic: {}, at {}.", messages.size(), kafkaTopic, new Date());
		}
		for (SyncRequestMessage message : messages) {
			this.handleRequest(message);
		}
	}
}
