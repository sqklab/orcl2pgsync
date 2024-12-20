package com.lguplus.fleta.domain.service.comparison;

import ch.qos.logback.classic.Logger;
import com.lguplus.fleta.adapters.messagebroker.KafkaConstants;
import com.lguplus.fleta.adapters.messagebroker.KafkaMessagesBehindApi;
import com.lguplus.fleta.adapters.rest.SyncRequestHelper;
import com.lguplus.fleta.domain.dto.LastMessageInfoDto;
import com.lguplus.fleta.domain.dto.MessagesBehindInfo;
import com.lguplus.fleta.domain.dto.comparison.ComparisonSummary;
import com.lguplus.fleta.domain.dto.comparison.DbComparisonScheduleDto;
import com.lguplus.fleta.domain.dto.ui.ComparisonResultDto;
import com.lguplus.fleta.domain.dto.ui.ComparisonResultForExport;
import com.lguplus.fleta.domain.model.LastMessageInfoEntity;
import com.lguplus.fleta.domain.model.comparison.*;
import com.lguplus.fleta.domain.service.constant.Constants;
import com.lguplus.fleta.domain.util.DateUtils;
import com.lguplus.fleta.domain.util.SQLBuilder;
import com.lguplus.fleta.ports.repository.*;
import com.lguplus.fleta.ports.service.LastMessageInfoService;
import com.lguplus.fleta.ports.service.LoggerManager;
import com.lguplus.fleta.ports.service.MessageCollectorService;
import com.lguplus.fleta.ports.service.SyncRequestService;
import com.lguplus.fleta.ports.service.comparison.ComparisonExecutorService;
import com.lguplus.fleta.ports.service.comparison.ComparisonInfoService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.lguplus.fleta.domain.service.constant.Constants.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
public class ComparisonInfoServiceImpl extends ComparisonBase implements ComparisonInfoService {

	private static final char DOT = '.';
	private final DbComparisonInfoRepository comparisonInfoRepo;
	private final DbComparisonScheduleRepository scheduleRepo;
	private final ViewComparisonInfoRepository viewComparisonInfoRepo;
	private final ViewComparisonResultRepository viewComparisonResultRepo;
	private final DbComparisonResultRepository dbComparisonResultRepo;
	private final DbComparisonResultSummaryRepository summaryRepository;
	private final ComparisonExecutorService executorService;
	private final KafkaMessagesBehindApi kafkaMessagesBehindApi;
	private final MessageCollectorService messageCollectorService;
	private final LastMessageInfoService lastMessageInfoService;
	private final SyncRequestService syncRequestService;
	private final ThreadPoolTaskExecutor comparisonPool;
	private final ComparerHelper comparerHelper;

	@Value("${app.comparison-worker.step-time:5}")
	private int defaultStepTime;
	@Value("${comparison.retry.count:3}")
	private int RETRY_COUNT;
	@Value("${comparison.retry.condition.difference.max:1000}")
	private int RETRY_CONDITION_DIFFERENCE_MAX;
	@Value("${comparison.retry.wait-interval-each-seconds:90}")
	private int WAIT_INTERVAL;

	public ComparisonInfoServiceImpl(DbComparisonInfoRepository comparisonInfoRepository,
									 DbComparisonScheduleRepository scheduleRepository,
									 ViewComparisonInfoRepository viewComparisonInfoRepository,
									 ViewComparisonResultRepository viewComparisonResultRepository,
									 DbComparisonResultRepository dbComparisonResultRepository,
									 DbComparisonResultSummaryRepository summaryRepository,
									 ComparisonExecutorService executorService,
									 ComparerHelper comparerHelper,
									 LoggerManager loggerManager,
									 KafkaMessagesBehindApi kafkaMessagesBehindCaller,
									 KafkaMessagesBehindApi kafkaMessagesBehindApi,
									 MessageCollectorService messageCollectorService,
									 LastMessageInfoService lastMessageInfoService,
									 SyncRequestService syncRequestService,
									 ThreadPoolTaskExecutor comparisonPool
	) {
		super(kafkaMessagesBehindCaller, loggerManager.getLogger(COMPARISON_LOG));

		this.comparisonInfoRepo = comparisonInfoRepository;
		this.scheduleRepo = scheduleRepository;
		this.viewComparisonInfoRepo = viewComparisonInfoRepository;
		this.viewComparisonResultRepo = viewComparisonResultRepository;
		this.dbComparisonResultRepo = dbComparisonResultRepository;
		this.summaryRepository = summaryRepository;
		this.executorService = executorService;
		this.comparerHelper = comparerHelper;
		this.kafkaMessagesBehindApi = kafkaMessagesBehindApi;
		this.messageCollectorService = messageCollectorService;
		this.lastMessageInfoService = lastMessageInfoService;
		this.syncRequestService = syncRequestService;
		this.comparisonPool = comparisonPool;
	}

