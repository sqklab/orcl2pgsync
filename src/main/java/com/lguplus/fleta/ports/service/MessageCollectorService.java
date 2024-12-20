package com.lguplus.fleta.ports.service;

import com.lguplus.fleta.domain.dto.LastMessageInfoDto;
import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import com.lguplus.fleta.domain.dto.analysis.MessageAnalysisPerMinuteDto;

import java.util.List;
import java.util.Map;

public interface MessageCollectorService {
	List<MessageAnalysisPerMinuteDto> getNumberOfReceivedMessagePerMinuteByDateHour(String kafkaTopic);

	void saveMessages(String kafkaTopic, List<SyncRequestMessage> messages, Long completedTime);

	void autoSaveReceivedKafkaMessage();

	LastMessageInfoDto getLastMessageInfoDto(String topic);

	Map<String, LastMessageInfoDto> getMapLastMessageInfoByListTopic(List<String> topics);

}
