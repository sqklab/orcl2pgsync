package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.model.LastMessageInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LastMessageInfoRepository extends JpaRepository<LastMessageInfoEntity, Long> {

	@Query(value = "SELECT * FROM tbl_last_received_message_info WHERE topic in :topic", nativeQuery = true)
	List<LastMessageInfoEntity> findLastMessageInfoEntityByListTopic(List<String> topic);
}
