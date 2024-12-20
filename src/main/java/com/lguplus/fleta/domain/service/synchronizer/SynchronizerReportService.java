package com.lguplus.fleta.domain.service.synchronizer;

import com.lguplus.fleta.adapters.messagebroker.KafkaMessageProducerFactory;
import com.lguplus.fleta.adapters.messagebroker.KafkaProperties;
import com.lguplus.fleta.domain.dto.ErrorMessage;
import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class SynchronizerReportService {

	private final KafkaTemplate<String, SyncRequestMessage> notifyTemplate;

	public SynchronizerReportService(KafkaMessageProducerFactory producerFactory, KafkaProperties kafkaProperties) {
		Assert.notNull(producerFactory, "A KafkaMessageProducerFactory must be provided");

		this.notifyTemplate = producerFactory.create(kafkaProperties.getNotifyBootstrapServers());
	}

	public void report(String kafkaTopic, ErrorMessage errorMessage) {
		notifyTemplate.send(kafkaTopic, errorMessage);
	}
}
