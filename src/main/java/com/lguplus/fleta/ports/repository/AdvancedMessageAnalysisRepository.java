package com.lguplus.fleta.ports.repository;

import com.lguplus.fleta.domain.dto.analysis.MsgLatencyPerMinuteDto;
import com.lguplus.fleta.domain.dto.analysis.ProcessedMessagePerMinuteDto;
import com.lguplus.fleta.domain.model.AdvancedMessageAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AdvancedMessageAnalysisRepository extends JpaRepository<AdvancedMessageAnalysisEntity, String> {

	@Query(value = "SELECT k.at_hour as atHour, k.at_minute as atMinute , SUM(k.received_message) as receivedMessage " +
			"FROM tbl_analysis_message_per_minute k " +
			"WHERE k.topic = :topic AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.at_hour >= :fromHour and k.at_hour < :toHour " +
			"GROUP BY k.at_minute, k.at_hour ORDER BY k.at_hour, k.at_minute ASC", nativeQuery = true)
	List<ProcessedMessagePerMinuteDto> findAdvancedMessageAnalysisByTopicAndDateTime(String topic, int year, int month, int date, int fromHour, int toHour);

	@Query(value = "SELECT k.at_hour as atHour, k.at_minute as atMinute , SUM(k.received_message) as receivedMessage " +
			"FROM tbl_analysis_message_per_minute k " +
			"WHERE lower(k.db_name) = lower(:dbName) AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.at_hour >= :fromHour and k.at_hour < :toHour " +
			"GROUP BY k.at_minute, k.at_hour ORDER BY k.at_hour, k.at_minute ASC", nativeQuery = true)
	List<ProcessedMessagePerMinuteDto> findAdvancedMessageAnalysisByDatabaseAndDateTime(String dbName, int year, int month, int date, int fromHour, int toHour);

	@Query(value = "SELECT k.at_hour as atHour, k.at_minute as atMinute , SUM(k.received_message) as receivedMessage " +
			"FROM tbl_analysis_message_per_minute k " +
			"WHERE lower(k.schm_name) = lower(:schmName) AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.at_hour >= :fromHour and k.at_hour < :toHour " +
			"GROUP BY k.at_minute, k.at_hour ORDER BY k.at_hour, k.at_minute ASC", nativeQuery = true)
	List<ProcessedMessagePerMinuteDto> findAdvancedMessageAnalysisBySchemaAndDateTime(String schmName, int year, int month, int date, int fromHour, int toHour);

	@Query(value = "SELECT k.at_hour as atHour, k.at_minute as atMinute, SUM(k.received_message) as receivedMessage " +
			"FROM tbl_analysis_message_per_minute k " +
			"WHERE lower(k.db_name) = lower(:dbName) AND lower(k.schm_name) = lower(:schmName) AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.at_hour >= :fromHour and k.at_hour < :toHour " +
			"GROUP BY k.at_minute, k.at_hour ORDER BY k.at_hour, k.at_minute ASC", nativeQuery = true)
	List<ProcessedMessagePerMinuteDto> findAdvancedMessageAnalysisByDatabaseAndSchemaAndDateTime(String dbName, String schmName, int year, int month, int date, int fromHour, int toHour);

	@Query(value = "SELECT k.at_hour as atHour, k.at_minute as atMinute , SUM(k.received_message) as receivedMessage " +
			"FROM tbl_analysis_message_per_minute k  " +
			"WHERE k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.at_hour >= :fromHour and k.at_hour < :toHour " +
			"GROUP BY k.at_minute, k.at_hour ORDER BY k.at_hour, k.at_minute ASC", nativeQuery = true)
	List<ProcessedMessagePerMinuteDto> getTotalMessageAnalysisMinutely(int year, int month, int date, int fromHour, int toHour);

	@Query(value = "SELECT k.at_hour as atHour, k.at_minute as atMinute , SUM(k.received_message) as receivedMessage " +
			"FROM tbl_analysis_message_per_minute k  " +
			"WHERE k.topic in :topics AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.at_hour >= :fromHour and k.at_hour < :toHour " +
			"GROUP BY k.at_minute, k.at_hour ORDER BY k.at_hour, k.at_minute ASC", nativeQuery = true)
	List<ProcessedMessagePerMinuteDto> getTotalMessageAnalysisMinutelyByDivision(List<String> topics, int year, int month, int date, int fromHour, int toHour);

	@Query(value = "SELECT k.at_hour as atHour, k.at_minute as atMinute , SUM(k.received_message) as receivedMessage, SUM(k.total_latency) as totalLatency " +
			"FROM tbl_analysis_message_per_minute k " +
			"WHERE k.topic = :topic AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.at_hour >= :fromHour and k.at_hour < :toHour " +
			"GROUP BY k.at_minute, k.at_hour ORDER BY k.at_hour, k.at_minute ASC", nativeQuery = true)
	List<MsgLatencyPerMinuteDto> getMessageLatencyByTopicAndDateTime(String topic, int year, int month, int date, int fromHour, int toHour);

	@Query(value = "SELECT k.at_hour as atHour, k.at_minute as atMinute , SUM(k.received_message) as receivedMessage, SUM(k.total_latency) as totalLatency " +
			"FROM tbl_analysis_message_per_minute k  " +
			"WHERE k.topic in :topics AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.at_hour >= :fromHour and k.at_hour < :toHour " +
			"GROUP BY k.at_minute, k.at_hour ORDER BY k.at_hour, k.at_minute ASC", nativeQuery = true)
	List<MsgLatencyPerMinuteDto> getTotalMessageLatencyAnalysisMinutelyByDivision(List<String> topics, int year, int month, int date, int fromHour, int toHour);

	@Query(value = "SELECT k.at_hour as atHour, k.at_minute as atMinute , SUM(k.received_message) as receivedMessage, SUM(k.total_latency) as totalLatency " +
			"FROM tbl_analysis_message_per_minute k  " +
			"WHERE k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.at_hour >= :fromHour and k.at_hour < :toHour " +
			"GROUP BY k.at_minute, k.at_hour ORDER BY k.at_hour, k.at_minute ASC", nativeQuery = true)
	List<MsgLatencyPerMinuteDto> getTotalMessageLatencyMinutely(int year, int month, int date, int fromHour, int toHour);

	@Query(value = "SELECT k.at_hour as atHour, k.at_minute as atMinute , SUM(k.received_message) as receivedMessage, SUM(k.total_latency) as totalLatency  " +
			"FROM tbl_analysis_message_per_minute k " +
			"WHERE lower(k.db_name) = lower(:dbName) AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.at_hour >= :fromHour and k.at_hour < :toHour " +
			"GROUP BY k.at_minute, k.at_hour ORDER BY k.at_hour, k.at_minute ASC", nativeQuery = true)
	List<MsgLatencyPerMinuteDto> getMessageLatencyByDatabaseAndDateTime(String dbName, int year, int month, int date, int fromHour, int toHour);

	@Query(value = "SELECT k.at_hour as atHour, k.at_minute as atMinute , SUM(k.received_message) as receivedMessage, SUM(k.total_latency) as totalLatency  " +
			"FROM tbl_analysis_message_per_minute k " +
			"WHERE lower(k.schm_name) = lower(:schmName) AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.at_hour >= :fromHour and k.at_hour < :toHour " +
			"GROUP BY k.at_minute, k.at_hour ORDER BY k.at_hour, k.at_minute ASC", nativeQuery = true)
	List<MsgLatencyPerMinuteDto> getMessageLatencyBySchemaAndDateTime(String schmName, int year, int month, int date, int fromHour, int toHour);

	@Query(value = "SELECT k.at_hour as atHour, k.at_minute as atMinute, SUM(k.received_message) as receivedMessage, SUM(k.total_latency) as totalLatency  " +
			"FROM tbl_analysis_message_per_minute k " +
			"WHERE lower(k.db_name) = lower(:dbName) AND lower(k.schm_name) = lower(:schmName) AND k.at_year = :year AND k.at_month = :month AND k.at_date = :date AND k.at_hour >= :fromHour and k.at_hour < :toHour " +
			"GROUP BY k.at_minute, k.at_hour ORDER BY k.at_hour, k.at_minute ASC", nativeQuery = true)
	List<MsgLatencyPerMinuteDto> getMessageLatencyByDatabaseAndSchemaAndDateTime(String dbName, String schmName, int year, int month, int date, int fromHour, int toHour);

	@Transactional
	@Modifying
	@Query(value = "delete from tbl_analysis_message_per_minute where make_date(at_year, at_month, at_date) < :date", nativeQuery = true)
	int deleteBeforeTime(LocalDate date);
}
