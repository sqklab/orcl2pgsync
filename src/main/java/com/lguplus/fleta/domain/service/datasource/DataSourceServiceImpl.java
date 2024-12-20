package com.lguplus.fleta.domain.service.datasource;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.gson.Gson;
import com.lguplus.fleta.domain.dto.DataSourceInfo;
import com.lguplus.fleta.domain.dto.DataSourceState;
import com.lguplus.fleta.domain.dto.IDataSource;
import com.lguplus.fleta.domain.dto.event.DatasourceBroadcastEventData;
import com.lguplus.fleta.domain.dto.event.DatasourceBroadcastEventData.BroadcastAction;
import com.lguplus.fleta.domain.dto.ui.DataSourceResponse;
import com.lguplus.fleta.domain.model.DataSourceEntity;
import com.lguplus.fleta.domain.service.secret.SecretValueClient;
import com.lguplus.fleta.ports.repository.DataSourceRepository;
import com.lguplus.fleta.ports.service.DataSourceService;
import com.lguplus.fleta.ports.service.SynchronizerService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.lguplus.fleta.domain.dto.DataSourceState.ACTIVE;
import static com.lguplus.fleta.domain.dto.DataSourceState.INACTIVE;
import static com.lguplus.fleta.domain.dto.event.DatasourceBroadcastEventData.BroadcastAction.DELETE;

public class DataSourceServiceImpl implements DataSourceService {

	private static final Logger logger = LoggerFactory.getLogger(DataSourceServiceImpl.class);

	private static final String QUESTION_MARK = "?";

	private final Map<String, DataSource> hikariDataSourceMap = new ConcurrentHashMap<>();

	private final Map<String, IDataSource> dataSourceMap = new ConcurrentHashMap<>();

	private final Map<String, DataSourceHealthCheckTask> dataSourceHealthCheckTaskMap = new ConcurrentHashMap<>();

	private final SecretValueClient secretValueClient;

	private final DataSourceRepository dataSourceRepository;

	private final DatasourceBroadcastPublisher datasourceBroadcastPublisher;

	@Value("${spring.profiles.active:dev}")
	private String ACTIVE_PROFILE;

	public DataSourceServiceImpl(SecretValueClient secretValueClient,
								 DataSourceRepository dataSourceRepository, DatasourceBroadcastPublisher datasourceBroadcastPublisher) {
		this.secretValueClient = secretValueClient;
		this.dataSourceRepository = dataSourceRepository;
		this.datasourceBroadcastPublisher = datasourceBroadcastPublisher;
	}

	@Override
	public void updateDataSourceStatus(String dataSourceName, DataSourceState status) throws SynchronizerService.DataSourceInvalidException {
		IDataSource dataSource = dataSourceMap.get(dataSourceName);
		if (dataSource == null) {
			throw new SynchronizerService.DataSourceInvalidException(String.format("The datasource %s not found. " +
					"All available datasource: %s", dataSourceName, new Gson().toJson(dataSourceMap.keySet())));
		}
		dataSource.setStatus(status);
		dataSourceMap.put(dataSourceName, dataSource);
		Optional<DataSourceEntity> dataSourceInfo = dataSourceRepository.findById(dataSource.getId());
		if (dataSourceInfo.isPresent()) {
			dataSourceInfo.get().setStatus(status);
			dataSourceRepository.save(dataSourceInfo.get());
		}
	}

	@Override
	public void checkServerNameAndUrl(IDataSource dataSource) throws DuplicateDataSourceException {
		String existed = dataSourceRepository.getIdByServerNameOrUrl(dataSource.getUrl(), dataSource.getServerName());
		if (existed != null) {
			throw new DuplicateDataSourceException(String.format("The datasource [Name: %s, Url: %s] already exist",
					dataSource.getServerName(), dataSource.getUrl()));
		}
	}

