package com.lguplus.fleta.domain.service.comparison;

import ch.qos.logback.classic.Logger;
import com.lguplus.fleta.domain.model.comparison.DbComparisonSchedulerEntity;
import com.lguplus.fleta.domain.service.constant.Constants;
import com.lguplus.fleta.domain.util.ShutdownHookUtils;
import com.lguplus.fleta.ports.service.DbComparisonSchedulerService;
import com.lguplus.fleta.ports.service.LoggerManager;
import com.lguplus.fleta.ports.service.comparison.ComparisonInfoService;
import com.lguplus.fleta.ports.service.comparison.ComparisonSchedulerService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static com.lguplus.fleta.domain.service.constant.Constants.COMPARISON_LOG;

public class ComparisonSchedulerServiceImpl implements ComparisonSchedulerService {

	private static final int DIFF = 5;// minutes
	private final ComparisonInfoService comparisonInfoService;
	private final DbComparisonSchedulerService scheduleService;
	private final Logger logger;

	private final ExecutorService executorService;

	private LocalDateTime lastRun;

	@Value("${app.comparison-worker.step-time:5}")
	private int stepTime;

	@Value("${spring.profiles.active}")
	private String ACTIVE_PROFILE;

	public ComparisonSchedulerServiceImpl(ComparisonInfoService comparisonInfoService,
										  DbComparisonSchedulerService scheduleService,
										  LoggerManager loggerManager,
										  @Qualifier("commonThreadPool") ExecutorService executorService
	) {
		this.comparisonInfoService = comparisonInfoService;
		this.scheduleService = scheduleService;
		this.executorService = executorService;
		this.logger = loggerManager.getLogger(COMPARISON_LOG);
		ShutdownHookUtils.initExecutorServiceShutdownHook(this.executorService);
	}

	@Scheduled(cron = "0 */5 * * * *")
	@SchedulerLock(name = "scheduleComparison", lockAtLeastFor = "10s", lockAtMostFor = "20s")
	public void scheduleFixedDelayTask() {
		LocalDateTime now = LocalDateTime.now(Constants.ZONE_ID);
		List<DbComparisonSchedulerEntity> schedulerEntities = scheduleService.findByStateOrderByTime();
		if (null == schedulerEntities || schedulerEntities.isEmpty()) {
			return;
		}

		logger.info("Start the process of comparing data between source table and destination table at {}", new Date());
		logger.info(">>> Found {} scheduler(s)", schedulerEntities.size());

		for (DbComparisonSchedulerEntity schedulerEntity : schedulerEntities) {
			if (around(now.toLocalTime(), schedulerEntity.getTime(), DIFF)) {
				if (lastRun != null) {
					LocalDateTime schedulerTime = LocalDateTime.of(now.toLocalDate(), schedulerEntity.getTime());
					if (Duration.between(schedulerTime, lastRun).toMillis() == 0) {
						logger.info("The scheduler {} of comparing data is already running. Please wait...", schedulerTime.toString());
						return;
					}
				}
				lastRun = LocalDateTime.of(now.toLocalDate(), schedulerEntity.getTime());
				startOneTimeNow(schedulerEntity.getTime(), now.toLocalDate());
				break;
			}
		}
	}

	@Override
	public void startOneTimeNow(LocalTime executionTime, LocalDate compareDate) {
		comparisonInfoService.compareAll(compareDate, executionTime, stepTime, logger);
	}

	/**
	 * @param before   before
	 * @param after    after
	 * @param stepTime stepTime minute
	 * @return true if after - before <= stepTime
	 */
	private boolean around(LocalTime before, LocalTime after, int stepTime) {
		return Math.abs(Duration.between(before, after).toMinutes()) < stepTime;
	}
}
