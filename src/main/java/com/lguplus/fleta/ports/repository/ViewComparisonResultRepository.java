package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.model.comparison.ComparisonResultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ViewComparisonResultRepository extends JpaRepository<ComparisonResultEntity, Integer> {

	List<ComparisonResultEntity> findByCompareDateAndCompareTimeOrderByLastModifiedDesc(LocalDate compareDate, LocalTime time);

	@Query(value = "SELECT s.*,s.target_count-s.source_count AS numberDiff FROM view_comparison_result AS s WHERE s.compare_date=:dateTime ORDER BY s.compare_time DESC", nativeQuery = true)
	List<ComparisonResultEntity> findByCompareDate(@Param("dateTime") LocalDate compareDate);

	@Query(value = "SELECT v.compare_result_id from view_comparison_result v WHERE v.compare_date=:compareDate and v.compare_time=:compareTime and v.number_diff <= :threadHoldRetry and v.number_diff > 0", nativeQuery = true)
	List<Long> findByDateTimeAndTheDiffLessThan(@Param("compareDate") LocalDate compareDate,
												@Param("compareTime") LocalTime compareTime,
												@Param("threadHoldRetry") int threadHoldRetry);

	@Query("select v from ComparisonResultEntity v where v.compareDate = :compareDate and v.compareTime = :compareTime" +
			" and (v.sourceDatabase like :sourceDB)" +
			" and (v.sourceSchema like :sourceSchema)" +
			" and (v.targetDatabase like :targetDB)" +
			" and (v.targetSchema like :targetSchema)" +
			" and (lower(v.sourceTable) like :sourceTable or (lower(v.targetTable) like :targetTable))" +
			" and v.comparisonState IN :comparisonState  and v.division like :divisions")
	Page<ComparisonResultEntity> findByCriteria(Pageable page,
												@Param("compareDate") LocalDate compareDate, @Param("compareTime") LocalTime compareTime,
												@Param("sourceDB") String sourceDB, @Param("sourceSchema") String sourceSchema,
												@Param("targetDB") String targetDB, @Param("targetSchema") String targetSchema,
												@Param("sourceTable") String sourceTable, @Param("targetTable") String targetTable,
												@Param("divisions") String divisions, @Param("comparisonState") List<ComparisonResultEntity.ComparisonState> comparisonState);
}
