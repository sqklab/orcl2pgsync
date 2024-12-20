package com.lguplus.fleta.domain.service.synchronizer;

import com.lguplus.fleta.adapters.messagebroker.SynchronizerHandler;
import com.lguplus.fleta.domain.service.constant.DivisionType;
import com.lguplus.fleta.domain.service.exception.InvalidTaskCreationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Dec 2021
 */
@Component
@RequiredArgsConstructor
public final class SynchronizerHandlerFactory {
	public SynchronizerHandler create(DivisionType division, final String kafkaTopic, final boolean isBatch) throws InvalidTaskCreationException {
		return isBatch
				? new BatchSynchronizerHandler(division, kafkaTopic)
				: new SimpleSynchronizerHandler(division, kafkaTopic);
	}
}