	@Override
	public void loadDataSourceForAddRuntimeCase(IDataSource iDataSource, DataSourceEntity dataSourceEntity) {
		Optional<DataSourceEntity> dataSource = dataSourceRepository.findByServerName(dataSourceEntity.getServerName());
		dataSource.ifPresent(sourceEntity -> iDataSource.setId(sourceEntity.getId()));
		dataSourceMap.put(iDataSource.getServerName(), iDataSource);
	}

	@Override
	public void initializeDataSource(IDataSource dataSource, boolean isBroadcast) throws DuplicateDataSourceException, DisconnectedDataSourceException {
		if (isBroadcast & isProduction()) {
			datasourceBroadcastPublisher.broadcast(new DatasourceBroadcastEventData(dataSource.getServerName(),
					new DataSourceEntity()
							.toDataSourceEntity(dataSource)
							.toDataSourceInfo(),
					BroadcastAction.ADD));
		}
		String serverName = dataSource.getServerName();
		// Parser AWS Secret Key with real value
		parseSecretValue(dataSource);

		if (hikariDataSourceMap.containsKey(serverName) || dataSourceMap.containsKey(serverName)) {
			throw new DuplicateDataSourceException(String.format("The datasource %s already exist. " +
					"All available datasource: %s", serverName, new Gson().toJson(dataSourceMap.keySet())));
		}

		try (Connection connection = DriverManager.getConnection(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword())) {
			if (dataSource.getId() != null) {
				setDataSourceStatus(dataSource, Objects.nonNull(connection) ? ACTIVE : INACTIVE);
			}
		} catch (Exception ex) {
			setDataSourceStatus(dataSource, INACTIVE);
			// Throw new exception with message "Cannot connect to datasource"
			throw new DisconnectedDataSourceException(String.format("Cannot connect to datasource %s", dataSource.getServerName()), ex.getCause());
		}

		if (dataSource.getMaxPoolSize() < 1) {
			dataSource.setMaxPoolSize(10);
		}

		if (dataSource.getIdleTimeout() < 0) {
			dataSource.setIdleTimeout(10000);
		}

		HikariConfig config = new HikariConfig();
		// TODO: Fix bug too many call of SET application_name = 'PostgreSQL JDBC Driver' command
		if (Objects.nonNull(dataSource.getUrl()) && dataSource.getUrl().contains("thin")) {
			config.setJdbcUrl(dataSource.getUrl());
		} else {
			String jdbcUrl = dataSource.getUrl();
			if (jdbcUrl.contains(QUESTION_MARK)) {
				jdbcUrl += "&ApplicationName=DataSyncApp";
			} else {
				jdbcUrl += "?ApplicationName=DataSyncApp";
			}
			config.setJdbcUrl(jdbcUrl);
		}
		config.setUsername(dataSource.getUsername());
		config.setPassword(dataSource.getPassword());
		config.setMinimumIdle(5);
		if (dataSource.getMaxPoolSize() < 20) {
			config.setMaximumPoolSize(20); // Min is 20
		} else {
			// TODO: Reduce from 2000 to 1000 because of too high memory problem
			config.setMaximumPoolSize(dataSource.getMaxPoolSize());
		}
		config.setPoolName("DataSyncHikariCP");
		config.setMaxLifetime(120000);
		if (Objects.nonNull(dataSource.getUrl()) && dataSource.getUrl().contains("thin")) {
			config.setConnectionTimeout(60000);
		} else {
			config.setConnectionTimeout(120000);

			// Add Postgres Driver level properties in seconds
			config.addDataSourceProperty("socketTimeout", 120);
			config.addDataSourceProperty("connectTimeout", 120);
			config.addDataSourceProperty("tcpKeepAlive", true);
		}
		config.setValidationTimeout(10000);
		config.setLeakDetectionThreshold(120000);
		config.setIdleTimeout(15000);

		if (Objects.nonNull(dataSource.getUrl()) && dataSource.getUrl().contains("thin")) {
			// TODO: do nothing here!!!
		} else {
			config.setConnectionInitSql("SELECT 1");
			config.setConnectionTestQuery("SELECT 1");
			config.addDataSourceProperty("cachePrepStmts", "true");
			config.addDataSourceProperty("useServerPrepStmts", "true");
			config.addDataSourceProperty("cacheResultsSetMeta", "true");
			config.addDataSourceProperty("prepStmtCacheSize", "250");
			config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		}
		HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
		config.setHealthCheckRegistry(healthCheckRegistry);

		HikariDataSource ds = new HikariDataSource(config);
		Assert.notNull(ds, String.format("HikariDataSource required. Cannot establish connection to %s by user %s",
				dataSource.getUrl(), dataSource.getUsername()));

		dataSourceMap.put(serverName, dataSource);
		hikariDataSourceMap.put(serverName, ds);
		// For debugging
		if (logger.isDebugEnabled()) {
			logger.debug("A new datasource {} is created.", serverName);
		}
		healthCheckDataSource(healthCheckRegistry, serverName);
	}

