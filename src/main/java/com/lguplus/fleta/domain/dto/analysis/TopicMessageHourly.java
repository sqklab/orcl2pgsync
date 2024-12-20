package com.lguplus.fleta.domain.dto.analysis;

import org.springframework.beans.factory.annotation.Value;

public interface TopicMessageHourly {

	@Value("#{target.atTime}")
	Integer getAtTime();

	@Value("#{target.receivedMessageHourly}")
	Long getReceivedMessageHourly();

	@Value("#{target.kafkaMessageHourly}")
	Long getKafkaMessageHourly();
}
