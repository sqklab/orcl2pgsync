package com.lguplus.fleta.domain.service.synchronizer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lguplus.fleta.domain.dto.*;
import com.lguplus.fleta.domain.dto.Synchronizer.ErrorState;
import com.lguplus.fleta.domain.dto.ui.SyncErrorDto;
import com.lguplus.fleta.domain.model.SyncErrorEntity;
import com.lguplus.fleta.domain.service.constant.Constants;
import com.lguplus.fleta.domain.service.exception.ErrorNotFoundException;
import com.lguplus.fleta.domain.service.mapper.ObjectMapperFactory;
import com.lguplus.fleta.domain.util.EnumUtil;
import com.lguplus.fleta.ports.repository.SyncErrorRepository;
import com.lguplus.fleta.ports.service.LoggerManager;
import com.lguplus.fleta.ports.service.SyncRequestService;
import com.lguplus.fleta.ports.service.SynchronizerDlqService;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.lguplus.fleta.domain.dto.Synchronizer.ErrorState.ERROR;
import static com.lguplus.fleta.domain.dto.Synchronizer.ErrorState.PROCESSING;

@Service
public class SynchronizerDlqServiceImpl implements SynchronizerDlqService {

	private static final String RETRY_LOG_FILENAME = "dlq-resolver";

	private final ObjectMapper objectMapper;

	private final SyncErrorRepository errorRepository;

	private final BuildProperties buildProperties;

	private final String activeProfile;

	private final SynchronizerDlqResolver dlqResolver;

	private final SyncRequestService syncRequestService;

	private final Logger logger;

	private static String PK_NULL_VALUE = "NULL";

	public SynchronizerDlqServiceImpl(SyncErrorRepository errorRepository,
									  ObjectMapper objectMapper,
									  BuildProperties buildProperties,
									  @Value("${spring.profiles.active}") String activeProfile,
									  LoggerManager loggerManager,
									  SynchronizerDlqResolver dlqResolver,
									  SyncRequestService syncRequestService) {
		this.errorRepository = errorRepository;
		this.objectMapper = objectMapper;
		this.buildProperties = buildProperties;
		this.activeProfile = activeProfile;
		this.dlqResolver = dlqResolver;
		this.syncRequestService = syncRequestService;
		this.logger = loggerManager.getLogger(RETRY_LOG_FILENAME);
	}

	@Override
	public SyncErrorDto findAllSyncErrorsByTopicName(String topicName, String errorState, Pageable pageable, String kidOfErrorType, LocalDateTime from, LocalDateTime to, List<String> operationState) {
		List<ErrorState> states = EnumUtil.getOrAll(ErrorState.class, errorState);
		List<DbSyncOperation> operations = EnumUtil.getOrAll(DbSyncOperation.class, operationState);
		List<ErrorType> kidOfErrorTypes = EnumUtil.getOrAll(ErrorType.class, kidOfErrorType);

		Page<SyncErrorEntity> byTopicName = this.errorRepository.findPageByTopicNameAndOrderByState(topicName, states, operations, kidOfErrorTypes, from, to, pageable);
		return getSyncErrorDto(byTopicName);
	}

	private SyncErrorDto getSyncErrorDto(Page<SyncErrorEntity> errorEntities) {
		List<SyncErrorEntity> collect = errorEntities.stream()
				.map(item -> {
					if (null != item.getSyncMessage()) {
						try {
							SyncRequestMessage syncRequestMessage = objectMapper.readValue(item.getSyncMessage(), SyncRequestMessage.class);
							if (null != syncRequestMessage) {
								item.setSqlRedo(syncRequestMessage.getSqlRedo());
							}
						} catch (Exception e) {
							// do nothing
						}
					}
					return item;
				}).collect(Collectors.toList());

		return SyncErrorDto.builder()
				.syncErrorEntities(collect)
				.totalPage(errorEntities.getTotalPages())
				.environment(activeProfile)
				.errorVersion(buildProperties.getVersion())
				.build();
	}