	@Override
	public void createNewDataSource(DataSourceEntity dataSource) {
		if (dataSource.getMaxPoolSize() < 1) {
			dataSource.setMaxPoolSize(10);
		}

		if (dataSource.getIdleTimeout() < 0) {
			dataSource.setIdleTimeout(10000);
		}

		try {
			dataSourceRepository.save(dataSource);
		} catch (Exception ex) {
			removeDataSourceFromMap(dataSource.getServerName(), true);
			throw ex;
		}
	}

	@Override
	public void removeDataSourceFromMap(String serverName, boolean broadcast) {
		if (dataSourceMap.containsKey(serverName) && hikariDataSourceMap.containsKey(serverName)) {
			dataSourceMap.remove(serverName);

			HikariDataSource dataSourceEntry = (HikariDataSource) hikariDataSourceMap.get(serverName);
			HealthCheckRegistry healthCheckRegistry = (HealthCheckRegistry) dataSourceEntry.getHealthCheckRegistry();
			hikariDataSourceMap.remove(serverName);

			dataSourceEntry.close();

			logger.info("Closing connection pool for Datasource - " + serverName);

			healthCheckRegistry.unregister(serverName);

			Optional.ofNullable(dataSourceHealthCheckTaskMap.get(serverName))
					.ifPresent(DataSourceHealthCheckTask::stop);

			if (broadcast) {
				datasourceBroadcastPublisher.broadcast(new DatasourceBroadcastEventData(serverName, null, DELETE));
			}
		}
	}

	@Override
	public void setDataSourceStatus(IDataSource dataSource, DataSourceState dataSourceState) {
		dataSource.setStatus(dataSourceState);

		Optional<DataSourceEntity> dataSourceInfo = dataSourceRepository.findById(dataSource.getId());
		if (dataSourceInfo.isPresent()) {
			dataSourceInfo.get().setStatus(dataSourceState);
			dataSourceRepository.save(dataSourceInfo.get());
		}
	}

	@Override
	public void updateDataSource(DataSourceEntity dataSource, DataSourceState dataSourceState) {
		dataSource.setStatus(dataSourceState);
		Optional<DataSourceEntity> dataSourceOpt = dataSourceRepository.findById(dataSource.getId());
		String currentServerName = "";
		if (dataSourceOpt.isPresent()) {
			parseSecretValue(dataSourceOpt.get());
			currentServerName = dataSourceOpt.get().getServerName();
		}
		removeDataSourceFromMap(currentServerName, true);

		if (dataSource.getMaxPoolSize() < 1) {
			dataSource.setMaxPoolSize(10);
		}

		if (dataSource.getIdleTimeout() < 0) {
			dataSource.setIdleTimeout(10000);
		}
		dataSourceRepository.save(dataSource);
	}

