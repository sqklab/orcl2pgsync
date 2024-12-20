package com.lguplus.fleta.domain.service.datasource;

import com.lguplus.fleta.domain.dto.ConnectionEvent;
import com.lguplus.fleta.domain.dto.DataSourceState;
import com.lguplus.fleta.ports.service.DataSourceService;
import com.lguplus.fleta.ports.service.SynchronizerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DefaultLostConnectionHandler implements ApplicationListener<ConnectionEvent> {

	private final DefaultLostConnectionSyncHandler lostConnectionSyncHandler;
	private final DataSourceService dataSourceService;

	public DefaultLostConnectionHandler(DefaultLostConnectionSyncHandler lostConnectionSyncHandler,
										@Qualifier("defaultDatasourceContainer") DataSourceService dataSourceService) {
		this.lostConnectionSyncHandler = lostConnectionSyncHandler;
		this.dataSourceService = dataSourceService;
	}

	@Override
	public void onApplicationEvent(ConnectionEvent event) {
		log.info("Synchronization event {}", event);

		try {
			String ds = event.getDatasource();
			switch (event.getConnectionStatus()) {
				case DISCONNECTED:
					dataSourceService.updateDataSourceStatus(ds, DataSourceState.DISCONNECTED);

					lostConnectionSyncHandler.stopSynchronizerByDataSource(ds);
					break;
				case CONNECTED:
					dataSourceService.updateDataSourceStatus(ds, DataSourceState.ACTIVE);

					lostConnectionSyncHandler.startSynchronizerByDataSource(ds);
					break;
				default:
					log.warn("Synchronization event type is not support");
			}
		} catch (SynchronizerService.DataSourceInvalidException ex) {
			log.warn(ex.getMessage(), ex);
		}
	}
}
