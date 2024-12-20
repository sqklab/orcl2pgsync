package com.lguplus.fleta.domain.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Sep 2021
 * <p>
 * The LoggerFactory is a utility class producing Loggers for various logging APIs
 */
@Getter
@Component
public final class CustomLoggerFactory {

	private static final String LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS, Asia/Seoul} %-10level [%L] [%.-24thread] %logger{50} %ex{30} - %msg%n";

	@Value("${logging.logback.rollingpolicy.max-history:60}")
	public int maxHistory;

	@Value("${logging.logback.rollingpolicy.max-file-size:50MB}")
	public String maxFileSize;

	@Value("${logging.logback.rollingpolicy.total-size-cap:600MB}")
	private String totalSizeCap;

	@Value("${logging.file.path:logs}")
	private String logDirectory;

	@Value("${logging.level.root:DEBUG}")
	private String loglevel;

	public Logger createLogger(String name) {
		LoggerContext logCtx = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();

		PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
		logEncoder.setContext(logCtx);
		logEncoder.setPattern(LOG_PATTERN);
		logEncoder.start();

		RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
		fileAppender.setContext(logCtx);
		fileAppender.setName("timestamp");
		fileAppender.setFile(getLogDirectory() + "/" + name + ".log");

		SizeAndTimeBasedRollingPolicy<ILoggingEvent> sizeTriggerPolicy = new SizeAndTimeBasedRollingPolicy<>();
		sizeTriggerPolicy.setContext(logCtx);
		sizeTriggerPolicy.setParent(fileAppender);
		sizeTriggerPolicy.setFileNamePattern(getLogDirectory() + "/archived/" + name + "_%d{yyyy-MM-dd}.%i.log.gz");
		sizeTriggerPolicy.setMaxHistory(getMaxHistory());
		sizeTriggerPolicy.setMaxFileSize(FileSize.valueOf(getMaxFileSize()));
		sizeTriggerPolicy.setContext(logCtx);
		sizeTriggerPolicy.setTotalSizeCap(FileSize.valueOf(getTotalSizeCap()));
		sizeTriggerPolicy.start();

		fileAppender.setEncoder(logEncoder);
		fileAppender.setRollingPolicy(sizeTriggerPolicy);
		fileAppender.start();

		Logger logger = logCtx.getLogger(name);
		logger.setAdditive(true);
		switch (loglevel) {
			case "ALL":
				logger.setLevel(Level.ALL);
				break;
			case "DEBUG":
				logger.setLevel(Level.DEBUG);
				break;
			case "ERROR":
				logger.setLevel(Level.ERROR);
				break;
			case "INFO":
				logger.setLevel(Level.INFO);
				break;
			case "OFF":
				logger.setLevel(Level.OFF);
				break;
			case "TRACE":
				logger.setLevel(Level.TRACE);
				break;
			case "WARN":
				logger.setLevel(Level.WARN);
				break;
			default:
				logger.setLevel(Level.INFO);
				break;
		}
		logger.addAppender(fileAppender);
		return logger;
	}
}
