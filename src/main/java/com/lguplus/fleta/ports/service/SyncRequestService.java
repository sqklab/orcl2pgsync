package com.lguplus.fleta.ports.service;

import com.lguplus.fleta.domain.dto.*;
import com.lguplus.fleta.domain.dto.Synchronizer.SyncState;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.domain.service.exception.DatasourceNotFoundException;
import com.lguplus.fleta.domain.service.exception.InvalidKafkaConsumerGroupStateException;
import com.lguplus.fleta.domain.service.exception.InvalidKafkaOffsetTimestampException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SyncRequestService {
	List<String> findTopicListByDivision(String division);

	List<SyncRequestEntity> findAll();

	Boolean hasAtLeastOneRunning(String consumerGroup);

	List<SyncInfoBase> getIdAndNameOfSyncTask();

	long resetOffset(String kafkaTopic, String synchronizerName, LocalDateTime dateTime) throws InvalidKafkaOffsetTimestampException, InvalidKafkaConsumerGroupStateException;

	List<SyncRequestEntity> findAllRunningSynchronizerWithSourceInformation(String sourceDatabase, String sourceSchema, String sourceTable);

	List<SyncRequestEntity> findByStateIn(List<SyncState> state);

	SyncRequestEntity findById(long id);

	List<SyncRequestEntity> findRunningTasksByIds(List<Long> ids);

	List<SyncRequestEntity> findByIds(List<Long> ids);

	SyncRequestEntity findBySynchronizerName(String synchronizerName);

	SyncRequestEntity findByTopicName(String topicName);

	List<SyncRequestEntity> findByTopicNames(List<String> topicNames);

	SyncRequestEntity findByTopicNameAndSynchronizerName(String topicName, String synchronizerName);

	SyncRequestParam viewByTopicName(String topicName);

	SyncRequestEntity createOrUpdate(SyncRequestEntity topic);

	String findPrimaryKeysWithTopicName(String topicName);

	SyncRequestEntity createOrUpdateSync(SyncRequestParam param);

	void deleteByIds(List<Long> ids);

	void deleteAll();

	int countRunningSynchronizer();

	Page<SyncInfoDto> findWithNumberOfError(Pageable pageable, LocalDate dateFrom, LocalDate dateTo, List<String> topicNames, SyncState state, String divisionValue, String db, String schema);

	List<SyncRequestEntity> saveAllSyncRequests(List<SyncRequestEntity> syncRequests);

	Page<SyncInfoByLastReceivedTimeDto> findWithNumberOfErrorByLastReceivedTime(Pageable pageable, String sortType, LocalDate dateFrom,
																				LocalDate dateTo, List<String> topicName, SyncState state, String divisionValue, String db, String schema);

	SyncStateCountDto countSyncState();

	List<String> findAllDivision();

	List<String> findAllTopicNames();

	String detectPrimaryKeys(String division, String sourceDatabase, String schema, String sourceTable) throws DatasourceNotFoundException;

	String detectUniqueKeys(String division, String sourceDatabase, String schema, String sourceTable) throws DatasourceNotFoundException;

	boolean isColumnComparisonError(SyncRequestEntity syncRequest);

	boolean isColumnComparisonError(String topic, String division, String sourceDatabase, String schema, String sourceTable, String targetDatabase, String targetSchema, String targetTable);

	boolean detectPartition(String division, String sourceDB, String sourceSchema, String sourceTable, String targetDB, String targetSchema, String targetTable) throws DatasourceNotFoundException;

	List<String> listConsumerGroups();
	List<SyncRequestEntity> findAllRunningSynchronizer();
}
