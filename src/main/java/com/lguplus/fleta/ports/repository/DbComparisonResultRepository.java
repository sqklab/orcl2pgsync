package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.dto.comparison.ComparisonSummary;
import com.lguplus.fleta.domain.model.comparison.ComparisonResultEntity;
import com.lguplus.fleta.domain.model.comparison.DbComparisonResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface DbComparisonResultRepository extends JpaRepository<DbComparisonResultEntity, Long> {

	@Query(value = "" +
			"SELECT count(id) " +
			"FROM tbl_db_comparison_result s " +
			"WHERE " +
			"	s.sync_compare_id =:comparisonId " +
			"	AND s.compare_date =:compareDate " +
			"	AND s.compare_time =:compareTime" +
			"", nativeQuery = true)
	int countBySyncCompareIdAndCompareDateAndCompareTime(
			@Param("comparisonId") int comparisonId,
			@Param("compareDate") LocalDate compareDate,
			@Param("compareTime") LocalTime compareTime
	);

	@Override
	Optional<DbComparisonResultEntity> findById(Long id);

	List<DbComparisonResultEntity> findAllByCompareDateAndCompareTimeAndComparisonStateNot(
			LocalDate compareDate,
			LocalTime compareTime,
			ComparisonResultEntity.ComparisonState comparisonState
	);

	@Query(value = "SELECT DISTINCT s.compareTime FROM DbComparisonResultEntity s WHERE s.compareDate = :compareDate ORDER BY s.compareTime DESC")
	List<LocalTime> findDistinctByCompareDate(@Param("compareDate") LocalDate compareDate);

	long countByCompareDateAndAndCompareTime(@Param("compareDate") LocalDate compareDate, @Param("compareTime") LocalTime compareTime);

	@Query(value = "SELECT info.compare_date, info.compare_time, COUNT(info.comparison_state) as total, SUM(info.source_count) as sourceCount, SUM(info.target_count) as targetCount, " +
			"       SUM(case when info.comparison_state = 0 then 1 else 0 end) as equal, " +
			"       SUM(case when info.comparison_state = 1 then 1 else 0 end) as different, " +
			"       SUM(case when info.comparison_state = 2 then 1 else 0 end) as fail " +
			"FROM tbl_db_comparison_result info WHERE info.compare_date =:compareDate AND info.compare_time =:compareTime GROUP BY info.compare_date, info.compare_time",
			nativeQuery = true)
	ComparisonSummary getComparisonSummary(@Param("compareDate") LocalDate compareDate, @Param("compareTime") LocalTime compareTime);


	@Query(value = "SELECT info.compare_date, info.compare_time, " +
			"       COUNT(info.comparison_state) as total, " +
			"       SUM(info.source_count) as sourceCount, " +
			"       SUM(info.target_count) as targetCount, " +
			"       SUM(case when info.comparison_state = 0 then 1 else 0 end) as equal, " +
			"       SUM(case when info.comparison_state = 1 then 1 else 0 end) as different, " +
			"       SUM(case when info.comparison_state = 2 then 1 else 0 end) as fail, info.division " +
			"FROM view_comparison_result info WHERE info.compare_date =:compareDate AND info.compare_time =:compareTime GROUP BY info.compare_date, info.compare_time, info.division order by info.division",
			nativeQuery = true)
	List<ComparisonSummary> getComparisonSummaryByDivision(@Param("compareDate") LocalDate compareDate, @Param("compareTime") LocalTime compareTime);


	@Transactional
	@Modifying
	@Query(value = "DELETE FROM tbl_db_comparison_result info WHERE info.compare_date=:compareDate AND info.compare_time=:compareTime",
			nativeQuery = true)
	void deleteSummaryByDateAndTime(@Param("compareDate") LocalDate compareDate, @Param("compareTime") LocalTime compareTime);

	@Transactional
	@Modifying
	@Query(value = "UPDATE tbl_db_comparison_result SET notified = true WHERE (notified = false OR notified is null) AND compare_date =:compareDate AND compare_time =:compareTime",
			nativeQuery = true)
	int updateNotified(@Param("compareDate") LocalDate compareDate, @Param("compareTime") LocalTime compareTime);

	@Transactional
	@Modifying
	@Query(value = "DELETE FROM DbComparisonResultEntity u WHERE u.compareDate < :time")
	int deleteBeforeTime(LocalDate time);
}
