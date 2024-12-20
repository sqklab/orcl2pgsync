package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.dto.LastMessageInfoDto;
import com.lguplus.fleta.domain.dto.analysis.MessageAnalysisPerMinuteDto;
import com.lguplus.fleta.domain.model.ReceivedMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ReceivedMessageRepository extends JpaRepository<ReceivedMessageEntity, String> {

	@Transactional
	@Modifying
	@Query(value = "with deleted as (delete from tbl_received_message rs where topic = " +
			"(select topic from tbl_received_message where topic = :topic FOR UPDATE SKIP LOCKED limit 1) " +
			"RETURNING rs.topic, rs.received_date, rs.received_time, rs.msg_latency) " +
			"SELECT cast(date_trunc('day', received_date) as date) AS atDate, " +
			"cast(date_trunc('minute', received_time) AS time) AS atTime, " +
			"count(*) AS receivedMessage," +
			"sum(msg_latency) as totalLatency FROM deleted WHERE topic = :topic " +
			"GROUP BY atDate, atTime", nativeQuery = true)
	List<MessageAnalysisPerMinuteDto> selectAndDeleteMessageByTopic(String topic);

	@Query(value = "select max(k.received_date + k.received_time) as receivedDateTime, " +
			"				max(k.scn) as scn, " +
			"				max(k.commit_scn) as commitScn, " +
			"				max(k.msg_timestamp) as msgTimestamp " +
			"from tbl_received_message k " +
			"where k.topic = :topic", nativeQuery = true)
	LastMessageInfoDto findLastMessageInfo(@Param("topic") String topic);

	@Query(value = "select k.topic as topic, " +
			"				max(k.received_date + k.received_time) as receivedDateTime, " +
			"				max(k.scn) as scn, " +
			"				max(k.commit_scn) as commitScn, " +
			"				max(k.msg_timestamp) as msgTimestamp " +
			"from tbl_received_message k " +
			"where k.topic in :topics group by topic", nativeQuery = true)
	List<LastMessageInfoDto> findLastMessageInfos(@Param("topics") List<String> topics);

}
