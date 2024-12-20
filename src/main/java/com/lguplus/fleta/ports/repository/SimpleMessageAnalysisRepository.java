package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.dto.analysis.*;
import com.lguplus.fleta.domain.model.MessageAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SimpleMessageAnalysisRepository extends JpaRepository<MessageAnalysisEntity, String> {

	List<MessageAnalysisEntity> findMessageAnalysisEntitiesByTopicAndAtYearAndAtMonthAndAtDateAndPerFiveIsFalse(String topic, int year, int month, int date);

	List<MessageAnalysisEntity> findMessageAnalysisEntitiesByTopicAndAtYearAndAtMonthAndPerFiveIsFalse(String topic, int year, int month);

	@Query(value = "SELECT k.at_hour as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.kafka_message_hourly) as sumTotalMessage FROM tbl_analysis_message_each_topic k " +
			"WHERE k.db_name = :dbName AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.per_five = false GROUP BY k.at_hour ORDER BY k.at_hour ASC", nativeQuery = true)
	List<TotalMessageAnalysisDto> findMessageAnalysisByDatabaseHourly(String dbName, int year, int month, int date);

	@Query(value = "SELECT k.at_hour as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.kafka_message_hourly) as sumTotalMessage FROM tbl_analysis_message_each_topic k " +
			"WHERE lower(k.schm_name) = lower(:schmName) AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.per_five = false GROUP BY k.at_hour ORDER BY k.at_hour ASC", nativeQuery = true)
	List<TotalMessageAnalysisDto> findMessageAnalysisBySchemaHourly(String schmName, int year, int month, int date);

	@Query(value = "SELECT k.at_hour as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.kafka_message_hourly) as sumTotalMessage FROM tbl_analysis_message_each_topic k " +
			"WHERE lower(k.db_name) = lower(:dbName) AND lower(k.schm_name) = lower(:schmName) AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.per_five = false GROUP BY k.at_hour ORDER BY k.at_hour ASC", nativeQuery = true)
	List<TotalMessageAnalysisDto> findMessageAnalysisByDatabaseAndSchemaHourly(String dbName, String schmName, int year, int month, int date);

	@Query(value = "SELECT k.at_hour as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.kafka_message_hourly) as sumTotalMessage FROM tbl_analysis_message_each_topic k  " +
			"WHERE k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.per_five = false GROUP BY k.at_hour ORDER BY k.at_hour ASC", nativeQuery = true)
	List<TotalMessageAnalysisDto> getTotalMessageAnalysisHourly(int year, int month, int date);

	@Query(value = "SELECT k.at_hour as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.kafka_message_hourly) as sumTotalMessage FROM tbl_analysis_message_each_topic k  " +
			"WHERE k.topic in :topics and k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.per_five = false GROUP BY k.at_hour ORDER BY k.at_hour ASC", nativeQuery = true)
	List<TotalMessageAnalysisDto> getTotalMessageAnalysisHourlyByDivision(List<String> topics, int year, int month, int date);

	@Query(value = "SELECT k.at_date as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.kafka_message_hourly) as sumTotalMessage FROM tbl_analysis_message_each_topic k " +
			"WHERE lower(k.db_name) = lower(:dbName) AND k.at_year = :year AND k.at_month = :month AND k.per_five = false GROUP BY k.at_date ORDER BY k.at_date ASC", nativeQuery = true)
	List<TotalMessageAnalysisDto> findMessageAnalysisByDatabaseDaily(String dbName, int year, int month);

	@Query(value = "SELECT k.at_date as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.kafka_message_hourly) as sumTotalMessage FROM tbl_analysis_message_each_topic k " +
			"WHERE lower(k.schm_name) = lower(:schmName) AND k.at_year = :year AND k.at_month = :month AND k.per_five = false GROUP BY k.at_date ORDER BY k.at_date ASC", nativeQuery = true)
	List<TotalMessageAnalysisDto> findMessageAnalysisBySchemaDaily(String schmName, int year, int month);

	@Query(value = "SELECT k.at_date as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.kafka_message_hourly) as sumTotalMessage FROM tbl_analysis_message_each_topic k " +
			"WHERE lower(k.db_name) = lower(:dbName) AND lower(k.schm_name) = lower(:schmName) AND k.at_year = :year AND k.at_month = :month AND k.per_five = false GROUP BY k.at_date ORDER BY k.at_date ASC", nativeQuery = true)
	List<TotalMessageAnalysisDto> findMessageAnalysisByDatabaseAndSchemaDaily(String dbName, String schmName, int year, int month);

	@Query(value = "SELECT k.at_date as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.kafka_message_hourly) as sumTotalMessage FROM tbl_analysis_message_each_topic k  " +
			"WHERE k.topic in :topics AND k.at_year = :year AND k.at_month = :month AND k.per_five = false GROUP BY k.at_date  ORDER BY k.at_date ASC", nativeQuery = true)
	List<TotalMessageAnalysisDto> getTotalMessageAnalysisDailyByDivision(List<String> topics, int year, int month);

	@Query(value = "SELECT k.at_date as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.kafka_message_hourly) as sumTotalMessage FROM tbl_analysis_message_each_topic k  " +
			"WHERE k.at_year = :year AND k.at_month = :month AND k.per_five = false GROUP BY k.at_date ORDER BY k.at_date ASC", nativeQuery = true)
	List<TotalMessageAnalysisDto> getTotalMessageAnalysisDaily(int year, int month);

	@Query(value = "select mae from MessageAnalysisEntity mae where mae.topic = :topic and mae.atYear = :year and mae.atMonth = :month and mae.atDate = :date and mae.atHour = :hour and mae.perFive = false")
	MessageAnalysisEntity findMostRecentReceivedMessage(String topic, int year, int month, int date, int hour);

	@Query(value = "select k.at_hour as atTime, k.received_message_hourly as receivedMessageHourly, k.kafka_message_hourly as kafkaMessageHourly from tbl_analysis_message_each_topic k " +
			"where topic = :topic AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.per_five = false order by k.at_hour ASC", nativeQuery = true)
	List<TopicMessageHourly> getMessageDataByTopicAndDate(String topic, int year, int month, int date);

	@Query(value = "select mae.db_name as dbName, mae.schm_name as schemaName from tbl_analysis_message_each_topic mae where mae.received_message_hourly = 0 AND mae.at_year = :year " +
			"AND mae.at_month = :month AND mae.at_date = :date AND mae.at_hour = :hour AND mae.per_five = false group by mae.schm_name,mae.db_name", nativeQuery = true)
	List<SchemaConnector> getDbSchemaHasNoMessageInHour(int year, int month, int date, int hour);

	@Query(value = "select k.end_offset_per_five from tbl_analysis_message_each_topic k where k.per_five = true and k.topic = :topic " +
			"and make_timestamp(k.at_year,k.at_month,k.at_date,k.at_hour,k.at_minute,0) " +
			"= (select max(make_timestamp(k.at_year,k.at_month,k.at_date,k.at_hour,k.at_minute,0)) " +
			"from tbl_analysis_message_each_topic k where k.topic = :topic and k.per_five = true)", nativeQuery = true)
	Long findMostRecentOffset(String topic);

	@Query(value = "SELECT k.at_hour as atHour, k.at_minute as atMinute , SUM(k.kafka_message_per_five) as receivedMessage " +
			"FROM tbl_analysis_message_each_topic k  " +
			"WHERE k.topic in :topics AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.at_hour >= :fromHour and k.at_hour < :toHour and k.per_five = true " +
			"GROUP BY k.at_minute, k.at_hour ORDER BY k.at_hour, k.at_minute ASC", nativeQuery = true)
	List<ProcessedMessagePerMinuteDto> getKafkaEndOffsetPerFiveMinuteByDivision(List<String> topics, int year, int month, int date, int fromHour, int toHour);

	@Query(value = "SELECT k.at_hour as atHour, k.at_minute as atMinute , SUM(k.kafka_message_per_five) as receivedMessage " +
			"FROM tbl_analysis_message_each_topic k " +
			"WHERE k.topic = :topic AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.at_hour >= :fromHour and k.at_hour < :toHour and k.per_five = true " +
			"GROUP BY k.at_minute, k.at_hour ORDER BY k.at_hour, k.at_minute ASC", nativeQuery = true)
	List<ProcessedMessagePerMinuteDto> findKafkaEndOffsetPerFiveByTopicAndDateTime(String topic, int year, int month, int date, int fromHour, int toHour);

	@Query(value = "SELECT k.at_hour as atHour, k.at_minute as atMinute , SUM(k.kafka_message_per_five) as receivedMessage " +
			"FROM tbl_analysis_message_each_topic k  " +
			"WHERE k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.at_hour >= :fromHour and k.at_hour < :toHour and k.per_five = true " +
			"GROUP BY k.at_minute, k.at_hour ORDER BY k.at_hour, k.at_minute ASC", nativeQuery = true)
	List<ProcessedMessagePerMinuteDto> getTotalKafkaEndOffsetPerFive(int year, int month, int date, int fromHour, int toHour);

	@Query(value = "SELECT k.at_hour as atHour, k.at_minute as atMinute , SUM(k.kafka_message_per_five) as receivedMessage " +
			"FROM tbl_analysis_message_each_topic k " +
			"WHERE lower(k.db_name) = lower(:dbName) AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.at_hour >= :fromHour and k.at_hour < :toHour and k.per_five = true " +
			"GROUP BY k.at_minute, k.at_hour ORDER BY k.at_hour, k.at_minute ASC", nativeQuery = true)
	List<ProcessedMessagePerMinuteDto> findKafkaEndOffsetPerFiveByDatabaseAndDateTime(String dbName, int year, int month, int date, int fromHour, int toHour);

	@Query(value = "SELECT k.at_hour as atHour, k.at_minute as atMinute , SUM(k.kafka_message_per_five) as receivedMessage " +
			"FROM tbl_analysis_message_each_topic k " +
			"WHERE lower(k.schm_name) = lower(:schmName) AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.at_hour >= :fromHour and k.at_hour < :toHour and k.per_five = true " +
			"GROUP BY k.at_minute, k.at_hour ORDER BY k.at_hour, k.at_minute ASC", nativeQuery = true)
	List<ProcessedMessagePerMinuteDto> findKafkaEndOffsetPerFiveBySchemaAndDateTime(String schmName, int year, int month, int date, int fromHour, int toHour);

	@Query(value = "SELECT k.at_hour as atHour, k.at_minute as atMinute, SUM(k.kafka_message_per_five) as receivedMessage " +
			"FROM tbl_analysis_message_each_topic k " +
			"WHERE lower(k.db_name) = lower(:dbName) AND lower(k.schm_name) = lower(:schmName) AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.at_hour >= :fromHour and k.at_hour < :toHour and k.per_five = true " +
			"GROUP BY k.at_minute, k.at_hour ORDER BY k.at_hour, k.at_minute ASC", nativeQuery = true)
	List<ProcessedMessagePerMinuteDto> findKafkaEndOffsetPerFiveByDatabaseAndSchemaAndDateTime(String dbName, String schmName, int year, int month, int date, int fromHour, int toHour);

	@Transactional
	@Modifying
	@Query(value = "delete from tbl_analysis_message_each_topic where make_date(at_year, at_month, at_date) < :date", nativeQuery = true)
	int deleteBeforeTime(LocalDate date);


	@Query(value = "SELECT k.at_hour as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.total_latency) as totalLatency FROM tbl_analysis_message_each_topic k  " +
			"WHERE k.topic in :topics and k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.per_five = false GROUP BY k.at_hour ORDER BY k.at_hour ASC", nativeQuery = true)
	List<MsgLatencyDto> getMsgLatencyHourlyByDivision(List<String> topics, int year, int month, int date);

	@Query(value = "SELECT k.at_hour as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.total_latency) as totalLatency FROM tbl_analysis_message_each_topic k  " +
			"WHERE k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.per_five = false GROUP BY k.at_hour ORDER BY k.at_hour ASC", nativeQuery = true)
	List<MsgLatencyDto> getMsgLatencyHourly(int year, int month, int date);

	@Query(value = "SELECT k.at_hour as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.total_latency) as totalLatency FROM tbl_analysis_message_each_topic k " +
			"WHERE lower(k.db_name) = lower(:dbName) AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.per_five = false GROUP BY k.at_hour ORDER BY k.at_hour ASC", nativeQuery = true)
	List<MsgLatencyDto> findMsgLatencyByDatabaseHourly(String dbName, int year, int month, int date);

	@Query(value = "SELECT k.at_hour as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.total_latency) as totalLatency FROM tbl_analysis_message_each_topic k " +
			"WHERE lower(k.schm_name) = lower(:schmName) AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.per_five = false GROUP BY k.at_hour ORDER BY k.at_hour ASC", nativeQuery = true)
	List<MsgLatencyDto> findMsgLatencyBySchemaHourly(String schmName, int year, int month, int date);

	@Query(value = "SELECT k.at_hour as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.total_latency) as totalLatency FROM tbl_analysis_message_each_topic k " +
			"WHERE lower(k.db_name) = lower(:dbName) AND lower(k.schm_name) = lower(:schmName) AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.per_five = false GROUP BY k.at_hour ORDER BY k.at_hour ASC", nativeQuery = true)
	List<MsgLatencyDto> findMsgLatencyByDatabaseAndSchemaHourly(String dbName, String schmName, int year, int month, int date);


	@Query(value = "SELECT k.at_date as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.total_latency) as totalLatency FROM tbl_analysis_message_each_topic k  " +
			"WHERE k.topic in :topics AND k.at_year = :year AND k.at_month = :month AND k.per_five = false GROUP BY k.at_date  ORDER BY k.at_date ASC", nativeQuery = true)
	List<MsgLatencyDto> getMsgLatencyDailyByDivision(List<String> topics, int year, int month);

	@Query(value = "SELECT k.at_date as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.total_latency) as totalLatency FROM tbl_analysis_message_each_topic k  " +
			"WHERE k.at_year = :year AND k.at_month = :month AND k.per_five = false GROUP BY k.at_date ORDER BY k.at_date ASC", nativeQuery = true)
	List<MsgLatencyDto> getMsgLatencyDaily(int year, int month);

	@Query(value = "SELECT k.at_date as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.total_latency) as totalLatency FROM tbl_analysis_message_each_topic k " +
			"WHERE lower(k.db_name) = lower(:dbName) AND k.at_year = :year AND k.at_month = :month AND k.per_five = false GROUP BY k.at_date ORDER BY k.at_date ASC", nativeQuery = true)
	List<MsgLatencyDto> getMsgLatencyByDatabaseDaily(String dbName, int year, int month);

	@Query(value = "SELECT k.at_date as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.total_latency) as totalLatency FROM tbl_analysis_message_each_topic k " +
			"WHERE lower(k.schm_name) = lower(:schmName) AND k.at_year = :year AND k.at_month = :month AND k.per_five = false GROUP BY k.at_date ORDER BY k.at_date ASC", nativeQuery = true)
	List<MsgLatencyDto> getMsgLatencyBySchemaDaily(String schmName, int year, int month);

	@Query(value = "SELECT k.at_date as atTime, SUM(k.received_message_hourly) as sumReceivedMessage, SUM(k.total_latency) as totalLatency FROM tbl_analysis_message_each_topic k " +
			"WHERE lower(k.db_name) = lower(:dbName) AND lower(k.schm_name) = lower(:schmName) AND k.at_year = :year AND k.at_month = :month AND k.per_five = false GROUP BY k.at_date ORDER BY k.at_date ASC", nativeQuery = true)
	List<MsgLatencyDto> getMsgLatencyByDatabaseAndSchemaDaily(String dbName, String schmName, int year, int month);

}
