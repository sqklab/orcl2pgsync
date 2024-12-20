package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.model.DbScheduler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface DbScheduleProcedureRepository extends JpaRepository<DbScheduler, Long> {

	@Query(value = "SELECT sp FROM DbScheduler sp " +
			"WHERE (sp.plSQL is null or sp.plSQL LIKE :sql) AND (sp.db is null or sp.db LIKE :targetDB) AND sp.status IN :status")
	Page<DbScheduler> filter(Pageable page,
							 @Param("sql") String sql,
							 @Param("targetDB") String targetDB,
							 @Param("status") List<Boolean> status);

	@Query(value = "SELECT sp FROM DbScheduler sp " +
			"WHERE (sp.plSQL is null or sp.plSQL LIKE :sql) AND (sp.db is null or sp.db LIKE :targetDB) AND sp.createdAt = :date AND sp.status IN :status")
	Page<DbScheduler> filterWithDate(Pageable page,
									 @Param("sql") String sql,
									 @Param("targetDB") String targetDB,
									 @Param("date") LocalDate date,
									 @Param("status") List<Boolean> status);

	@Transactional(timeout = 300)
	@Modifying
	@Query(value = "WITH cte AS (SELECT id FROM tbl_db_schedule_procedure ci WHERE ci.enable=true AND ci.process_status = :stateStop LIMIT :limit FOR UPDATE SKIP LOCKED ) " +
			" UPDATE tbl_db_schedule_procedure s SET process_status = true FROM cte WHERE s.id = cte.id RETURNING *", nativeQuery = true)
	List<DbScheduler> findStoppedListToSchedule(@Param("stateStop") Boolean stateStop,
												@Param("limit") int limit);

	@Transactional(timeout = 300)
	@Modifying
	@Query(value = "WITH cte AS (SELECT id FROM tbl_db_schedule_procedure ci WHERE ci.id IN :ids FOR UPDATE SKIP LOCKED ) " +
			" UPDATE tbl_db_schedule_procedure s SET last_run = :last_run FROM cte WHERE s.id = cte.id RETURNING s.id", nativeQuery = true)
	Set<Long> findExistedIDAndUpdateLastRun(@Param("ids") List<Long> ids, @Param("last_run") LocalDateTime lastRun);

	@Transactional
	@Modifying
	@Query(value = "UPDATE tbl_db_schedule_procedure SET process_status=:stateStop WHERE id IN :ids", nativeQuery = true)
	int releaseData(@Param("ids") List<Long> ids, @Param("stateStop") Boolean stateStop);

	@Transactional(timeout = 300)
	@Modifying
	@Query(value = "WITH cte AS (SELECT * FROM tbl_db_schedule_procedure ci WHERE ci.enable=true and process_status=:running and last_run < :timeAlive FOR UPDATE SKIP LOCKED ) " +
			" UPDATE tbl_db_schedule_procedure s SET last_run = :last_run FROM cte WHERE s.id = cte.id RETURNING *", nativeQuery = true)
	List<DbScheduler> findAllByProcessStatusAndLastRunLessThanAndUpdateLastRunQuickly(@Param("running") boolean running,
																					  @Param("timeAlive") LocalDateTime timeAlive,
																					  @Param("last_run") LocalDateTime lastRun);

	List<DbScheduler> findAllByStatus(boolean running);
}
