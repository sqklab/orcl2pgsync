package com.lguplus.fleta.adapters.messagebroker;

import lombok.Getter;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface KafkaMessagesBehindApi {

	@GET("/api/clusters/{clusterId}/consumer-groups/{groupId}")
	Call<KafkaConsumerGroupMetaData> getConsumerGroupMetaData(@Path("clusterId") String clusterId, @Path("groupId") String groupId);

	@GET("/api/clusters/{clusterId}/consumer-groups")
	Call<List<KafkaConsumerGroupMetaData>> getConsumerGroups(@Path("clusterId") String clusterId);

	@Getter
	@Setter
	class Partition implements Comparable<Partition>, Serializable {
		private String topic;
		private long partition;
		private int currentOffset;
		private int endOffset;
		private long messagesBehind;
		private String consumerId;
		private String host;
		private LocalDate receivedDate;
		private LocalTime receivedTime;

		@Override
		public int compareTo(Partition o) {
			return Long.compare(getMessagesBehind(), o.getMessagesBehind());
		}
	}

	@Getter
	@Setter
	class Coordinator implements Serializable {
		private int id;
		private int port;
		private String host;
	}

	@Getter
	class KafkaConsumerGroupMetaData implements Comparable<KafkaConsumerGroupMetaData>, Serializable {
		private String groupId;
		private int members;
		private int topics;
		private boolean simple;
		private String partitionAssignor;
		private String state;
		private Coordinator coordinator;
		private long messagesBehind;
		private List<Partition> partitions;

		@Override
		public int compareTo(KafkaConsumerGroupMetaData o) {
			return Long.compare(getMessagesBehind(), o.getMessagesBehind());
		}
	}
}