	/**
	 * show like that: MYLGDB.IMCSUSER.PT_LB_CUESHEET_PAY_INFO.vodprogramming_mylgdb.imcsuser.pt_lb_cuesheet_pay_info
	 *
	 * @param s
	 * @return
	 */
	private static String buildColumnValues(ComparisonResultEntity s) {
		StringBuilder builder = new StringBuilder();

		builder.append(s.getSourceDatabase()).append(DOT)
				.append(s.getSourceSchema()).append(DOT)
				.append(s.getSourceTable()).append(DOT)
				.append(s.getTargetDatabase()).append(DOT)
				.append(s.getTargetSchema()).append(DOT)
				.append(s.getTargetTable());
		return builder.toString();
	}


	@Override
	public LocalDateTime getEarliestLastRun() {
		return comparisonInfoRepo.getEarliestLastRun();
	}

	private boolean needRetry(DbComparisonResultEntity e) {
		if (Objects.isNull(e) || Objects.isNull(e.getComparisonState())){
			return false;
		}
		switch (e.getComparisonState()){
			case SAME:
				return false;
			case DIFFERENT:
				return Math.abs(e.getSourceCount() - e.getTargetCount()) < RETRY_CONDITION_DIFFERENCE_MAX;
			case FAILED:
				// java.sql.SQLException: ORA-08103: object no longer exists
				return e.getErrorMsgSource().contains("ORA-08103") || e.getErrorMsgTarget().contains("ORA-08103");
			default:
				return false;
		}
	}

	private class RetryComparisonModel implements Comparable<RetryComparisonModel>{
		@Getter
		final private DbComparisonResultEntity dbComparisonResultEntity;
		@Getter
		final private LocalDateTime lastRetried;
		@Getter
		final private int retryCount;

		public RetryComparisonModel(DbComparisonResultEntity dbComparisonResultEntity, int retryCount) {
			this.dbComparisonResultEntity = dbComparisonResultEntity;
			this.retryCount = retryCount;
			this.lastRetried = LocalDateTime.now(Constants.ZONE_ID);
		}

		@Override
		public int compareTo(RetryComparisonModel o) {
			int compareWithTime = this.getLastRetried().compareTo(o.getLastRetried());
			if(compareWithTime == 0) {
				Integer thisCompOrder = dbComparisonResultEntity.getDbComparisonInfo().getComparisonOrder();
				Integer oCompOrder = o.getDbComparisonResultEntity().getDbComparisonInfo().getComparisonOrder();

				if (Objects.nonNull(thisCompOrder) && Objects.isNull(oCompOrder)) {
					return 1;
				} else if (Objects.isNull(thisCompOrder) && Objects.nonNull(oCompOrder)) {
					return -1;
				} else if (Objects.equals(thisCompOrder, oCompOrder)){
					long thisAllCount = dbComparisonResultEntity.getTargetCount() + dbComparisonResultEntity.getSourceCount();
					long oAllCount = (o.getDbComparisonResultEntity().getTargetCount() + o.getDbComparisonResultEntity().getTargetCount());
					return thisAllCount > oAllCount ? 1 : 0;
				} else {
					return thisCompOrder > oCompOrder ? 1 : 0;
				}
			} else {
				return compareWithTime;
			}
		}
	}

