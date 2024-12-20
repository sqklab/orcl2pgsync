package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.model.comparison.DbComparisonResultSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

public interface DbComparisonResultSummaryRepository extends JpaRepository<DbComparisonResultSummaryEntity, Long> {

	DbComparisonResultSummaryEntity findByCompareDateAndCompareTime(LocalDate compareDate, LocalTime compareTime);

	@Transactional
	@Modifying
	@Query(value = "DELETE FROM tbl_db_comparison_result_summary info WHERE info.compare_date=:compareDate AND info.compare_time=:compareTime",
			nativeQuery = true)
	void deleteSummaryByDateAndTime(@Param("compareDate") LocalDate compareDate, @Param("compareTime") LocalTime compareTime);

	@Transactional
	@Modifying
	@Query(value = "DELETE FROM DbComparisonResultSummaryEntity u WHERE u.compareDate < :time")
	int deleteBeforeTime(LocalDate time);
}
