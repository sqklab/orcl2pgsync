package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.model.comparison.DbComparisonInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DbComparisonInfoRepository extends JpaRepository<DbComparisonInfoEntity, Integer> {

  @Query ("SELECT c FROM DbComparisonInfoEntity c WHERE c.syncInfo.id =:syncId")
	DbComparisonInfoEntity getBySyncId(@Param("syncId") Long syncId);
	
	@Query("" +
			"SELECT c " +
			"FROM DbComparisonInfoEntity c " +
			"WHERE c.syncInfo.id = :syncId" +
			""
	)
	List<DbComparisonInfoEntity> findAllBySyncInfoId(@Param("syncId") long syncId);

	@Query(value = "" +
			"SELECT " +
			"	ci.last_run " +
			"FROM tbl_db_comparison_info ci " +
			"WHERE " +
			"	ci.is_comparable = 'Y' " +
			"ORDER BY ci.last_run asc " +
			"limit 1" +
			"", nativeQuery = true
	)
	LocalDateTime getEarliestLastRun();

	@Transactional(timeout = 300)
	@Modifying
	@Query(value = "" +
			"WITH cte AS (" +
			"	SELECT id " +
			"	FROM tbl_db_comparison_info ci " +
			"	WHERE " +
			"		ci.state = 0 " +
			"		AND (ci.last_run is null OR ci.last_run < :beforeDate) " +
			"		AND ci.is_comparable = 'Y' " +
			"	LIMIT 1000 FOR UPDATE SKIP LOCKED" +
			") " +
			"UPDATE tbl_db_comparison_info s " +
			"SET " +
			"	state = 1, " +
			"	last_run = :runDate " +
			"FROM cte " +
			"WHERE s.id = cte.id " +
			"RETURNING *" +
			"", nativeQuery = true)
	List<DbComparisonInfoEntity> findAllComparisonItemsWithUpdatingFlagToRunning(
			@Param("runDate") LocalDateTime runDate,
			@Param("beforeDate") LocalDateTime beforeDate
	);

	@Query(value = "" +
			"SELECT " +
			"	COUNT(id) " +
			"FROM tbl_db_comparison_info ci " +
			"WHERE " +
			"	ci.is_comparable = 'Y'" +
			"", nativeQuery = true
	)
	long countListToCompare();


	@Transactional
	@Modifying
	@Query(value = "" +
			"UPDATE tbl_db_comparison_info " +
			"set " +
			"	state = 0 " +
			"WHERE " +
			"	state <> 0 " +
			"	and last_run <> :runDate " +
			"	and is_comparable = 'Y'" +
			"", nativeQuery = true
	)
	void resetCompareState(@Param("runDate") LocalDateTime runDate);
}
