package com.lguplus.fleta.adapters.rest;

import com.google.gson.Gson;
import com.lguplus.fleta.domain.dto.*;
import com.lguplus.fleta.domain.dto.Synchronizer.SortType;
import com.lguplus.fleta.domain.dto.Synchronizer.SyncState;
import com.lguplus.fleta.domain.dto.rest.HttpResponse;
import com.lguplus.fleta.domain.dto.ui.LogInfo;
import com.lguplus.fleta.domain.dto.ui.SyncErrorDto;
import com.lguplus.fleta.domain.dto.ui.SyncResponse;
import com.lguplus.fleta.domain.model.LastMessageInfoEntity;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.domain.model.SynchronizerHistoryEntity;
import com.lguplus.fleta.domain.service.exception.DatasourceNotFoundException;
import com.lguplus.fleta.domain.service.exception.InvalidKafkaConsumerGroupStateException;
import com.lguplus.fleta.domain.service.exception.InvalidKafkaOffsetTimestampException;
import com.lguplus.fleta.domain.util.DateUtils;
import com.lguplus.fleta.ports.repository.DbComparisonInfoRepository;
import com.lguplus.fleta.ports.repository.SyncHistoryRepository;
import com.lguplus.fleta.ports.service.*;
import com.lguplus.fleta.ports.service.comparison.ComparisonInfoService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dbsync/synchronizers")
@CrossOrigin
public class DbSyncController {

	public static final double THRESHOLD_MAX_RESPONSE_TIME = 1.5;
	private final SynchronizerService synchronizerService;

	private final SyncRequestService requestService;

	private final SyncHistoryService syncHistoryService;

	private final SyncRequestImportService syncRequestImportService;

	private final MessageCollectorService messageCollectorService;

	private final LastMessageInfoService lastMessageInfoService;

	private final SynchronizerDlqService synchronizerDlqService;

	private final DataSourceService dataSourceService;

	private final SyncRequestExportService exportService;

	private final LogService logService;

	public DbSyncController(SynchronizerService synchronizerService,
							SyncRequestService requestService,
							SyncHistoryService syncHistoryService,
							SyncRequestImportService syncRequestImportService,
							MessageCollectorService messageCollectorService,
							LastMessageInfoService lastMessageInfoService,
							SynchronizerDlqService synchronizerDlqService,
							DataSourceService dataSourceService,
							SyncRequestExportService exportService,
							LogService logService) {
		this.synchronizerService = synchronizerService;
		this.requestService = requestService;
		this.syncHistoryService = syncHistoryService;
		this.syncRequestImportService = syncRequestImportService;
		this.messageCollectorService = messageCollectorService;
		this.lastMessageInfoService = lastMessageInfoService;
		this.synchronizerDlqService = synchronizerDlqService;
		this.dataSourceService = dataSourceService;
		this.exportService = exportService;
		this.logService = logService;
	}

