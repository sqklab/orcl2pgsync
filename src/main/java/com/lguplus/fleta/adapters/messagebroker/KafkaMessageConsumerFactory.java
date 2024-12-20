package com.lguplus.fleta.adapters.messagebroker;

import com.lguplus.fleta.domain.service.constant.Constants;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Component
public class KafkaMessageConsumerFactory {
	public <K, V> KafkaMessageListenerContainer<K, V> create(final MessageConsumer<V> consumer, final Class<V> messageType) {
		final MessageConsumer.ConsumerProperties consumerProperties = consumer.getConsumerProperties();
		ContainerProperties containerProps = new ContainerProperties(consumerProperties.getAllTopics());
//		containerProps.setMessageListener((MessageListener<String, V>) data -> consumer.consume(data.value()));
		containerProps.setMessageListener((BatchMessageListener<String, V>) records -> consumer.consume(records.stream().map(ConsumerRecord::value).collect(Collectors.toList())));

		Map<String, Object> consumerProps = new HashMap<>();
		consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerProperties.getServerUrl());
		// Consumer groups are very useful for scaling your consumers according to demand. Consumers within a group do
		// not read data from the same partition, but can receive data exclusively from zero or more partitions. Each
		// partition is assigned to exactly one member of a consumer group.
		consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerProperties.getGroupId());

		// Improving throughput by increasing the minimum amount of data fetched in a request
		// Use the fetch.max.wait.ms and fetch.min.bytes configuration properties to set thresholds that control the
		// number of requests from your consumer.
		consumerProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
		consumerProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 16384);

		// Increasing the minimum amount of data fetched in a request can help with increasing throughput. But if you
		// want to do something to improve latency, you can extend your thresholds by increasing the maximum amount of
		// data that can be fetched by the consumer from the broker.
		consumerProps.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, ConsumerConfig.DEFAULT_FETCH_MAX_BYTES);
		consumerProps.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, ConsumerConfig.DEFAULT_FETCH_MAX_BYTES);

		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		// If consumers fail within a consumer group, a rebalance is triggered and partition ownership is reassigned to
		// the members of the group. You want to get the timings of your checks just right so that the consumer group
		// can recover quickly, but unnecessary rebalances are not triggered. And you use two properties to do it:
		// session.timeout.ms and heartbeat.interval.ms.
		consumerProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
		consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);

		// Minimizing the impact of rebalancing your consumer group
		consumerProps.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, consumerProperties.getUniqueGroupId());
		consumerProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
		consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, consumerProperties.getMaxPollRecords());

		consumerProps.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 90000);

		// Custom value 로써..
		DefaultKafkaConsumerFactory<K, V> cf = new DefaultKafkaConsumerFactory<>(
				consumerProps, null, new ErrorHandlingDeserializer<>(new JsonDeserializer<>(messageType))
		);
		return new KafkaMessageListenerContainer<>(cf, containerProps);
	}

	public <K, V> Consumer<K, V> create(final Properties props, final String... topicNames) {
		Assert.notNull(props, "A KafkaProperties must be provided");

		props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.KAFKA_ERROR_GROUP_ID);
		// Set this property, if auto commit should happen.
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		// Auto commit interval, kafka would commit offset at this interval.
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

		KafkaConsumer<K, V> consumer = new KafkaConsumer<>(props);
		// Subscribe to all partition in that topic. 'assign' could be used here
		// instead of 'subscribe' to subscribe to specific partition.
		consumer.subscribe(Arrays.asList(topicNames));
		return consumer;
	}
}
