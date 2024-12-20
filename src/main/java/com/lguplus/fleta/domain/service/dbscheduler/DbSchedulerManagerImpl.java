package com.lguplus.fleta.domain.service.dbscheduler;

import com.lguplus.fleta.domain.dto.Synchronizer;
import com.lguplus.fleta.domain.dto.ui.DbScheduleProcedureResponse;
import com.lguplus.fleta.domain.dto.ui.DbScheduleProcedureResultResponse;
import com.lguplus.fleta.domain.dto.ui.DbSchedulerResultDto;
import com.lguplus.fleta.domain.dto.ui.ResultSummary;
import com.lguplus.fleta.domain.model.DbScheduler;
import com.lguplus.fleta.domain.model.DbSchedulerResult;
import com.lguplus.fleta.domain.util.SQLBuilder;
import com.lguplus.fleta.ports.repository.DbScheduleProcedureRepository;
import com.lguplus.fleta.ports.repository.DbScheduleProcedureResultRepository;
import com.lguplus.fleta.ports.service.dbschedule.DbSchedulerManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DbSchedulerManagerImpl implements DbSchedulerManager {

	private final DbScheduleProcedureRepository scheduleRepository;
	private final DbScheduleProcedureResultRepository resultRepository;

	public DbSchedulerManagerImpl(DbScheduleProcedureRepository scheduleRepository, DbScheduleProcedureResultRepository resultRepository) {
		this.scheduleRepository = scheduleRepository;
		this.resultRepository = resultRepository;
	}

	@Override
	public DbScheduler add(DbScheduler dbSchedule) {
		return this.scheduleRepository.save(dbSchedule);
	}

	@Override
	public void delete(Long id) {
		scheduleRepository.deleteById(id);
	}

	@Override
	public DbScheduler view(Long id) {
		return scheduleRepository.findById(id).orElse(null);
	}

	@Override
	public List<LocalTime> getScheduleByDate(Optional<LocalDate> date) {
		return this.resultRepository.getScheduleByDate(date.get());
	}

	@Override
	public void updateScheduleResultById(Long oldIdOfScheduleInfo, Long newIdOfScheduleInfo) {
		int updated = this.resultRepository.updateScheduleResultById(oldIdOfScheduleInfo, newIdOfScheduleInfo);
		log.info("*** update schedule result following new id, total rows updated={}", updated);
	}

	@Override
	public ResultSummary getResultSummary(LocalDate date, LocalTime time) {
		List<DbSchedulerResultDto> resultSummary = resultRepository.getResultSummary(date, time);
		long success = resultSummary.stream().filter(DbSchedulerResultDto::getStatus).count();
		long failure = resultSummary.stream().filter(s -> !s.getStatus()).count();
		return new ResultSummary(success, failure, resultSummary.size());
	}

	@Override
	public int deleteResult(LocalDate date, LocalTime time) {
		return resultRepository.deleteByScheduleDateAndScheduleTime(date, time);
	}

	@Override
	public void deleteResultByIds(List<Long> ids) {
		resultRepository.deleteAllById(ids);
	}

	@Override
	@Transactional
	public void deleteRegistrationByIds(List<Long> ids) {
		this.scheduleRepository.deleteAllById(ids);
		this.resultRepository.deleteAllByFkIdProcedureIn(ids);
	}

	@Override
	public DbScheduleProcedureResponse filter(Pageable pageable, String sql, String targetDB, LocalDate date, List<Boolean> status, Integer pageNo, Integer pageSize,
											  String sortField, Synchronizer.SortType sortType) {
		Page<DbScheduler> entities;
		if (date != null) {
			entities = this.scheduleRepository.filterWithDate(pageable, SQLBuilder.prepareSearchTerms(sql), SQLBuilder.prepareSearchTerms(targetDB), date, status);
		} else {

			entities = this.scheduleRepository.filter(pageable, SQLBuilder.prepareSearchTerms(sql), SQLBuilder.prepareSearchTerms(targetDB), status);
		}
		List<DbScheduler> collect = entities.stream().collect(Collectors.toList());
		DbScheduleProcedureResponse response = new DbScheduleProcedureResponse();
		response.setEntities(collect);
		response.setTotalPage(entities.getTotalPages());

		return response;
	}

	@Override
	public DbScheduleProcedureResultResponse filterResult(Pageable pageable, Optional<String> sql, Optional<String> targetDB,
														  Optional<String> targetSchema, LocalDate date, LocalTime time, Optional<String> table,
														  List<Boolean> status, Integer pageNo, Integer pageSize,
														  String sortField, Synchronizer.SortType sortType) {
		Page<DbSchedulerResultDto> entities = this.resultRepository.filter(pageable,
				SQLBuilder.prepareSearchTerms(sql.get()), SQLBuilder.prepareSearchTerms(targetDB.get()),
				date, time, status);

		List<DbSchedulerResultDto> collect = entities.stream().collect(Collectors.toList());
		DbScheduleProcedureResultResponse response = new DbScheduleProcedureResultResponse();
		response.setEntities(collect);
		response.setTotalPage(entities.getTotalPages());

		return response;
	}

	@Override
	public Map<LocalTime, List<DbSchedulerResult>> export(LocalDate date) {
		return this.resultRepository.findByScheduleDateOrderByScheduleTime(date).stream().collect(
				Collectors.groupingBy(DbSchedulerResult::getScheduleTime, Collectors.toList()));
	}

	@Override
	public Map<LocalTime, List<DbSchedulerResultDto>> exportByDateAndTime(LocalDate date, LocalTime time) {
		return this.resultRepository.findByScheduleDateAndScheduleTimeOrderByScheduleTime(date, time).stream().collect(
				Collectors.groupingBy(DbSchedulerResultDto::getScheduleTime, Collectors.toList()));
	}
}
