package com.lguplus.fleta.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableScheduling
public class TaskSchedulerConfig implements SchedulingConfigurer {

	private static final int POOL_SIZE = 10;

	private static final int N_THREADS = 128; // Prod memory was 64GB

	/**
	 * Configures the scheduler to allow multiple pools.
	 *
	 * @param taskRegistrar The task registrar.
	 */
	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(scheduledExecutorCommon());
	}

	/**
	 * thread pool for scheduler parallel task (not async):
	 * pull block, trans monitor, statistic trans, delete info, reset groupList
	 *
	 * @return ThreadPoolTaskScheduler
	 */
	@Bean(destroyMethod = "shutdown")
	@Qualifier("defaultThreadPool")
	public ThreadPoolTaskScheduler defaultThreadPool() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(POOL_SIZE);
		taskScheduler.setThreadNamePrefix("DefaultPool-");
		taskScheduler.setAwaitTerminationSeconds(600);
		taskScheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
		taskScheduler.setErrorHandler(throwable -> log.error("An exception occurred in the scheduled task", throwable));
		return taskScheduler;
	}

	@Bean
	@Qualifier("commonThreadPool")
	public ExecutorService commonThreadPool() {
		return Executors.newFixedThreadPool(POOL_SIZE);
	}

	@Bean
	@Qualifier("messageCollectorThreadPool")
	public ExecutorService messageCollectorThreadPool() {
		// Creates a thread pool that reuses a fixed number of threads operating off a shared unbounded
		// queue. At any point, at most nThreads threads will be active processing tasks. If additional tasks
		// are submitted when all threads are active, they will wait in the queue until a thread is available. If
		// any thread terminates due to a failure during execution prior to shutdown, a new one will take its
		// place if needed to execute subsequent tasks. The threads in the pool will exist until it is explicitly
		return Executors.newFixedThreadPool(N_THREADS);
	}

	@Bean
	@Qualifier("scheduledExecutorCommon")
	public ScheduledExecutorService scheduledExecutorCommon() {
		// Creates a thread pool that can schedule commands to run after a given delay,
		// or to execute periodically.
		return Executors.newScheduledThreadPool(POOL_SIZE);
	}
}
