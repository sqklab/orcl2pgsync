package com.lguplus.fleta.domain.service.datasource;

import com.lguplus.fleta.domain.dto.event.DatasourceBroadcastEventData;
import com.lguplus.fleta.ports.service.DataSourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
@Configuration
public class DatasourceEventListener {
	private final DataSourceService dataSourceService;

	public DatasourceEventListener(@Qualifier("defaultDatasourceContainer") DataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}

	/**
	 * This is datasourceSink-in-0 in application-local.yml
	 * subscribe from datasourceSink-out-0
	 *
	 * @return Consumer
	 */
	@Bean
	public Consumer<DatasourceBroadcastEventData> datasourceSink() {
		return eventData -> {
			log.info("At datasourceSink. Received message {}", eventData);
			if (Objects.isNull(eventData) || eventData.inValid()) {
				log.error("Error when handle broadcast message. Event data is invalid");
				return;
			}

			switch (eventData.getAction()) {
				case ADD:
					dataSourceService.initializeDataSource(eventData.getDataSource(), false);
					return;
				case DELETE:
					if (eventData.getCurrentServerName() == null) {
						log.error("Error when received broadcast even from datasourceSink-in-0. getCurrentServerName is required. data={}", eventData);
						return;
					}
					dataSourceService.removeDataSourceFromMap(eventData.getCurrentServerName(), false);
					return;
				default:
					log.warn("Operation not support");
			}
		};
	}


}
