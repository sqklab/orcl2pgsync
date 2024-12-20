package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.model.comparison.DbComparisonSchedulerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DbComparisonScheduleRepository extends JpaRepository<DbComparisonSchedulerEntity, Integer> {

	List<DbComparisonSchedulerEntity> findByStateOrderByTime(DbComparisonSchedulerEntity.ComparisonScheduleState state);

	long countByState(@Param("state") DbComparisonSchedulerEntity.ComparisonScheduleState state);

	@Transactional
	@Modifying
	@Query("UPDATE DbComparisonSchedulerEntity e SET e.state = :state")
	int updateState(@Param("state") DbComparisonSchedulerEntity.ComparisonScheduleState state);
}
