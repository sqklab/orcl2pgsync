package com.lguplus.fleta.domain.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Jan 2022
 */
public class ShutdownHookUtils {

	private static final Logger logger = LoggerFactory.getLogger(ShutdownHookUtils.class);

	/**
	 * Init a hook to release lock when application shutdown
	 *
	 * @param executorService
	 */
	public static void initExecutorServiceShutdownHook(ExecutorService executorService) {
		Runtime.getRuntime().addShutdownHook(new Thread("ShutdownHook_ExecutorService") {
			@Override
			public void run() {
				executorService.shutdown(); // Disable new tasks from being submitted
				try {
					// Wait a while for existing tasks to terminate
					if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
						executorService.shutdownNow(); // Cancel currently executing tasks
						// Wait a while for tasks to respond to being cancelled
						if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
							logger.error("Pool did not terminate");
						}
					}
				} catch (InterruptedException ex) {
					// (Re-)Cancel if current thread also interrupted
					executorService.shutdownNow();
					// Preserve interrupt status
					Thread.currentThread().interrupt();
					logger.error(ex.getMessage(), ex);
				}
			}
		});
	}
}
