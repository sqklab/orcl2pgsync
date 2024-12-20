package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.dto.SyncExportInfoDto;
import com.lguplus.fleta.domain.dto.SyncInfoDto;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SyncRequestExportRepository extends JpaRepository<SyncRequestEntity, Long> {

	@Query(value = "SELECT info.*, " +
			"  cp.is_comparable, " +
			"  cp.enable_column_comparison, " +
			"  cp.source_compare_database, " +
			"  cp.target_compare_database, " +
			"  cp.source_query, " +
			"  cp.target_query  " +
			" FROM tbl_sync_task_info info LEFT JOIN tbl_db_comparison_info cp ON cp.sync_id = info.ID " +
			" WHERE info.state IN :states " +
			" AND info.division LIKE %:division% " +
			" AND lower(info.topic_name) SIMILAR TO lower(:searchTopicNames) " +
			" AND (info.source_schema like %:selectedSchema%  or info.target_schema like %:selectedSchema% ) " +
			" AND (info.source_database like %:selectedDB% or info.target_database like %:selectedDB%)  ",
			nativeQuery = true)
	List<SyncExportInfoDto> exportByConditions(String division, String searchTopicNames, String selectedDB, String selectedSchema, int[] states);



	@Query(value = "SELECT info.*, " +
			"  cp.is_comparable, " +
			"  cp.enable_column_comparison, " +
			"  cp.source_compare_database, " +
			"  cp.target_compare_database, " +
			"  cp.source_query, " +
			"  cp.target_query  " +
			" FROM tbl_sync_task_info info LEFT JOIN tbl_db_comparison_info cp ON cp.sync_id = info.ID " +
			" WHERE info.updated_at >= :dateFrom  " +
			" AND info.updated_at <= :dateTo " +
			" AND info.state IN :states" +
			" AND info.division LIKE %:division% " +
			" AND lower(info.topic_name) SIMILAR TO lower(:searchTopicNames) " +
			" AND (info.source_schema like %:selectedSchema%  or info.target_schema like %:selectedSchema% ) " +
			" AND (info.source_database like %:selectedDB% or info.target_database like %:selectedDB%)  ",
			nativeQuery = true)
	List<SyncExportInfoDto> exportByPeriodTimeParams(String division, String searchTopicNames, String selectedDB, String selectedSchema, int[] states, LocalDate dateFrom, LocalDate dateTo);
}