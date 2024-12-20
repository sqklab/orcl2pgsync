package com.lguplus.fleta.domain.service.comparison;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class SchedulerConfiguration {
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        final JdbcTemplateLockProvider.Configuration schdlockConfiguration;

        schdlockConfiguration = JdbcTemplateLockProvider.Configuration.builder()
                .withTableName("tbl_shedlock")
                .withColumnNames(new JdbcTemplateLockProvider.ColumnNames(
                        "name",
                        "lock_until",
                        "locked_at",
                        "locked_by"
                ))
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                //.usingDbTime()
                .build();

        return new JdbcTemplateLockProvider(schdlockConfiguration);
    }
}
