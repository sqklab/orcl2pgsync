package com.lguplus.fleta.domain.service.synchronizer;

import com.google.gson.Gson;
import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import com.lguplus.fleta.domain.service.constant.DivisionType;
import com.lguplus.fleta.domain.service.exception.InvalidTaskCreationException;

import java.util.Date;
import java.util.List;

public class BatchSynchronizerHandler extends DefaultSynchronizerHandler {
	public BatchSynchronizerHandler(DivisionType division, final String kafkaTopic) throws InvalidTaskCreationException {
		super(division, kafkaTopic);
	}

	@Override
	public void handle(List<SyncRequestMessage> messages) {
		if (logger.isDebugEnabled()) {
			logger.debug("** Handling message topic: {}, at {}. JSON message: \n\t{}", kafkaTopic, new Date(), new Gson().toJson(messages));
		} else {
			logger.info("** Handling {} message topic: {}, at {}.", messages.size(), kafkaTopic, new Date());
		}
		try {
			// multiple execute first > single execute next on exception
			batchTaskExecutor.execute(messages);
			long completedTime = System.currentTimeMillis();
			messageCollectorService.saveMessages(kafkaTopic, messages, completedTime);
		} catch (Exception ex) {
			logger.warn("There is an error occurred while executing a batch of sync messages.\n\t>>> " + "Causes of Errors: {}", ex.getMessage(), ex);

			for (SyncRequestMessage message : messages) {
				this.handleRequest(message);
			}
		}
	}
}
