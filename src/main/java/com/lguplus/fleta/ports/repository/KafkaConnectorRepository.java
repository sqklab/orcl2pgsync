package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.model.KafkaConnectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KafkaConnectorRepository extends JpaRepository<KafkaConnectorEntity, Long> {

	@Query(value = "SELECT u FROM KafkaConnectorEntity u WHERE u.deleted IS NULL or u.deleted = false")
	List<KafkaConnectorEntity> findAllActive();

	@Query(value = "SELECT u from KafkaConnectorEntity u where u.name = :name AND u.type = :type AND (u.deleted IS NULL or u.deleted = false)")
	List<KafkaConnectorEntity> findByNameAndTypeAndDeletedFalse(@Param("name") String name, @Param("type") String type);

	@Query(value = "SELECT u from KafkaConnectorEntity u where u.name = :name AND u.type = :type AND u.deleted = true")
	List<KafkaConnectorEntity> findByNameAndTypeAndDeletedTrue(@Param("name") String name, @Param("type") String type);

	KafkaConnectorEntity findById(long id);

}