	@Override
	public void compareAll(LocalDate compareDate, LocalTime time, int stepTime, Logger log) {
		if (log.isDebugEnabled()) {
			log.debug("Computing comparison compare date: {}, compare time: {}", compareDate, time);
		}

		final LocalDate compareDate_ = Objects.isNull(compareDate) ? LocalDate.now(Constants.ZONE_ID) : compareDate;

		if(Objects.isNull(summaryRepository.findByCompareDateAndCompareTime(compareDate_, time))){
			log.info("Starting comparison: {} {}", compareDate_, time);
		} else {
			log.warn("Comparison: {} {}, This comparison is already compared or running on another node", compareDate_, time);
			return;
		}
		executorService.createSummary(compareDate_, time);

		LocalDateTime runDate = LocalDateTime.of(compareDate_, time);
		LocalDateTime before = runDate.minus(Duration.of(stepTime > 0 ? stepTime : defaultStepTime, ChronoUnit.MINUTES));

		try {
			/*
			 *  Sometime, comparison state is not equal 0 after counting done due to interrupted server problem.
			 *  Need to set comparison info state to 0 before counting.
			 */
			comparisonInfoRepo.resetCompareState(runDate); //TODO Deadlock exception may throw here
		} catch (Exception e) {
			log.warn("Error occurred during reset comparison info state to 0 before counting.");
			log.warn(e.getMessage(), e);
		}

		List<DbComparisonInfoEntity> comparisonItems = comparisonInfoRepo.findAllComparisonItemsWithUpdatingFlagToRunning(runDate, before);

		if (comparisonItems.size() == 0) return;

		if (log.isDebugEnabled()) {
			log.debug("Get {} comparison info {}. compare date: {}, compare time: {}",
					comparisonItems.size(),
					comparisonItems.stream()
							.map(e -> String.valueOf(e.getId()))
							.collect(Collectors.joining(",")),
					compareDate_,
					time);
		}

		List<CompletableFuture<DbComparisonResultEntity>> comparedResults = comparisonItems.stream()
				.sorted(Comparator.comparing(comparisonInfo -> Objects.requireNonNullElse(comparisonInfo.getComparisonOrder(), Integer.MAX_VALUE)))
				.map(item -> CompletableFuture.supplyAsync(() -> {
					Instant threadStart = Instant.now();
					DbComparisonResultEntity comparisonResult = executorService.compareNew(item, runDate.toLocalDate(), time);
					long threadDurationMs = Duration.between(threadStart, Instant.now()).toMillis();
					logger.info("[compareNew] (comparisonOrder={}, duration={}ms, sourceCount={}, targetCount={}) {}",
							item.getComparisonOrder(), threadDurationMs, comparisonResult.getSourceCount(), comparisonResult.getTargetCount(), item.getSyncInfo().getTopicName()
					);
					return comparisonResult;
				}, comparisonPool))
				.collect(toList());

		PriorityQueue<RetryComparisonModel> retryQueue = new PriorityQueue<>();

		List<Long> diffIds = comparedResults.stream()
				.map(CompletableFuture::join)
				.filter(this::needRetry)
				.map(comparisonResult -> {
					retryQueue.add(new RetryComparisonModel(comparisonResult, 1));
					return comparisonResult.getId();
				})
				.collect(toList());

		logger.info("*** Retry with ids: {}, at: {}", diffIds, DateUtils.getDateTime());

		while(!(retryQueue.isEmpty() && comparisonPool.getActiveCount() == 0)){
			try{
				if (retryQueue.isEmpty()) {
					Thread.sleep(100);
					continue;
				}

				final RetryComparisonModel retryComparisonModel = retryQueue.poll();

				long DIFF = ChronoUnit.MILLIS.between(retryComparisonModel.getLastRetried(), LocalDateTime.now(Constants.ZONE_ID));
				long waitMills = (WAIT_INTERVAL * 1000L) - DIFF;
				if (waitMills > 0){
					logger.debug("compareRetryWaiting: {}ms", waitMills);
					Thread.sleep(waitMills);
				}

				final Long comparisonResultId = retryComparisonModel.getDbComparisonResultEntity().getId();

				comparisonPool.execute(()->{
					Instant threadStart = Instant.now();
					DbComparisonResultEntity comparisonResult = executorService.compareWithComparisonResultId(comparisonResultId);
					long threadDurationMs = Duration.between(threadStart, Instant.now()).toMillis();
					logger.info("[compareRetry] (lastRetried={}, retryCount={}, duration={}ms, sourceCount={}, targetCount={}) {}",
							retryComparisonModel.getLastRetried().format(DateTimeFormatter.ISO_LOCAL_TIME),
							retryComparisonModel.getRetryCount(),
							threadDurationMs,
							comparisonResult.getSourceCount(),
							comparisonResult.getTargetCount(),
							retryComparisonModel.getDbComparisonResultEntity().getDbComparisonInfo().getSyncInfo().getSynchronizerName()
					);
					if(retryComparisonModel.getRetryCount() < RETRY_COUNT){
						retryQueue.add(new RetryComparisonModel(comparisonResult, retryComparisonModel.getRetryCount()+1));
					}
				});
			} catch (Exception e) {
				logger.error("compareRetryFailed: {}", e.getMessage());
				break;
			}
		}

		executorService.sendSlackMessage(compareDate_, time);
	}

