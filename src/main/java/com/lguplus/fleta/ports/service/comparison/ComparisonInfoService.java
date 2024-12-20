package com.lguplus.fleta.ports.service.comparison;

import ch.qos.logback.classic.Logger;
import com.lguplus.fleta.adapters.messagebroker.KafkaMessagesBehindApi;
import com.lguplus.fleta.domain.dto.comparison.DbComparisonScheduleDto;
import com.lguplus.fleta.domain.dto.ui.ComparisonResultForExport;
import com.lguplus.fleta.domain.model.comparison.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface ComparisonInfoService {

	LocalDateTime getEarliestLastRun();

	void compareAll(LocalDate date, LocalTime time, int stepTime, Logger log);

	List<DbComparisonScheduleDto> getSchedules();

	void save(List<DbComparisonSchedulerEntity> body);

	void deleteAll();

	List<ComparisonEntity> getViewInfo();

	List<LocalTime> getResultByDate(LocalDate compareDate);

	// Export by Date
	ComparisonResultForExport export(LocalDate compareDate);

	// Export by Date and Time
	Map<LocalTime, List<ComparisonResultEntity>> exportByDateAndTime(LocalDate compareDate, LocalTime time);

	DbComparisonResultSummaryEntity getComparisonSummary(LocalDate compareDate, LocalTime time);

	boolean deleteComparisonSummary(LocalDate compareDate, LocalTime time);

	/**
	 * @param ids of DbComparisonResultEntity
	 */
	void compareSelectedIds(List<Long> ids);

	Page<ComparisonResultEntity> filter(Pageable pageable, ComparisonFilter filter);

	int deleteBeforeTime(LocalDate date);

	List<KafkaMessagesBehindApi.Partition> viewKafkaConsumerGroup(int filter, int reverse, int topN) throws IOException;


	List<DbComparisonInfoEntity> findAllBySyncInfoId(long syncId);
}
