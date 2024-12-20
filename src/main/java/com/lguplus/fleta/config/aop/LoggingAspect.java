package com.lguplus.fleta.config.aop;

import ch.qos.logback.classic.Logger;
import com.lguplus.fleta.ports.service.LoggerManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
public class LoggingAspect {

	private static final String TIME_EXECUTION_LOG = "logging-time-execution";

	private final Logger log;

	public LoggingAspect(LoggerManager loggerManager) {
		log = loggerManager.getLogger(TIME_EXECUTION_LOG);
	}

	@Around("@annotation(com.lguplus.fleta.config.aop.LogExecutionTime)")
	public Object methodTimeLogger(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();

		// Get intercepted method details
		String className = methodSignature.getDeclaringType().getSimpleName();
		String methodName = methodSignature.getName();

		// Measure method execution time
		StopWatch stopWatch = new StopWatch(className + "->" + methodName);
		stopWatch.start(methodName);
		Object result = proceedingJoinPoint.proceed();
		stopWatch.stop();

		if (log.isDebugEnabled()) {
			log.debug(stopWatch.prettyPrint());
		}
		return result;
	}
}
