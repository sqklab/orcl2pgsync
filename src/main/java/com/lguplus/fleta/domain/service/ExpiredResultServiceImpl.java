package com.lguplus.fleta.domain.service;

import com.lguplus.fleta.domain.util.DateUtils;
import com.lguplus.fleta.ports.service.SynchronizerDlqService;
import com.lguplus.fleta.ports.service.ExpiredResultService;
import com.lguplus.fleta.ports.service.MessageAnalysisService;
import com.lguplus.fleta.ports.service.comparison.ComparisonInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
public class ExpiredResultServiceImpl implements ExpiredResultService {

	private static final int DEFAULT_KEEP_TIME = 7;
	private static final int ANALYSIS_KEEP_TIME = 90;
	private static final String DEFAULT_TRIGGER_TIME = "0 10 0 * * *"; // 00:10:00 AM

	private final ComparisonInfoService comparisonInfoService;
	private final MessageAnalysisService messageAnalysisService;
	private final SynchronizerDlqService synchronizerDlqService;

	private final ThreadPoolTaskScheduler taskScheduler;
	private ScheduledFuture<?> scheduler;

	public ExpiredResultServiceImpl(ComparisonInfoService comparisonInfoService, MessageAnalysisService messageAnalysisService, SynchronizerDlqService synchronizerDlqService, @Qualifier("defaultThreadPool") ThreadPoolTaskScheduler taskScheduler) {
		this.comparisonInfoService = comparisonInfoService;
		this.messageAnalysisService = messageAnalysisService;
		this.synchronizerDlqService = synchronizerDlqService;
		this.taskScheduler = taskScheduler;
	}

	@PostConstruct
	private void init() {
		Runtime.getRuntime().addShutdownHook(new Thread(this::onDestroy));
	}

	private void onDestroy() {
		if (log.isDebugEnabled()) {
			log.debug("Shutting down ExecutorService...");
		}
		stopAllSchedule();
	}

	@Override
	public void startWorker() {
		LocalDate before = DateUtils.getDate().minusDays(DEFAULT_KEEP_TIME);

		LocalDate limitAnalysis = DateUtils.getDate().minusDays(ANALYSIS_KEEP_TIME);
		log.info("Create cleaning with cron expression {} at {}, keep {} day(s) recent result",
				DEFAULT_TRIGGER_TIME, DateUtils.getDate(), DEFAULT_KEEP_TIME);
		scheduler = taskScheduler.schedule(
				() -> {
					// Remove resolved error after 7 days
					int deleteResolvedMsg = synchronizerDlqService.deleteBeforeTime(before.atStartOfDay());
					log.info("Deleted resolved error result = {}", deleteResolvedMsg);

					// Remove comparison log after 7 days
					int deleteBeforeTime = comparisonInfoService.deleteBeforeTime(before);
					log.info("Deleted comparison result = {}", deleteBeforeTime);

					// Remove message analysis log after 90 days
					int deletePerTopicBeforeTime = messageAnalysisService.deletePerTopicBeforeTime(limitAnalysis);
					int deletePerMinuteBeforeTime = messageAnalysisService.deletePerMinuteBeforeTime(limitAnalysis);
					log.info("Deleted old records from (tbl_analysis_message_each_topic) Before Time result = {}", deletePerTopicBeforeTime);
					log.info("Deleted old records from (tbl_analysis_message_per_minute) Before Time result = {}", deletePerMinuteBeforeTime);
				},
				new CronTrigger(DEFAULT_TRIGGER_TIME, TimeZone.getTimeZone(TimeZone.getDefault().getID())));
	}

	/**
	 * stop scheduler
	 */
	private void stopAllSchedule() {
		if (log.isDebugEnabled()) {
			log.debug("Stop error cleaning scheduler");
		}
		if (null != scheduler) {
			scheduler.cancel(false);
		}
	}
}
