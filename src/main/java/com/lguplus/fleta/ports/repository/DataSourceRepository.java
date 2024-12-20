package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.dto.DataSourceState;
import com.lguplus.fleta.domain.model.DataSourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataSourceRepository extends JpaRepository<DataSourceEntity, Long> {

	@Transactional
	@Modifying
	@Query(value = "DELETE  FROM DataSourceEntity WHERE id IN :ids and status = :status")
	void deleteByIds(List<Long> ids, DataSourceState status);

	@Transactional
	@Query(value = "SELECT id FROM DataSourceEntity WHERE url = :url OR serverName = :serverName")
	String getIdByServerNameOrUrl(String url, String serverName);

	@Transactional
	@Modifying
	@Query(value = "update DataSourceEntity ds set ds.status =:activeState WHERE ds.status = :inUsedState")
	int updateState(DataSourceState inUsedState, DataSourceState activeState);

	Optional<DataSourceEntity> findByServerName(String serverName);
}
