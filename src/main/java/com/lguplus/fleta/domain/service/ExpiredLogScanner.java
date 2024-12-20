package com.lguplus.fleta.domain.service;

import com.lguplus.fleta.config.IDefaultLogPath;
import com.lguplus.fleta.domain.service.scheduling.CompletableFutures;
import com.lguplus.fleta.domain.util.ShutdownHookUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Feb 2022
 */
@Slf4j
@Component
public class ExpiredLogScanner {

	private static final int N_DAYS = 15;
	private static final String LOG_ARCHIVED = "archived";

	private final IDefaultLogPath logPath;

	private final ScheduledExecutorService executorService;

	public ExpiredLogScanner(IDefaultLogPath logPath, @Qualifier("scheduledExecutorCommon") ScheduledExecutorService scheduledExecutorService) {
		this.logPath = logPath;
		this.executorService = scheduledExecutorService;
	}

	/**
	 * Scan using recursive option, that traverses sub-folders and deletes all files that are older than N days
	 */
	public void scanAndDeleteFilesOlderThanNDays() {
		final Duration initialDelay = Duration.ofHours(1);
		final Duration delay = Duration.ofHours(24);
		CompletableFutures.scheduler(executorService).scheduleWithFixedDelay(() -> {
			try {
				recursiveDeleteFilesOlderThanNDays(N_DAYS, getArchivedLogPath());
			} catch (IOException ex) {
				log.error(ex.getMessage(), ex);
			}
		}, initialDelay.toHours(), delay.toHours(), TimeUnit.HOURS);
		ShutdownHookUtils.initExecutorServiceShutdownHook(executorService);
	}

	private String getArchivedLogPath() {
		return logPath.getRootPath() + "/" + LOG_ARCHIVED;
	}

	private void recursiveDeleteFilesOlderThanNDays(int days, String dirPath) throws IOException {
		long cutOff = System.currentTimeMillis() - ((long) days * 24 * 60 * 60 * 1000);
		Files.list(Paths.get(dirPath)).forEach(path -> {
			if (Files.isDirectory(path)) {
				try {
					recursiveDeleteFilesOlderThanNDays(days, path.toString());
				} catch (IOException ex) {
					log.error("Unable to delete file..{}", ex.getMessage(), ex);
				}
			} else {
				try {
					if (Files.getLastModifiedTime(path).to(TimeUnit.MILLISECONDS) < cutOff) {
						Files.delete(path);
					}
				} catch (IOException ex) {
					log.error("Unable to delete file..{}", ex.getMessage(), ex);
				}
			}
		});
	}
}
