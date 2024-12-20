package com.lguplus.fleta.ports.service;

import com.lguplus.fleta.domain.dto.KafkaProducerDto;

import java.util.List;

public interface KafkaProducerService {
	void send(KafkaProducerDto param) throws Exception;

	List<String> getTopics(String regex);
}
