package com.lguplus.fleta.adapters.messagebroker;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public interface MessageConsumer<V> {

	ConsumerProperties getConsumerProperties();

	void consume(List<V> values);

	@Getter
	@Builder
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	final class ConsumerProperties {

		private final String serverUrl;
		private final String topic;
		private final String groupId;

		private final Integer maxPollRecords;

		/**
		 * Support sub-kafka topics (listen from multi-topics)
		 */
		private final List<String> subTopics = new ArrayList<>();

		/**
		 * Get all topics
		 *
		 * @return
		 */
		public String[] getAllTopics() {
			subTopics.add(topic);
			return subTopics.toArray(String[]::new);
		}

		public String getUniqueGroupId() {
			return String.format("%s-%s", groupId, UUID.randomUUID());
		}
	}
}
