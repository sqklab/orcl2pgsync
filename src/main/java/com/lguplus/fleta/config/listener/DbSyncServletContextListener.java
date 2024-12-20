package com.lguplus.fleta.config.listener;

import com.lguplus.fleta.config.context.DbSyncContext;
import com.lguplus.fleta.domain.service.analysis.MessageCollectorServiceImpl;
import com.lguplus.fleta.ports.service.MessageCollectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Dec 2021
 */
public class DbSyncServletContextListener implements ServletContextListener {

	private static final Logger logger = LoggerFactory.getLogger(DbSyncServletContextListener.class);

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		logger.info("Callback triggered - ContextListener for DataSync.");

		MessageCollectorService messageCollectorService = DbSyncContext.getBean(MessageCollectorServiceImpl.class);
		messageCollectorService.autoSaveReceivedKafkaMessage();
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {

	}
}
