package com.lguplus.fleta.ports.service.dbschedule;

import com.lguplus.fleta.domain.dto.Synchronizer;
import com.lguplus.fleta.domain.dto.ui.DbScheduleProcedureResponse;
import com.lguplus.fleta.domain.dto.ui.DbScheduleProcedureResultResponse;
import com.lguplus.fleta.domain.dto.ui.DbSchedulerResultDto;
import com.lguplus.fleta.domain.dto.ui.ResultSummary;
import com.lguplus.fleta.domain.model.DbScheduler;
import com.lguplus.fleta.domain.model.DbSchedulerResult;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DbSchedulerManager {

	DbScheduler add(DbScheduler dbSchedule);

	void delete(Long id);

	DbScheduler view(Long id);

	List<LocalTime> getScheduleByDate(Optional<LocalDate> date);

	void updateScheduleResultById(Long oldIdOfScheduleInfo, Long newIdOfScheduleInfo);

	ResultSummary getResultSummary(LocalDate date, LocalTime time);

	int deleteResult(LocalDate date, LocalTime time);

	void deleteResultByIds(List<Long> ids);

	void deleteRegistrationByIds(List<Long> ids);

	DbScheduleProcedureResponse filter(Pageable pageable, String sql, String targetDB,
									   LocalDate date, List<Boolean> status,
									   Integer pageNo, Integer pageSize, String sortField, Synchronizer.SortType sortType);

	DbScheduleProcedureResultResponse filterResult(Pageable pageable, Optional<String> sql, Optional<String> targetDB,
												   Optional<String> targetSchema, LocalDate date, LocalTime time,
												   Optional<String> table, List<Boolean> status,
												   Integer pageNo, Integer pageSize, String sortField, Synchronizer.SortType sortType);

	Map<LocalTime, List<DbSchedulerResult>> export(LocalDate date);

	Map<LocalTime, List<DbSchedulerResultDto>> exportByDateAndTime(LocalDate date, LocalTime time);
}
