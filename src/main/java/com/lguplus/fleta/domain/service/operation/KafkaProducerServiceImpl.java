package com.lguplus.fleta.domain.service.operation;

import com.lguplus.fleta.adapters.messagebroker.KafkaProperties;
import com.lguplus.fleta.domain.dto.KafkaProducerDto;
import com.lguplus.fleta.ports.service.KafkaHealthCheckService;
import com.lguplus.fleta.ports.service.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class KafkaProducerServiceImpl implements KafkaProducerService {

	private final KafkaHealthCheckService kafkaHealthIndicator;
	private final KafkaProperties kafkaProperties;

	public KafkaProducerServiceImpl(KafkaHealthCheckService kafkaHealthIndicator, KafkaProperties kafkaProperties) {
		this.kafkaHealthIndicator = kafkaHealthIndicator;
		this.kafkaProperties = kafkaProperties;
	}

	@Override
	public void send(KafkaProducerDto param) throws Exception {
		String topic = "";
		if ("POSTGRES".equals(param.getTopic())) {
			topic = "connect-offsets-postgres";
		} else if ("ORACLE".equals(param.getTopic())) {
			topic = "connect-offsets-oracle";
		} else {
			throw new Exception("Invalid topic");
		}
		ListenableFuture<SendResult<Object, Object>> future = this.buildKafkaTemplate().send(topic, param.getKey(), param.getMessage());
		future.get();
	}

	@Override
	public List<String> getTopics(String regex) {
		List<String> topics = this.kafkaHealthIndicator.getTopics();
		if (StringUtils.isEmpty(regex)) {
			return topics;
		}
		return topics.stream().filter(x -> x.contains(regex)).collect(Collectors.toList());
	}

	private KafkaTemplate<Object, Object> buildKafkaTemplate() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		ProducerFactory<Object, Object> producerFactory = new DefaultKafkaProducerFactory<>(configProps);
		return new KafkaTemplate<>(producerFactory);
	}
}
