package com.lguplus.fleta.domain.service.synchronizer;

import com.lguplus.fleta.adapters.messagebroker.*;
import com.lguplus.fleta.config.Profile;
import com.lguplus.fleta.config.context.DbSyncContext;
import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import com.lguplus.fleta.domain.dto.Synchronizer.SyncState;
import com.lguplus.fleta.domain.dto.event.SyncBroadcastEventData;
import com.lguplus.fleta.domain.dto.event.SyncBroadcastEventData.BroadcastAction;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.domain.service.constant.Constants;
import com.lguplus.fleta.domain.service.constant.DivisionType;
import com.lguplus.fleta.domain.service.exception.InvalidTaskCreationException;
import com.lguplus.fleta.domain.util.DateUtils;
import com.lguplus.fleta.ports.service.SyncHistoryService;
import com.lguplus.fleta.ports.service.SynchronizerService;
import com.lguplus.fleta.ports.service.SynchronizerDlqService;
import com.lguplus.fleta.ports.service.SyncRequestService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class SynchronizerServiceImpl implements SynchronizerService {

	private static final Logger logger = LoggerFactory.getLogger(SynchronizerServiceImpl.class);

	private static final int DEFAULT_CONCURRENCY = 1;

	private final SyncHistoryService syncHistoryService;
	private final KafkaProperties kafkaProperties;

	private final Map<String, KafkaMessageListenerContainer<String, SyncRequestMessage>> kafkaContainerKeys = new ConcurrentHashMap<>();

	private final Map<String, SynchronizerHandler> dataSynchronizerKeys = new ConcurrentHashMap<>();

	private final KafkaMessageConsumerFactory kafkaMessageConsumerFactory;

	private final SyncRequestService syncRequestService;

	private final SynchronizerDlqService synchronizerDlqService;

	// Map<DataSource, List<Synchronizer>>
	private final Map<String, List<String>> dataSourceToSynchronizerMap = new ConcurrentHashMap<>();

	private final SynchronizerBroadcastPublisher broadcastPublisher;

	private final SynchronizerValidator synchronizerValidator;

	@Value("${spring.profiles.active}")
	private String ACTIVE_PROFILE;

	private static final String SYNCHRONIZER_BROADCAST_GROUP = "synchronizer-out-0";

	public SynchronizerServiceImpl(SyncHistoryService syncHistoryService, KafkaMessageConsumerFactory kafkaMessageConsumerFactory,
								   SyncRequestService syncRequestService,
								   SynchronizerDlqService synchronizerDlqService,
								   KafkaProperties kafkaProperties, StreamBridge streamBridge,
								   SynchronizerValidator synchronizerValidator) {
		Assert.notNull(kafkaMessageConsumerFactory, "A KafkaMessageConsumerFactory must be provided");
		Assert.notNull(kafkaProperties, "A KafkaProperties must be provided");

		this.syncHistoryService = syncHistoryService;
		this.synchronizerValidator = synchronizerValidator;
		this.kafkaMessageConsumerFactory = kafkaMessageConsumerFactory;
		this.kafkaProperties = kafkaProperties;
		this.syncRequestService = syncRequestService;
		this.synchronizerDlqService = synchronizerDlqService;
		this.broadcastPublisher = new SynchronizerBroadcastPublisher(streamBridge, SYNCHRONIZER_BROADCAST_GROUP);
	}

	private boolean isProduction() {
		return Profile.isProduction(this.ACTIVE_PROFILE);
	}

	@Override
	public void startSynchronizer() {
		List<SyncState> states = Arrays.asList(SyncState.STOPPED, SyncState.PENDING);
		List<SyncRequestEntity> synchronizers = syncRequestService.findByStateIn(states);
		if (Objects.nonNull(synchronizers)) {
			synchronizers.forEach(synchronizer -> {
				startSynchronizer(synchronizer.getTopicName(), synchronizer.getSynchronizerName(), DEFAULT_CONCURRENCY, true);
				syncHistoryService.insertHistory(synchronizer.getId(), synchronizer.getTopicName(),synchronizer.getState(),"STARTED",synchronizer);
				addToMapDataSource(synchronizer);
			});
		}
	}

	@Override
	public void addToMapDataSource(SyncRequestEntity synchronizer) {
		dataSourceToSynchronizerMap
				.computeIfAbsent(synchronizer.getTargetDatabase(), value -> new ArrayList<>())
				.add(synchronizer.getTopicName());
	}

	@Override
	public void doStop() {
		syncRequestService.findAll()
				.stream()
				.filter(synchronizer -> synchronizer.getState() == SyncState.RUNNING)
				.forEachOrdered(synchronizer -> doStop(synchronizer.getTopicName(), synchronizer.getSynchronizerName(),true));
	}

	@Override
	public void doStop(String kafkaTopic, String syncName, boolean isBroadcast) {
		doStop(kafkaTopic, syncName, null, isBroadcast);
	}

	@Override
	public void doStop(String kafkaTopic, String syncName, Runnable callback, boolean isBroadcast) {
		try {
			// Finding the stopping synchronizer for give kafka topic and its name
			SyncRequestEntity requestEntity = syncRequestService.findByTopicNameAndSynchronizerName(kafkaTopic, syncName);
			if (Objects.isNull(requestEntity)) {
				logger.info("Could not find any synchronizer of topic {} for given name {}", kafkaTopic, syncName);
				return;
			}

			logger.info("Trying to stop synchronizer {} of topic {} at {}, please wait...",
					syncName, kafkaTopic, new Date());
			doStop(kafkaTopic);

			// Update topic state to STOPPED
			requestEntity.setState(SyncState.STOPPED);
			requestEntity.setUpdatedAt(LocalDateTime.now());
			syncRequestService.createOrUpdate(requestEntity);

			// Remove topic from map datasource:List<topic>
			dataSourceToSynchronizerMap.computeIfPresent(requestEntity.getTargetDatabase(), (k, v) -> {
				v.removeIf(x -> v.contains(requestEntity.getTopicName()));
				return v;
			});

			if (isBroadcast && isProduction()) {
				broadcastPublisher.broadcast(new SyncBroadcastEventData(kafkaTopic, syncName, BroadcastAction.STOP));
			}

			if (Objects.nonNull(callback)) {
				callback.run(); // Execute callback
			}
		} catch (Exception ex) {
			logger.error("Error while update state of topic {} with error message {}", kafkaTopic, ex.getMessage());
		}
	}

	/**
	 * Stop consume message from kafka
	 * In the case stop by lost connection of datasource (DefaultLostConnectionHandler), we don't need to remove container otherwise remove it
	 *
	 * @param kafkaTopic
	 */
	private void doStop(String kafkaTopic) {
		KafkaMessageListenerContainer<String, SyncRequestMessage> container = kafkaContainerKeys.get(kafkaTopic);
		if (container != null) {
			if (container.isRunning()) {
				// Regardless of this behavior, you should not call stop() on the listener thread - it will cause a delay
				// (because stop() waits for the listener thread to exit until shutdownTimeout).
				// Call stop(() -> { }) instead.
				container.stop(() -> {
					logger.info("The KafkaMessageListenerContainer for kafka topic {} is stopped at {}", kafkaTopic, new Date());
				});
			}
			kafkaContainerKeys.remove(kafkaTopic, container);

			// Call shutdown method for interrupting orderly consumer
			SynchronizerHandler synchronizerHandler = dataSynchronizerKeys.get(kafkaTopic);
			if (Objects.nonNull(synchronizerHandler)) {
				synchronizerHandler.shutdown(() -> logger.info("The DataSynchronizer for kafka topic {} is stopped at {}", kafkaTopic, new Date()));
			}
		}
	}

	@Override
	public void startSynchronizer(String kafkaTopic, String syncName, int concurrency, boolean isBroadcast) {
		logger.info("Creating kafka consumer for topic {} at {}", kafkaTopic, new Date());
		boolean isLoop = synchronizerValidator.syncValidation(kafkaTopic, syncName);
		KafkaMessageListenerContainer<String, SyncRequestMessage> container = kafkaContainerKeys.get(kafkaTopic);
		SyncRequestEntity syncRequest = syncRequestService.findByTopicNameAndSynchronizerName(kafkaTopic, syncName);

		// Not start synchronizer if column comparison fail
		// Checking isBroadcast == true to ensure only one instance check column comparison
		if (isLoop) {
			logger.error("{} have Potential Infinite Loop. Please check Synchronizer", kafkaTopic);
			return;
		} else if (isBroadcast && syncRequestService.isColumnComparisonError(syncRequest)) {
			return;
		}

		if (null != container) {
			if (!container.isRunning()) {
				logger.info(">>> Consumer already created for topic {}, starting consumer!!", kafkaTopic);

				// Start KafkaMessageListenerContainer
				container.start();
				if (isBroadcast && isProduction()) {
					broadcastPublisher.broadcast(new SyncBroadcastEventData(kafkaTopic, syncName, BroadcastAction.START));
				}
				logger.info(">>> Consumer for topic {} started!!!!", kafkaTopic);
			}

			logger.info(">>> kafka consumer for topic {} is running already", kafkaTopic);

			try {
				if (Objects.nonNull(syncRequest) && !syncRequest.hasPrimaryKeys()) {
					String primaryKeys = syncRequestService.detectPrimaryKeys(syncRequest.getDivision(), syncRequest.getTargetDatabase(), syncRequest.getTargetSchema(), syncRequest.getTargetTable());
					if (StringUtils.isNoneEmpty(primaryKeys)) {
						syncRequest.setPrimaryKeys(primaryKeys);
					}
				}

				if (Objects.nonNull(syncRequest) && !syncRequest.hasUniqueKeys()) {
					String uniqueKeys = syncRequestService.detectUniqueKeys(syncRequest.getDivision(), syncRequest.getTargetDatabase(), syncRequest.getTargetSchema(), syncRequest.getTargetTable());
					if (StringUtils.isNoneEmpty(uniqueKeys)) {
						syncRequest.setUniqueKeys(uniqueKeys);
					}
				}

				boolean isPartitioned = false;
				if (Objects.nonNull(syncRequest)) {
					isPartitioned = syncRequestService.detectPartition(syncRequest.getDivision(), syncRequest.getSourceDatabase(), syncRequest.getSourceSchema(),
							syncRequest.getSourceTable(), syncRequest.getTargetDatabase(), syncRequest.getTargetSchema(), syncRequest.getTargetTable());
				}

				syncRequest.setIsPartitioned(isPartitioned);
				syncRequest.setState(SyncState.RUNNING);
				syncRequest.setUpdatedAt(DateUtils.getDateTime());
				syncRequestService.createOrUpdate(syncRequest);

				logger.info(">>> The synchronizer {} has been linked to kafka topic {} at {}",
						syncRequest.getSynchronizerName(), kafkaTopic, System.currentTimeMillis());
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
			return;
		}

		MessageConsumer.ConsumerProperties properties = MessageConsumer.ConsumerProperties.builder()
				.groupId(syncRequest.getConsumerGroup())
				.topic(kafkaTopic)
				.serverUrl(kafkaProperties.getBootstrapServers())
				.maxPollRecords(syncRequest.getMaxPollRecords())
				.build();

		SynchronizerHandler handler;
		try{
			handler = DbSyncContext.getBean(SynchronizerHandlerFactory.class)
					.create(DivisionType.getDivision(syncRequest.getDivision()), kafkaTopic, syncRequest.isBatch());
		} catch (InvalidTaskCreationException e) {
			throw new IllegalArgumentException(e);
		}

		MessageConsumer<SyncRequestMessage> consumer = new DataSyncMessageConsumer(properties, handler);
		container = kafkaMessageConsumerFactory.create(consumer, SyncRequestMessage.class);
		container.setBatchErrorHandler(new KafkaBatchErrorHandler());

		// start the container
		container.start();

		kafkaContainerKeys.put(kafkaTopic, container);
		dataSynchronizerKeys.put(kafkaTopic, handler);

		// Update topic state from PENDING to RUNNING
		try {
			// TODO: Move to corresponding component
			if (!syncRequest.hasPrimaryKeys()) {
				String primaryKeys = syncRequestService.detectPrimaryKeys(syncRequest.getDivision(), syncRequest.getTargetDatabase(), syncRequest.getTargetSchema(), syncRequest.getTargetTable());
				logger.info("The topic {} has primary keys: {}", kafkaTopic, primaryKeys);
				if (StringUtils.isNoneEmpty(primaryKeys)) {
					syncRequest.setPrimaryKeys(primaryKeys);
				}
			}

			if (!syncRequest.hasUniqueKeys()) {
				String uniqueKeys = syncRequestService.detectUniqueKeys(syncRequest.getDivision(), syncRequest.getTargetDatabase(), syncRequest.getTargetSchema(), syncRequest.getTargetTable());
				logger.info("The topic {} has primary keys: {}", kafkaTopic, uniqueKeys);
				if (StringUtils.isNoneEmpty(uniqueKeys)) {
					syncRequest.setUniqueKeys(uniqueKeys);
				}
			}

			if (syncRequest.isIn(SyncState.PENDING, SyncState.STOPPED)) {
				syncRequest.setState(SyncState.RUNNING);
				syncRequest.setUpdatedAt(DateUtils.getDateTime());
				syncRequestService.createOrUpdate(syncRequest);

				logger.info(">>> Topic state has change to RUNNING {}", kafkaTopic);
			} else {
				logger.info(">>> This topic state in database is already RUNNING {}", kafkaTopic);
			}
		} catch (Exception ex) {
			logger.error(">>> Error while update state of topic {} with error message {}. \n\t{}", kafkaTopic, ex.getMessage(), ex);
		}

		if (isBroadcast && isProduction()) {
			broadcastPublisher.broadcast(new SyncBroadcastEventData(kafkaTopic, syncName, BroadcastAction.START));
		}

		logger.info(">>> Created and started kafka consumer for topic {}", kafkaTopic);
	}

	@Override
	public void stopSynchronizerByDataSource(String datasource) throws DataSourceInvalidException {
		if (!dataSourceToSynchronizerMap.containsKey(datasource)) {
			throw new DataSourceInvalidException(String.format("Datasource %s does not exist inside map runtime", datasource));
		}

		List<String> topics = dataSourceToSynchronizerMap.get(datasource);
		try {
			if (topics.isEmpty() == Boolean.FALSE) {
				for (String topic : topics) {
					// In case can't connect to datasource, no need to broadcast event to other instance.
					// Other instances has their own datasource health check.
					// We don't need to remove container, because is will be use for starting again when the datasource is re-connect
					doStop(topic);
				}
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@Override
	public void startSynchronizerByDataSource(String datasource) throws DataSourceInvalidException {
		if (!dataSourceToSynchronizerMap.containsKey(datasource)) {
			throw new DataSourceInvalidException(String.format("Datasource %s does not exist inside map runtime", datasource));
		}

		List<String> topics = dataSourceToSynchronizerMap.get(datasource);
		if (Objects.isNull(topics) || topics.isEmpty()) return;
		List<SyncRequestEntity> synchronizers = syncRequestService.findByTopicNames(topics);
		// In case receive re-connect message from datasource, no need to broadcast event to other instance.
		// Other instances has their own datasource health check.
		synchronizers.forEach(synchronizer -> {
			startSynchronizer(synchronizer.getTopicName(), synchronizer.getSynchronizerName(), 1, false);
		});
	}

	@Override
	public void startDefaultSynchronizers() {
		List<SyncRequestEntity> syncRequests = syncRequestService.findByStateIn(List.of(SyncState.RUNNING));

		if (Objects.nonNull(syncRequests) && syncRequests.size() > 0) {
			logger.info("Found {} synchronizers for given RUNNING state. Doing start at {}, please wait...",
					syncRequests.size(), new Date());
		} else {
			logger.info("Found 0 synchronizer for given RUNNING state.");
		}

		for (SyncRequestEntity syncRequest : syncRequests) {
			startSynchronizer(syncRequest.getTopicName(), syncRequest.getSynchronizerName(), 1, true);

			// Add to map to stop or start by datasource
			addToMapDataSource(syncRequest);
		}
	}

	@Override
	public void startDefaultErrorTracker() {
		try {
			Properties props = new Properties();
			props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getNotifyBootstrapServers());
			props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.KAFKA_ERROR_GROUP_ID);

			// Create the consumer using props.
			final Consumer<Long, String> consumer = kafkaMessageConsumerFactory.create(props, Constants.KAFKA_ERROR_TOPIC_NAME);
			// Create new Thread to listen error message
			Thread errorTracker = new Thread(new Runnable() {
				@Override
				public void run() {
					synchronizerDlqService.handleError(consumer);
				}
			});
			errorTracker.setName("ErrorTrackingConsumer");
			errorTracker.setDaemon(true);
			errorTracker.start();

			logger.info(">>> The default tracker has been started at {}", new Date());
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@Override
	public int getContainerStatus(String kafkaTopic) {
		if (!kafkaContainerKeys.containsKey(kafkaTopic)) {
			return 0;
		}

		KafkaMessageListenerContainer<String, SyncRequestMessage> container = kafkaContainerKeys.get(kafkaTopic);
		if (Objects.isNull(container)) return 0;
		if (container.isRunning()) return 1;
		if (container.isContainerPaused()) return 3;
		return 2;
	}

	@Override
	public List<String> findAllSynchronizers() {
		return new ArrayList<>(kafkaContainerKeys.keySet());
	}
}
