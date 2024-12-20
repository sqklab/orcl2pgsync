package com.lguplus.fleta.adapters.messagebroker;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class KafkaMessageProducerFactory {

	public <K, V> MessageProducer<K, V> create(final String bootstrapServers, final String topic) {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		ProducerFactory<K, V> producerFactory = new DefaultKafkaProducerFactory<>(configProps);

		KafkaTemplate<K, V> kafkaTemplate = new KafkaTemplate<>(producerFactory);
		return (key, value) -> {
			ListenableFuture<SendResult<K, V>> future = kafkaTemplate.send(topic, key, value);
			future.completable().thenAccept(result -> {
				log.info("!!! Sent message=[ {} ] with offset=[ {} ]", value.toString(), result.getRecordMetadata().offset());
			});
			return "DONE";
		};
	}

	public <K, V> KafkaTemplate<K, V> create(final String bootstrapServers) {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 600_000);
		// Batch up to 64K buffer sizes.
		configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16_384 * 4);
		// Use Snappy compression for batch compression.
		configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
		// The producer groups together any records that arrive in between request transmissions into a single batched request.
		configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);

		configProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 30000);

		ProducerFactory<K, V> producerFactory = new DefaultKafkaProducerFactory<>(configProps);
		return new KafkaTemplate<>(producerFactory);
	}
}