	@Override
	public List<DbComparisonScheduleDto> getSchedules() {
		Sort sortById = Sort.by("id");
		return this.scheduleRepo.findAll(sortById).stream()
				.map(DbComparisonSchedulerEntity::toDbComparisonScheduleDto)
				.collect(Collectors.toList());
	}
	@Override
	public void save(List<DbComparisonSchedulerEntity> body) {
		this.scheduleRepo.saveAll(body);
	}

	@Override
	public void deleteAll() {
		this.scheduleRepo.deleteAll();
	}

	@Override
//	@Cacheable(value = CacheKey.SYNC_REQUEST_COMPARISON)
	public List<ComparisonEntity> getViewInfo() {
		return this.viewComparisonInfoRepo.findAll();
	}

	@Override
	public List<LocalTime> getResultByDate(LocalDate compareDate) {
		return this.dbComparisonResultRepo.findDistinctByCompareDate(compareDate);
	}

	@Override
	public ComparisonResultForExport export(LocalDate compareDate) {
		List<ComparisonResultEntity> entities = this.viewComparisonResultRepo.findByCompareDate(compareDate);
		Set<String> syncInfo = new HashSet<>();

		entities.forEach(item -> syncInfo.add(buildColumnValues(item)));

		Map<LocalTime, List<ComparisonResultEntity>> groupByTime = entities.stream().collect(
				Collectors.groupingBy(ComparisonResultEntity::getCompareTime, LinkedHashMap::new, Collectors.toList()));
		// sorted by lastest time
		groupByTime.entrySet()
				.stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

		ComparisonResultForExport forExport = new ComparisonResultForExport();
		// key=MYLGDB.IMCSUSER.PT_LB_CUESHEET_PAY_INFO.vodprogramming_mylgdb.imcsuser.pt_lb_cuesheet_pay_info
		// value: List of Cell
		Map<String, ComparisonResultDto> mapTableAndDiff = new HashMap<>();
		for (String column : syncInfo) {
			ComparisonResultDto comparisonResultDto = new ComparisonResultDto();
			List<Map<LocalTime, Long>> list = new ArrayList<>();
			groupByTime.forEach((key, value) -> value.stream().filter(s -> buildColumnValues(s).equals(column)).findAny().ifPresent(any -> {
				Map<LocalTime, Long> cell = new HashMap<>();
				cell.put(key, any.getNumberDiff());
				list.add(cell);
			}));
			comparisonResultDto.setDiffs(list);
			mapTableAndDiff.put(column, comparisonResultDto);
		}
		forExport.setHeaders(groupByTime.keySet().stream().map(LocalTime::toString).collect(toList()));
		forExport.setMap(mapTableAndDiff);
		forExport.setRawValues(groupByTime);
		return forExport;
	}

	@Override
	public Map<LocalTime, List<ComparisonResultEntity>> exportByDateAndTime(LocalDate compareDate, LocalTime time) {
		return this.viewComparisonResultRepo.findByCompareDateAndCompareTimeOrderByLastModifiedDesc(compareDate, time).stream()
				.collect(Collectors.groupingBy(ComparisonResultEntity::getCompareTime, Collectors.toList()));
	}

	@Override
	public DbComparisonResultSummaryEntity getComparisonSummary(LocalDate compareDate, LocalTime compareTime) {
		DbComparisonResultSummaryEntity resultSummaryEntity = this.summaryRepository.findByCompareDateAndCompareTime(compareDate, compareTime);
		if (resultSummaryEntity != null) {
			return resultSummaryEntity;
		}
		long totalComparison = comparisonInfoRepo.countListToCompare();
		ComparisonSummary comparisonSummary = dbComparisonResultRepo.getComparisonSummary(compareDate, compareTime);
		DbComparisonResultSummaryEntity summary = new DbComparisonResultSummaryEntity();
		if (comparisonSummary == null) {
			return summary;
		}
		summary.setCompareTime(compareTime);
		summary.setCompareDate(compareDate);
		summary.setTargetCount(comparisonSummary.getTargetCount());
		summary.setSourceCount(comparisonSummary.getSourceCount());

		summary.setTotal(Long.valueOf(totalComparison).intValue());
		summary.setEqual(comparisonSummary.getEqual());
		summary.setDifferent(comparisonSummary.getDifferent());
		summary.setFail(comparisonSummary.getFail());

		MessagesBehindInfo messageBehind = getKafkaMessagesBehind();
		if (messageBehind != null) {
			summary.setMsgDtBehind(messageBehind.getMsgDtBehind());
		}
		return summary;
	}

