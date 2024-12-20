package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.model.SynchronizerHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface SyncHistoryRepository extends JpaRepository<SynchronizerHistoryEntity, Integer> {

	@Query(value = "select s FROM SynchronizerHistoryEntity s where s.synchronizerId = :syncId")
	List<SynchronizerHistoryEntity> getHistoryInfo(@Param("syncId") Long syncId);
}