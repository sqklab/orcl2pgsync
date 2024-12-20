package com.lguplus.fleta.domain.service.analysis;

import ch.qos.logback.classic.Logger;
import com.lguplus.fleta.adapters.messagebroker.KafkaConstants;
import com.lguplus.fleta.adapters.messagebroker.KafkaProperties;
import com.lguplus.fleta.domain.dto.LastMessageInfoDto;
import com.lguplus.fleta.domain.dto.analysis.MessageAnalysisPerMinuteDto;
import com.lguplus.fleta.domain.model.MessageAnalysisEntity;
import com.lguplus.fleta.domain.service.constant.Constants;
import com.lguplus.fleta.ports.service.LoggerManager;
import com.lguplus.fleta.ports.service.MessageAnalysisService;
import com.lguplus.fleta.ports.service.MessageCollectorService;
import com.lguplus.fleta.ports.service.SyncRequestService;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.lguplus.fleta.domain.service.constant.Constants.ANALYSIS_LOG;
import static com.lguplus.fleta.domain.service.constant.Constants.REV_TOPIC_PREFIX;

@Component
// TODO: important
// If this bean is prototype-scoped, you will get new instance each time you call it.
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class GetOffsetCollector {

	private final MessageCollectorService messageCollectorService;

	private final MessageAnalysisService messageAnalysisService;

	private final SyncRequestService syncRequestService;

	private final Logger logger;

	private final KafkaProperties kafkaProperties;

	@Autowired
	private DataSource dataSource;

	public GetOffsetCollector(MessageCollectorService messageCollectorService,
							  MessageAnalysisService messageAnalysisService,
							  SyncRequestService syncRequestService,
							  LoggerManager loggerManager,
							  KafkaProperties kafkaProperties) {

		this.messageCollectorService = messageCollectorService;
		this.messageAnalysisService = messageAnalysisService;
		this.syncRequestService = syncRequestService;
		this.kafkaProperties = kafkaProperties;
		this.logger = loggerManager.getLogger(ANALYSIS_LOG);
	}

	private void analyzingMessages(Properties props, int retry, Connection connection, PreparedStatement lastMessageInfoStatement, PreparedStatement analysisPerHourStatement, String clusterId, String groupId, List<String> topicNames) {
		while (retry > 0) {
			final LocalDateTime datetime = LocalDateTime.now(Constants.ZONE_ID);
			try {
				props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
				try (AdminClient client = AdminClient.create(props);
					 KafkaConsumer<Object, Object> consumer = new KafkaConsumer<>(props)) {
					ListConsumerGroupOffsetsResult currentOffsets = client.listConsumerGroupOffsets(groupId);
					Map<TopicPartition, OffsetAndMetadata> consumedOffsets = currentOffsets.partitionsToOffsetAndMetadata().get(3, TimeUnit.SECONDS);
					Map<TopicPartition, Long> endOffset = consumer.endOffsets(consumedOffsets.keySet());
					if (Objects.isNull(endOffset) || endOffset.isEmpty()) return;
					for (TopicPartition partition : endOffset.keySet()) {
						String topicName = partition.topic();
						if (KafkaConstants.MNT_CLUSTER.equals(clusterId)) {
							// If current processing partition is not in the list, skip it.
							if (topicNames.contains(topicName)) {
								// Save all received messages by adding to batch
								saveLastReceivedMessageInfo(lastMessageInfoStatement, topicName, datetime);
								// Compute and analyze all received message for each topic by adding to batch
								computeAndAnalyzeKafkaMessage(connection, analysisPerHourStatement, topicName, endOffset.get(partition), datetime);
							}
						}
					}
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					logger.warn(e.getMessage(), e);
				}
			} catch (Exception e) {
				retry--;
				if (retry == 0) {
					logger.warn("All retries for analysis are failed");
					try {
						if (Objects.nonNull(connection)) connection.rollback();
					} catch (SQLException ex2) {
						logger.warn(ex2.getMessage(), ex2);
					}
					throw e;
				}
				if ((retry > 0) && logger.isDebugEnabled()) {
					logger.debug("{} retry analyzing at {}", (retry), System.currentTimeMillis());
				}

				try {
					// TODO: Delay 5 minutes before retrying to analyze again
					TimeUnit.MINUTES.sleep(5);
				} catch (InterruptedException ex2) {
					Thread.currentThread().interrupt();
				}
				analyzingMessages(props, retry, connection, lastMessageInfoStatement, analysisPerHourStatement, clusterId, groupId, topicNames);
			}
			// break loop if there is no exception
			break;
		}
	}

	@Scheduled(cron = "${app.get-offset-schedule.delay:5 0/5 * * * ?}") // every 5 minute
	public synchronized void getEndOffsetTaskEveryFiveMinute() {
		final LocalDateTime datetime = LocalDateTime.now(Constants.ZONE_ID);
		List<String> consumerGroup = syncRequestService.listConsumerGroups();
		if (consumerGroup.isEmpty()) return;
		List<String> topicNames = new ArrayList<>(syncRequestService.findAllTopicNames());

		try (Connection connection = dataSource.getConnection()) {
			String endOffsetEveryFiveMinuteSql = "INSERT INTO tbl_analysis_message_each_topic (id, db_name, schm_name, topic, at_year, at_month, at_date, at_hour, at_minute, kafka_message_per_five, end_offset_per_five, per_five) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
					"ON CONFLICT DO NOTHING";
			// TODO: Manually get connection from datasource
			connection.setAutoCommit(false); // Disable auto commit

			// Init PreparedStatement for each SQL query
			try (PreparedStatement endOffsetEveryFiveMinuteStatement = connection.prepareStatement(endOffsetEveryFiveMinuteSql)) {
				List.of(KafkaConstants.MNT_CLUSTER).forEach(clusterId -> {
					Properties props = new Properties();
					props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
					props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
					props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
					for (String groupId : consumerGroup) {
						analysisEveryFiveMinuteTask(props, datetime, topicNames, endOffsetEveryFiveMinuteStatement, clusterId, groupId);
					}
					try {
						endOffsetEveryFiveMinuteStatement.executeBatch();
						// 3) Do commit only one time after executed all batch
						connection.commit();
					} catch (SQLException e) {
						logger.warn(e.getMessage(), e);
					}
				});
			} catch (Exception ex) {
				logger.warn(ex.getMessage(), ex);
			}
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
		}
	}

	private void analysisEveryFiveMinuteTask(Properties props, LocalDateTime datetime, List<String> topicNames, PreparedStatement endOffsetEveryFiveMinuteStatement, String clusterId, String groupId) {
		try {
			props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
			try (AdminClient client = AdminClient.create(props);
				 KafkaConsumer<Object, Object> consumer = new KafkaConsumer<>(props)) {
				ListConsumerGroupOffsetsResult currentOffsets = client.listConsumerGroupOffsets(groupId);
				Map<TopicPartition, OffsetAndMetadata> consumedOffsets = currentOffsets.partitionsToOffsetAndMetadata().get(3, TimeUnit.SECONDS);
				Map<TopicPartition, Long> endOffset = consumer.endOffsets(consumedOffsets.keySet());

				if (KafkaConstants.MNT_CLUSTER.equals(clusterId)) {
					consumedOffsets.keySet().stream()
							.filter(p -> topicNames.contains(p.topic()))
							.forEachOrdered(p -> computeKafkaOffsetEveryFiveMinute(endOffsetEveryFiveMinuteStatement, p.topic(), endOffset.get(p), datetime));
				}
			}
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.warn(e.getMessage(), e);
			} else {
				logger.warn("An error occurred while getting Kafka EndOffset at {} from cluster {}, and group {}. Error: {}",
						datetime.toLocalTime(), clusterId, groupId, e.getMessage());
			}
		}
	}

	@Scheduled(cron = "${app.analysis-schedule.delay:5 0 * * * *}") // 1 hour (at minute 0 and second 5 of every hour)
	public synchronized void kafkaMessageAnalysisTaskEveryHour() {
		List<String> consumerGroup = syncRequestService.listConsumerGroups();
		if (consumerGroup.isEmpty()) return;
		int retry = 3;
		Random rand = new Random(); //instance of random class
		int upperbound = 300;
		//generate random values from 0-300
		int int_random = rand.nextInt(upperbound);

		try {
			// TODO: Delay 0-300 seconds before start analyzing again
			TimeUnit.SECONDS.sleep(int_random);
		} catch (InterruptedException ex2) {
			Thread.currentThread().interrupt();
		}

		List<String> topicNames = new ArrayList<>(syncRequestService.findAllTopicNames());
		Collections.shuffle(topicNames, new Random(int_random));

		try (Connection connection = dataSource.getConnection()) {
			connection.setAutoCommit(false); // Disable auto commit
			logger.info("Start to analyze all synchronizers at {}...", LocalDateTime.now(Constants.ZONE_ID));

			String lastReceivedMessageSql = "INSERT INTO tbl_last_received_message_info (topic, scn, commit_scn, msg_timestamp, received_date, received_time) " +
					"VALUES (?, ?, ?, ?, ?, ?)" +
					"ON CONFLICT (topic) " +
					"DO UPDATE SET (scn, commit_scn, msg_timestamp, received_date, received_time) = (?, ?, ?, ?, ?)";

			String analysisPerHourSql = "INSERT INTO tbl_analysis_message_each_topic (id, db_name, schm_name, topic, received_message, total_message, at_year, at_month, at_date, at_hour, received_message_hourly, kafka_message_hourly, total_latency) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
					"ON CONFLICT (topic, at_year, at_month, at_date, at_hour, at_minute, per_five) " +
					"DO UPDATE SET (received_message, total_message, received_message_hourly, kafka_message_hourly, total_latency) = (?, ?, tbl_analysis_message_each_topic.received_message_hourly + ?, ?, tbl_analysis_message_each_topic.total_latency + ?)";

			// Init PreparedStatement for each SQL query
			try (PreparedStatement analysisPerHourStatement = connection.prepareStatement(analysisPerHourSql);
				 PreparedStatement lastMessageInfoStatement = connection.prepareStatement(lastReceivedMessageSql)) {
				List.of(KafkaConstants.MNT_CLUSTER).forEach(clusterId -> {
					Properties props = new Properties();
					props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
					props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
					props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
					for (String groupId : consumerGroup) {
						try {
							analyzingMessages(props, retry, connection, lastMessageInfoStatement, analysisPerHourStatement, clusterId, groupId, topicNames);
						} catch (Exception ex) {
							if (logger.isDebugEnabled()) {
								logger.warn(ex.getMessage(), ex);
							} else {
								logger.warn("An error occurred while analyzing messages and getting Kafka EndOffset from cluster {}, and group {}. Error: {}",
										clusterId, groupId, ex.getMessage());
							}
						}
					}
				});
				// 1) Do execute batch for last message statement
				lastMessageInfoStatement.executeBatch();
				// 2) Do execute batch for computing statement
				analysisPerHourStatement.executeBatch();
			} catch (SQLException e) {
				logger.warn(e.getMessage(), e);
			}
			// 3) Do commit only one time after executed all batch
			connection.commit();
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
		}
	}

	private void saveLastReceivedMessageInfo(PreparedStatement statement, String topic, LocalDateTime dateTime) {
		try {
			LastMessageInfoDto lastReceivedMessageInfo = messageCollectorService.getLastMessageInfoDto(topic);
			if (lastReceivedMessageInfo.getReceivedDateTime() != null &&
					lastReceivedMessageInfo.getScn() != null &&
					lastReceivedMessageInfo.getCommitScn() != null &&
					lastReceivedMessageInfo.getMsgTimestamp() != null) {
				statement.setString(1, topic);
				statement.setLong(2, lastReceivedMessageInfo.getScn());
				statement.setLong(3, lastReceivedMessageInfo.getCommitScn());
				statement.setLong(4, lastReceivedMessageInfo.getMsgTimestamp());
				statement.setDate(5, Date.valueOf(lastReceivedMessageInfo.getReceivedDateTime().toLocalDate()));
				statement.setTime(6, Time.valueOf(lastReceivedMessageInfo.getReceivedDateTime().toLocalTime()));
				statement.setLong(7, lastReceivedMessageInfo.getScn());
				statement.setLong(8, lastReceivedMessageInfo.getCommitScn());
				statement.setLong(9, lastReceivedMessageInfo.getMsgTimestamp());
				statement.setDate(10, Date.valueOf(lastReceivedMessageInfo.getReceivedDateTime().toLocalDate()));
				statement.setTime(11, Time.valueOf(lastReceivedMessageInfo.getReceivedDateTime().toLocalTime()));
				statement.addBatch();
			}
		} catch (Exception ex) {
			logger.error("ERROR when retrieving last message info with time : {}, date : {}. Error Message : {}",
					dateTime.toLocalTime(), dateTime.toLocalDate(), ex.getMessage(), ex);
		}
	}

	private void computeAndAnalyzeKafkaMessage(Connection connection, PreparedStatement analysisStatement, String topicName, long endOffset, LocalDateTime dateTime) {
		PreparedStatement statement = null;
		try {
			String databaseName = "";
			if (topicName.startsWith(REV_TOPIC_PREFIX)) {
				databaseName = topicName.substring(4, topicName.indexOf('.'));
			} else {
				databaseName = topicName.substring(0, topicName.indexOf('.'));
			}
			String schemaAndTableName = topicName.substring(topicName.indexOf('.') + 1);
			String schemaName = schemaAndTableName.substring(0, schemaAndTableName.indexOf('.'));

			// TODO: DO NOT SUPPORT MINUTES
			List<MessageAnalysisPerMinuteDto> messagePerMinutes = messageCollectorService.getNumberOfReceivedMessagePerMinuteByDateHour(topicName);
			long receivedByHour = 0L;
			long totalLatencyByHour = 0L;
			String sql = "INSERT INTO tbl_analysis_message_per_minute (id, db_name, schm_name, topic, received_message, at_year, at_month, at_date, at_hour, at_minute, total_latency)" +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
					"ON CONFLICT (topic, at_year, at_month, at_date, at_hour, at_minute)" +
					"DO UPDATE SET received_message = tbl_analysis_message_per_minute.received_message + ?, total_latency = tbl_analysis_message_per_minute.total_latency + ?";

			statement = connection.prepareStatement(sql);

			for (MessageAnalysisPerMinuteDto messagePerMinute : messagePerMinutes) {
				if (messagePerMinute == null) continue;
				receivedByHour += messagePerMinute.getReceivedMessage();
				totalLatencyByHour += messagePerMinute.getTotalLatency();
				LocalDate atDate = messagePerMinute.getAtDate();
				LocalTime atTime = messagePerMinute.getAtTime();

				statement.setString(1, UUID.randomUUID().toString());
				statement.setString(2, databaseName);
				statement.setString(3, schemaName);
				statement.setString(4, topicName);
				statement.setLong(5, messagePerMinute.getReceivedMessage());
				statement.setInt(6, atDate.getYear());
				statement.setInt(7, atDate.getMonthValue());
				statement.setInt(8, atDate.getDayOfMonth());
				statement.setInt(9, atTime.getHour());
				statement.setInt(10, atTime.getMinute());
				statement.setLong(11, messagePerMinute.getTotalLatency());
				statement.setLong(12, messagePerMinute.getReceivedMessage());
				statement.setLong(13, messagePerMinute.getTotalLatency());
				statement.addBatch();
			}
			statement.executeBatch();
			// Do commit for saving number of message per minute
			connection.commit();
			if (Objects.nonNull(statement)) {
				statement.close();
			}

			MessageAnalysisEntity beforeOneHour = messageAnalysisService.getMostRecentReceivedMessage(topicName, dateTime.minusHours(1));
			long totalReceived = (beforeOneHour == null) ? receivedByHour : receivedByHour + beforeOneHour.getReceivedMessage();
			long endOffsetHourly = (beforeOneHour == null) ? 0 : endOffset - beforeOneHour.getTotalMessage();
			long kafkaByHour = endOffsetHourly < 0 ? 0 : endOffsetHourly;

			// Set parameter for analysis statement
			analysisStatement.setString(1, UUID.randomUUID().toString());
			analysisStatement.setString(2, databaseName);
			analysisStatement.setString(3, schemaName);
			analysisStatement.setString(4, topicName);
			analysisStatement.setLong(5, totalReceived);
			analysisStatement.setLong(6, endOffset);
			analysisStatement.setInt(7, dateTime.getYear());
			analysisStatement.setInt(8, dateTime.getMonthValue());
			analysisStatement.setInt(9, dateTime.getDayOfMonth());
			analysisStatement.setInt(10, dateTime.getHour());
			analysisStatement.setLong(11, receivedByHour);
			analysisStatement.setLong(12, kafkaByHour);
			analysisStatement.setLong(13, totalLatencyByHour);// total latency
			analysisStatement.setLong(14, totalReceived);
			analysisStatement.setLong(15, endOffset);
			analysisStatement.setLong(16, receivedByHour);
			analysisStatement.setLong(17, kafkaByHour);
			analysisStatement.setLong(18, totalLatencyByHour);
			analysisStatement.addBatch();
		} catch (Exception ex) {
			logger.error("ERROR when analyzing kafka messages for given time: {}, date: {}. Error Message: {}",
					dateTime.toLocalTime(), dateTime.toLocalDate(), ex.getMessage(), ex);
		}
	}

	private void computeKafkaOffsetEveryFiveMinute(PreparedStatement kafkaOffsetStatement, String topicName, long endOffset, LocalDateTime dateTime) {
		String databaseName = "";
		if (topicName.startsWith(REV_TOPIC_PREFIX)) {
			databaseName = topicName.substring(4, topicName.indexOf('.'));
		} else {
			databaseName = topicName.substring(0, topicName.indexOf('.'));
		}
		String schemaAndTableName = topicName.substring(topicName.indexOf('.') + 1);
		String schemaName = schemaAndTableName.substring(0, schemaAndTableName.indexOf('.'));
		try {
			Long previousEndOffset = messageAnalysisService.getRecentKafkaEndOffSet(topicName);
			long endOffsetPerFive = (previousEndOffset == null) ? 0 : endOffset - previousEndOffset;
			long finalKafkaMessagesPerFive = endOffsetPerFive < 0 ? 0 : endOffsetPerFive;

			kafkaOffsetStatement.setString(1, UUID.randomUUID().toString());
			kafkaOffsetStatement.setString(2, databaseName);
			kafkaOffsetStatement.setString(3, schemaName);
			kafkaOffsetStatement.setString(4, topicName);
			kafkaOffsetStatement.setInt(5, dateTime.getYear());
			kafkaOffsetStatement.setInt(6, dateTime.getMonthValue());
			kafkaOffsetStatement.setInt(7, dateTime.getDayOfMonth());
			kafkaOffsetStatement.setInt(8, dateTime.getHour());
			kafkaOffsetStatement.setInt(9, dateTime.getMinute());
			kafkaOffsetStatement.setLong(10, finalKafkaMessagesPerFive);
			kafkaOffsetStatement.setLong(11, endOffset);
			kafkaOffsetStatement.setBoolean(12, true);
			kafkaOffsetStatement.addBatch();
		} catch (Exception ex) {
			logger.error("ERROR when getting kafka offset for given time: {}, date: {}. Error Message: {}",
					dateTime.toLocalTime(), dateTime.toLocalDate(), ex.getMessage(), ex);
		}
	}
}
