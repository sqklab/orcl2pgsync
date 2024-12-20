package com.lguplus.fleta.ports.service;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.lguplus.fleta.domain.dto.DataSourceInfo;
import com.lguplus.fleta.domain.dto.DataSourceState;
import com.lguplus.fleta.domain.dto.IDataSource;
import com.lguplus.fleta.domain.dto.ui.DataSourceResponse;
import com.lguplus.fleta.domain.model.DataSourceEntity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public interface DataSourceService {

	DataSource findDatasourceByServerName(String serverName);

	Connection findConnectionByServerName(String serverName) throws SQLException;

	void checkServerNameAndUrl(IDataSource dataSource);

	void initializeDataSource(IDataSource dataSource, boolean broadcast) throws DuplicateDataSourceException, DisconnectedDataSourceException;

	void retryInitializeDataSource(String targetDatabase);

	void removeDataSourceFromMap(String serverName, boolean broadcast);

	void createNewDataSource(DataSourceEntity dataSource);

	void loadDataSourceForAddRuntimeCase(IDataSource dataSource, DataSourceEntity dataSourceEntity);

	void initialize();

	void setDataSourceStatus(IDataSource dataSource, DataSourceState dataSourceState);

	void updateDataSource(DataSourceEntity dto, DataSourceState dataSourceState);

	void healthCheckDataSource(HealthCheckRegistry healthCheckRegistry, String serverName);

	boolean testConnection(IDataSource sourceInfo);

	IDataSource findDataSourceByDatabaseName(String databaseName);

	boolean deleteByIdAndStatus(List<Long> ids);

	void updateDataSourceStatus(String datasource, DataSourceState status) throws SynchronizerService.DataSourceInvalidException;

	DataSourceInfo createOrUpdate(DataSourceEntity dataSourceInfo);

	DataSourceResponse findPaginated(int pageNo, int pageSize, String sortField);

	DataSourceInfo findDataSourceInfoById(long id);

	Set<String> findAllAvailableDataSources();

	int activeInUsedDataSources();

	class DuplicateDataSourceException extends RuntimeException {

		public DuplicateDataSourceException(String message) {
			super(message);
		}
	}

	class DisconnectedDataSourceException extends RuntimeException {

		public DisconnectedDataSourceException(String message, Throwable cause) {
			super(message, cause);
		}

		public DisconnectedDataSourceException(String message) {
			super(message);
		}
	}

}
