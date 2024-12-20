package com.lguplus.fleta.config;

import com.lguplus.fleta.config.listener.DbSyncServletContextListener;
import com.lguplus.fleta.config.security.JWTSecurityConfig;
import com.lguplus.fleta.config.security.KeyCloakSecurityConfig;
import com.lguplus.fleta.domain.service.datasource.DataSourceServiceImpl;
import com.lguplus.fleta.domain.service.datasource.DatasourceBroadcastPublisher;
import com.lguplus.fleta.domain.service.comparison.ComparisonBroadcastPublisherImpl;
import com.lguplus.fleta.domain.service.comparison.ComparisonSchedulerServiceImpl;
import com.lguplus.fleta.domain.service.secret.SecretValueClient;
import com.lguplus.fleta.ports.repository.DataSourceRepository;
import com.lguplus.fleta.ports.service.DataSourceService;
import com.lguplus.fleta.ports.service.DbComparisonSchedulerService;
import com.lguplus.fleta.ports.service.LoggerManager;
import com.lguplus.fleta.ports.service.comparison.ComparisonInfoService;
import com.lguplus.fleta.ports.service.comparison.ComparisonSchedulerService;
import com.logviewer.springboot.LogViewerSpringBootConfig;
import com.logviewer.springboot.LogViewerWebsocketConfig;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.servlet.ServletContextListener;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Dec 2021
 */
@Configuration
@Import(value = {
		OpenApi30Config.class,
		JWTSecurityConfig.class,
		KeyCloakSecurityConfig.class,
		RetrofitHttpConfig.class,
		TaskSchedulerConfig.class,
		WebMvcConfig.class,
		MyLogViewerAutoConfig.class,
		LogViewerSpringBootConfig.class,
		LogViewerWebsocketConfig.class
})
@EnableAsync
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT10S")
public class DbSyncServiceConfig {

	@Value("${spring.profiles.active}")
	private String ACTIVE_PROFILE;

	private boolean isProduction() {
		return Profile.isProduction(this.ACTIVE_PROFILE);
	}

	public InputStream getResourceAsStream(String fileName) {
		ClassLoader classLoader = this.getClass().getClassLoader();
		return classLoader.getResourceAsStream(fileName);
	}

	@Bean
	@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
	public ComparisonSchedulerService createComparisonSchedulerService(ComparisonInfoService comparisonInfoService,
																	   DbComparisonSchedulerService scheduleService,
																	   StreamBridge streamBridge,
																	   LoggerManager loggerManager,
																	   @Qualifier("commonThreadPool") ExecutorService executorService
	) {
		return new ComparisonSchedulerServiceImpl(
				comparisonInfoService,
				scheduleService,
				loggerManager,
				executorService
		);
	}

	@Bean(name = "defaultDatasourceContainer")
	@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON) // TODO: important
	public DataSourceService createDefaultDatasourceContainer(SecretValueClient secretValueClient,
															  DataSourceRepository dataSourceRepository,
															  StreamBridge streamBridge) {
		/*
		 * spring:
		 *   cloud:
		 *     stream:
		 *       bindings:
		 *          datasource-out-0:
		 * => broadcastGroup = datasource-out-0
		 */
		return new DataSourceServiceImpl(secretValueClient, dataSourceRepository,
				new DatasourceBroadcastPublisher(streamBridge));
	}

	/**
	 * Register the callback at bean level, the ServletContextListener registers the callback at context level.
	 *
	 * @return
	 */
	@Bean
	public ServletListenerRegistrationBean<ServletContextListener> servletListener() {
		ServletListenerRegistrationBean<ServletContextListener> srb = new ServletListenerRegistrationBean<>();
		srb.setListener(new DbSyncServletContextListener());
		return srb;
	}

	@Bean
	public ServletContextInitializer initializer() {
		return servletContext -> {
			// TODO: Remove the simplefan and ons jars from the classpath
			servletContext.setInitParameter("oracle.jdbc.fanEnabled", "false");

			if (isProduction()) {
				servletContext.setInitParameter("spring.cloud.stream.bindings.synchronizer-out-0.destination", "DBSYNC.SYNCHRONIZER_MONITOR");
				servletContext.setInitParameter("spring.cloud.stream.bindings.synchronizerSink-in-0.destination", "DBSYNC.SYNCHRONIZER_MONITOR");
				servletContext.setInitParameter("spring.cloud.stream.bindings.syncComparison-out-0.destination", "DBSYNC.SYNC_COMPARISON");
				servletContext.setInitParameter("spring.cloud.stream.bindings.syncComparisonSink-in-0.destination", "DBSYNC.SYNC_COMPARISON");
				servletContext.setInitParameter("spring.cloud.stream.bindings.datasource-out-0.destination", "DBSYNC.DATASOURCE_MONITOR");
				servletContext.setInitParameter("spring.cloud.stream.bindings.datasourceSink-in-0.destination", "DBSYNC.DATASOURCE_MONITOR");
				servletContext.setInitParameter("spring.cloud.stream.bindings.operation-out-0.destination", "DBSYNC.OPERATION_MONITOR");
				servletContext.setInitParameter("spring.cloud.stream.bindings.operationSink-in-0.destination", "DBSYNC.OPERATION_MONITOR");
				servletContext.setInitParameter("spring.cloud.function.definition", "synchronizer;syncComparison;synchronizerSink;syncComparisonSink;datasource;datasourceSink;operation;operationSink");
			}
		};
	}

	@Bean
	public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
		return new KeycloakSpringBootConfigResolver();
	}
}
