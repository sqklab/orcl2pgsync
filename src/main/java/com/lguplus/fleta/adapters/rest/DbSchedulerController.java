package com.lguplus.fleta.adapters.rest;

import com.lguplus.fleta.domain.dto.Synchronizer;
import com.lguplus.fleta.domain.dto.rest.HttpResponse;
import com.lguplus.fleta.domain.dto.ui.*;
import com.lguplus.fleta.domain.model.DbScheduler;
import com.lguplus.fleta.domain.model.DbSchedulerResult;
import com.lguplus.fleta.domain.util.DateUtils;
import com.lguplus.fleta.ports.service.DataSourceService;
import com.lguplus.fleta.ports.service.dbschedule.DbSchedulerManager;
import com.lguplus.fleta.ports.service.dbschedule.DbSchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dbschedule")
@CrossOrigin
public class DbSchedulerController {

	private final DataSourceService dataSourceService;

	private final DbSchedulerManager dbSchedulerManager;

	private final DbSchedulerService dbSchedulerService;

	public DbSchedulerController(@Qualifier("defaultDatasourceContainer") DataSourceService dataSourceService,
								 DbSchedulerManager dbSchedulerManager,
								 DbSchedulerService dbSchedulerService) {
		this.dataSourceService = dataSourceService;
		this.dbSchedulerManager = dbSchedulerManager;
		this.dbSchedulerService = dbSchedulerService;
	}

	@GetMapping("serverNames")
	public Set<String> getServerNameAvailable() {
		return this.dataSourceService.findAllAvailableDataSources();
	}

