package com.lguplus.fleta.ports.service;

import com.lguplus.fleta.domain.dto.analysis.DataAnalysisDto;
import com.lguplus.fleta.domain.dto.analysis.SchemaConnector;
import com.lguplus.fleta.domain.model.MessageAnalysisEntity;
import com.lguplus.fleta.domain.dto.analysis.ChartType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageAnalysisService {

	Long getRecentKafkaEndOffSet(String topic);

	DataAnalysisDto getHourlyAnalysis(List<String> dbNameList, List<String> schmNameList, String division, String topic, LocalDate dateTime);

	DataAnalysisDto getDailyAnalysis(List<String> dbNameList, List<String> schmNameList, String division, String topic, LocalDate dateTime);

	DataAnalysisDto getMinutelyAnalysis(List<String> dbNameList, List<String> schmNameList, String division, String topic, LocalDate dateTime, int fromHour, int toHour, int type);

	MessageAnalysisEntity getMostRecentReceivedMessage(String topic, LocalDateTime dateTime);

	DataAnalysisDto viewHourlyByTopics(List<String> topics, int year, int month, int date);

	int deletePerTopicBeforeTime(LocalDate date);

	int deletePerMinuteBeforeTime(LocalDate date);

	DataAnalysisDto getMsgLatencyHourly(List<String> dbNameList, List<String> schmNameList, String division, String topic, LocalDate dateTime);

	DataAnalysisDto getMsgLatencyByMinutely(List<String> dbNameList, List<String> schmNameList, String division, String topic, LocalDate dateTime, int fromHour, int toHour, int type);

	DataAnalysisDto getMsgLatencyDaily(List<String> dbNameList, List<String> schmNameList, String division, String topic, LocalDate dateTime);
}
