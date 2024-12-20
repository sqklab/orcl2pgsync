package com.lguplus.fleta.domain.service.synchronizer;

import com.lguplus.fleta.adapters.messagebroker.KafkaProperties;
import com.lguplus.fleta.adapters.rest.SyncRequestHelper;
import com.lguplus.fleta.domain.dto.*;
import com.lguplus.fleta.domain.dto.Synchronizer.SyncState;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.domain.model.comparison.DbComparisonInfoEntity;
import com.lguplus.fleta.domain.service.constant.Constants;
import com.lguplus.fleta.domain.service.constant.DivisionType;
import com.lguplus.fleta.domain.service.exception.DatasourceNotFoundException;
import com.lguplus.fleta.domain.service.exception.InvalidKafkaConsumerGroupStateException;
import com.lguplus.fleta.domain.service.exception.InvalidKafkaOffsetTimestampException;
import com.lguplus.fleta.domain.util.DateUtils;
import com.lguplus.fleta.domain.util.JdbcUtil;
import com.lguplus.fleta.ports.repository.DbComparisonInfoRepository;
import com.lguplus.fleta.ports.repository.SyncRequestRepository;
import com.lguplus.fleta.ports.service.DataSourceService;
import com.lguplus.fleta.ports.service.SlackService;
import com.lguplus.fleta.ports.service.SyncRequestService;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SyncRequestServiceImpl implements SyncRequestService {

	private final DbComparisonInfoRepository comparisonInfoRepo;

	private final SyncRequestRepository syncRequestRepository;

	private final DataSourceService dataSourceService;

	private final KafkaProperties kafkaProperties;

	@Value("${app.import.domain}")
	public String domainTypes;

	private final SlackService slackService;

	public SyncRequestServiceImpl(SyncRequestRepository syncRequestRepository,
								  @Qualifier("defaultDatasourceContainer") DataSourceService dataSourceService,
								  KafkaProperties kafkaProperties,
								  SlackService slackService,
								  DbComparisonInfoRepository comparisonInfoRepo) {
		this.syncRequestRepository = syncRequestRepository;
		this.dataSourceService = dataSourceService;
		this.kafkaProperties = kafkaProperties;
		this.slackService = slackService;
		this.comparisonInfoRepo = comparisonInfoRepo;
	}

	@Override
	public List<String> findTopicListByDivision(String division) {
		return syncRequestRepository.findTopicListByDivision(division);
	}

	@Override
	public List<SyncRequestEntity> findAll() {
		return syncRequestRepository.findAll();
	}

	@Override
	public Boolean hasAtLeastOneRunning(String consumerGroup) {
		try (AdminClient client = AdminClient.create(getKafkaProperties(consumerGroup))) {
			Collection<ConsumerGroupListing> groups = client.listConsumerGroups().all().get(10, TimeUnit.SECONDS);
			for (ConsumerGroupListing group : groups) {
				if (group.groupId().equals(consumerGroup) && ConsumerGroupState.STABLE.equals(group.state().get())) {
					log.info("The kafka consumer group {} has state = {}", consumerGroup, group.state());
					return true;
				}
			}
		} catch (ExecutionException | TimeoutException | InterruptedException ex) {
			log.error("Error during check state of kafka consumer group");
		}
		return false;
	}

	@Override
	public List<SyncInfoBase> getIdAndNameOfSyncTask() {
		return this.syncRequestRepository.findIdAndNameOfSyncTask();
	}

	@Override
	public long resetOffset(String kafkaTopic, String synchronizerName, LocalDateTime dateTime) throws InvalidKafkaOffsetTimestampException, InvalidKafkaConsumerGroupStateException {
		Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
		int partition = 0;
		SyncRequestEntity syncRequestInfo = findByTopicNameAndSynchronizerName(kafkaTopic, synchronizerName);
		String groupId = syncRequestInfo.getConsumerGroup();
		try (AdminClient client = AdminClient.create(getKafkaProperties(syncRequestInfo.getConsumerGroup()));
			 KafkaConsumer<Object, Object> consumer = buildKafkaConsumer(syncRequestInfo.getConsumerGroup());) {
			Collection<ConsumerGroupListing> groups = client.listConsumerGroups().all().get(10, TimeUnit.SECONDS);

			for (ConsumerGroupListing group : groups) {
				if (group.groupId().equals(groupId) && ConsumerGroupState.STABLE.equals(group.state().get())) {
					String message = "The state of group is stable ";
					throw new InvalidKafkaConsumerGroupStateException(message);
				}
			}
			Optional<OffsetAndTimestamp> offsetAndTimestamp = getOffsetAndTimestamp(dateTime, kafkaTopic, partition, consumer);
			if (offsetAndTimestamp.isEmpty()) {
				log.warn("Offset not found!");
				throw new InvalidKafkaOffsetTimestampException("Kafka offset not found for given timestamp");
			}

			long targetOffset = offsetAndTimestamp.get().offset();
			OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(targetOffset);
			TopicPartition topicPartition0 = new TopicPartition(kafkaTopic, partition);
			offsets.put(topicPartition0, offsetAndMetadata);
			consumer.commitSync(offsets);
			return targetOffset;

		} catch (ExecutionException | TimeoutException | InterruptedException ex) {
			throw new InvalidKafkaConsumerGroupStateException(ex.getMessage(), ex.getCause());
		}
	}

	private Optional<OffsetAndTimestamp> getOffsetAndTimestamp(LocalDateTime dateTime, String kafkaTopic, int partition, KafkaConsumer<Object, Object> consumer) {
		Map<TopicPartition, Long> timestamps = new HashMap<>();
		timestamps.put(new TopicPartition(kafkaTopic, partition),
				dateTime.atZone(Constants.ZONE_ID).toInstant().toEpochMilli());
		Map<TopicPartition, OffsetAndTimestamp> values = consumer.offsetsForTimes(timestamps);
		return values.values().stream().filter(Objects::nonNull).findAny();
	}

	private Properties getKafkaProperties(String groupId) {
		Properties props = new Properties();
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		return props;
	}

	private KafkaConsumer<Object, Object> buildKafkaConsumer(String groupId) {
		return new KafkaConsumer<>(getKafkaProperties(groupId));
	}

	@Override
	public SyncRequestEntity findById(long id) {
		return syncRequestRepository.findSyncRequestById(id);
	}

	@Override
	public List<SyncRequestEntity> findByIds(List<Long> ids) {
		return syncRequestRepository.findByIds(ids);
	}

	@Override
	public SyncRequestEntity findBySynchronizerName(String synchronizername) {
		return syncRequestRepository.findBySynchronizerName(synchronizername);
	}

	public List<SyncRequestEntity> findRunningTasksByIds(List<Long> ids) {
		return syncRequestRepository.findByIdsAndState(ids, SyncState.RUNNING);
	}

	@Override
	public SyncRequestEntity findByTopicName(String topicName) {
		return syncRequestRepository.findByTopicName(topicName);
	}

	@Override
	public List<SyncRequestEntity> findByTopicNames(List<String> topicNames) {
		return syncRequestRepository.findByTopicNames(topicNames);
	}

	@Override
	public SyncRequestEntity findByTopicNameAndSynchronizerName(String topicName, String synchronizerName) {
		return syncRequestRepository.findByTopicNameAndSynchronizerName(topicName, synchronizerName);
	}

	@Transactional
	@Override
	public SyncRequestParam viewByTopicName(String topicName) {
		SyncRequestEntity syncRequestEntity = syncRequestRepository.findByTopicName(topicName);
		return Optional.ofNullable(syncRequestEntity).map(SyncRequestEntity::toSyncParam).orElse(null);
	}

	@Override
	public SyncRequestEntity createOrUpdate(SyncRequestEntity topic) {
		return syncRequestRepository.save(topic);
	}

	@Override
	public SyncRequestEntity createOrUpdateSync(SyncRequestParam param) {
		if (param.getPrimaryKeys() == null || ("").equals(param.getPrimaryKeys())) {
			try {
				String primaryKeys = detectPrimaryKeys(param.getDivision(), param.getTargetDatabase(), param.getTargetSchema(), param.getTargetTable());
				param.setPrimaryKeys(primaryKeys);
			} catch (Exception ex) {
				log.error("Error during detect primary keys {}", ex.getMessage());
			}
		}
		if (param.getUniqueKeys() == null || ("").equals(param.getUniqueKeys())) {
			try {
				String uniqueKeys = detectUniqueKeys(param.getDivision(), param.getTargetDatabase(), param.getTargetSchema(), param.getTargetTable());
				param.setUniqueKeys(uniqueKeys);
			} catch (Exception ex) {
				log.error("Error during detect unique keys {}", ex.getMessage());
			}
		}
		SyncRequestEntity syncRequest = SyncRequestHelper.toSyncEntity(param);
		return syncRequestRepository.save(syncRequest);
	}

	@Override
	@Transactional
	public void deleteByIds(List<Long> ids) {
		try {
			syncRequestRepository.deleteAllByIdInBatch(ids);
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void deleteAll() {
		try {
			syncRequestRepository.deleteAll();
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public int countRunningSynchronizer() {
		return syncRequestRepository.countByState(SyncState.RUNNING);
	}

	@Override
	public Page<SyncInfoDto> findWithNumberOfError(Pageable pageable, LocalDate dateFrom, LocalDate dateTo, List<String> topicNames, SyncState state, String divisionValue, String db, String schema) {
		int[] states = state == null ? SyncState.getIntValues() : new int[]{state.getState()};
		String searchTopicNames = "%(" + String.join("|", topicNames) + ")%";
		if (dateFrom == null) {
			return this.syncRequestRepository.findAllByNumberOfError(pageable, states, divisionValue, searchTopicNames, db, schema);
		}
		if (dateTo == null) {
			dateTo = DateUtils.getDate();
		}
		dateTo = dateTo.plusDays(1);
		return this.syncRequestRepository.findAllByNumberOfErrorAndPeriod(pageable, dateFrom, dateTo, states, divisionValue, searchTopicNames, db, schema);
	}

	@Override
	public Page<SyncInfoByLastReceivedTimeDto> findWithNumberOfErrorByLastReceivedTime(Pageable pageable, String sortType, LocalDate dateFrom, LocalDate dateTo,
																					   List<String> topicName, SyncState state, String divisionValue, String db, String schema) {
		int[] states = state == null ? SyncState.getIntValues() : new int[]{state.getState()};
		LocalDateTime fromDateTime = dateFrom == null ? LocalDateTime.of(2021, 1, 1, 23, 59) : dateFrom.atStartOfDay();
		LocalDateTime toDateTime = dateTo == null ? LocalDateTime.now().plusDays(1) : dateTo.atStartOfDay();
		String searchTopicNames = "%(" + String.join("|", topicName) + ")%";
		if (Synchronizer.SortType.DESC.name().equals(sortType)) {
			return this.syncRequestRepository.findAllByNumberOfErrorByLastReceivedTimeDESC(pageable, fromDateTime, toDateTime, searchTopicNames, states, divisionValue, db, schema);
		} else {
			return this.syncRequestRepository.findAllByNumberOfErrorByLastReceivedTimeASC(pageable, fromDateTime, toDateTime, states, divisionValue, searchTopicNames, db, schema);
		}
	}

	@Override
	public List<SyncRequestEntity> findAllRunningSynchronizerWithSourceInformation(
			String sourceDatabase, String sourceSchema, String sourceTable
	) {
		return syncRequestRepository.findAllBySourceDatabaseAndSourceSchemaAndSourceTableAndState(
				sourceDatabase, sourceSchema, sourceTable, SyncState.RUNNING
		);
	}

	@Override
	public String findPrimaryKeysWithTopicName(String topicName) {
		return syncRequestRepository.findPrimaryKeysWithTopicName(topicName);
	}

	@Override
	public SyncStateCountDto countSyncState() {
		return this.syncRequestRepository.countSyncState();
	}

	@Override
	public List<String> findAllDivision() {
		return this.syncRequestRepository.findDivisions();
	}

	@Override
	public List<String> findAllTopicNames() {
		return syncRequestRepository.findAllTopicNames();
	}

	@Override
	public List<SyncRequestEntity> saveAllSyncRequests(List<SyncRequestEntity> syncRequests) {
		return syncRequestRepository.saveAll(syncRequests);
	}

	@Override
	public String detectPrimaryKeys(String division, String sourceDatabase, String schema, String sourceTable) {
		try (Connection connection = dataSourceService.findConnectionByServerName(sourceDatabase)) {
			if (Objects.isNull(connection)) {
				throw new DatasourceNotFoundException(String.format("Can't get connection to %s", sourceDatabase));
			}
			if (division.equals(DivisionType.POSTGRES_TO_ORACLE.getDivisionStr())) {
				return JdbcUtil.detectOraclePrimaryKeys(connection, schema, sourceTable);
			} else {
				return JdbcUtil.detectPostgresPrimaryKeys(connection, schema, sourceTable);
			}
		} catch (Exception ex) {
			log.error("Error during detect primary keys", ex);
			return "";
		}
	}

	@Override
	public String detectUniqueKeys(String division, String sourceDatabase, String schema, String sourceTable) {
		try (Connection connection = dataSourceService.findConnectionByServerName(sourceDatabase)) {
			if (Objects.isNull(connection)) {
				throw new DatasourceNotFoundException(String.format("Can't get connection to %s", sourceDatabase));
			}
			if (division.equals(DivisionType.POSTGRES_TO_ORACLE.getDivisionStr())) {
				return JdbcUtil.detectOracleUniqueKeys(connection, schema, sourceTable);
			} else {
				return JdbcUtil.detectPostgresUniqueKeys(connection, schema, sourceTable);
			}
		} catch (Exception ex) {
			log.error("Error during detect unique keys", ex);
			return "";
		}
	}

	@Override
	public boolean isColumnComparisonError(SyncRequestEntity syncRequest) {
		List<DbComparisonInfoEntity> comparisons = comparisonInfoRepo.findAllBySyncInfoId(syncRequest.getId())
				.stream()
				.filter(item -> null != item.getEnableColumnComparison() && item.getEnableColumnComparison())
				.collect(Collectors.toList());
		if (comparisons.isEmpty()) return false;

		boolean diff = isColumnComparisonError(syncRequest.getTopicName(), syncRequest.getDivision(),
				syncRequest.getSourceDatabase(), syncRequest.getSourceSchema(), syncRequest.getSourceTable(),
				syncRequest.getTargetDatabase(), syncRequest.getTargetSchema(), syncRequest.getTargetTable());

		if (diff) {
			log.error("Fail to start synchronizer for topic {} due to different column comparison between source and target database",
					syncRequest.getTopicName());
		}
		return diff;
	}

	/**
	 * Send slack notification if column comparison is different
	 *
	 * @return true if column comparison is different
	 */
	@Override
	public boolean isColumnComparisonError(String topic, String division,
										   String sourceDatabase, String sourceSchema, String sourceTable,
										   String targetDatabase, String targetSchema, String targetTable) {
		List<String> oracleColumns;
		List<String> postgresColumns;
		if (division.equals(DivisionType.ORACLE_TO_POSTGRES.getDivisionStr())) {
			oracleColumns = getListColumns(true, sourceDatabase, sourceSchema, sourceTable);
			postgresColumns = getListColumns(false, targetDatabase, targetSchema, targetTable);
		} else {
			oracleColumns = getListColumns(true, targetDatabase, targetSchema, targetTable);
			postgresColumns = getListColumns(false, sourceDatabase, sourceSchema, sourceTable);
		}

		String reason = null;
		List<ColumnCompare> diff = null;
		if (null == oracleColumns || null == postgresColumns || oracleColumns.isEmpty() || postgresColumns.isEmpty()) {
			log.error("Fail to compare column between 2 tables {}.{}.{} and {}.{}.{}",
					sourceDatabase, sourceSchema, sourceTable, targetDatabase, targetSchema, targetTable);
			reason = String.format("Fail to get column count to compare, source table (%s) and target table (%s)",
					oracleColumns.size(), postgresColumns.size());
		} else if (oracleColumns.size() != postgresColumns.size()) {
			log.error("{}.{}.{} and {}.{}.{} has different columns",
					sourceDatabase, sourceSchema, sourceTable, targetDatabase, targetSchema, targetTable);
			reason = String.format("Column count of source table(%s) and target table(%s) are different",
					oracleColumns.size(), postgresColumns.size());
		} else {
			diff = getDiffColumnsName(oracleColumns, postgresColumns);
			if (!diff.isEmpty()) {
				log.error("{}.{}.{} and {}.{}.{} has different columns name",
						sourceDatabase, sourceSchema, sourceTable, targetDatabase, targetSchema, targetTable);
				reason = "There are some columns has different name";
			}
		}
		if (org.apache.commons.lang3.StringUtils.isBlank(reason)) {
			return false;
		}

		//send slack
		LocalDateTime now = LocalDateTime.now(Constants.ZONE_ID);
		SlackService.ColumnComparisonMessage message = SlackService.ColumnComparisonMessage.builder()
				.compareDate(now.toLocalDate())
				.compareTime(now.toLocalTime())
				.topic(topic)
				.errorMessage(reason)
				.sourceDatabase(sourceDatabase)
				.sourceSchema(sourceSchema)
				.sourceTable(sourceTable)
				.targetDatabase(targetDatabase)
				.targetSchema(targetSchema)
				.targetTable(targetTable)
				.diffColumns(diff)
				.build();

		try {
			ChatPostMessageResponse response = slackService.send(message);
			if (response != null && response.getError() != null) {
				log.error("ERROR during send notify to Slack, error : {}", response.getError());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return true;
	}

	/**
	 * @param oracleColumns   oracleColumns
	 * @param postgresColumns postgresColumns
	 * @return true if 2 columns list is different
	 */
	private List<ColumnCompare> getDiffColumnsName(List<String> oracleColumns, List<String> postgresColumns) {
		/*  key: column name, value: count */
		Map<String, ColumnCompare> map = new HashMap<>();
		oracleColumns.stream()
				.map(String::toLowerCase)
				.forEach(col -> {
					map.putIfAbsent(col, new ColumnCompare());
					ColumnCompare c = map.get(col);
					c.increase();
					c.setOracleColumn(col.toUpperCase());
				});
		postgresColumns.forEach(col -> {
			map.putIfAbsent(col, new ColumnCompare());
			ColumnCompare c = map.get(col);
			c.increase();
			c.setPostgresColumn(col);
		});

		List<ColumnCompare> diff = new LinkedList<>();
		for (String col : map.keySet()) {
			if (map.get(col).getCount() < 2) {
				diff.add(map.get(col));
			}
		}

		return diff;
	}

	/**
	 * @param isOracle isOracle
	 * @param database database
	 * @param schema   schema
	 * @param table    table
	 * @return list of columns
	 */
	private List<String> getListColumns(boolean isOracle, String database, String schema, String table) {
		try (Connection connection = dataSourceService.findConnectionByServerName(database)) {
			if (Objects.isNull(connection)) {
				throw new DatasourceNotFoundException(String.format("Can't get connection to %s", database));
			}
			if (isOracle) {
				return JdbcUtil.detectColumnsFromOracleTable(connection, schema, table);
			} else {
				return JdbcUtil.detectColumnsFromPostgresTable(connection, schema, table);
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		return new ArrayList<>();
	}


	public boolean detectPartition(String division, String sourceDB, String sourceSchema, String sourceTable, String targetDB, String targetSchema, String targetTable) throws DatasourceNotFoundException {
		String postgresDatabase = (division.equals(DivisionType.ORACLE_TO_POSTGRES.getDivisionStr()) ? targetDB : sourceDB);
		String postgresSchema = (division.equals(DivisionType.ORACLE_TO_POSTGRES.getDivisionStr()) ? targetSchema : sourceSchema);
		String postgresTable = (division.equals(DivisionType.ORACLE_TO_POSTGRES.getDivisionStr()) ? targetTable : sourceTable);
		try (Connection connection = dataSourceService.findConnectionByServerName(postgresDatabase)) {
			if (Objects.isNull(connection)) {
				throw new DatasourceNotFoundException(String.format("Can't get connection to %s", postgresDatabase));
			}
			return JdbcUtil.detectPartition(connection, postgresSchema, postgresTable);
		} catch (Exception ex) {
			log.error("Error during detect partition", ex);
			return false;
		}
	}

	@Override
	public List<String> listConsumerGroups() {
		return syncRequestRepository.findConsumerGroups(Constants.PREFIX_GROUP_DBSYNC);
	}

	@Override
	public List<SyncRequestEntity> findByStateIn(List<SyncState> states) {
		return syncRequestRepository.findByStateIn(states);
	}
	public List<SyncRequestEntity> findAllRunningSynchronizer(){
		return syncRequestRepository.findAllRunningSynchronizer();
	}
}
