package com.lguplus.fleta.domain.service.datasource;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.lguplus.fleta.config.context.DbSyncContext;
import com.lguplus.fleta.domain.dto.ConnectionEvent;
import com.lguplus.fleta.ports.service.LostConnectionPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedMap;

public class DataSourceHealthCheckTask implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(DataSourceHealthCheckTask.class);

	private final LostConnectionPublisher eventPublisher;

	private final HealthCheckRegistry healthCheckRegistry;

	private final String serverName;

	private Thread thread;

	private boolean stopped;

	private int aliveCount;

	DataSourceHealthCheckTask(String serverName, HealthCheckRegistry healthCheckRegistry) {
		this.eventPublisher = DbSyncContext.getBean(LostConnectionPublisher.class);
		this.healthCheckRegistry = healthCheckRegistry;
		this.serverName = serverName;
	}

	@Override
	public void run() {
		boolean oldState = true;
		boolean currentState;

		while (Thread.currentThread().isAlive() && !stopped) {
			healthCheckRegistry.register(serverName, new HealthCheck() {
				@Override
				protected Result check() {
					return null;
				}
			});
			SortedMap<String, HealthCheck.Result> resultSortedMap = healthCheckRegistry.runHealthChecks();
			HealthCheck.Result res = resultSortedMap.get(resultSortedMap.firstKey());
			currentState = res.isHealthy();

			if (!currentState && oldState) {
				ConnectionEvent event = new ConnectionEvent(this, serverName, ConnectionEvent.ConnectionStatus.DISCONNECTED);
				eventPublisher.publishEvent(event);
				oldState = false;
				aliveCount = 0;
				logger.info("*** Health-check for server << {} >>: {}", serverName, event.getConnectionStatus());
			} else if (!currentState) {
				aliveCount = 0;
				logger.info("*** Health-check for server << {} >>: aliveCount: {}", serverName, aliveCount);
			} else if (!oldState && aliveCount < 3) {
				aliveCount++;
				logger.info("*** Health-check for server << {} >>: aliveCount: {}", serverName, aliveCount);
			} else if (!oldState) {
				ConnectionEvent event = new ConnectionEvent(this, serverName, ConnectionEvent.ConnectionStatus.CONNECTED);
				eventPublisher.publishEvent(event);
				oldState = true;
				logger.info("*** Health-check for server << {} >>: {}", serverName, event.getConnectionStatus());
			}

			try {
				Thread.sleep(10000);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this, serverName);
			thread.start();
		}
	}

	public void stop() {
		this.stopped = true;
	}
}
