package com.lguplus.fleta.domain.service.analysis;

import com.lguplus.fleta.domain.dto.LastMessageInfoDto;
import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import com.lguplus.fleta.domain.dto.analysis.MessageAnalysisPerMinuteDto;
import com.lguplus.fleta.domain.model.ReceivedMessageEntity;
import com.lguplus.fleta.domain.service.constant.Constants;
import com.lguplus.fleta.domain.util.ShutdownHookUtils;
import com.lguplus.fleta.ports.repository.ReceivedMessageRepository;
import com.lguplus.fleta.ports.service.MessageCollectorService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MessageCollectorServiceImpl implements MessageCollectorService {

	private static final Logger logger = LoggerFactory.getLogger(MessageCollectorServiceImpl.class);

	private static final int NUMBER_OF_MESSAGE = 1000;

	private final ReceivedMessageRepository analysisRepository;

	private final BlockingQueue<ReceivedMessageEntity> messageEntities = new LinkedBlockingQueue<>(Integer.MAX_VALUE);

	private final ExecutorService executorService;

	public MessageCollectorServiceImpl(ReceivedMessageRepository analysisRepository,
									   @Qualifier("messageCollectorThreadPool") ExecutorService executorService) {
		this.analysisRepository = analysisRepository;
		this.executorService = executorService;
		ShutdownHookUtils.initExecutorServiceShutdownHook(this.executorService);
	}

	@Override
	public void saveMessages(String kafkaTopic, List<SyncRequestMessage> messages, Long completedTime) {
		Objects.requireNonNull(messages, "Kafka message must not be bull");

		try {
			executorService.submit(() -> {
				for (SyncRequestMessage msg : messages) {
					String msgTimestamp = msg.getPayload().getSource().getTsMs();
					Long latency = StringUtils.isEmpty(msgTimestamp) ? null : completedTime - Long.parseLong(msgTimestamp);
					onSaveMessage(kafkaTopic, msg.getCommitScn(), msg.getScn(), msg.getTimestamp(), latency);
				}
			});
		} catch (java.util.concurrent.RejectedExecutionException ex) {
			for (SyncRequestMessage message : messages) {
				messageEntities.add(new ReceivedMessageEntity(kafkaTopic,
						LocalDate.now(Constants.ZONE_ID), LocalTime.now(Constants.ZONE_ID),
						message.getCommitScn(), message.getScn(), message.getTimestamp()));
			}
		}
	}

	protected void onSaveMessage(String kafkaTopic, Long commitScn, Long scn, Long timestamp, Long latency) {
		ReceivedMessageEntity messageEntity = new ReceivedMessageEntity(kafkaTopic,
				LocalDate.now(Constants.ZONE_ID), LocalTime.now(Constants.ZONE_ID), commitScn, scn, timestamp);
		messageEntity.setMsgLatency(latency);
		messageEntities.add(messageEntity);

		// If the number of elements stored in BlockingQueue greater than default batch size then drains to
		// a collection and flush it into database.
		int numberOfItems = messageEntities.size();
		if (numberOfItems >= NUMBER_OF_MESSAGE) {
			List<ReceivedMessageEntity> entities = new ArrayList<>();
			try {
				do {
					messageEntities.drainTo(entities, NUMBER_OF_MESSAGE);
					if (!entities.isEmpty()) {
						// Save into database
						analysisRepository.saveAllAndFlush(entities);
						entities.clear();
					}
					numberOfItems = messageEntities.size();
				} while (numberOfItems >= NUMBER_OF_MESSAGE);
			} catch (Exception ex) {
				logger.warn(ex.getMessage(), ex);
			}
		}
	}

	/**
	 * A schedule job that will automatically save all received messages into database
	 */
	@Scheduled(fixedRateString = "${app.save-message-schedule.delay:300000}") // 5 minutes
	public void autoSaveReceivedKafkaMessage() {
		int numberOfItems = messageEntities.size();
		if (numberOfItems <= 0) return;

		logger.info("Auto flush {} message(s) into database for analyzing...", numberOfItems);

		try {
			List<ReceivedMessageEntity> entities = new ArrayList<>();

			do {
				messageEntities.drainTo(entities, NUMBER_OF_MESSAGE); // Consume 1000 messages for each execution
				if (!entities.isEmpty()) {
					// Save into database
					analysisRepository.saveAllAndFlush(entities);
					entities.clear();
				}
				numberOfItems = messageEntities.size();
			} while (numberOfItems >= NUMBER_OF_MESSAGE);
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
		}
	}

	@Override
	public List<MessageAnalysisPerMinuteDto> getNumberOfReceivedMessagePerMinuteByDateHour(String kafkaTopic) {
		try {
			return analysisRepository.selectAndDeleteMessageByTopic(kafkaTopic);
		} catch (org.springframework.dao.CannotAcquireLockException | org.hibernate.exception.LockAcquisitionException ex) {
			logger.warn(ex.getMessage());
			logger.info(">>> Let the thread sleep for a while and then try to acquire the lock again...");

			try {
				// TODO: Delay 15 seconds before trigger again
				TimeUnit.SECONDS.sleep(15);
			} catch (InterruptedException ex2) {
				Thread.currentThread().interrupt();
			}
			return analysisRepository.selectAndDeleteMessageByTopic(kafkaTopic);
		}
	}

	@Override
	public LastMessageInfoDto getLastMessageInfoDto(String topic) {
		return analysisRepository.findLastMessageInfo(topic);
	}

	@Override
	public Map<String, LastMessageInfoDto> getMapLastMessageInfoByListTopic(List<String> topics) {
		return analysisRepository.findLastMessageInfos(topics).stream()
				.collect(Collectors.toMap(LastMessageInfoDto::getTopic, dto -> dto));
	}

}
