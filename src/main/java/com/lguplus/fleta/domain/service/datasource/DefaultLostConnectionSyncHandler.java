package com.lguplus.fleta.domain.service.datasource;

import com.lguplus.fleta.domain.dto.ConnectionEvent;
import com.lguplus.fleta.domain.dto.DataSourceState;
import com.lguplus.fleta.ports.service.DataSourceService;
import com.lguplus.fleta.ports.service.SynchronizerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DefaultLostConnectionSyncHandler {

	private final SynchronizerService synchronizerService;

	public DefaultLostConnectionSyncHandler(SynchronizerService synchronizerService) {
		this.synchronizerService = synchronizerService;
	}

	@Async
	public void stopSynchronizerByDataSource(String database) {
		synchronizerService.stopSynchronizerByDataSource(database);
	}

	@Async
	public void startSynchronizerByDataSource(String database) {
		synchronizerService.startSynchronizerByDataSource(database);
	}
}