	@PutMapping("/error")
	public ResponseEntity<Integer> resolvedAllErrorsByErrorIds(@RequestParam("errorIds") List<Integer> errorIds) {
		if (CollectionUtils.isEmpty(errorIds)) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		try {
			int updated = synchronizerDlqService.resolveAllErrorsByErrorIds(errorIds);
			return new ResponseEntity<>(updated, HttpStatus.OK);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/error/deleteList")
	public ResponseEntity<HttpResponse<String>> deleteErrorsByErrorIds(@RequestParam("errorIds") List<Long> errorIds) {
		if (CollectionUtils.isEmpty(errorIds)) {
			HttpResponse response = new HttpResponse(HttpStatus.BAD_REQUEST.value(),
					"Errors id has no data", "Errors id has no data");
			return ResponseEntity.ok(response);
		}
		try {
			synchronizerDlqService.deleteAllByErrorIds(errorIds);
			HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "Success", "Success!");
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), ex.getCause());
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/error/retries")
	public ResponseEntity<HttpResponse<String>> retriesErrorsByErrorIds(@RequestParam("errorIds") List<Long> errorIds,
																		@RequestParam("topic") String topic) {
		if (CollectionUtils.isEmpty(errorIds)) {
			HttpResponse response = new HttpResponse(HttpStatus.BAD_REQUEST.value(),
					"Errors id has no data", "Errors id has no data");
			return ResponseEntity.ok(response);
		}
		try {
			int retried = synchronizerDlqService.retryToSolveErrorsByErrorIds(errorIds, topic);
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.OK.value(), String.valueOf(retried), String.valueOf(retried));
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), ex.getCause());
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/error/retriesAll")
	public ResponseEntity<HttpResponse<String>> retriesAllErrorsByTopicName(@RequestParam("topicName") String topicName) {
		if (!StringUtils.hasText(topicName)) {
			HttpResponse response = new HttpResponse(HttpStatus.BAD_REQUEST.value(),
					"Errors id has no data", "Errors id has no data");
			return ResponseEntity.ok(response);
		}
		try {
			int numberOfSolvedError = synchronizerDlqService.retryToSolveErrorByTopicName(topicName);
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.OK.value(), String.valueOf(numberOfSolvedError), String.valueOf(numberOfSolvedError));
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), ex.getCause());
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/error/deleteAll")
	public ResponseEntity<HttpResponse<String>> deleteErrorsByTopicName(@RequestParam("topicName") String topicName) {
		if (!StringUtils.hasText(topicName)) {
			HttpResponse response = new HttpResponse(HttpStatus.BAD_REQUEST.value(), "must have topic", "must have topic");
			return ResponseEntity.ok(response);
		}
		try {
			synchronizerDlqService.deleteAllByTopic(topicName);
			HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "Success", "Success!");
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), ex.getCause());
			return ResponseEntity.ok(response);
		}
	}

	@PutMapping("/errorAll")
	public ResponseEntity<HttpResponse<String>> resolvedAllErrorsByTopicName(@RequestParam("topicName") String topicName) {
		if (!StringUtils.hasText(topicName)) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		try {
			int all = synchronizerDlqService.resolveAllErrorsByTopic(topicName);
			HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "OK", all + " Resolved!");
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), ex.getCause());
			return ResponseEntity.ok(response);
		}
	}

	/**
	 * @param topicName
	 * @param pageNo
	 * @param pageSize
	 * @param sortField
	 * @param errorState:     ERROR, RESOLVED, PROCESSING
	 * @param kidOfErrorType: GENERAL_ERROR, CONNECTION_TIMEOUT, ...
	 * @param sortType:       DESC, ASC
	 * @return
	 */
	@GetMapping("/error")
	public ResponseEntity<HttpResponse<SyncErrorDto>> filterErrorsByCondition(@RequestParam("topicName") String topicName,
																			  @RequestParam("pageNo") Integer pageNo,
																			  @RequestParam("pageSize") Integer pageSize,
																			  @RequestParam("sortField") String sortField,
																			  @RequestParam("errorState") String errorState,
																			  @RequestParam("operationState") List<String> operationState,
																			  @RequestParam("kidOfErrorType") String kidOfErrorType,
																			  @RequestParam("sortType") SortType sortType,
																			  @RequestParam("dateFrom") @DateTimeFormat(iso = ISO.DATE) Optional<LocalDate> dateFrom,
																			  @RequestParam("dateTo") @DateTimeFormat(iso = ISO.DATE) Optional<LocalDate> dateTo,
																			  @RequestParam("timeFrom") @DateTimeFormat(iso = ISO.TIME) Optional<LocalTime> timeFrom,
																			  @RequestParam("timeTo") @DateTimeFormat(iso = ISO.TIME) Optional<LocalTime> timeTo) {
		try {
			Sort sort = Sort.by(sortField);
			if (SortType.DESC.equals(sortType)) {
				sort = sort.descending();
			}
			Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
			LocalDateTime from = LocalDateTime.of(2021, 1, 1, 0, 0, 0, 000);                // 기본값

			if (dateFrom.isPresent() && timeFrom.isPresent()) {
				from = LocalDateTime.of(dateFrom.get(), timeFrom.get());
			}
			LocalDateTime to = LocalDateTime.now().plusDays(1);
			if (dateTo.isPresent() && timeTo.isPresent()) {
				to = LocalDateTime.of(dateTo.get(), timeTo.get());
			}
			SyncErrorDto syncErrorDto = synchronizerDlqService.findAllSyncErrorsByTopicName(topicName, errorState, pageable, kidOfErrorType, from, to, operationState);
			HttpResponse<SyncErrorDto> response = new HttpResponse<>(HttpStatus.OK.value(), "Success", syncErrorDto);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			HttpResponse<SyncErrorDto> response = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/topicNames")
	public ResponseEntity<List<SyncInfoBase>> getAllTopicNames() {
		List<SyncInfoBase> collect = this.requestService.getIdAndNameOfSyncTask();
		return ResponseEntity.ok(collect);
	}

	@PostMapping("/resetOffset")
	public ResponseEntity<HttpResponse<Long>> resetOffset(@RequestParam("topicName") String topicName, @RequestParam("synchronizerName") String synchronizerName,
														  @RequestParam("dateFrom") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime dateTime) {
		long resetOffset = 0;
		try {
			resetOffset = this.requestService.resetOffset(topicName, synchronizerName, dateTime);
		} catch (InvalidKafkaOffsetTimestampException ex) {
			HttpResponse response = new HttpResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), resetOffset);
			return ResponseEntity.ok(response);
		} catch (InvalidKafkaConsumerGroupStateException ex) {
			HttpResponse response = new HttpResponse(HttpStatus.PROCESSING.value(), ex.getMessage(), resetOffset);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resetOffset);
			return ResponseEntity.ok(response);
		}
		HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "OK", resetOffset);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/hasAtLeastOneRunning")
	public ResponseEntity<Boolean> hasAtLeastOneRunning(@RequestParam(value = "syncId") Long syncId) {
		SyncRequestEntity syncRequestEntity = this.requestService.findById(syncId);
		if (syncRequestEntity == null) {
			String message = String.format("Synchronizer not found for given id {%s}", syncId);
			log.warn(message);
			return ResponseEntity.ok(false);
		}
		Boolean hasAtLeastOneRunning = this.requestService.hasAtLeastOneRunning(syncRequestEntity.getConsumerGroup());
		return ResponseEntity.ok(hasAtLeastOneRunning);
	}

	@PostMapping("/detectPrimaryKeys")
	public ResponseEntity<HttpResponse<ConstraintKeysAndPartitionInfo>> detectPrimaryKeys(@RequestParam("topicName") String topicName,
																						@RequestParam("division") String division,
																						@RequestParam("sourceDatabase") String sourceDB,
																						@RequestParam("sourceSchema") String sourceSchema,
																						@RequestParam("sourceTable") String sourceTable,
																						@RequestParam("targetDatabase") String targetDB,
																						@RequestParam("targetSchema") String targetSchema,
																						@RequestParam("targetTable") String targetTable) {
		if (!StringUtils.hasText(division)
				&& !StringUtils.hasText(sourceDB)
				&& !StringUtils.hasText(sourceSchema)
				&& !StringUtils.hasText(sourceTable)
				&& !StringUtils.hasText(targetDB)
				&& !StringUtils.hasText(targetSchema)
				&& !StringUtils.hasText(targetTable)) {
			HttpResponse response = new HttpResponse(HttpStatus.BAD_REQUEST.value(), "Invalid parameters", "parameters");
			return ResponseEntity.ok(response);
		}
		try {
			ConstraintKeysAndPartitionInfo constraintKeysAndPartitionInfo = new ConstraintKeysAndPartitionInfo();
			SyncRequestEntity bySynchronizerName = this.requestService.findBySynchronizerName(topicName);
			boolean isPartitioned = this.requestService.detectPartition(division, sourceDB, sourceSchema, sourceTable, targetDB, targetSchema, targetTable);
			String primaryKeys = this.requestService.detectPrimaryKeys(division, targetDB, targetSchema, targetTable);
			String uniqueKeys = this.requestService.detectUniqueKeys(division, targetDB, targetSchema, targetTable);
			if (bySynchronizerName != null) {
				bySynchronizerName.setIsPartitioned(isPartitioned);
				if (StringUtils.hasText(primaryKeys)) {
					bySynchronizerName.setPrimaryKeys(primaryKeys);
				}
				if (StringUtils.hasText(uniqueKeys)) {
					bySynchronizerName.setUniqueKeys(uniqueKeys);
				}
				this.requestService.saveAllSyncRequests(List.of(bySynchronizerName));
			}
			constraintKeysAndPartitionInfo.setPrimaryKeys(primaryKeys);
			constraintKeysAndPartitionInfo.setUniqueKeys(uniqueKeys);
			constraintKeysAndPartitionInfo.setIsPartitioned(isPartitioned);

			HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "OK", constraintKeysAndPartitionInfo);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "");
			return ResponseEntity.ok(response);
		}
	}

	@GetMapping("/topics")
	public ResponseEntity<SyncResponse> getSyncInfosByCondition(@RequestParam("dateFrom") @DateTimeFormat(iso = ISO.DATE) Optional<LocalDate> dateFrom,
																@RequestParam("dateTo") @DateTimeFormat(iso = ISO.DATE) Optional<LocalDate> dateTo,
																@RequestParam("topicNames") List<String> topicNames,
																@RequestParam("state") Optional<SyncState> state,
																@RequestParam("pageNo") Integer pageNo,
																@RequestParam("pageSize") Integer pageSize,
																@RequestParam("sortField") String sortField,
																@RequestParam("divisionValue") String divisionValue,
																@RequestParam("sortType") SortType sortType,
																@RequestParam("db") String db, @RequestParam("schema") String schema) {
		try {
			Sort sort = Sort.by(sortField);
			if (SortType.DESC.equals(sortType)) {
				sort = sort.descending();
			}
			Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
			if (topicNames != null) {
				topicNames.removeIf(x -> x == null || "".equals(x));
			}
			return new ResponseEntity<>(getSyncInfoPaging(pageable,
					dateFrom.isEmpty() ? null : dateFrom.get(),
					dateTo.isEmpty() ? null : dateTo.get(),
					topicNames,
					state.isEmpty() ? null : state.get(), divisionValue,
					db == null ? "" : db, schema == null ? "" : schema), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Searching Synchronizer has an error", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/sortLastReceivedTime")
	public ResponseEntity<SyncResponse> sortLastReceivedTime(@RequestParam("pageNo") Integer pageNo,
															 @RequestParam("pageSize") Integer pageSize,
															 @RequestParam("type") String sortType,
															 @RequestParam("dateFrom") @DateTimeFormat(iso = ISO.DATE) Optional<LocalDate> dateFrom,
															 @RequestParam("dateTo") @DateTimeFormat(iso = ISO.DATE) Optional<LocalDate> dateTo,
															 @RequestParam("topicNames") List<String> topicName,
															 @RequestParam("state") Optional<SyncState> state,
															 @RequestParam("divisionValue") String divisionValue,
															 @RequestParam("db") String db, @RequestParam("schema") String schema) {
		try {
			Sort sort = Sort.by("received_date_time");
			if (SortType.DESC.name().equals(sortType)) {
				sort = sort.descending();
			}
			Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
			return new ResponseEntity<>(getSyncInfoPagingByLastReceivedTime(pageable, sortType, dateFrom.isEmpty() ? null : dateFrom.get(),
					dateTo.isEmpty() ? null : dateTo.get(),
					topicName,
					state.isEmpty() ? null : state.get(), divisionValue, db, schema), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/stateCount")
	public ResponseEntity<SyncStateCountDto> getSyncInfos() {
		try {
			return new ResponseEntity<>(requestService.countSyncState(), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/error/operationCount")
	public ResponseEntity<Object> sumOperationInErrors(@RequestParam("topicName") String topicName,
													   @RequestParam("stateParam") String stateParam) {
		List<Synchronizer.ErrorState> states;
		if (StringUtils.hasText(stateParam)) {
			Synchronizer.ErrorState stateByName = Synchronizer.ErrorState.getStateByName(stateParam);
			if (stateByName == null) {
				states = List.of(Synchronizer.ErrorState.values());
			} else {
				states = List.of(stateByName);
			}
		} else {
			states = List.of(Synchronizer.ErrorState.values());
		}
		try {
			return new ResponseEntity<>(synchronizerDlqService.countOperations(topicName, states), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/division")
	public ResponseEntity<List<String>> getDivision() {
		try {
			return new ResponseEntity<>(requestService.findAllDivision(), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/status/{topic}")
	public ResponseEntity<KafkaMessageListenerState> getSyncStatusByTopicName(@PathVariable("topic") String kafkaTopic) {
		int containerStatus = synchronizerService.getContainerStatus(kafkaTopic);
		return ResponseEntity.ok(KafkaMessageListenerState.of(kafkaTopic, containerStatus));
	}

	@GetMapping("/status")
	public ResponseEntity<List<KafkaMessageListenerState>> getAllSyncStatus() {
		// For all domain tables
		List<String> listenerNames = new ArrayList<>(synchronizerService.findAllSynchronizers());
		final List<KafkaMessageListenerState> listenerStates = new ArrayList<>();
		listenerNames.forEach(kafkaTopic -> {
			int containerStatus = synchronizerService.getContainerStatus(kafkaTopic);
			listenerStates.add(KafkaMessageListenerState.of(kafkaTopic, containerStatus));
		});
		return ResponseEntity.ok(listenerStates);
	}

	@PostMapping("/update")
	public ResponseEntity<HttpResponse<SyncRequestParam>> updateTopic(@RequestBody SyncRequestParam topicParam) {
		SyncRequestEntity topic = requestService.findById(topicParam.getId());
		if (topic == null || topic.getState() == SyncState.RUNNING) {
			HttpResponse response = new HttpResponse(HttpStatus.BAD_REQUEST.value(), "Can not update Running Synchronizer!", null);
			return ResponseEntity.ok(response);
		}
		topicParam.setState(topic.getState());
		return createOrUpdateSynchronizer(topicParam);
	}

	private ResponseEntity<HttpResponse<SyncRequestParam>> createOrUpdateSynchronizer(@RequestBody SyncRequestParam topicParam) {
		try {
			SyncRequestEntity orUpdate = requestService.createOrUpdateSync(topicParam);
			if (topicParam.getId() == null) {
				syncHistoryService.insertHistory(orUpdate.getId(), orUpdate.getTopicName(), orUpdate.getState(), "CREATED", orUpdate);
			} else {
				syncHistoryService.insertHistory(orUpdate.getId(), orUpdate.getTopicName(), orUpdate.getState(), "UPDATED", orUpdate);
			}
			HttpResponse<SyncRequestParam> response = new HttpResponse(HttpStatus.OK.value(), "Success!", orUpdate.toSyncParam());
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping("/add")
	public ResponseEntity<HttpResponse<SyncRequestParam>> addTopic(@RequestBody SyncRequestParam topicParam) {
		if (requestService.findBySynchronizerName(topicParam.getSynchronizerName()) != null) {
			HttpResponse response = new HttpResponse(HttpStatus.BAD_REQUEST.value(), "Synchronizer Name existed!", null);
			return ResponseEntity.ok(response);
		}
		return createOrUpdateSynchronizer(topicParam);
	}

	@GetMapping("/view/{id}")
	public ResponseEntity<SyncRequestParam> getTopicById(@PathVariable("id") long id) {
		SyncRequestEntity topic = requestService.findById(id);
		if (topic == null) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(topic.toSyncParam());
	}

	@GetMapping("/viewByTopic/{topic}")
	public ResponseEntity<SyncRequestParam> viewByTopic(@PathVariable("topic") String topic) {
		return ResponseEntity.ok(requestService.viewByTopicName(topic));
	}

	@GetMapping("/file-logs")
	public ResponseEntity<List<LogInfo>> getAllLogsByTopicName(@RequestParam("topicName") String topicName) {
		return ResponseEntity.ok(this.logService.findAllLogsByTopic(topicName));
	}

	@GetMapping(value = "/download")
	public ResponseEntity<FileSystemResource> downloadFileByPath(@RequestParam(value = "filePath") String filePath) {
		FileSystemResource file = this.logService.downloadLogByPath(filePath);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

	@PostMapping(value = "/start")
	public ResponseEntity<HttpResponse<String>> startSynchronizersByTopicIds(@RequestBody List<Long> topicIds) {
		try {
			log.info("Received {} kafka topics. Trying to start it...", topicIds.size());

			List<SyncRequestEntity> synchronizers = requestService.findByIds(topicIds);
			if (Objects.isNull(synchronizers) || synchronizers.isEmpty()) {
				log.warn("There is no SyncRequestEntity found in the list of {} topics", new Gson().toJson(topicIds));
			} else {
				log.info("Found {} topics. Doing start, please wait...", synchronizers.size());

				synchronizers.forEach(synchronizer -> {
					synchronizerService.startSynchronizer(synchronizer.getTopicName(), synchronizer.getSynchronizerName(), 1, true);
					syncHistoryService.insertHistory(synchronizer.getId(), synchronizer.getTopicName(), synchronizer.getState(), "RUNNING", synchronizer);
					synchronizerService.addToMapDataSource(synchronizer);
				});
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			HttpResponse<String> response = new HttpResponse(HttpStatus.OK.value(), ex.getMessage(), ex);
			return ResponseEntity.ok(response);
		}
		HttpResponse<String> response = new HttpResponse(HttpStatus.OK.value(), "OK", "OK");
		return ResponseEntity.ok(response);
	}

	@PostMapping(value = "/stop")
	public ResponseEntity<HttpResponse<String>> stopSynchronizersByTopicIds(@RequestBody List<Long> topicIds) {
		try {
			log.info("Received {} kafka topics. Trying to stop it...", topicIds.size());

			List<SyncRequestEntity> synchronizers = requestService.findByIds(topicIds);
			if (Objects.isNull(synchronizers) || synchronizers.isEmpty()) {
				log.warn("There is no SyncRequestEntity found in the list of {} topics", new Gson().toJson(topicIds));
			} else {
				log.info("Found {} synchronizer(s). Doing stop, please wait...", synchronizers.size());

				synchronizers.forEach(synchronizer -> {
					synchronizerService.doStop(synchronizer.getTopicName(), synchronizer.getSynchronizerName(), true);
					syncHistoryService.insertHistory(synchronizer.getId(), synchronizer.getTopicName(), synchronizer.getState(), "STOPPED", synchronizer);
				});
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			HttpResponse<String> response = new HttpResponse(HttpStatus.OK.value(), ex.getMessage(), ex);
			return ResponseEntity.ok(response);
		}
		HttpResponse<String> response = new HttpResponse(HttpStatus.OK.value(), "OK", "OK");
		return ResponseEntity.ok(response);
	}

	@PostMapping(value = "/delete")
	public ResponseEntity<HttpResponse<String>> deleteSynchronizersByTopicIds(@RequestBody List<Long> topicIds) {
		if (topicIds == null || topicIds.isEmpty()) {
			HttpResponse<String> body = new HttpResponse(HttpStatus.BAD_REQUEST.value(), "Invalid parameters!", null);
			return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
		}
		try {
			List<SyncRequestEntity> runningTasks = requestService.findRunningTasksByIds(topicIds);
			if (!runningTasks.isEmpty()) {
				HttpResponse<String> body = new HttpResponse(HttpStatus.BAD_REQUEST.value(), "Can not delete running tasks", null);
				return new ResponseEntity<>(body, HttpStatus.OK);
			}
			List<SyncRequestEntity> synchronizers = requestService.findByIds(topicIds);
			synchronizers.forEach(synchronizer -> {
				syncHistoryService.insertHistory(synchronizer.getId(), synchronizer.getTopicName(), synchronizer.getState(), "DELETED", synchronizer);
			});
			requestService.deleteByIds(topicIds);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);

			HttpResponse<String> body = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null);
			return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		HttpResponse<String> body = new HttpResponse(HttpStatus.OK.value(), "Delete success!", null);
		return new ResponseEntity<>(body, HttpStatus.OK);
	}

	@PostMapping("/stopAll")
	public ResponseEntity<HttpResponse<String>> stopAll() {
		List<SyncRequestEntity> synchronizers = requestService.findAllRunningSynchronizer();
		try {
			log.info("Received a request to stop all synchronizers. Please wait...");
			synchronizerService.doStop();
			synchronizers.forEach(synchronizer -> {
				syncHistoryService.insertHistory(synchronizer.getId(), synchronizer.getTopicName(), synchronizer.getState(), "STOPPED", synchronizer);
			});
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);

			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), ex.getCause());
			return ResponseEntity.ok(response);
		} finally {
			dataSourceService.activeInUsedDataSources();
		}
		HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "Stop all success!", "Stop all success!");
		return ResponseEntity.ok(response);
	}

	@PostMapping("/startAll")
	public ResponseEntity<HttpResponse<String>> startAll() {
		try {
			log.info("Received a request to start all synchronizers. Please wait...");

			synchronizerService.startSynchronizer();
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), ex.getCause());
			return ResponseEntity.ok(response);
		}
		HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "Start all success!", "Start all success!");
		return ResponseEntity.ok(response);
	}

	@PostMapping("/deleteAll")
	public ResponseEntity<HttpResponse<String>> deleteAll() {
		int runningSynchronizer = requestService.countRunningSynchronizer();
		List<SyncRequestEntity> synchronizers = requestService.findAll();
		if (runningSynchronizer > 0) {
			HttpResponse response = new HttpResponse(HttpStatus.BAD_REQUEST.value(), "Can not delete Running Synchronizer!", "Can not delete Running Synchronizer!");
			return ResponseEntity.ok(response);
		}
		try {
			synchronizerService.doStop();
			requestService.deleteAll();
			synchronizers.forEach(synchronizer -> {
				syncHistoryService.insertHistory(synchronizer.getId(), synchronizer.getTopicName(), synchronizer.getState(), "DELETED", synchronizer);
			});
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), ex.getCause());
			return ResponseEntity.ok(response);
		}
		HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "Delete all success!", "Delete all success!");
		return ResponseEntity.ok(response);
	}


	@PostMapping(value = "/upload")
	public ResponseEntity<HttpResponse<String>> upload(@RequestParam("files") List<MultipartFile> multipartFiles) {
		for (MultipartFile file : multipartFiles) {
			try {
				List<SyncRequestEntity> importList = syncRequestImportService.readDataFromExcelFile(file.getInputStream());
				if (importList.isEmpty()) {
					HttpResponse response = new HttpResponse(HttpStatus.BAD_REQUEST.value(), "No record imported", "No record imported");
					return ResponseEntity.ok(response);
				}
				// If synchronizer need to create, primary keys and partitioned should be detected automatically
				importList.forEach(bySynchronizerName -> {
					try {
						String primaryKeys = this.requestService.detectPrimaryKeys(bySynchronizerName.getDivision(), bySynchronizerName.getTargetDatabase(), bySynchronizerName.getTargetSchema(), bySynchronizerName.getTargetTable());
						String uniqueKeys = this.requestService.detectUniqueKeys(bySynchronizerName.getDivision(), bySynchronizerName.getTargetDatabase(), bySynchronizerName.getTargetSchema(), bySynchronizerName.getTargetTable());
						bySynchronizerName.setPrimaryKeys(primaryKeys);
						bySynchronizerName.setUniqueKeys(uniqueKeys);
						boolean partition = this.requestService.detectPartition(bySynchronizerName.getDivision(), bySynchronizerName.getSourceDatabase(),
								bySynchronizerName.getSourceSchema(), bySynchronizerName.getSourceTable(),
								bySynchronizerName.getTargetDatabase(), bySynchronizerName.getTargetSchema(), bySynchronizerName.getTargetTable());
						bySynchronizerName.setIsPartitioned(partition);
					} catch (DatasourceNotFoundException e) {
						log.error("Cant not detect primary of {}", bySynchronizerName);
					}
				});
				requestService.saveAllSyncRequests(importList);
				syncHistoryService.saveAllSyncHistory(importList);
			} catch (Exception e) {
				log.error(e.getMessage(), e);

				HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), e.getCause());
				return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		HttpResponse response = new HttpResponse(HttpStatus.OK.value(), "File has uploaded", "File has uploaded");
		return ResponseEntity.ok(response);
	}

	@RequestMapping(value = "/export")
	public ResponseEntity<Object> exportWithConditions(@RequestParam("dateFrom") @DateTimeFormat(iso = ISO.DATE) Optional<LocalDate> dateFrom,
	                                     @RequestParam("dateTo") @DateTimeFormat(iso = ISO.DATE) Optional<LocalDate> dateTo,
	                                     @RequestParam("topicNames") List<String> topicNames,
	                                     @RequestParam("state") Optional<SyncState> state,
	                                     @RequestParam("divisionValue") String divisionValue,
	                                     @RequestParam(value = "db", defaultValue = "") String db,
	                                     @RequestParam(value = "schema", defaultValue = "") String schema) {
		try {
			if (topicNames != null) {
				topicNames.removeIf(x -> x == null || "".equals(x));
			}
			SyncState syncState = state.isEmpty() ? null : state.get();
			LocalDate toDate = dateTo.isEmpty() ? null : dateTo.get();
			LocalDate fromDate = dateFrom.isEmpty() ? null : dateFrom.get();
			InputStreamResource file = new InputStreamResource(this.exportService.exportByConditions(fromDate, toDate, topicNames, syncState, divisionValue, db, schema));
			String fileName = "DBSynchronizer-" + DateUtils.getDate() + ".xlsx";
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
					.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition") // important property for client download with fileName
					.body(file);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			HttpResponse<Object> body = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), e);
			return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private SyncResponse getSyncInfoPaging(Pageable pageable, LocalDate dateFrom, LocalDate dateTo, List<String> topicNames, SyncState state, String divisionValue, String db, String schema) {
		Instant start = Instant.now();
		Page<SyncInfoDto> page = requestService.findWithNumberOfError(pageable, dateFrom, dateTo, topicNames, state, divisionValue, db, schema);
		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toSeconds();
		List<String> topics = page.stream().map(SyncInfoDto::getTopicName).collect(Collectors.toList());
		Map<String, LastMessageInfoDto> receivedMessageMap = this.messageCollectorService.getMapLastMessageInfoByListTopic(topics);
		List<LastMessageInfoEntity> lastMessageList = this.lastMessageInfoService.getLastMessageInfoByListTopic(topics);

		Map<String, LastMessageInfoEntity> lastMessageMap = SyncRequestHelper.getStringLastMessageInfoEntityMap(receivedMessageMap, lastMessageList);

		List<SyncRequestParam> syncInfos = page.stream()
				.map(syncPj -> {
					SyncRequestParam syncInfo = SyncRequestParam.buildFromProjection(syncPj);
					assert syncInfo != null;
					Resource log = logService.findLogByTopic(syncInfo.getTopicName());
					if (log != null) {
						syncInfo.setLogFile(log.getFilename());
					}
					if (lastMessageMap.containsKey(syncInfo.getTopicName())) {
						LastMessageInfoEntity dto = lastMessageMap.get(syncInfo.getTopicName());
						syncInfo.setScn(dto.getScn());
						syncInfo.setCommitScn(dto.getCommitScn());
						syncInfo.setReceivedDate(dto.getReceivedDate());
						syncInfo.setReceivedTime(dto.getReceivedTime());
						syncInfo.setMsgTimestamp(dto.getMsgTimestamp());
					}
					return syncInfo;
				})
				.collect(Collectors.toList());
		SyncResponse syncResponse = new SyncResponse();
		syncResponse.setSynchronizationParams(syncInfos);
		syncResponse.setTotalPage(page.getTotalPages());
		syncResponse.setIsToSlow(timeElapsed > THRESHOLD_MAX_RESPONSE_TIME);
		return syncResponse;
	}

	private SyncResponse getSyncInfoPagingByLastReceivedTime(Pageable pageable, String sortType, LocalDate dateFrom,
															 LocalDate dateTo, List<String> topicName, SyncState state, String divisionValue, String db, String schema) {
		Instant start = Instant.now();
		Page<SyncInfoByLastReceivedTimeDto> page = requestService.findWithNumberOfErrorByLastReceivedTime(pageable, sortType, dateFrom, dateTo, topicName, state, divisionValue, db, schema);
		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toSeconds();

		List<SyncRequestParam> syncInfos = page.stream()
				.map(syncPj -> {
					SyncRequestParam syncInfo = SyncRequestParam.buildFromProjection(syncPj);
					assert syncInfo != null;
					Resource log = logService.findLogByTopic(syncInfo.getTopicName());
					if (log != null) {
						syncInfo.setLogFile(log.getFilename());
					}
					return syncInfo;
				})
				.collect(Collectors.toList());
		SyncResponse syncResponse = new SyncResponse();
		syncResponse.setSynchronizationParams(syncInfos);
		syncResponse.setTotalPage(page.getTotalPages());
		syncResponse.setIsToSlow(timeElapsed > THRESHOLD_MAX_RESPONSE_TIME);
		return syncResponse;
	}

	@Setter
	@Getter
	static
	class ConstraintKeysAndPartitionInfo {

		String primaryKeys;
		String uniqueKeys;

		Boolean isPartitioned;
	}

	interface KafkaMessageListenerState {

		/**
		 * STOPPED Status
		 */
		String STOPPED = "STOPPED";
		/**
		 * PAUSED Status
		 */
		String PAUSED = "PAUSED";
		/**
		 * RUNNING Status
		 */
		String RUNNING = "RUNNING";

		/**
		 * UNKNOWN Status
		 *
		 * @return
		 */
		String UNKNOWN = "UNKNOWN";

		String getTopic();

		String getListenerId();

		String getStatus();

		static KafkaMessageListenerState of(String kafkaTopic, int containerStatus) {
			return new KafkaMessageListenerState() {

				@Override
				public String getTopic() {
					return kafkaTopic;
				}

				@Override
				public String getListenerId() {
					return null;
				}

				@Override
				public String getStatus() {
					if (containerStatus == 0) return UNKNOWN;
					else if (containerStatus == 1) return RUNNING;
					else if (containerStatus == 2) return STOPPED;
					else return PAUSED;
				}
			};
		}
	}

	@GetMapping("error/export")
	public ResponseEntity<Object> exportPrimaryKeys(
			@RequestParam("topicName") String topicName,
			@RequestParam("dateFrom") @DateTimeFormat(iso = ISO.DATE) Optional<LocalDate> dateFrom,
			@RequestParam("dateTo") @DateTimeFormat(iso = ISO.DATE) Optional<LocalDate> dateTo,
			@RequestParam("timeFrom") @DateTimeFormat(iso = ISO.TIME) Optional<LocalTime> timeFrom,
			@RequestParam("timeTo") @DateTimeFormat(iso = ISO.TIME) Optional<LocalTime> timeTo,
			@RequestParam(value = "errorState", required = false) String errorState,
			@RequestParam(value = "errorType", required = false) String errorType,
			@RequestParam(value = "operationState", required = false) List<String> operationState) {
		try {
			LocalDateTime from = LocalDateTime.of(2022, 1, 1, 0, 0, 0, 000);
			if (dateFrom.isPresent() && timeFrom.isPresent()) {
				from = LocalDateTime.of(dateFrom.get(), timeFrom.get()); }
			LocalDateTime to = LocalDateTime.now().plusDays(1);
			if (dateTo.isPresent() && timeTo.isPresent()) {
				to = LocalDateTime.of(dateTo.get(), timeTo.get());  }

			InputStreamResource file = new InputStreamResource(synchronizerDlqService.exportPrimaryKeys(topicName, from, to, errorState, errorType,operationState));
			String fileName = "DlqPkList-" + DateUtils.getDate() + ".xlsx";
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
					.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition") // important property for client download with fileName
					.body(file);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			HttpResponse<Object> body = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), e);
			return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/watchHistory")
	public ResponseEntity<List<SynchronizerHistoryEntity>> watchHistory(@RequestParam("syncId") Long syncId) {
		List<SynchronizerHistoryEntity> entities = syncHistoryService.getHistoryInfo(syncId);
		return ResponseEntity.ok(entities);
	}

}
