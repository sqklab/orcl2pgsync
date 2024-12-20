package com.lguplus.fleta.domain.dto.ui;

import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface DbSchedulerResultDto {

	@Value("#{target.id}")
	Long getId();

	@Value("#{target.fk_id_schedule_procedure}")
	String getFkIdProcedure();

	@Value("#{target.error_msg}")
	String getErrorMsg();

	@Value("#{target.schedule_time}")
	LocalTime getScheduleTime();

	@Value("#{target.schedule_date}")
	LocalDate getScheduleDate();

	@Value("#{target.status}")
	Boolean getStatus();

	@Value("#{target.start_at}")
	LocalDateTime getStartAt();

	@Value("#{target.end_at}")
	LocalDateTime getEndAt();

	@Value("#{target.pl_sql}")
	String getPlSQL();

	@Value("#{target.schedule_schema}")
	String getSchema();

	@Value("#{target.schedule_table}")
	String getTable();

	@Value("#{target.db}")
	String getDb();
}
