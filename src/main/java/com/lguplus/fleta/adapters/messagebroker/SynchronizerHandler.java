package com.lguplus.fleta.adapters.messagebroker;

import com.lguplus.fleta.domain.dto.SyncRequestMessage;

import java.util.List;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Sep 2021
 */
public interface SynchronizerHandler {

	/**
	 * CudEvent Handler a list of message
	 *
	 * @param messages message
	 */
	void handle(List<SyncRequestMessage> messages);

	/**
	 * Start consumer
	 */
	default void start() {

	}

	/**
	 * Stop / interrupt the consumer by using Poison Pill technique
	 *
	 * @param callback
	 */
	default void shutdown(final Runnable callback) {
		//TODO is it necessary ?
	}
}
