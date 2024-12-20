package com.lguplus.fleta.domain.service;

import ch.qos.logback.classic.Logger;
import com.lguplus.fleta.ports.service.LoggerManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LoggerManagerImpl implements LoggerManager {

	private final Map<String, Logger> loggerCache = new ConcurrentHashMap<>();

	private final CustomLoggerFactory factory;

	public LoggerManagerImpl(CustomLoggerFactory factory) {
		Assert.notNull(factory, "A LoggerFactory must be provided");

		this.factory = factory;
	}

	@Override
	public Logger getLogger(String name) {
		if (loggerCache.containsKey(name)) {
			return loggerCache.get(name);
		}
		return createLogger(name);
	}

	@Override
	public Logger createLogger(String name) {
		Logger logger = factory.createLogger(name);
		loggerCache.put(name, logger);
		return logger;
	}
}
