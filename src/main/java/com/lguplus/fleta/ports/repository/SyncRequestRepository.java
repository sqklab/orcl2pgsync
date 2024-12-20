package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.dto.*;
import com.lguplus.fleta.domain.dto.Synchronizer.SyncState;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.domain.model.comparison.DbComparisonInfoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SyncRequestRepository extends JpaRepository<SyncRequestEntity, Long>, CrudRepository<SyncRequestEntity, Long> {

	int countByState(SyncState state);

	@Query(value = "SELECT s.topicName FROM SyncRequestEntity s WHERE s.division = :division")
	List<String> findTopicListByDivision(String division);

	@Query(value = "SELECT s FROM SyncRequestEntity s WHERE s.topicName = :name")
	SyncRequestEntity findByTopicName(@Param("name") String name);

	@Query(value = "SELECT s FROM SyncRequestEntity s WHERE s.topicName IN :names")
	List<SyncRequestEntity> findByTopicNames(@Param("names") List<String> names);

	@Query(value = "SELECT s FROM SyncRequestEntity s WHERE s.topicName = :name AND s.synchronizerName = :synchronizer")
	SyncRequestEntity findByTopicNameAndSynchronizerName(@Param("name") String name, @Param("synchronizer") String synchronizer);

	@Query(value = "SELECT s FROM SyncRequestEntity s WHERE s.state IN :state")
	List<SyncRequestEntity> findByStateIn(@Param("state") List<SyncState> state);

	@Query(value = "SELECT s.primaryKeys FROM SyncRequestEntity s WHERE s.topicName = :name")
	String findPrimaryKeysWithTopicName(@Param("name") String name);

	@Query(value = "SELECT s FROM SyncRequestEntity s WHERE s.id IN :ids AND s.state = :state")
	List<SyncRequestEntity> findByIdsAndState(@Param("ids") List<Long> ids, SyncState state);

	@Query(value = "SELECT s FROM SyncRequestEntity s WHERE s.id IN :ids")
	List<SyncRequestEntity> findByIds(@Param("ids") List<Long> ids);

	@Query(value = "SELECT d FROM DbComparisonInfoEntity d JOIN SyncRequestEntity s ON s.id = :id AND d.syncInfo = :id")
	DbComparisonInfoEntity getDbComparisonInfo(@Param("id") Long id);

	List<SyncRequestEntity> findAllBySourceDatabaseAndSourceSchemaAndSourceTableAndState(
			String sourceDatabase, String sourceSchema, String sourceTable, Synchronizer.SyncState state
	);

	@Query(value = "select s FROM SyncRequestEntity s where s.state = 1")
	List<SyncRequestEntity> findAllRunningSynchronizer();
	@Query(value = "SELECT info.id AS id," +
			"	info.synchronizer_name," +
			"	info.source_database," +
			"	info.source_schema," +
			"	info.source_table," +
			"	info.topic_name," +
			"	info.target_database," +
			"	info.target_schema," +
			"	info.target_table," +
			"	info.state," +
			"	info.division," +
			"	info.enable_truncate," +
			"	info.created_at," +
			"	info.updated_at," +
			"	info.created_user," +
			"	info.updated_user," +
			"	coalesce(error.total_error, 0) AS total_error, " +
			"	coalesce(error.total_resolve, 0) AS total_resolve, " +
			"	coalesce(error.total, 0) AS total, " +
			"	info.primary_keys, info.unique_keys, info.is_partitioned " +
			"		FROM" +
			"	  (SELECT *" +
			"	   FROM tbl_sync_task_info info" +
			"	   WHERE info.updated_at >= :dateFrom" +
			"			AND info.updated_at <= :dateTo " +
			"			AND info.state IN :states" +
			"			AND info.division LIKE %:division% AND lower(info.topic_name) SIMILAR TO lower(:searchTopicNames) " +
			"           and (info.source_schema like %:selectedSchema%  or info.target_schema like %:selectedSchema% )" +
			"           and (info.source_database like %:selectedDB% or info.target_database like %:selectedDB%)" +
			"	   ) info" +
			"	LEFT JOIN" +
			"	  (" +
			"      SELECT " +
			"   error.topic_name as topic_name, " +
			" SUM(case when error.state = 0 then 1 else 0 end) as total_error, " +
			" SUM(case when error.state = 1 then 1 else 0 end) as total_resolve, " +
			" count(1) as total " +
			" FROM tbl_sync_task_error as error " +
			" GROUP BY topic_name" +
			") error ON info.topic_name = error.topic_name ",
			countQuery = "SELECT COUNT(id) FROM tbl_sync_task_info info" +
					"	WHERE info.updated_at >= :dateFrom" +
					"			AND info.updated_at < :dateTo " +
					"	AND info.state IN :states AND info.division LIKE %:division% AND lower(info.topic_name) SIMILAR TO lower(:searchTopicNames) " +
					"   and (info.source_schema like %:selectedSchema%  or info.target_schema like %:selectedSchema% )" +
					"   and (info.source_database like %:selectedDB% or info.target_database like %:selectedDB%)",
			nativeQuery = true)
	Page<SyncInfoDto> findAllByNumberOfErrorAndPeriod(Pageable page, LocalDate dateFrom, LocalDate dateTo, int[] states, String division, String searchTopicNames, String selectedDB, String selectedSchema);

	@Query(value = "SELECT info.id AS id," +
			"	info.synchronizer_name," +
			"	info.source_database," +
			"	info.source_schema," +
			"	info.source_table," +
			"	info.topic_name," +
			"	info.target_database," +
			"	info.target_schema," +
			"	info.target_table," +
			"	info.state," +
			"	info.division," +
			"	info.enable_truncate," +
			"	info.created_at," +
			"	info.updated_at," +
			"	info.created_user," +
			"	info.updated_user," +
			"	coalesce(error.total_error, 0) AS total_error, " +
			"	coalesce(error.total_resolve, 0) AS total_resolve, " +
			"	coalesce(error.total, 0) AS total " +
			"		FROM" +
			"	  (SELECT *" +
			"	   FROM tbl_sync_task_info info" +
			"	   WHERE info.updated_at >= :dateFrom" +
			"			AND info.updated_at <= :dateTo " +
			"			AND info.state IN :states" +
			"			AND info.topic_name LIKE %:topicName% AND info.division LIKE %:division% " +
			"	   ) info" +
			"	LEFT JOIN" +
			"	  (" +
			"      SELECT " +
			"   error.topic_name as topic_name, " +
			" SUM(case when error.state = 0 then 1 else 0 end) as total_error, " +
			" SUM(case when error.state = 1 then 1 else 0 end) as total_resolve, " +
			" count(1) as total " +
			" FROM tbl_sync_task_error as error " +
			" GROUP BY topic_name" +
			") error ON info.topic_name = error.topic_name",
			countQuery = "SELECT COUNT(id) FROM tbl_sync_task_info info" +
					"	WHERE info.updated_at >= :dateFrom" +
					"			AND info.updated_at < :dateTo " +
					"	AND info.state IN :states AND info.division LIKE %:division%  AND info.topic_name LIKE %:topicName%",
			nativeQuery = true)
	Page<SyncInfoDto> findSyncAndRelatedInfo(Pageable page, LocalDate dateFrom, LocalDate dateTo, String topicName, int[] states, String division);

	@Query(value = "SELECT info.id AS id," +
			"	info.synchronizer_name," +
			"	info.source_database," +
			"	info.source_schema," +
			"	info.source_table," +
			"	info.topic_name," +
			"	info.target_database," +
			"	info.target_schema," +
			"	info.target_table," +
			"	info.state," +
			"	info.division," +
			"	info.enable_truncate," +
			"	info.created_at," +
			"	info.updated_at," +
			"	info.created_user," +
			"	info.updated_user," +
			"	coalesce(error.total_error, 0) AS total_error, " +
			"	coalesce(error.total_resolve, 0) AS total_resolve, " +
			"	coalesce(error.total, 0) AS total, " +
			"	info.primary_keys, info.unique_keys, info.is_partitioned " +
			"	FROM" +
			"	  (SELECT *" +
			"	  FROM tbl_sync_task_info info" +
			"	  WHERE " +
			"			info.state IN :states" +
			"			AND info.division LIKE %:division% AND lower(info.topic_name) SIMILAR TO lower(:searchTopicNames) " +
			"           and (info.source_schema like %:selectedSchema%  or info.target_schema like %:selectedSchema% )" +
			"           and (info.source_database like %:selectedDB% or info.target_database like %:selectedDB%) " +

			"	   ) info" +
			"	LEFT JOIN" +
			"	  (" +
			"      SELECT " +
			"   error.topic_name as topic_name, " +
			" SUM(case when error.state = 0 then 1 else 0 end) as total_error, " +
			" SUM(case when error.state = 1 then 1 else 0 end) as total_resolve, " +
			" count(1) as total " +
			" FROM tbl_sync_task_error as error " +
			" GROUP BY topic_name" +
			") error ON info.topic_name = error.topic_name ",
			countQuery = "SELECT COUNT(id) FROM tbl_sync_task_info info" +
					"	WHERE info.state IN :states AND info.division LIKE %:division% AND lower(info.topic_name) SIMILAR TO lower(:searchTopicNames)" +
					" and (info.source_schema like %:selectedSchema%  or info.target_schema like %:selectedSchema% )" +
					"   and (info.source_database like %:selectedDB% or info.target_database like %:selectedDB%) " ,
			nativeQuery = true)
	Page<SyncInfoDto> findAllByNumberOfError(Pageable page, int[] states, String division, String searchTopicNames, String selectedDB, String selectedSchema);

	@Query(value = "SELECT info.id AS id, " +
			" info.synchronizer_name, " +
			" info.source_database, " +
			" info.source_schema, " +
			" info.source_table, " +
			" info.topic_name, " +
			" info.target_database, " +
			" info.target_schema, " +
			" info.target_table, " +
			" info.state, " +
			" info.division, " +
			" info.enable_truncate," +
			" info.created_at, " +
			" info.updated_at, " +
			" info.created_user," +
			" info.updated_user," +
			" coalesce(error.total_error, 0) AS total_error, " +
			" coalesce(error.total_resolve, 0) AS total_resolve, " +
			" coalesce(error.total, 0) AS total, " +
			" info.primary_keys, info.unique_keys, info.is_partitioned, " +
			"lrm.scn, " +
			"lrm.commit_scn, " +
			"lrm.msg_timestamp, " +
			"cast(lrm.received_datetime as date) as received_date, " +
			"cast(lrm.received_datetime as time) as received_time," +
			"lrm.received_datetime as received_date_time " +
			" FROM " +
			"   (SELECT * " +
			"FROM tbl_sync_task_info info " +
			"WHERE info.updated_at >= :dateFrom " +
			"AND info.updated_at <= :dateTo " +
			"AND info.state IN :states " +
			" AND lower(info.topic_name) SIMILAR TO lower(:searchTopicNames) AND info.division LIKE %:division%" +
			" and (info.source_schema like %:selectedSchema%  or info.target_schema like %:selectedSchema% )" +
			"   and (info.source_database like %:selectedDB% or info.target_database like %:selectedDB%) " +
			"    ) info " +
			" LEFT JOIN " +
			"   ( " +
			"      SELECT " +
			"   error.topic_name as topic_name, " +
			" SUM(case when error.state = 0 then 1 else 0 end) as total_error, " +
			" SUM(case when error.state = 1 then 1 else 0 end) as total_resolve, " +
			" count(1) as total " +
			" FROM tbl_sync_task_error as error " +
			" GROUP BY topic_name " +
			") error ON info.topic_name = error.topic_name " +
			" LEFT JOIN (select l1.topic as topic, " +
			"       case when r2.last_received is null or r2.last_received < l1.last_received  then l1.last_received else r2.last_received end as received_datetime, " +
			"       case when r2.last_commit_scn is null or r2.last_commit_scn < l1.last_commit_scn  then l1.last_commit_scn else r2.last_commit_scn end as commit_scn, " +
			"       case when r2.last_scn is null or r2.last_scn < l1.last_scn  then l1.last_scn else r2.last_scn end as scn, " +
			"       case when r2.last_msg_timestamp is null or r2.last_msg_timestamp < l1.last_msg_timestamp  then l1.last_msg_timestamp else r2.last_msg_timestamp end as msg_timestamp " +
			"from (select topic, " +
			"             received_date + received_time as last_received, " +
			"             commit_scn as last_commit_scn, " +
			"             scn as last_scn, " +
			"             msg_timestamp as last_msg_timestamp from tbl_last_received_message_info ) as l1 " +
			"    left join (select topic, " +
			"                      max(r.received_date + r.received_time) as last_received, " +
			"                      max(r.commit_scn) as last_commit_scn, " +
			"                      max(r.scn) as last_scn, " +
			"                      max(r.msg_timestamp) as last_msg_timestamp " +
			"                from tbl_received_message r group by topic) r2 " +
			"    on l1.topic = r2.topic order by r2.last_received) " +
			"     lrm ON lrm.topic = info.topic_name " +
			"order by case when lrm.received_datetime is null then 1 else 0 end, received_date_time DESC",
			countQuery = "SELECT COUNT(id) FROM tbl_sync_task_info info" +
					"	WHERE info.updated_at >= :dateFrom" +
					"			AND info.updated_at < :dateTo " +
					"	AND info.state IN :states AND info.division LIKE %:division%  AND lower(info.topic_name) SIMILAR TO lower(:searchTopicNames)" +
					"   and (info.source_schema like %:selectedSchema%  or info.target_schema like %:selectedSchema% )" +
					"   and (info.source_database like %:selectedDB% or info.target_database like %:selectedDB%) ",
			nativeQuery = true)
	Page<SyncInfoByLastReceivedTimeDto> findAllByNumberOfErrorByLastReceivedTimeDESC(Pageable page, LocalDateTime dateFrom, LocalDateTime dateTo, String searchTopicNames,
	                                                                                 int[] states, String division, String selectedDB, String selectedSchema);


	@Query(value = "SELECT info.id AS id, " +
			" info.synchronizer_name, " +
			" info.source_database, " +
			" info.source_schema, " +
			" info.source_table, " +
			" info.topic_name, " +
			" info.target_database, " +
			" info.target_schema, " +
			" info.target_table, " +
			" info.state, " +
			" info.division, " +
			" info.enable_truncate," +
			" info.created_at, " +
			" info.updated_at, " +
			" info.created_user," +
			" info.updated_user," +
			" coalesce(error.total_error, 0) AS total_error, " +
			" coalesce(error.total_resolve, 0) AS total_resolve, " +
			" coalesce(error.total, 0) AS total, " +
			" info.primary_keys, info.unique_keys, info.is_partitioned," +
			"lrm.scn, " +
			"lrm.commit_scn, " +
			"lrm.msg_timestamp, " +
			"cast(lrm.received_datetime as date) as received_date, " +
			"cast(lrm.received_datetime as time) as received_time," +
			"lrm.received_datetime as received_date_time " +
			" FROM " +
			"   (SELECT * FROM tbl_sync_task_info info " +
			"WHERE info.updated_at >= :dateFrom " +
			"AND info.updated_at <= :dateTo " +
			"AND info.state IN :states " +
			"  AND info.division LIKE %:division% AND lower(info.topic_name) SIMILAR TO lower(:searchTopicNames)" +
			"  and (info.source_schema like %:selectedSchema%  or info.target_schema like %:selectedSchema% )" +
			"  and (info.source_database like %:selectedDB% or info.target_database like %:selectedDB%) " +
			") info " +
			" LEFT JOIN " +
			"   ( " +
			"      SELECT " +
			"   error.topic_name as topic_name, " +
			" SUM(case when error.state = 0 then 1 else 0 end) as total_error, " +
			" SUM(case when error.state = 1 then 1 else 0 end) as total_resolve, " +
			" count(1) as total " +
			" FROM tbl_sync_task_error as error " +
			" GROUP BY topic_name " +
			") error ON info.topic_name = error.topic_name " +
			" LEFT JOIN (select l1.topic as topic, " +
			"       case when r2.last_received is null or r2.last_received < l1.last_received  then l1.last_received else r2.last_received end as received_datetime, " +
			"       case when r2.last_commit_scn is null or r2.last_commit_scn < l1.last_commit_scn  then l1.last_commit_scn else r2.last_commit_scn end as commit_scn, " +
			"       case when r2.last_scn is null or r2.last_scn < l1.last_scn  then l1.last_scn else r2.last_scn end as scn, " +
			"       case when r2.last_msg_timestamp is null or r2.last_msg_timestamp < l1.last_msg_timestamp  then l1.last_msg_timestamp else r2.last_msg_timestamp end as msg_timestamp " +
			"from (select topic, " +
			"             received_date + received_time as last_received, " +
			"             commit_scn as last_commit_scn, " +
			"             scn as last_scn, " +
			"             msg_timestamp as last_msg_timestamp from tbl_last_received_message_info ) as l1 " +
			"    left join (select topic, " +
			"                      max(r.received_date + r.received_time) as last_received, " +
			"                      max(r.commit_scn) as last_commit_scn, " +
			"                      max(r.scn) as last_scn, " +
			"                      max(r.msg_timestamp) as last_msg_timestamp " +
			"                from tbl_received_message r group by topic) r2 " +
			"    on l1.topic = r2.topic order by r2.last_received) " +
			"     lrm ON lrm.topic = info.topic_name " +
			"order by case when lrm.received_datetime is null then 1 else 0 end, received_date_time ASC",
			countQuery = "SELECT COUNT(id) FROM tbl_sync_task_info info" +
					"	WHERE info.updated_at >= :dateFrom" +
					"			AND info.updated_at < :dateTo " +
					"	AND info.state IN :states AND info.division LIKE %:division% AND lower(info.topic_name) SIMILAR TO lower(:searchTopicNames)" +
					"   and (info.source_schema like %:selectedSchema%  or info.target_schema like %:selectedSchema% )" +
					"   and (info.source_database like %:selectedDB% or info.target_database like %:selectedDB%) ",
			nativeQuery = true)
	Page<SyncInfoByLastReceivedTimeDto> findAllByNumberOfErrorByLastReceivedTimeASC(Pageable page, LocalDateTime dateFrom, LocalDateTime dateTo, int[] states,
	                                                                                String division, String searchTopicNames, String selectedDB, String selectedSchema);

	@Query(value = "SELECT	" +
			"SUM(case when info.state  = 0 then 1 else 0 end) as pending, " +
			"SUM(case when info.state  = 1 then 1 else 0 end) as running, " +
			"SUM(case when info.state  = 2 then 1 else 0 end) as stopped,  " +
			"SUM(case when info.state  = 3 then 1 else 0 end) as linked  " +
			"FROM tbl_sync_task_info info",
			nativeQuery = true)
	SyncStateCountDto countSyncState();

	SyncRequestEntity findBySynchronizerName(String synchronzierName);

	@Query(value = "SELECT sre FROM SyncRequestEntity sre LEFT JOIN FETCH sre.dbComparisonInfoEntities where sre.id = :id")
	SyncRequestEntity findSyncRequestById(Long id);

	@Query(value = "SELECT sre.id, sre.topic_name, sre.synchronizer_name FROM tbl_sync_task_info sre", nativeQuery = true)
	List<SyncInfoBase> findIdAndNameOfSyncTask();

	@Query(value = "SELECT distinct s.division FROM SyncRequestEntity s")
	List<String> findDivisions();

	@Query(value = "SELECT s.topicName FROM SyncRequestEntity s WHERE s.state IN :states")
	List<String> findAllTopicsByState(SyncState[] states);

	@Query(value = "SELECT s FROM SyncRequestEntity s WHERE s.state IN :states")
	List<SyncRequestEntity> findByStateIn(SyncState[] states);

	@Query(value = "SELECT s.topicName FROM SyncRequestEntity s")
	List<String> findAllTopicNames();

	@Query(value = "SELECT s.topicName FROM SyncRequestEntity s WHERE s.topicName IN :topicNames")
	List<String> findAllTopicsByTopicNames(String[] topicNames);

	@Query(value = "select distinct  (coalesce(consumer_group, concat(:pattern, id)))" +
			"    as consumer_group from tbl_sync_task_info;", nativeQuery = true)
	List<String> findConsumerGroups(@Param("pattern") String pattern);
}