package com.lguplus.fleta.ports.service;

import ch.qos.logback.classic.Logger;

public interface LoggerManager {

	Logger getLogger(String name);

	Logger createLogger(String name);
}
