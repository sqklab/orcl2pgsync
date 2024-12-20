package com.lguplus.fleta.ports.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface KafkaHealthCheckService {

	SseEmitter startHeathCheck();

	List<String> getTopics();

	void stopHeathCheck();
}
