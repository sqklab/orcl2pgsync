package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.dto.ui.DbSchedulerResultDto;
import com.lguplus.fleta.domain.model.DbSchedulerResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface DbScheduleProcedureResultRepository extends JpaRepository<DbSchedulerResult, Long> {

	@Query(value = "SELECT" +
			" sp.id AS id," +
			" sp.fk_id_schedule_procedure AS fk_id_schedule_procedure," +
			" sp.error_msg AS error_msg," +
			" sp.schedule_time AS schedule_time," +
			" sp.schedule_date AS schedule_date," +
			" sp.status AS status," +
			" sp.start_at AS start_at," +
			" sp.end_at AS end_at ," +
			" info.pl_sql AS pl_SQL," +
			" info.db AS db," +
			" info.schedule_schema AS schedule_schema," +
			" info.schedule_table AS schedule_table " +
			" FROM tbl_db_schedule_procedure_result sp" +
			" INNER JOIN tbl_db_schedule_procedure info ON info.id = sp.fk_id_schedule_procedure " +
			" WHERE info.pl_sql LIKE :sql AND info.db LIKE :targetDB " +
			" AND sp.schedule_date =:date AND sp.schedule_time =:time AND sp.status IN :statusFilter", nativeQuery = true)
	Page<DbSchedulerResultDto> filter(Pageable page,
									  @Param("sql") String sql,
									  @Param("targetDB") String targetDB,
									  @Param("date") LocalDate date,
									  @Param("time") LocalTime time,
									  @Param("statusFilter") List<Boolean> statusFilter);

	@Query(value = "SELECT DISTINCT sp.scheduleTime FROM DbSchedulerResult sp " +
			"WHERE sp.scheduleDate = :date ORDER BY sp.scheduleTime DESC")
	List<LocalTime> getScheduleByDate(@Param("date") LocalDate date);

	List<DbSchedulerResult> findByScheduleDateOrderByScheduleTime(LocalDate compareDate);

	@Query(value = "SELECT " +
			"  sp.id AS id, " +
			"  sp.fk_id_schedule_procedure AS fk_id_schedule_procedure, " +
			"  sp.error_msg AS error_msg, " +
			"  sp.schedule_time AS schedule_time, " +
			"  sp.schedule_date AS schedule_date, " +
			"  sp.status AS status, " +
			"  sp.start_at AS start_at, " +
			"  sp.end_at AS end_at , " +
			"  info.pl_sql AS pl_sql, " +
			"  info.db AS db, " +
			"  info.schedule_schema AS schedule_schema, " +
			"  info.schedule_table AS schedule_table  " +
			"FROM tbl_db_schedule_procedure_result sp INNER JOIN tbl_db_schedule_procedure info ON info.ID = sp.fk_id_schedule_procedure  " +
			"WHERE sp.schedule_date = :date AND sp.schedule_time=:time", nativeQuery = true)
	List<DbSchedulerResultDto> findByScheduleDateAndScheduleTimeOrderByScheduleTime(@Param("date") LocalDate compareDate, @Param("time") LocalTime time);

	@Query(value = "SELECT " +
			"  sp.id AS id, " +
			"  sp.fk_id_schedule_procedure AS fk_id_schedule_procedure, " +
			"  sp.error_msg AS error_msg, " +
			"  sp.schedule_time AS schedule_time, " +
			"  sp.schedule_date AS schedule_date, " +
			"  sp.status AS status, " +
			"  sp.start_at AS start_at, " +
			"  sp.end_at AS end_at , " +
			"  info.pl_sql AS pl_sql, " +
			"  info.db AS db, " +
			"  info.schedule_schema AS schedule_schema, " +
			"  info.schedule_table AS schedule_table  " +
			"FROM tbl_db_schedule_procedure_result sp INNER JOIN tbl_db_schedule_procedure info ON info.ID = sp.fk_id_schedule_procedure  " +
			"WHERE sp.schedule_date = :date AND sp.schedule_time=:time", nativeQuery = true)
	List<DbSchedulerResultDto> getResultSummary(LocalDate date, LocalTime time);

	@Transactional
	@Modifying
	@Query(value = "UPDATE tbl_db_schedule_procedure_result s SET fk_id_schedule_procedure = :newIdOfScheduleInfo WHERE s.fk_id_schedule_procedure = :oldIdOfScheduleInfo", nativeQuery = true)
	int updateScheduleResultById(@Param("oldIdOfScheduleInfo") Long oldIdOfScheduleInfo, @Param("newIdOfScheduleInfo") Long newIdOfScheduleInfo);

	@Transactional
	@Modifying
	int deleteByScheduleDateAndScheduleTime(LocalDate date, LocalTime time);

	@Modifying
	int deleteAllByFkIdProcedureIn(List<Long> id);
}