	@Override
	public int resolveAllErrorsByTopic(String topicName) {
		return errorRepository.resolvedAll(topicName, ErrorState.RESOLVED.getState());
	}

	@Override
	public void handleError(Consumer<Long, String> consumer) {
		final List<SyncErrorEntity> errorEntities = new LinkedList<>();
		while (true) {
			ConsumerRecords<Long, String> records = consumer.poll(Duration.ofMillis(100)); // default value of max.poll.records is 500
			records.forEach(consumerRecord -> {
				if (logger.isDebugEnabled()) {
					logger.debug("START-CONSUMER: Message in group: " + Constants.KAFKA_ERROR_GROUP_ID + " topic: " +
							Constants.KAFKA_ERROR_TOPIC_NAME);
					logger.debug("MESSAGE: {}", consumerRecord);
				}
				try {
					ErrorMessage errorMessage = objectMapper.readValue(consumerRecord.value(), ErrorMessage.class);
					if (errorMessage == null) {
						logger.info("ERROR-CONSUMER: Can not cast SyncTaskError to SyncErrorInfo :: {}", consumerRecord.value());
					} else {
						SyncErrorEntity entity = new SyncErrorEntity(errorMessage.getTopicName(), errorMessage.getSyncMessage(),
								errorMessage.getErrorMessage(), errorMessage.getErrorType(), DbSyncOperation.valueOf(errorMessage.getOperation()));
						errorEntities.add(entity);
					}
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			});

			try {
				if (!errorEntities.isEmpty()) {
					// Save batch of errors
					List<SyncErrorEntity> errorMessages = errorRepository.saveAllAndFlush(errorEntities);
					logger.info("Save batch (Error: {} error(s)) done!", errorMessages.size());
					// Removes all of the elements from this list
					errorEntities.clear();
				}
			} catch (Exception e) {
				// Removes all of the elements from this list
				errorEntities.clear();

				logger.error(e.getMessage(), e);
			}
			consumer.commitAsync();
		}
	}

	@Override
	public ByteArrayInputStream exportPrimaryKeys(String topicName, LocalDateTime dateFrom, LocalDateTime dateTo, String errorState, String errorType, List<String> operationState) {
		String primaryKeys = syncRequestService.findPrimaryKeysWithTopicName(topicName);
		if (StringUtils.isEmpty(primaryKeys)) {
			throw new RuntimeException(String.format("topic %s has no primaryKeys", topicName));
		}
		List<String> pkList = Arrays.asList(primaryKeys.split(","));			// pk list
		List<ErrorState> states = EnumUtil.getOrAll(ErrorState.class, errorState);		// List에 저장
		List<DbSyncOperation> operations = EnumUtil.getOrAll(DbSyncOperation.class, operationState);		// operation
		List<ErrorType> errorTypes = EnumUtil.getOrAll(ErrorType.class, errorType);

		List<SyncErrorEntity> syncErrorList = this.errorRepository.findByTopicNameAndOrderByState(topicName, states, operations, errorTypes, dateFrom, dateTo);

		List<SyncRequestMessage> messages = extractSyncRequestMessages(syncErrorList);				// Sync Message만 불러옴

		Map<String, List<String>> pkValuesJsonMap = new HashMap<>();
		messages.stream()
				.map(message -> {
					DbSyncOperation operation = DbSyncOperation.valueOf(message.getOperation());			// Operation 값 대입
					List<String> pkValues;
					switch (operation) {
						case d:
							pkValues = this.transitionToPkValues(message.getPayload().getBefore(), pkList);
							break;
						case c:
						case u:
							pkValues = this.transitionToPkValues(message.getPayload().getAfter(), pkList);
							break;
						default:
							pkValues = new ArrayList<>();
					}
					return pkValues;
				})
				.filter(pkValues -> !pkValues.isEmpty())
				.forEach(pkValues -> {
					try {
						pkValuesJsonMap.put(ObjectMapperFactory.getInstance().getObjectMapper().writeValueAsString(pkValues), pkValues);
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}
				});

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("pkList");
			Row headerRow = sheet.createRow(0);

			for (int i = 0; i < pkList.size(); i++) {
				Cell headerCell = headerRow.createCell(i);
				headerCell.setCellValue(pkList.get(i));
			}

			List<List<String>> pkValuesArrayList = new ArrayList<>(pkValuesJsonMap.values());
			IntStream.range(0, pkValuesArrayList.size()).boxed()
					.forEach(index -> {
						List<String> pkValues = pkValuesArrayList.get(index);
						int rowNumber = index + 1;
						Row row = sheet.createRow(rowNumber);
						for (int j = 0; j < pkList.size(); j++) {
							Cell cell = row.createCell(j);
							cell.setCellValue(pkValues.get(j));
						}
					});
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			return new ByteArrayInputStream(outputStream.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<String> transitionToPkValues(Map<String, Object> columnAndValueMap, List<String> pkList) {
		return pkList.stream()
				.map(pk -> columnAndValueMap.keySet()
						.stream()
						.filter(pk::equalsIgnoreCase)
						.map(columnAndValueMap::get)
						.filter(Objects::nonNull)
						.map(Object::toString)
						.findAny()
						.orElse(PK_NULL_VALUE)).collect(Collectors.toList());
	}


	private List<SyncRequestMessage> extractSyncRequestMessages(List<SyncErrorEntity> synchronizerErrorList) {
		ObjectMapper mapper = ObjectMapperFactory.getInstance().getObjectMapper();
		return synchronizerErrorList.stream()
				.map(entity -> {
					try {
						return mapper.readValue(entity.getSyncMessage(), SyncRequestMessage.class);
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}
				}).collect(Collectors.toList());
	}

	@Override
	public void deleteAllByTopic(String topicName) {
		errorRepository.deleteAllResolvedByTopic(topicName);
	}

	@Override
	public void deleteAllByErrorIds(List<Long> ids) {
		errorRepository.deleteResolvedByIds(ids);
	}


	/**
	 * retry list of errors in a topic
	 *
	 * @return number of error
	 */
	private int retry(List<SyncErrorEntity> syncErrors, String topic) {
		if (CollectionUtils.isEmpty(syncErrors)) {
			logger.info("There is no error need to be solved.");
			return 0;
		}
		logger.info("Found {} error(s) need to be solved", syncErrors.size());
		dlqResolver.resolve(syncErrors, topic);
		return syncErrors.size();
	}

	/**
	 * retry list of errors in a topic
	 *
	 * @param ids ids
	 * @return
	 */
	@Override
	public int retryToSolveErrorsByErrorIds(List<Long> ids, String topic) {
		List<SyncErrorEntity> syncErrors = this.errorRepository.getErrorToRetry(ids, ERROR.getState(), PROCESSING.getState());
		return retry(syncErrors, topic);
	}

	@Override
	public int retryToSolveErrorByTopicName(String topicName) {
		List<SyncErrorEntity> syncErrors = errorRepository.getAllErrorsByTopicNameAndUpdateStateByState(topicName, PROCESSING.getState());
		return retry(syncErrors, topicName);
	}

	@Override
	public SyncErrorCountOperationsDto countOperations(String topic, List<ErrorState> states) {
		return this.errorRepository.countOperationsByTopicAndState(topic, states.stream().map(ErrorState::getState).collect(Collectors.toList()));
	}

	@Override
	public int deleteBeforeTime(LocalDateTime time) {
		return errorRepository.deleteBeforeTime(time);
	}


	@Override
	public int resolveAllErrorsByErrorIds(List<Integer> errorIds) throws ErrorNotFoundException {
		if (errorIds == null || errorIds.isEmpty()) {
			throw new ErrorNotFoundException("There are not any error to resolve");
		}
		return errorRepository.updateErrorStateByIds(errorIds, ErrorState.RESOLVED.getState());
	}
}
