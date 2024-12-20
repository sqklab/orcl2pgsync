package com.lguplus.fleta.ports.service;

import com.lguplus.fleta.domain.model.SyncRequestEntity;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Sep 2021
 */
public interface SynchronizerService {

	/**
	 * Stop all synchronizers
	 */
	void doStop();

	/**
	 * Stop for given a kafka topic
	 *
	 * @param kafkaTopic
	 * @param syncName
	 * @param isBroadcast
	 */
	void doStop(String kafkaTopic, String syncName, boolean isBroadcast);

	/**
	 * Stop for given a kafka topic and execute a callback
	 *
	 * @param kafkaTopic  kafka topic
	 * @param callback    callback
	 * @param isBroadcast send isBroadcast to other instance
	 */
	@Deprecated(since = "0.2.8")
	default void doStop(String kafkaTopic, Runnable callback, boolean isBroadcast) {
		throw new UnsupportedOperationException("Unsupported Operation");
	}

	/**
	 * Stop for given a kafka topic and execute a callback
	 *
	 * @param kafkaTopic
	 * @param syncName
	 * @param callback
	 * @param isBroadcast
	 */
	void doStop(String kafkaTopic, String syncName, Runnable callback, boolean isBroadcast);

	/**
	 * Start all synchronizer
	 */
	void startSynchronizer() throws IOException;

	/**
	 * Start a synchronizer for given kafka topic
	 *
	 * @param kafkaTopic  kafka topic
	 * @param isBroadcast send isBroadcast to other instance
	 */
	@Deprecated(since = "0.2.8")
	default void startSynchronizer(String kafkaTopic, boolean isBroadcast) {
		throw new UnsupportedOperationException("Unsupported Operation");
	}

	/**
	 * Start a synchronizer for given kafka topic and number of concurrency (number of consumer)
	 *
	 * @param kafkaTopic  kafka topic
	 * @param concurrency thread
	 */
	@Deprecated(since = "0.2.8")
	default void startSynchronizer(String kafkaTopic, int concurrency, boolean isBroadcast) {
		throw new UnsupportedOperationException("Unsupported Operation");
	}

	/**
	 * Start a synchronizer for given kafka topic and number of concurrency (number of consumer)
	 *
	 * @param kafkaTopic
	 * @param syncName
	 * @param concurrency
	 * @param isBroadcast
	 */
	void startSynchronizer(String kafkaTopic, String syncName, int concurrency, boolean isBroadcast);

	void startSynchronizerByDataSource(String datasource) throws DataSourceInvalidException;

	void stopSynchronizerByDataSource(String datasource) throws DataSourceInvalidException;

	void startDefaultSynchronizers();

	void startDefaultErrorTracker();

	void addToMapDataSource(SyncRequestEntity synchronizer);

	/**
	 * Get current container status:
	 * 0 - UNKNOWN
	 * 1 - RUNNING
	 * 2 - STOPPED
	 * 3 - PAUSED
	 *
	 * @param kafkaTopic
	 * @return
	 */
	int getContainerStatus(String kafkaTopic);

	/**
	 * Return all available synchronizers
	 *
	 * @return {@code List<String>}
	 */
	List<String> findAllSynchronizers();

	class DataSourceInvalidException extends RuntimeException {

		public DataSourceInvalidException(String message) {
			super(message);
		}
	}
}