	@Override
	public boolean deleteComparisonSummary(LocalDate compareDate, LocalTime time) {
		try {
			this.dbComparisonResultRepo.deleteSummaryByDateAndTime(compareDate, time);
			this.summaryRepository.deleteSummaryByDateAndTime(compareDate, time);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	@Override
	public void compareSelectedIds(List<Long> ids) {
		this.executorService.compareWithComparisonResultIds(ids);
	}

	@Override
	public Page<ComparisonResultEntity> filter(Pageable pageable, ComparisonFilter filter) {
		return viewComparisonResultRepo.findByCriteria(
				pageable,
				filter.getCompareDate(), filter.getCompareTime(), SQLBuilder.prepareSearchTerms(filter.getSourceDB()),
				SQLBuilder.prepareSearchTerms(filter.getSourceSchema()), SQLBuilder.prepareSearchTerms(filter.getTargetDB()),
				SQLBuilder.prepareSearchTerms(filter.getTargetSchema()), SQLBuilder.prepareSearchTerms(filter.getSourceTable()),
				SQLBuilder.prepareSearchTerms(filter.getTargetTable()), SQLBuilder.prepareSearchTerms(filter.getDivision()), filter.getState());
	}

	@Override
	public int deleteBeforeTime(LocalDate date) {
		this.summaryRepository.deleteBeforeTime(date);
		return this.dbComparisonResultRepo.deleteBeforeTime(date);
	}

	@Override
	public List<KafkaMessagesBehindApi.Partition> viewKafkaConsumerGroup(int filter, int reverse, int topN) throws IOException {
		List<String> consumerGroup = syncRequestService.listConsumerGroups();
		List<KafkaMessagesBehindApi.Partition> partitions = new ArrayList<>();
		for (String groupId : consumerGroup) {
			Call<KafkaMessagesBehindApi.KafkaConsumerGroupMetaData> caller = kafkaMessagesBehindApi.getConsumerGroupMetaData(KafkaConstants.MNT_CLUSTER, groupId);
			Response<KafkaMessagesBehindApi.KafkaConsumerGroupMetaData> response = caller.execute();
			if (response.isSuccessful()) {
				KafkaMessagesBehindApi.KafkaConsumerGroupMetaData consumerGroupMetaData = response.body();
				assert consumerGroupMetaData != null;
				partitions.addAll(consumerGroupMetaData.getPartitions());
			}
		}
		if (Objects.isNull(partitions) || partitions.isEmpty()) {
			return Collections.emptyList();
		}
		if (reverse == 1) {
			partitions.sort(Collections.reverseOrder());
		} else {
			Collections.sort(partitions);
		}
		List<KafkaMessagesBehindApi.Partition> partitionList = partitions.stream().filter(x -> {
			if (filter == 1) {
				return x.getMessagesBehind() > 0;
			} else {
				return true;
			}
		}).limit(topN).collect(toList());
		Map<String, LastMessageInfoEntity> lastMessageMap = getLastMessageInfoEntityMap(partitionList.stream().map(KafkaMessagesBehindApi.Partition::getTopic).collect(toList()));
		for (KafkaMessagesBehindApi.Partition partition : partitionList) {
			if (lastMessageMap.containsKey(partition.getTopic())) {
				LastMessageInfoEntity infoEntity = lastMessageMap.get(partition.getTopic());
				partition.setReceivedDate(infoEntity.getReceivedDate());
				partition.setReceivedTime(infoEntity.getReceivedTime());
			}
		}
		return partitionList;
	}

	@Override
	public List<DbComparisonInfoEntity> findAllBySyncInfoId(long syncId) {
		return comparisonInfoRepo.findAllBySyncInfoId(syncId);
	}

	private Map<String, LastMessageInfoEntity> getLastMessageInfoEntityMap(List<String> topics) {
		Map<String, LastMessageInfoDto> receivedMessageMap = this.messageCollectorService.getMapLastMessageInfoByListTopic(topics);
		List<LastMessageInfoEntity> lastMessageList = this.lastMessageInfoService.getLastMessageInfoByListTopic(topics);
		return SyncRequestHelper.getStringLastMessageInfoEntityMap(receivedMessageMap, lastMessageList);
	}
}
