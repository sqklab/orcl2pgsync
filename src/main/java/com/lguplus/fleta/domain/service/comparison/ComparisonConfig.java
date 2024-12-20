package com.lguplus.fleta.domain.service.comparison;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ComparisonConfig {

    private final ThreadPoolTaskExecutor taskExecutor;

    public ComparisonConfig(
            @Value("${comparison.thread:10}") Integer thread,
            @Value("${comparison.queue-capacity:1000}") Integer queueCapacity
    ) {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(thread); // 기본 스레드 수
        taskExecutor.setMaxPoolSize(thread); // 최대 스레드 수
        taskExecutor.setQueueCapacity(queueCapacity); // QUEUE 수
        taskExecutor.setThreadNamePrefix("comparisonPool-");
        taskExecutor.initialize();
        this.taskExecutor = taskExecutor;
    }

    @Bean("comparisonPool")
    public ThreadPoolTaskExecutor comparisonPoolTaskExecutor() {
        return taskExecutor;
    }
}
