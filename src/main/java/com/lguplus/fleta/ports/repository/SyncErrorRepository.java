package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.dto.DbSyncOperation;
import com.lguplus.fleta.domain.dto.ErrorType;
import com.lguplus.fleta.domain.dto.SyncErrorCountOperationsDto;
import com.lguplus.fleta.domain.dto.Synchronizer;
import com.lguplus.fleta.domain.model.SyncErrorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SyncErrorRepository extends JpaRepository<SyncErrorEntity, Long>, CrudRepository<SyncErrorEntity, Long> {

	// important!: should have @Transactional in modifying data
	@Transactional
	@Modifying
	@Query(value = "UPDATE tbl_sync_task_error SET state=:state WHERE topic_name = :topicName and state!=:state", nativeQuery = true)
	int resolvedAll(@Param("topicName") String topicName, @Param("state") int state);

	@Query("SELECT s FROM SyncErrorEntity s WHERE s.topicName = :topicName and s.state in :states and s.errorType in :kidOfErrorType " +
			" and s.operation in :operations and s.errorTime between :dateFrom and :dateTo")
	Page<SyncErrorEntity> findPageByTopicNameAndOrderByState(@Param("topicName") String topicName,
															 @Param("states") List<Synchronizer.ErrorState> states,
															 @Param("operations") List<DbSyncOperation> operations,
															 @Param("kidOfErrorType") List<ErrorType> kidOfErrorTypes,
															 @Param("dateFrom") LocalDateTime dateFrom,
															 @Param("dateTo") LocalDateTime dateTo,
															 Pageable pageable);

	@Query("SELECT s FROM SyncErrorEntity s WHERE s.topicName = :topicName and s.state in :states and s.errorType in :kidOfErrorType " +
			" and s.operation in :operations and s.errorTime between :dateFrom and :dateTo")
	List<SyncErrorEntity> findByTopicNameAndOrderByState(@Param("topicName") String topicName,
														 @Param("states") List<Synchronizer.ErrorState> states,
														 @Param("operations") List<DbSyncOperation> operations,
														 @Param("kidOfErrorType") List<ErrorType> kidOfErrorTypes,
														 @Param("dateFrom") LocalDateTime dateFrom,
														 @Param("dateTo") LocalDateTime dateTo);

	@Transactional(timeout = 600)
	@Modifying
	@Query(value = "WITH cte AS (SELECT id FROM tbl_sync_task_error WHERE state = :stateError AND id IN :ids FOR UPDATE SKIP LOCKED) " +
			"UPDATE tbl_sync_task_error s SET state = :stateProcessing FROM cte WHERE  s.id = cte.id RETURNING *", nativeQuery = true)
	List<SyncErrorEntity> getErrorToRetry(@Param("ids") List<Long> ids,
										  @Param("stateError") int stateError,
										  @Param("stateProcessing") int stateProcessing);

	@Transactional(timeout = 600)
	@Modifying
	@Query(value = "WITH cte AS (SELECT id FROM tbl_sync_task_error WHERE topic_name = :topicName AND state = 0 order by error_time FOR UPDATE ) " +
			"UPDATE tbl_sync_task_error s SET state = :stateProcessing FROM cte WHERE  s.id = cte.id RETURNING *", nativeQuery = true)
	List<SyncErrorEntity> getAllErrorsByTopicNameAndUpdateStateByState(@Param("topicName") String topicName,
																	   @Param("stateProcessing") int stateProcessing);

	@Query(value = "select SUM(case when operation in ('INSERT', 'c') then 1 else 0 end) as total_insert, " +
			"       SUM(case when operation in ('UPDATE', 'u') then 1 else 0 end) as total_update, " +
			"       SUM(case when operation in ('DELETE', 'd') then 1 else 0 end) as total_delete from tbl_sync_task_error " +
			"       where topic_name = :topic and state IN :states", nativeQuery = true)
	SyncErrorCountOperationsDto countOperationsByTopicAndState(@Param("topic") String topic, @Param("states") List<Integer> states);

	@Transactional
	@Modifying
	@Query(value = "DELETE FROM tbl_sync_task_error WHERE topic_name = :topicName", nativeQuery = true)
	int deleteAllResolvedByTopic(@Param("topicName") String topicName);

	@Transactional
	@Modifying
	@Query(value = "DELETE FROM tbl_sync_task_error WHERE id IN :errorIds", nativeQuery = true)
	int deleteResolvedByIds(@Param("errorIds") List<Long> errorIds);

	@Transactional
	@Modifying
	@Query(value = "UPDATE tbl_sync_task_error SET state=:state WHERE state!=:state AND id IN :errorIds", nativeQuery = true)
	int updateErrorStateByIds(@Param("errorIds") List<Integer> errorIds, @Param("state") int state);

	@Transactional
	@Modifying
	@Query(value = "DELETE FROM SyncErrorEntity u WHERE u.errorTime < :time")
	int deleteBeforeTime(LocalDateTime time);
}
