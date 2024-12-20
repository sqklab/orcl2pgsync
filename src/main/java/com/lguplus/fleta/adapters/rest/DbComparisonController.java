package com.lguplus.fleta.adapters.rest;

import com.lguplus.fleta.adapters.messagebroker.KafkaMessagesBehindApi;
import com.lguplus.fleta.domain.dto.LastMessageInfoDto;
import com.lguplus.fleta.domain.dto.Synchronizer;
import com.lguplus.fleta.domain.dto.comparison.DbComparisonScheduleDto;
import com.lguplus.fleta.domain.dto.rest.HttpResponse;
import com.lguplus.fleta.domain.dto.ui.ComparisonResultForExport;
import com.lguplus.fleta.domain.dto.ui.QuickRunComparisonQuery;
import com.lguplus.fleta.domain.dto.ui.ViewComparisonResult;
import com.lguplus.fleta.domain.model.LastMessageInfoEntity;
import com.lguplus.fleta.domain.model.comparison.ComparisonEntity;
import com.lguplus.fleta.domain.model.comparison.ComparisonFilter;
import com.lguplus.fleta.domain.model.comparison.ComparisonResultEntity;
import com.lguplus.fleta.domain.model.comparison.DbComparisonResultSummaryEntity;
import com.lguplus.fleta.domain.model.comparison.DbComparisonSchedulerEntity.ComparisonScheduleState;
import com.lguplus.fleta.domain.service.comparison.ComparerHelper;
import com.lguplus.fleta.domain.service.constant.Constants;
import com.lguplus.fleta.ports.service.DbComparisonSchedulerService;
import com.lguplus.fleta.ports.service.LastMessageInfoService;
import com.lguplus.fleta.ports.service.MessageCollectorService;
import com.lguplus.fleta.ports.service.comparison.ComparisonInfoService;
import com.lguplus.fleta.ports.service.comparison.ComparisonSchedulerService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.lguplus.fleta.domain.service.constant.Constants.PATTERN_DATE_FORMAT;