	@GetMapping("/filter")
	public ResponseEntity<DbScheduleProcedureResponse> filter(@RequestParam("sql") String sql,
															  @RequestParam("targetDB") String targetDB,
															  @RequestParam(value = "date", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate date,
															  @RequestParam List<Boolean> status,
															  @RequestParam("pageNo") Integer pageNo,
															  @RequestParam("pageSize") Integer pageSize,
															  @RequestParam("sortField") String sortField,
															  @RequestParam("sortType") Synchronizer.SortType sortType) {
		try {
			Sort sort = Sort.by(sortField);
			if (Synchronizer.SortType.DESC.equals(sortType)) {
				sort = sort.descending();
			}
			Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);

			DbScheduleProcedureResponse filter = this.dbSchedulerManager.filter(pageable, sql, targetDB, date, status, pageNo, pageSize, sortField, sortType);
			return ResponseEntity.ok(filter);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/filterResult")
	public ResponseEntity<DbScheduleProcedureResultResponse> filterResult(
			@RequestParam("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date,
			@RequestParam("time") @DateTimeFormat(iso = ISO.TIME) LocalTime time,
			@RequestParam("sql") Optional<String> sql,
			@RequestParam("targetDB") Optional<String> targetDB,
			@RequestParam("targetSchema") Optional<String> targetSchema,
			@RequestParam("table") Optional<String> table,
			@RequestParam List<Boolean> status,
			@RequestParam("pageNo") Integer pageNo,
			@RequestParam("pageSize") Integer pageSize,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortType") Synchronizer.SortType sortType) {
		try {
			Sort sort = Sort.by(sortField);
			if (Synchronizer.SortType.DESC.equals(sortType)) {
				sort = sort.descending();
			}
			Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
			DbScheduleProcedureResultResponse filter = this.dbSchedulerManager.filterResult(pageable, sql, targetDB,
					targetSchema, date, time, table, status, pageNo, pageSize, sortField, sortType);
			return ResponseEntity.ok(filter);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("view")
	public ResponseEntity<HttpResponse<DbScheduler>> view(@RequestParam("id") Long id) {
		try {
			DbScheduler procedure = this.dbSchedulerManager.view(id);
			HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "OK", procedure);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", ex);
			return ResponseEntity.ok(response);
		}
	}

	@GetMapping("getScheduleByDate")
	public ResponseEntity<HttpResponse<List<String>>> getScheduleByDate(@RequestParam("date") @DateTimeFormat(iso = ISO.DATE) Optional<LocalDate> date) {
		try {

			List<LocalTime> times = this.dbSchedulerManager.getScheduleByDate(date);
			List<String> collect = times.stream().map(s -> s.format(DateTimeFormatter.ofPattern("HH:mm"))).collect(Collectors.toList());

			HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "OK", collect);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", ex);
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("exportAllByDate")
	public Map<LocalTime, List<DbSchedulerResult>> exportAllByDate(@RequestParam("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
		return this.dbSchedulerManager.export(date);
	}

	@PostMapping("exportByDateAndTime")
	public Map<LocalTime, List<DbSchedulerResultDto>> exportByDateAndTime(@RequestParam("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date,
																		  @RequestParam("time") @DateTimeFormat(iso = ISO.TIME) LocalTime time) {
		return this.dbSchedulerManager.exportByDateAndTime(date, time);
	}

	@GetMapping("getResultSummary")
	public ResponseEntity<HttpResponse<ResultSummary>> getResultSummary(@RequestParam("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date,
																		@RequestParam("time") @DateTimeFormat(iso = ISO.TIME) LocalTime time) {
		try {
			ResultSummary summary = this.dbSchedulerManager.getResultSummary(date, time);
			HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "OK", summary);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", ex);
			return ResponseEntity.ok(response);
		}
	}

	@PutMapping("deleteResult")
	public ResponseEntity<HttpResponse<ResultSummary>> deleteResult(@RequestParam("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date,
																	@RequestParam("time") @DateTimeFormat(iso = ISO.TIME) LocalTime time) {
		try {
			int rows = this.dbSchedulerManager.deleteResult(date, time);
			HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "OK", rows);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error("Delete result error {}", ex);
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", ex);
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("deleteResultByIds")
	public ResponseEntity<HttpResponse<String>> deleteResultByIds(@RequestParam("ids") List<Long> ids) {
		try {
			this.dbSchedulerManager.deleteResultByIds(ids);
			HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "OK", "OK");
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error("Delete result error {}", ex);
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", ex);
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("deleteRegistrationByIds")
	public ResponseEntity<HttpResponse<String>> deleteRegistrationByIds(@RequestParam("ids") List<Long> ids) {
		try {
			this.dbSchedulerManager.deleteRegistrationByIds(ids);
			HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "OK", "OK");
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error("Delete result error {}", ex);
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", ex);
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("retry")
	public ResponseEntity<HttpResponse<String>> retry(@RequestBody DbSchedulerResult procedureResult) {
		DbScheduler procedure = this.dbSchedulerManager.view(procedureResult.getFkIdProcedure());
		if (procedure == null) {
			log.warn("Invalid Data. The fk-id-procedule={} not found", procedureResult.getFkIdProcedure());
			HttpResponse response = new HttpResponse(HttpStatus.BAD_REQUEST.value(),
					"The schedule has deleted", "The schedule has deleted");
			return ResponseEntity.ok(response);
		}
		try {
			this.dbSchedulerService.retry(procedureResult, procedure);
			HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "OK", "OK");
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error("Delete result error {}", ex);
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", ex);
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("test")
	public ResponseEntity<HttpResponse<String>> testScript(@RequestBody DbScheduler procedure) {
		try {
			this.dbSchedulerService.testScript(procedure);
			HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "OK", "OK");
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error("Delete result error {}", ex);
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", ex.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("scheduleAll")
	public ResponseEntity<HttpResponse<SimpleDbSchedulerResultDto>> scheduleAll() {
		try {
			SimpleDbSchedulerResultDto result = this.dbSchedulerService.scheduleAll();
			HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "OK", result);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error("Delete result error {}", ex);
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", ex.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	@GetMapping("countAll")
	public ResponseEntity<HttpResponse<Integer>> countAll() {
		try {
			long result = this.dbSchedulerService.total();
			HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "OK", result);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error("Delete result error {}", ex);
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", ex.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("add")
	@Transactional
	public ResponseEntity<HttpResponse<DbScheduler>> insertOrUpdate(@RequestBody() DbScheduler dbScheduleProcedure) {
		try {
			Long olderId = dbScheduleProcedure.getId();
			boolean isUpdate = olderId != null;
			boolean isCreateNew = !isUpdate;
			LocalDateTime bkCreatedAt = dbScheduleProcedure.getCreatedAt();
			boolean isChangeToNewSchedule = false;
			if (isUpdate && this.isChangedTime(dbScheduleProcedure)) { // it's updating
				this.dbSchedulerManager.delete(olderId);
				dbScheduleProcedure.setId(null);
				isChangeToNewSchedule = true;
			}
			dbScheduleProcedure.setProcessStatus(dbScheduleProcedure.getStatus());
			dbScheduleProcedure.setCreatedAt(isUpdate ? bkCreatedAt : DateUtils.getDateTime());
			dbScheduleProcedure.setUpdatedAt(DateUtils.getDateTime());
			dbScheduleProcedure.setLastRun(DateUtils.getDateTime());
			DbScheduler added = dbSchedulerManager.add(dbScheduleProcedure);
			if (isCreateNew || isChangeToNewSchedule) {
				this.dbSchedulerService.run(added);
			}
			// update references
			if (isUpdate && isChangeToNewSchedule) {
				this.dbSchedulerManager.updateScheduleResultById(olderId, added.getId());
			}
			HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "OK", dbScheduleProcedure);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error("*** insertOrUpdate Schedule has an error", ex);
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", ex);
			return ResponseEntity.ok(response);
		}
	}

	private boolean isChangedTime(DbScheduler dbScheduleProcedure) {
		if (dbScheduleProcedure.getId() == null) {
			return false;
		}
		DbScheduler currentSchedule = this.dbSchedulerManager.view(dbScheduleProcedure.getId());
		if (dbScheduleProcedure.getType() != currentSchedule.getType()) {
			return true;
		}
		if (dbScheduleProcedure.isDaily()) {
			boolean isDifferentTimeDaily = !dbScheduleProcedure.getTimeDaily().equals(currentSchedule.getTimeDaily());
			if (isDifferentTimeDaily) {
				return true;
			}
		}
		if (dbScheduleProcedure.isWeekly()) {
			boolean isDifferentDayOfWeek = !dbScheduleProcedure.getDayOfWeek().equals(currentSchedule.getDayOfWeek());
			if (isDifferentDayOfWeek) {
				return true;
			}
			boolean isDifferentTimeDayOfWeek = dbScheduleProcedure.getTimesOfWeek().equals(currentSchedule.getTimesOfWeek());
			if (isDifferentTimeDayOfWeek) {
				return true;
			}
		}
		if (dbScheduleProcedure.isMonthly()) {
			return !dbScheduleProcedure.getMonthly().equals(currentSchedule.getMonthly());
		}
		if (dbScheduleProcedure.isQuarterly()) {
			return !dbScheduleProcedure.getQuarterly().equals(currentSchedule.getQuarterly());
		}
		if (dbScheduleProcedure.isYearly()) {
			return !dbScheduleProcedure.getYearly().equals(currentSchedule.getYearly());
		}
		return false;
	}
}
