package com.lguplus.fleta.config.initializer;

import com.lguplus.fleta.domain.service.ExpiredLogScanner;
import com.lguplus.fleta.ports.service.*;
import com.lguplus.fleta.ports.service.dbschedule.DbSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

@Component
@Profile("!test")
public class DbSyncInitializer implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(DbSyncInitializer.class);

	private final DataSourceService dataSourceService;

	private final SynchronizerService synchronizerService;

	private final ExpiredResultService expiredResultService;

	private final DbSchedulerService dbSchedulerService;
	private final ConnectorService connectorService;
	private final OperationService operationService;

	private final ExpiredLogScanner expiredLogScanner;

	@Value("${spring.profiles.active}")
	private String ACTIVE_PROFILE;

	public DbSyncInitializer(@Qualifier("defaultDatasourceContainer") DataSourceService dataSourceService,
							 SynchronizerService synchronizerService,
							 ExpiredResultService expiredResultService, DbSchedulerService dbSchedulerService,
							 ConnectorService connectorService,
							 OperationService operationService, ExpiredLogScanner expiredLogScanner) {
		this.dataSourceService = dataSourceService;
		this.synchronizerService = synchronizerService;

		this.expiredResultService = expiredResultService;
		this.dbSchedulerService = dbSchedulerService;

		this.connectorService = connectorService;
		this.operationService = operationService;
		this.expiredLogScanner = expiredLogScanner;
	}

	private boolean isNotProduction() {
		return !com.lguplus.fleta.config.Profile.isProduction(this.ACTIVE_PROFILE);
	}

	private boolean isProduction() {
		return com.lguplus.fleta.config.Profile.isProduction(this.ACTIVE_PROFILE);
	}

	@Override
	public void run(ApplicationArguments args) throws SQLException {

		if (true) {
			logger.info("Initializing datasource, please wait...");

			// Initialize all created datasources
			dataSourceService.initialize();

			logger.info("Starting all synchronizer, please wait...");

			// Start all synchronizer and start to track error
			synchronizerService.startDefaultSynchronizers();
			synchronizerService.startDefaultErrorTracker();

			// register to delete out date data
			this.operationService.register();

			// job run every day to delete old log: comparison result, message analysis, dbscheduler result
			expiredResultService.startWorker();

			// Initialize DB Scheduler
			dbSchedulerService.register();

			// Initialize connectors synchronization
			try {
				connectorService.fetchConnectors();
			} catch (IOException e) {
				logger.info("Cannot retrieve connectors info from base-url");
			}
		}

		// Delete log files older than N days
		expiredLogScanner.scanAndDeleteFilesOlderThanNDays();

		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		logger.info(">> DataSync has been successfully started at {}. Welcome to DataSync application !!! ", new Date());
		logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}
}