@Slf4j
@RestController
@RequestMapping("/comparison")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DbComparisonController {

	private final ComparisonInfoService comparisonInfoService;

	private final ComparisonSchedulerService schedulerService;

	private final DbComparisonSchedulerService dbComparisonScheduleService;

	private final MessageCollectorService messageCollectorService;

	private final LastMessageInfoService lastMessageInfoService;

	private final ComparerHelper comparerHelper;

	@Value("${app.comparison-worker.step-time:5}")
	private int stepTime;

	public DbComparisonController(
			ComparisonInfoService comparisonInfoService,
			ComparisonSchedulerService schedulerService,
			DbComparisonSchedulerService dbComparisonScheduleService,
			MessageCollectorService messageCollectorService,
			LastMessageInfoService lastMessageInfoService,
			ComparerHelper comparerHelper
	) {
		this.comparisonInfoService = comparisonInfoService;
		this.schedulerService = schedulerService;
		this.dbComparisonScheduleService = dbComparisonScheduleService;
		this.messageCollectorService = messageCollectorService;
		this.lastMessageInfoService = lastMessageInfoService;
		this.comparerHelper = comparerHelper;
	}

	@RequestMapping("schedules")
	public List<DbComparisonScheduleDto> getSchedules() {
		return this.comparisonInfoService.getSchedules();
	}

	@PutMapping("schedules/update")
	public ResponseEntity<HttpResponse> updateSchedules(@RequestBody List<DbComparisonScheduleDto> body) {
		long runningCount = dbComparisonScheduleService.countByState();
		this.comparisonInfoService.deleteAll();
		this.comparisonInfoService.save(body.stream()
				.map(DbComparisonScheduleDto::toEntity)
				.map(en -> {
					if (runningCount > 0) {
						en.setState(ComparisonScheduleState.ENABLED);
					} else {
						en.setState(ComparisonScheduleState.DISABLED);
					}
					return en;
				})
				.collect(Collectors.toList()));

		HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "Success", "Success");
		return ResponseEntity.ok(response);
	}

	@GetMapping("/runningCount")
	public ResponseEntity<?> scheduleStatus() {
		return ResponseEntity.ok(dbComparisonScheduleService.countByState());
	}

	@GetMapping("/getTimeOnServer")
	public ResponseEntity<HttpResponse> getTimeOnServer() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN_DATE_FORMAT);
		LocalDateTime dateTime = LocalDateTime.now(Constants.ZONE_ID);

		HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "Success",
				new CustomDateTime(dateTime.format(formatter), dateTime.getMonth().getValue(), PATTERN_DATE_FORMAT, Constants.ZONE_ID.getId()));
		return ResponseEntity.ok(response);
	}

	@GetMapping("/startSchedule")
	public ResponseEntity<?> startSchedule() {
		int start = dbComparisonScheduleService.start();
		return ResponseEntity.ok(start);
	}

	@GetMapping("/stopSchedule")
	public ResponseEntity<?> stopSchedule() {
		int stop = dbComparisonScheduleService.stop();
		return ResponseEntity.ok(stop);
	}

	@RequestMapping("viewInfoForSearching")
	public List<ComparisonEntity> viewInfo() {
		return this.comparisonInfoService.getViewInfo();
	}

	@RequestMapping("getResultByDate")
	public List<LocalTime> getResultByDate(@RequestParam("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
		return this.comparisonInfoService.getResultByDate(date);
	}

	@RequestMapping("viewKafkaConsumerGroup")
	public List<KafkaMessagesBehindApi.Partition> viewKafkaConsumerGroup(@RequestParam(name = "topN", required = false, defaultValue = "10000") int topN,
																		 @RequestParam(name = "reverse", required = false, defaultValue = "0") int reverse,
																		 @RequestParam(name = "filter", required = false, defaultValue = "0") int filter) {
		try {
			return this.comparisonInfoService.viewKafkaConsumerGroup(filter, reverse, topN);
		} catch (IOException e) {
			log.error("Can not fetch kafka api to get consumer group info", e);
			return new ArrayList<>();
		}
	}

	@RequestMapping("getSummaryByDateAndTime")
	public DbComparisonResultSummaryEntity getSummary(@RequestParam("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date,
													  @RequestParam("time") @DateTimeFormat(iso = ISO.TIME) LocalTime time) {
		return this.comparisonInfoService.getComparisonSummary(date, time);
	}

	@PostMapping("deleteSummaryByDateAndTime")
	public ResponseEntity<Boolean> deleteDatasource(@RequestParam("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date,
													@RequestParam("time") @DateTimeFormat(iso = ISO.TIME) LocalTime time) {
		return ResponseEntity.status(HttpStatus.OK).body(comparisonInfoService.deleteComparisonSummary(date, time));
	}

	@PostMapping("exportAllByDate")
	public ComparisonResultForExport exportAllByDate(@RequestParam("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
		return this.comparisonInfoService.export(date);
	}

	@PostMapping("exportByDateAndTime")
	public Map<LocalTime, List<ComparisonResultEntity>> exportByDateAndTime(@RequestParam("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date,
																			@RequestParam("time") @DateTimeFormat(iso = ISO.TIME) LocalTime time) {
		return this.comparisonInfoService.exportByDateAndTime(date, time);
	}

	@GetMapping("/compareAll")
	public ResponseEntity<HttpResponse<?>> compareAll() {
		LocalDateTime now = LocalDateTime.now(Constants.ZONE_ID);
		LocalDateTime earliestLastRun = comparisonInfoService.getEarliestLastRun();
		boolean skip = null != earliestLastRun && Math.abs(Duration.between(earliestLastRun, now).toMinutes()) < stepTime;
		if (skip) {
			HttpResponse<?> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(),
					"Other recent comparison request is running",
					"Other recent comparison request is running");
			return ResponseEntity.badRequest().body(response);
		}

		schedulerService.startOneTimeNow(now.toLocalTime(), now.toLocalDate());
		HttpResponse<?> response = new HttpResponse<>(HttpStatus.OK.value(), "Started comparing all", "Started comparing all");
		return ResponseEntity.ok(response);
	}

	@PostMapping(value = "compareSelected")
	public ResponseEntity<HttpResponse<?>> compareSelectedIds(@RequestBody() List<Long> ids) {
		try {
			comparisonInfoService.compareSelectedIds(ids);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);

			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
			return ResponseEntity.ok(response);
		}
		HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "Started comparing", "Started comparing");
		return ResponseEntity.ok(response);
	}

	@PostMapping(value = "quickRunQuery")
	public ResponseEntity<HttpResponse<?>> quickRunQuery(@RequestBody() QuickRunComparisonQuery comparisonQuery) {
		try {
			long computeCount = comparerHelper.executeCountQueryToTargetDb(comparisonQuery.getDatabase(), comparisonQuery.getQuery());
			HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "Success!", computeCount);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), ex.getCause());
			return ResponseEntity.ok(response);
		}
	}

	@GetMapping("/filter")
	public ResponseEntity<ViewComparisonResult> search(@RequestParam("dateFrom") @DateTimeFormat(iso = ISO.DATE) LocalDate dateFrom,
													   @RequestParam("time") @DateTimeFormat(iso = ISO.TIME) LocalTime time,
													   @RequestParam("sourceDB") Optional<String> sourceDB,
													   @RequestParam("sourceSchema") Optional<String> sourceSchema,
													   @RequestParam("targetDB") Optional<String> targetDB,
													   @RequestParam("targetSchema") Optional<String> targetSchema,
													   @RequestParam("sourceTable") Optional<String> sourceTable,
													   @RequestParam("targetTable") Optional<String> targetTable,
													   @RequestParam("state") Optional<ComparisonResultEntity.ComparisonState> state,
													   @RequestParam("pageNo") Integer pageNo,
													   @RequestParam("pageSize") Integer pageSize,
													   @RequestParam("sortField") String sortField,
													   @RequestParam("division") String division,
													   @RequestParam(value = "sortType", defaultValue = "ASC") String sortType) {
		try {
			Sort sort = Sort.by(sortField);
			if (Synchronizer.SortType.DESC.name().equals(sortType)) {
				sort = sort.descending();
			}
			Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);

			return ResponseEntity.ok(this.filter(pageable, dateFrom, time, sourceDB, sourceSchema, targetDB, targetSchema, sourceTable, targetTable, state, division));
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private ViewComparisonResult filter(Pageable pageable,
										LocalDate dateFrom,
										LocalTime time,
										Optional<String> sourceDB,
										Optional<String> sourceSchema,
										Optional<String> targetDB,
										Optional<String> targetSchema,
										Optional<String> sourceTable,
										Optional<String> targetTable,
										Optional<ComparisonResultEntity.ComparisonState> state,
										String division) {
		ComparisonFilter filter = new ComparisonFilter(dateFrom, time, sourceDB.orElse(""),
				sourceSchema.orElse(""), targetDB.orElse(""),
				targetSchema.orElse(""), sourceTable.orElse(""),
				targetTable.orElse(""), state.map(Arrays::asList).orElseGet(() -> Arrays.asList(ComparisonResultEntity.ComparisonState.values())), division);

		Page<ComparisonResultEntity> entities = comparisonInfoService.filter(pageable, filter);
		List<String> topics = entities.getContent().stream().map(ComparisonResultEntity::getTopicName).collect(Collectors.toList());
		Map<String, LastMessageInfoDto> receivedMessageMap = this.messageCollectorService.getMapLastMessageInfoByListTopic(topics);
		List<LastMessageInfoEntity> lastMessageList = this.lastMessageInfoService.getLastMessageInfoByListTopic(topics);

		Map<String, LastMessageInfoEntity> lastMessageMap = SyncRequestHelper.getStringLastMessageInfoEntityMap(receivedMessageMap, lastMessageList);

		List<ComparisonResultEntity> collect = entities.stream().map(entity -> {
			assert entity != null;
			if (lastMessageMap.containsKey(entity.getTopicName())) {
				LastMessageInfoEntity dto = lastMessageMap.get(entity.getTopicName());
				entity.setScn(dto.getScn());
				entity.setCommitScn(dto.getCommitScn());
				entity.setReceivedDate(dto.getReceivedDate());
				entity.setReceivedTime(dto.getReceivedTime());
				entity.setMsgTimestamp(dto.getMsgTimestamp());
			}
			return entity;
		}).collect(Collectors.toList());

		ViewComparisonResult response = new ViewComparisonResult();
		response.setEntities(collect);
		response.setTotalPage(entities.getTotalPages());

		return response;
	}

	@Setter
	@Getter
	@AllArgsConstructor
	static class CustomDateTime {
		private String dateTime;
		private int month;
		private String formatter;
		private String zoneId;
	}
}