	@Override
	public boolean testConnection(IDataSource dataSource) {
		// Parser AWS Secret Key with real value
		parseSecretValue(dataSource);

		try (Connection connection = DriverManager.getConnection(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword())) {
			if (null != connection) {
				return true;
			}
		} catch (SQLException ex) {
			logger.warn(ex.getMessage(), ex);
		}
		// Ignore
		return false;
	}

	public void healthCheckDataSource(HealthCheckRegistry healthCheckRegistry, final String serverName) {
		DataSourceHealthCheckTask healthCheckTask = new DataSourceHealthCheckTask(serverName, healthCheckRegistry);
		healthCheckTask.start();
		dataSourceHealthCheckTaskMap.put(serverName, healthCheckTask);
	}

	@Override
	public DataSource findDatasourceByServerName(String serverName) {
		return this.hikariDataSourceMap.get(serverName);
	}

	@Override
	public Connection findConnectionByServerName(String serverName) throws SQLException {
		if (!hikariDataSourceMap.containsKey(serverName) || !dataSourceMap.containsKey(serverName)) {
			Set<String> availableDataSources = dataSourceMap.keySet();
			logger.error("The datasource {} not found. All available datasource: {}", serverName, new Gson().toJson(availableDataSources));
			return null;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Get connection by servername {}", serverName);
		}
		DataSource dataSource = hikariDataSourceMap.get(serverName);
		Assert.notNull(dataSource, "DataSource required. Detail server name:" + serverName);

		IDataSource dataSourceEntity = dataSourceMap.get(serverName);
		if (DataSourceState.DISCONNECTED.equals(dataSourceEntity.getStatus())) {
			throw new DisconnectedDataSourceException(String.format("Cannot connect to disconnected datasource %s", dataSourceEntity.getServerName()));
		}
		if (DataSourceState.IN_USE != dataSourceEntity.getStatus()) {
			Optional<DataSourceEntity> dataSourceInfo = dataSourceRepository.findById(dataSourceEntity.getId());
			if (dataSourceInfo.isPresent()) {
				DataSourceEntity sourceEntity = dataSourceInfo.get();
				sourceEntity.enableInUseState();
				dataSourceRepository.save(sourceEntity);
				dataSourceEntity.enableInUseState();
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Datasource List {} ", new Gson().toJson(hikariDataSourceMap.keySet()));
		}
		return DataSourceUtils.getConnection(dataSource);
	}

	public IDataSource findDataSourceByDatabaseName(String databaseName) {
		if (!dataSourceMap.containsKey(databaseName)) {
			// Re-initialize datasource again one more time before throw exception
			reinitializeDataSource(databaseName);

			// Fetch again after re-initialize
			IDataSource dataSource = dataSourceMap.get(databaseName);
			if (Objects.isNull(dataSource)) {
				logger.warn("The datasource {} does not exist. All available datasource: {}", databaseName,
						new Gson().toJson(dataSourceMap.keySet()));
				return null;
			}
		}
		return dataSourceMap.get(databaseName);
	}

	@Override
	public void retryInitializeDataSource(String targetDatabase) {
		Optional<DataSourceEntity> dataSourceEntity = dataSourceRepository.findByServerName(targetDatabase);
		dataSourceEntity.ifPresent(sourceEntity -> initializeDataSource(sourceEntity, false));
	}

	@Override
	public void initialize() {
		List<DataSourceEntity> dataSources = dataSourceRepository.findAll()
				.stream()
				.filter(ds -> ds.isNotIn(DataSourceState.PENDING))
				.collect(Collectors.toList());
		if (null != dataSources && !dataSources.isEmpty()) {
			// TODO: For debugging only
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			System.out.println("Call initialize() :: " + new Date());
			System.out.println("There are " + dataSources.size() + " datasource(s)");

			dataSources.forEach(dataSource -> {
				System.out.println("\t" + dataSource.getServerName());
			});

			System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		}

		for (DataSourceEntity dataSource : dataSources) {
			if (dataSource.getStatus() == DataSourceState.IN_USE) {
				dataSource.disableInUseState();
			}

			System.out.println("** Url: " + dataSource.getUrl());

			try {
				initializeDataSource(dataSource, false);
			} catch (DuplicateDataSourceException ex) {
				// TODO: do nothing here if the datasource has been initialized
			} catch (DisconnectedDataSourceException ex) {
				logger.warn("An error occurred while initializing the datasource [{}].\n\t{}\n\t{}",
						dataSource.getServerName(), ex.getMessage(), ex);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	@PreDestroy
	public void closeAllRunningDataSource() {
		for (Map.Entry<String, DataSource> dataSourceEntry : hikariDataSourceMap.entrySet()) {
			HikariDataSource dataSource = (HikariDataSource) dataSourceEntry.getValue();
			logger.info("Closing connection pool for datasource {}", dataSourceEntry.getKey());
			dataSource.close();
		}
	}

	@Override
	public DataSourceResponse findPaginated(int pageNo, int pageSize, String sortField) {
		Sort sort = Sort.by(sortField).descending();
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);

		Page<DataSourceEntity> all = this.dataSourceRepository.findAll(pageable);
		List<DataSourceInfo> dataSources = all
				.stream()
				.map(DataSourceEntity::toDataSourceInfo)
				.collect(Collectors.toList());
		return DataSourceResponse.builder().totalPage(all.getTotalPages()).dataSourceDescriptions(dataSources).build();
	}

	@Override
	public DataSourceInfo createOrUpdate(DataSourceEntity dataSourceInfo) {
		if (dataSourceInfo.getStatus() != DataSourceState.IN_USE) {
			DataSourceEntity save = dataSourceRepository.save(dataSourceInfo);
			return save.toDataSourceInfo();
		} else {
			return dataSourceInfo.toDataSourceInfo();
		}
	}

	@Override
	public boolean deleteByIdAndStatus(List<Long> ids) {
		try {
			if (ids.contains(null)) {
				return false;
			}

			List<DataSourceEntity> dataSources = dataSourceRepository.findAllById(ids);
			for (DataSourceEntity dataSource : dataSources) {
				parseSecretValue(dataSource);
				if (dataSource.getStatus() == DataSourceState.IN_USE) {
					return false;
				} else {
					dataSourceRepository.deleteByIds(ids, dataSource.getStatus());
					removeDataSourceFromMap(dataSource.getServerName(), true);
				}
			}

			return true;
		} catch (Exception ex) {
			logger.error("Cannot delete datasource id {}.\n\t{}", ids, ex);
			return false;
		}
	}

	@Override
	public DataSourceInfo findDataSourceInfoById(long id) {
		Optional<DataSourceEntity> sourceInfo = dataSourceRepository.findById(id);
		if (sourceInfo.isEmpty()) {
			return null;
		}
		return sourceInfo.get().toDataSourceInfo();
	}

	@Override
	public Set<String> findAllAvailableDataSources() {
		return dataSourceMap.keySet();
	}

	@Override
	public int activeInUsedDataSources() {
		int ds = dataSourceRepository.updateState(DataSourceState.IN_USE, DataSourceState.ACTIVE);
		logger.info("Change datasource state from IN_USE to ACTIVE {}", ds);
		return ds;
	}

	private void parseSecretValue(IDataSource dataSource) {
		// Parser Url
		// e.g: jdbc:postgresql://postgres.searchword.write.host:postgres.searchword.write.port/postgres.searchword.write.db.mylgdb
		// e.g: jdbc:oracle:thin:@oracle.mylgdb.host:oracle.mylgdb.port:oracle.mylgdb.serviceName
		JdbcUrlSplitter splitter = new JdbcUrlSplitter(dataSource.getUrl());
		String driverName = splitter.getDriverName();
		if (null != driverName) {
			dataSource.setUrl(driverName,
					secretValueClient.parseSecretValue(splitter.getHost()),
					secretValueClient.parseSecretValue(splitter.getPort()),
					secretValueClient.parseSecretValue(splitter.getDatabase()),
					secretValueClient.parseSecretValue(splitter.getParams()),
					splitter.isSID());
		}

		// Parser Username
		String username = secretValueClient.parseSecretValue(dataSource.getUsername());
		if (null != username) {
			dataSource.setUsername(username);
		}

		// Parser Password
		String password = secretValueClient.parseSecretValue(dataSource.getPassword());
		if (null != password) {
			dataSource.setPassword(password);
		}
	}

	/**
	 * Re-initialize datasource for given specific list of datasource names.
	 *
	 * @param dataSourceNames
	 */
	private void reinitializeDataSource(String... dataSourceNames) {
		final List<DataSourceEntity> dataSources = dataSourceRepository.findAll();
		if (dataSources.isEmpty()) return;

		dataSources
				.stream()
				.filter(ds -> Arrays.asList(dataSourceNames).contains(ds.getServerName()))
				.forEach(ds -> {
					try {
						initializeDataSource(ds, false);
					} catch (DuplicateDataSourceException | DisconnectedDataSourceException ex) {
						logger.warn("An error occurred while re-initializing the datasource [{}].\n\t{}\n\t{}",
								ds.getServerName(), ex.getMessage(), ex);
					}
				});
	}

	private boolean isNotProduction() {
		return !com.lguplus.fleta.config.Profile.isProduction(this.ACTIVE_PROFILE);
	}

	private boolean isProduction() {
		return com.lguplus.fleta.config.Profile.isProduction(this.ACTIVE_PROFILE);
	}

	@Getter
	static class JdbcUrlSplitter {
		private final String driverName;
		private String host;
		private String port;
		private String database;
		private String params;
		private boolean isSID = true;

		public JdbcUrlSplitter(String jdbcUrl) {
			int pos, pos1, pos2;
			String connUri;

			if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:") || (pos1 = jdbcUrl.indexOf(':', 5)) == -1) {
				throw new IllegalArgumentException("Invalid JDBC url.");
			}

			driverName = jdbcUrl.substring(5, pos1);
			if (driverName.equals("oracle")) {
				if ((pos2 = jdbcUrl.indexOf(';', pos1)) == -1) {
					connUri = jdbcUrl.substring(pos1 + 6);
				} else {
					connUri = jdbcUrl.substring(pos1 + 1, pos2);
					params = jdbcUrl.substring(pos2 + 1);
				}

				if (connUri.startsWith("@")) {
					if (connUri.charAt(1) == '/') {
						isSID = false;
						if ((pos = connUri.indexOf(':', 1)) != -1) {
							host = connUri.substring(3, pos);
							database = connUri.substring(pos + 1);
							if ((pos = database.indexOf('/')) != -1) {
								port = database.substring(0, pos);
								database = database.substring(pos + 1);
							}
						}
					} else {
						if ((pos = connUri.indexOf(':', 1)) != -1) {
							host = connUri.substring(1, pos);
							database = connUri.substring(pos + 1);
							if ((pos = database.indexOf(':')) != -1) {
								port = database.substring(0, pos);
								database = database.substring(pos + 1);
							}
						}
					}
				} else {
					database = connUri;
				}
			} else {
				if ((pos2 = jdbcUrl.indexOf(';', pos1)) == -1) {
					connUri = jdbcUrl.substring(pos1 + 1);
				} else {
					connUri = jdbcUrl.substring(pos1 + 1, pos2);
					params = jdbcUrl.substring(pos2 + 1);
				}

				if (connUri.startsWith("//")) {
					if ((pos = connUri.indexOf('/', 2)) != -1) {
						host = connUri.substring(2, pos);
						database = connUri.substring(pos + 1);
						if ((pos = host.indexOf(':')) != -1) {
							port = host.substring(pos + 1);
							host = host.substring(0, pos);
						}
					}
				} else {
					database = connUri;
				}
			}
		}
	}
}
