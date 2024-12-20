package com.lguplus.fleta.domain.service.analysis;

import com.lguplus.fleta.domain.dto.analysis.*;
import com.lguplus.fleta.domain.model.MessageAnalysisEntity;
import com.lguplus.fleta.ports.repository.AdvancedMessageAnalysisRepository;
import com.lguplus.fleta.ports.repository.SimpleMessageAnalysisRepository;
import com.lguplus.fleta.ports.service.MessageAnalysisService;
import com.lguplus.fleta.ports.service.SyncRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MessageAnalysisServiceImpl implements MessageAnalysisService {

	public static final String KAFKA = "_KAFKA";
	public static final String PROCESSED = "_PROCESSED";
	public static final String LATENCY = "_AVG_LATENCY(s)";
	private static final Logger logger = LoggerFactory.getLogger(MessageAnalysisServiceImpl.class);
	private static final int MINUTES = 60;
	private static final int HOURS = 24;
	private static final int DAYS = 31;
	private static final int MONTHS = 12;
	private static final int ONE = 1;
	private static final int FIVE = 5;
	private static final int FIFTEEN = 15;
	private static final int THIRTY = 30;
	private final SyncRequestService syncRequestService;
	private final SimpleMessageAnalysisRepository messageAnalysisRepository;
	private final AdvancedMessageAnalysisRepository analysisRepository;
	private String dbName = "";
	private String schmName = "";

	public MessageAnalysisServiceImpl(SyncRequestService syncRequestService,
									  SimpleMessageAnalysisRepository messageAnalysisRepository,
									  AdvancedMessageAnalysisRepository analysisRepository) {
		this.syncRequestService = syncRequestService;
		this.messageAnalysisRepository = messageAnalysisRepository;
		this.analysisRepository = analysisRepository;
	}

	@Override
	public MessageAnalysisEntity getMostRecentReceivedMessage(String topic, LocalDateTime localDateTime) {
		return messageAnalysisRepository.findMostRecentReceivedMessage(topic, localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth(), localDateTime.getHour());
	}

	@Override
	public int deletePerTopicBeforeTime(LocalDate date) {
		return messageAnalysisRepository.deleteBeforeTime(date);
	}

	@Override
	public int deletePerMinuteBeforeTime(LocalDate date) {
		return analysisRepository.deleteBeforeTime(date);
	}

	@Override
	public Long getRecentKafkaEndOffSet(String topic) {
		Long mostRecentEndOffset = messageAnalysisRepository.findMostRecentOffset(topic);
		return (mostRecentEndOffset != null) ? mostRecentEndOffset : 0;
	}

	@Override
	public DataAnalysisDto viewHourlyByTopics(List<String> topics, int year, int month, int date) {
		HashMap<String, List<Long>> searchMap = new HashMap<>();
		topics.forEach((topic) -> {
			List<TopicMessageHourly> messageAnalysis = messageAnalysisRepository.getMessageDataByTopicAndDate(topic, year, month, date);
			Long[] receivedMessageArr = new Long[HOURS];
			Long[] totalMessageArr = new Long[HOURS];
			Arrays.fill(receivedMessageArr, 0L);
			Arrays.fill(totalMessageArr, 0L);
			List<Long> receivedMessageList = Arrays.asList(receivedMessageArr);
			List<Long> totalMessageList = Arrays.asList(totalMessageArr);
			for (TopicMessageHourly dto : messageAnalysis) {
				if (dto == null) continue;
				if (dto.getAtTime() < 0 || dto.getAtTime() >= HOURS) continue;
				receivedMessageList.set(dto.getAtTime(), dto.getReceivedMessageHourly());
				totalMessageList.set(dto.getAtTime(), dto.getKafkaMessageHourly());
			}
			searchMap.put(topic + PROCESSED, receivedMessageList);
			searchMap.put(topic + KAFKA, totalMessageList);
		});

		return getDataAnalysisDto(searchMap, HOURS, 0, 0, 0);
	}

	@Override
	public DataAnalysisDto getMsgLatencyHourly(List<String> dbNameList, List<String> schmNameList, String division, String topic, LocalDate dateTime) {
		int year = dateTime.getYear();
		int month = dateTime.getMonthValue();
		int date = dateTime.getDayOfMonth();
		HashMap<String, List<Long>> searchMap = new HashMap<>();
		int searchSize = Math.max(dbNameList.size(), schmNameList.size());

		Long[] avgLatencyArr = new Long[HOURS];
		Arrays.fill(avgLatencyArr, 0L);
		List<Long> avgLatencyList = Arrays.asList(avgLatencyArr);

		if (!division.isEmpty()) {
			List<String> topicList = syncRequestService.findTopicListByDivision(division);
			List<MsgLatencyDto> totalLatencyHourly = messageAnalysisRepository.getMsgLatencyHourlyByDivision(topicList, year, month, date);
			for (MsgLatencyDto dto : totalLatencyHourly) {
				if (dto == null) continue;
				if (dto.getAtTime() < 0 || dto.getAtTime() >= HOURS) continue;
				if (dto.getTotalLatency() > 0) {
					avgLatencyList.set(dto.getAtTime(), dto.getTotalLatency() / dto.getSumReceivedMessage());
				} else {
					avgLatencyList.set(dto.getAtTime(), 0L);
				}
			}
			searchMap.put(division + "." + LATENCY, avgLatencyList);
			return getDataAnalysisDto(searchMap, HOURS, 0, 0, 0);
		}

		if (!topic.isEmpty()) {        // Calculate number of messages for a topic
			List<MessageAnalysisEntity> messageAnalysisDtoList =
					messageAnalysisRepository.findMessageAnalysisEntitiesByTopicAndAtYearAndAtMonthAndAtDateAndPerFiveIsFalse(topic, year, month, date);
			for (MessageAnalysisEntity dto : messageAnalysisDtoList) {
				if (dto == null) continue;
				if (dto.getAtHour() < 0 || dto.getAtHour() >= HOURS) continue;
				if (dto.getTotalLatency() > 0) {
					avgLatencyList.set(dto.getAtHour(), dto.getTotalLatency() / dto.getReceivedMessageHourly());
				} else {
					avgLatencyList.set(dto.getAtHour(), 0L);
				}
			}
			searchMap.put(topic + LATENCY, avgLatencyList);
			return getDataAnalysisDto(searchMap, HOURS, 0, 0, 0);
		}
		do {
			avgLatencyArr = new Long[HOURS];
			Arrays.fill(avgLatencyArr, 0L);
			avgLatencyList = Arrays.asList(avgLatencyArr);

			retrieveDataFromSearchList(dbNameList, schmNameList, searchSize);
			List<MsgLatencyDto> msgLatencyHourlyDtos = new ArrayList<>();

			if (schmName.isEmpty() && dbName.isEmpty())
				msgLatencyHourlyDtos = messageAnalysisRepository.getMsgLatencyHourly(year, month, date);
			if (schmName.isEmpty() && !dbName.isEmpty())
				msgLatencyHourlyDtos = messageAnalysisRepository.findMsgLatencyByDatabaseHourly(dbName, year, month, date);
			if (!schmName.isEmpty() && dbName.isEmpty())
				msgLatencyHourlyDtos = messageAnalysisRepository.findMsgLatencyBySchemaHourly(schmName, year, month, date);
			if (!schmName.isEmpty() && !dbName.isEmpty()) {
				schmName = dbName.equals(dbName.toLowerCase(Locale.ROOT)) ? schmName.toLowerCase(Locale.ROOT) : schmName;
				msgLatencyHourlyDtos = messageAnalysisRepository.findMsgLatencyByDatabaseAndSchemaHourly(dbName, schmName, year, month, date);
			}

			for (MsgLatencyDto dto : msgLatencyHourlyDtos) {
				if (dto == null) continue;
				if (dto.getAtTime() < 0 || dto.getAtTime() >= HOURS) continue;
				if (dto.getTotalLatency() <= 0) {
					avgLatencyList.set(dto.getAtTime(), 0L);
				} else {
					avgLatencyList.set(dto.getAtTime(), dto.getTotalLatency() / dto.getSumReceivedMessage());
				}


			}
			dbName = dbName.isEmpty() ? "All_Database" : dbName;
			schmName = schmName.isEmpty() ? "All_Schema" : schmName;
			searchMap.put(dbName + "." + schmName + LATENCY, avgLatencyList);

			dbName = "";
			schmName = "";
			searchSize--;
		} while (searchSize > 0);
		return getDataAnalysisDto(searchMap, HOURS, 0, 0, 0);
	}

	@Override
	public DataAnalysisDto getHourlyAnalysis(List<String> dbNameList, List<String> schmNameList, String division, String topic, LocalDate dateTime) {
		try {
			int year = dateTime.getYear();
			int month = dateTime.getMonthValue();
			int date = dateTime.getDayOfMonth();
			HashMap<String, List<Long>> searchMap = new HashMap<>();
			int searchSize = Math.max(dbNameList.size(), schmNameList.size());

			// Calculating and analyzing data from filter by database names, schema names OR a topic name
			do {
				Long[] receivedMessageArr = new Long[HOURS];
				Long[] totalMessageArr = new Long[HOURS];
				Arrays.fill(receivedMessageArr, 0L);
				Arrays.fill(totalMessageArr, 0L);
				List<Long> receivedMessageList = Arrays.asList(receivedMessageArr);
				List<Long> totalMessageList = Arrays.asList(totalMessageArr);

				if (!division.isEmpty()) {
					List<String> topicList = syncRequestService.findTopicListByDivision(division);
					List<TotalMessageAnalysisDto> totalAnalysisDtoList = messageAnalysisRepository.getTotalMessageAnalysisHourlyByDivision(topicList, year, month, date);
					assignDataIntoResultList(division, searchMap, receivedMessageList, totalMessageList, totalAnalysisDtoList, HOURS);
					break;
				}

				if (!topic.isEmpty()) {        // Calculate number of messages for a topic
					List<MessageAnalysisEntity> messageAnalysisDtoList =
							messageAnalysisRepository.findMessageAnalysisEntitiesByTopicAndAtYearAndAtMonthAndAtDateAndPerFiveIsFalse(topic, year, month, date);
					for (MessageAnalysisEntity dto : messageAnalysisDtoList) {
						if (dto == null) continue;
						if (dto.getAtHour() < 0 || dto.getAtHour() >= HOURS) continue;
						receivedMessageList.set(dto.getAtHour(), dto.getReceivedMessageHourly());
						totalMessageList.set(dto.getAtHour(), dto.getKafkaMessageHourly());
					}
					searchMap.put(topic + PROCESSED, receivedMessageList);
					searchMap.put(topic + KAFKA, totalMessageList);
					break;
				}

				retrieveDataFromSearchList(dbNameList, schmNameList, searchSize);
				List<TotalMessageAnalysisDto> totalAnalysisDtoList = new ArrayList<>();

				if (schmName.isEmpty() && dbName.isEmpty())
					totalAnalysisDtoList = messageAnalysisRepository.getTotalMessageAnalysisHourly(year, month, date);
				if (schmName.isEmpty() && !dbName.isEmpty())
					totalAnalysisDtoList = messageAnalysisRepository.findMessageAnalysisByDatabaseHourly(dbName, year, month, date);
				if (!schmName.isEmpty() && dbName.isEmpty())
					totalAnalysisDtoList = messageAnalysisRepository.findMessageAnalysisBySchemaHourly(schmName, year, month, date);
				if (!schmName.isEmpty() && !dbName.isEmpty()) {
					schmName = dbName.equals(dbName.toLowerCase(Locale.ROOT)) ? schmName.toLowerCase(Locale.ROOT) : schmName;
					totalAnalysisDtoList = messageAnalysisRepository.findMessageAnalysisByDatabaseAndSchemaHourly(dbName, schmName, year, month, date);
				}

				searchSize = assignDataIntoResultByDbSchema(searchMap, searchSize, receivedMessageList, totalMessageList, totalAnalysisDtoList, HOURS);
			} while (searchSize > 0);
			return getDataAnalysisDto(searchMap, HOURS, 0, 0, 0);
		} catch (Exception e) {
			logger.warn("Error occurs when retrieving data for message's analysis - {}", e.getMessage());
			return null;
		}
	}

	private int assignDataIntoResultByDbSchema(HashMap<String, List<Long>> searchMap, int searchSize, List<Long> receivedMessageList, List<Long> totalMessageList, List<TotalMessageAnalysisDto> totalAnalysisDtoList, int hourOrDay) {
		assignData(receivedMessageList, totalMessageList, totalAnalysisDtoList, hourOrDay);

		dbName = dbName.isEmpty() ? "All_Database" : dbName;
		schmName = schmName.isEmpty() ? "All_Schema" : schmName;
		searchMap.put(dbName + "." + schmName + PROCESSED, receivedMessageList);
		searchMap.put(dbName + "." + schmName + KAFKA, totalMessageList);

		dbName = "";
		schmName = "";
		searchSize--;
		return searchSize;
	}

	private void retrieveDataFromSearchList(List<String> dbNameList, List<String> schmNameList, int searchSize) {
		if (searchSize != 0) {
			if (schmNameList.isEmpty() && !dbNameList.isEmpty()) {
				dbName = dbNameList.get(searchSize - 1);
			} else if (dbNameList.isEmpty() && !schmNameList.isEmpty()) {
				schmName = schmNameList.get(searchSize - 1);
			} else {
				dbName = (dbNameList.size() > schmNameList.size()) ? dbNameList.get(searchSize - 1) : dbNameList.get(0);
				schmName = (schmNameList.size() > dbNameList.size()) ? schmNameList.get(searchSize - 1) : schmNameList.get(0);
			}
		}
	}

	@Override
	public DataAnalysisDto getMsgLatencyDaily(List<String> dbNameList, List<String> schmNameList, String division, String topic, LocalDate dateTime) {
		try {
			int year = dateTime.getYear();
			int month = dateTime.getMonthValue();
			HashMap<String, List<Long>> searchMap = new HashMap<>();
			int searchSize = Math.max(dbNameList.size(), schmNameList.size());

			do {
				Long[] receivedMessageArr = new Long[DAYS + 1];
				Long[] totalMessageArr = new Long[DAYS + 1];
				Arrays.fill(receivedMessageArr, 0L);
				Arrays.fill(totalMessageArr, 0L);
				List<Long> avgMsLatencyList = Arrays.asList(receivedMessageArr);

				if (!division.isEmpty()) {
					List<String> topicList = syncRequestService.findTopicListByDivision(division);
					List<MsgLatencyDto> msgLatencyDto = messageAnalysisRepository.getMsgLatencyDailyByDivision(topicList, year, month);
					for (MsgLatencyDto dto : msgLatencyDto) {
						if (dto == null) continue;
						if (dto.getAtTime() < 0 || dto.getAtTime() >= DAYS) continue;
						avgMsLatencyList.set(dto.getAtTime(), dto.getTotalLatency() / dto.getSumReceivedMessage());
						searchMap.put(division + "." + LATENCY, avgMsLatencyList);
					}
					break;
				}

				if (!topic.isEmpty()) {        // Calculate number of messages for a topic
					List<MessageAnalysisEntity> messageAnalysisDtoList =
							messageAnalysisRepository.findMessageAnalysisEntitiesByTopicAndAtYearAndAtMonthAndPerFiveIsFalse(topic, year, month);
					for (MessageAnalysisEntity dto : messageAnalysisDtoList) {
						if (dto == null) continue;
						if (dto.getAtHour() < 1 || dto.getAtHour() >= DAYS + 1) continue;
						avgMsLatencyList.set(dto.getAtHour(), dto.getTotalLatency() / dto.getReceivedMessageHourly());
					}
					searchMap.put(topic + LATENCY, avgMsLatencyList);
					break;
				}

				retrieveDataFromSearchList(dbNameList, schmNameList, searchSize);
				List<MsgLatencyDto> msgLatencyDtoList = new ArrayList<>();

				if (schmName.isEmpty() && dbName.isEmpty())
					msgLatencyDtoList = messageAnalysisRepository.getMsgLatencyDaily(year, month);
				if (schmName.isEmpty() && !dbName.isEmpty())
					msgLatencyDtoList = messageAnalysisRepository.getMsgLatencyByDatabaseDaily(dbName, year, month);
				if (!schmName.isEmpty() && dbName.isEmpty())
					msgLatencyDtoList = messageAnalysisRepository.getMsgLatencyBySchemaDaily(schmName, year, month);
				if (!schmName.isEmpty() && !dbName.isEmpty()) {
					schmName = dbName.equals(dbName.toLowerCase(Locale.ROOT)) ? schmName.toLowerCase(Locale.ROOT) : schmName;
					msgLatencyDtoList = messageAnalysisRepository.getMsgLatencyByDatabaseAndSchemaDaily(dbName, schmName, year, month);
				}
				for (MsgLatencyDto dto : msgLatencyDtoList) {
					if (dto == null) continue;
					if (dto.getAtTime() < 0 || dto.getAtTime() >= DAYS) continue;
					if (dto.getTotalLatency() <= 0) {
						avgMsLatencyList.set(dto.getAtTime(), 0L);
					} else {
						avgMsLatencyList.set(dto.getAtTime(), dto.getTotalLatency() / dto.getSumReceivedMessage());
					}

				}

				dbName = dbName.isEmpty() ? "All_Database" : dbName;
				schmName = schmName.isEmpty() ? "All_Schema" : schmName;
				searchMap.put(dbName + "." + schmName + LATENCY, avgMsLatencyList);

				dbName = "";
				schmName = "";
				searchSize--;
			} while (searchSize > 0);
			return getDataAnalysisDto(searchMap, DAYS, 0, 0, 0);
		} catch (Exception e) {
			logger.warn("Error occurs when retreiving data for message's analysis - {}", e.getMessage());
			return null;
		}
	}


	@Override
	public DataAnalysisDto getDailyAnalysis(List<String> dbNameList, List<String> schmNameList, String division, String topic, LocalDate dateTime) {
		try {
			int year = dateTime.getYear();
			int month = dateTime.getMonthValue();
			HashMap<String, List<Long>> searchMap = new HashMap<>();
			int searchSize = Math.max(dbNameList.size(), schmNameList.size());

			do {
				Long[] receivedMessageArr = new Long[DAYS + 1];
				Long[] totalMessageArr = new Long[DAYS + 1];
				Arrays.fill(receivedMessageArr, 0L);
				Arrays.fill(totalMessageArr, 0L);
				List<Long> receivedMessageList = Arrays.asList(receivedMessageArr);
				List<Long> totalMessageList = Arrays.asList(totalMessageArr);

				if (!division.isEmpty()) {
					List<String> topicList = syncRequestService.findTopicListByDivision(division);
					List<TotalMessageAnalysisDto> totalAnalysisDtoList = messageAnalysisRepository.getTotalMessageAnalysisDailyByDivision(topicList, year, month);
					assignDataIntoResultList(division, searchMap, receivedMessageList, totalMessageList, totalAnalysisDtoList, DAYS);
					break;
				}

				if (!topic.isEmpty()) {        // Calculate number of messages for a topic
					List<MessageAnalysisEntity> messageAnalysisDtoList =
							messageAnalysisRepository.findMessageAnalysisEntitiesByTopicAndAtYearAndAtMonthAndPerFiveIsFalse(topic, year, month);
					for (MessageAnalysisEntity dto : messageAnalysisDtoList) {
						if (dto == null) continue;
						if (dto.getAtHour() < 1 || dto.getAtHour() >= DAYS + 1) continue;
						receivedMessageList.set(dto.getAtHour(), dto.getReceivedMessageHourly());
						totalMessageList.set(dto.getAtHour(), dto.getKafkaMessageHourly());
					}
					searchMap.put(topic + PROCESSED, receivedMessageList);
					searchMap.put(topic + KAFKA, totalMessageList);
					break;
				}

				retrieveDataFromSearchList(dbNameList, schmNameList, searchSize);
				List<TotalMessageAnalysisDto> totalAnalysisDtoList = new ArrayList<>();

				if (schmName.isEmpty() && dbName.isEmpty())
					totalAnalysisDtoList = messageAnalysisRepository.getTotalMessageAnalysisDaily(year, month);
				if (schmName.isEmpty() && !dbName.isEmpty())
					totalAnalysisDtoList = messageAnalysisRepository.findMessageAnalysisByDatabaseDaily(dbName, year, month);
				if (!schmName.isEmpty() && dbName.isEmpty())
					totalAnalysisDtoList = messageAnalysisRepository.findMessageAnalysisBySchemaDaily(schmName, year, month);
				if (!schmName.isEmpty() && !dbName.isEmpty()) {
					schmName = dbName.equals(dbName.toLowerCase(Locale.ROOT)) ? schmName.toLowerCase(Locale.ROOT) : schmName;
					totalAnalysisDtoList = messageAnalysisRepository.findMessageAnalysisByDatabaseAndSchemaDaily(dbName, schmName, year, month);
				}

				searchSize = assignDataIntoResultByDbSchema(searchMap, searchSize, receivedMessageList, totalMessageList, totalAnalysisDtoList, DAYS);
			} while (searchSize > 0);
			return getDataAnalysisDto(searchMap, DAYS, 0, 0, 0);
		} catch (Exception e) {
			logger.warn("Error occurs when retreiving data for message's analysis - {}", e.getMessage());
			return null;
		}
	}

	private void assignDataIntoResultList(String division, HashMap<String, List<Long>> searchMap, List<Long> receivedMessageList, List<Long> totalMessageList, List<TotalMessageAnalysisDto> totalAnalysisDtoList, int hourOrDay) {
		assignData(receivedMessageList, totalMessageList, totalAnalysisDtoList, hourOrDay);
		searchMap.put(division + "." + PROCESSED, receivedMessageList);
		searchMap.put(division + "." + KAFKA, totalMessageList);
	}

	private void assignData(List<Long> receivedMessageList, List<Long> totalMessageList, List<TotalMessageAnalysisDto> totalAnalysisDtoList, int hourOrDay) {
		for (TotalMessageAnalysisDto dto : totalAnalysisDtoList) {
			if (dto == null) continue;
			if (dto.getAtTime() < 0 || dto.getAtTime() >= hourOrDay) continue;
			receivedMessageList.set(dto.getAtTime(), dto.getSumReceivedMessage());
			totalMessageList.set(dto.getAtTime(), dto.getSumTotalMessage());
		}
	}

	@Override
	public DataAnalysisDto getMsgLatencyByMinutely(List<String> dbNameList, List<String> schmNameList, String division, String topic, LocalDate dateTime, int fromHour, int toHour, int type) {
		try {
			HashMap<String, List<Long>> searchMap = new HashMap<>();

			int year = dateTime.getYear();
			int month = dateTime.getMonthValue();
			int date = dateTime.getDayOfMonth();

			Long[] receivedMessageArr = new Long[MINUTES * (toHour - fromHour) / type + 1];
			Arrays.fill(receivedMessageArr, 0L);
			List<Long> receivedMessageList = Arrays.asList(receivedMessageArr);
			Long[] kafkaMessageArr = new Long[MINUTES * (toHour - fromHour) / type + 1];
			Arrays.fill(kafkaMessageArr, 0L);

			if (!topic.isEmpty()) {        // Calculate number of messages for a topic
				List<MsgLatencyPerMinuteDto> totalLatencyDtoList = analysisRepository.getMessageLatencyByTopicAndDateTime(topic, year, month, date, fromHour, toHour);
				analyzeLatencyMinute(type, receivedMessageList, totalLatencyDtoList, fromHour);
				searchMap.put(division + "." + LATENCY, receivedMessageList);
				return getDataAnalysisDto(searchMap, MINUTES, fromHour, toHour, type);
			}

			if (!division.isEmpty()) {
				List<String> topicList = syncRequestService.findTopicListByDivision(division);
				List<MsgLatencyPerMinuteDto> totalLatencyDtoList = analysisRepository.getTotalMessageLatencyAnalysisMinutelyByDivision(topicList, year, month, date, fromHour, toHour);
				analyzeLatencyMinute(type, receivedMessageList, totalLatencyDtoList, fromHour);
				searchMap.put(division + "." + LATENCY, receivedMessageList);
				return getDataAnalysisDto(searchMap, MINUTES, fromHour, toHour, type);
			}

			int searchSize = Math.max(dbNameList.size(), schmNameList.size());
			do {
				retrieveDataFromSearchList(dbNameList, schmNameList, searchSize);
				List<MsgLatencyPerMinuteDto> totalAnalysisDtoList = new ArrayList<>();

				if (schmName.isEmpty() && dbName.isEmpty()) {
					totalAnalysisDtoList = analysisRepository.getTotalMessageLatencyMinutely(year, month, date, fromHour, toHour);
				}
				if (schmName.isEmpty() && !dbName.isEmpty()) {
					totalAnalysisDtoList = analysisRepository.getMessageLatencyByDatabaseAndDateTime(dbName, year, month, date, fromHour, toHour);
				}
				if (!schmName.isEmpty() && dbName.isEmpty()) {
					totalAnalysisDtoList = analysisRepository.getMessageLatencyBySchemaAndDateTime(schmName.toLowerCase(), year, month, date, fromHour, toHour);
				}
				if (!schmName.isEmpty() && !dbName.isEmpty()) {
					schmName = dbName.equals(dbName.toLowerCase(Locale.ROOT)) ? schmName.toLowerCase(Locale.ROOT) : schmName;
					totalAnalysisDtoList = analysisRepository.getMessageLatencyByDatabaseAndSchemaAndDateTime(dbName, schmName, year, month, date, fromHour, toHour);
				}

				analyzeLatencyMinute(type, receivedMessageList, totalAnalysisDtoList, fromHour);
				dbName = dbName.isEmpty() ? "All_Database" : dbName;
				schmName = schmName.isEmpty() ? "All_Schema" : schmName;
				searchMap.put(dbName + "." + schmName + LATENCY, receivedMessageList);

				dbName = "";
				schmName = "";
				searchSize--;
			} while (searchSize > 0);
			return getDataAnalysisDto(searchMap, MINUTES, fromHour, toHour, type);
		} catch (Exception e) {
			logger.warn("Error occurs when retrieving data for message's analysis - {}", e.getMessage());
			return null;
		}
	}

	@Override
	public DataAnalysisDto getMinutelyAnalysis(List<String> dbNameList, List<String> schmNameList, String division, String topic, LocalDate dateTime, int fromHour, int toHour, int type) {
		try {
			int year = dateTime.getYear();
			int month = dateTime.getMonthValue();
			int date = dateTime.getDayOfMonth();
			HashMap<String, List<Long>> searchMap = new HashMap<>();
			int searchSize = Math.max(dbNameList.size(), schmNameList.size());
			do {
				Long[] receivedMessageArr = new Long[MINUTES * (toHour - fromHour) / type + 1];
				Arrays.fill(receivedMessageArr, 0L);
				List<Long> receivedMessageList = Arrays.asList(receivedMessageArr);
				Long[] kafkaMessageArr = new Long[MINUTES * (toHour - fromHour) / type + 1];
				Arrays.fill(kafkaMessageArr, 0L);
				List<Long> kafkaMessageList = Arrays.asList(kafkaMessageArr);

				if (!division.isEmpty()) {
					List<String> topicList = syncRequestService.findTopicListByDivision(division);
					List<ProcessedMessagePerMinuteDto> totalAnalysisDtoList = analysisRepository.getTotalMessageAnalysisMinutelyByDivision(topicList, year, month, date, fromHour, toHour);
					List<ProcessedMessagePerMinuteDto> kafkaMessagePerFiveList = messageAnalysisRepository.getKafkaEndOffsetPerFiveMinuteByDivision(topicList, year, month, date, fromHour, toHour);
					analyzeAndExtractMinuteResult(type, receivedMessageList, 0, 0, 0L, totalAnalysisDtoList, fromHour);
					searchMap.put(division + "." + PROCESSED, receivedMessageList);
					if (type != ONE) {
						analyzeAndExtractKafkaOffsetResult(type, kafkaMessageList, 0, 0, 0L, kafkaMessagePerFiveList, fromHour);
						searchMap.put(division + "." + KAFKA, kafkaMessageList);
					}
					break;
				}

				if (!topic.isEmpty()) {        // Calculate number of messages for a topic
					List<ProcessedMessagePerMinuteDto> messageAnalysisDtoList =
							analysisRepository.findAdvancedMessageAnalysisByTopicAndDateTime(topic, year, month, date, fromHour, toHour);
					List<ProcessedMessagePerMinuteDto> kafkaMessagePerFiveList = messageAnalysisRepository.findKafkaEndOffsetPerFiveByTopicAndDateTime(topic, year, month, date, fromHour, toHour);
					analyzeAndExtractMinuteResult(type, receivedMessageList, 0, 0, 0L, messageAnalysisDtoList, fromHour);
					searchMap.put(topic + PROCESSED, receivedMessageList);
					if (type != ONE) {
						analyzeAndExtractKafkaOffsetResult(type, kafkaMessageList, 0, 0, 0L, kafkaMessagePerFiveList, fromHour);
						searchMap.put(topic + "." + KAFKA, kafkaMessageList);
					}
					break;
				}

				retrieveDataFromSearchList(dbNameList, schmNameList, searchSize);
				List<ProcessedMessagePerMinuteDto> totalAnalysisDtoList = new ArrayList<>();
				List<ProcessedMessagePerMinuteDto> kafkaMessagePerFiveList = new ArrayList<>();

				if (schmName.isEmpty() && dbName.isEmpty()) {
					totalAnalysisDtoList = analysisRepository.getTotalMessageAnalysisMinutely(year, month, date, fromHour, toHour);
					kafkaMessagePerFiveList = messageAnalysisRepository.getTotalKafkaEndOffsetPerFive(year, month, date, fromHour, toHour);
				}
				if (schmName.isEmpty() && !dbName.isEmpty()) {
					totalAnalysisDtoList = analysisRepository.findAdvancedMessageAnalysisByDatabaseAndDateTime(dbName, year, month, date, fromHour, toHour);
					kafkaMessagePerFiveList = messageAnalysisRepository.findKafkaEndOffsetPerFiveByDatabaseAndDateTime(dbName, year, month, date, fromHour, toHour);
				}
				if (!schmName.isEmpty() && dbName.isEmpty()) {
					totalAnalysisDtoList = analysisRepository.findAdvancedMessageAnalysisBySchemaAndDateTime(schmName, year, month, date, fromHour, toHour);
					kafkaMessagePerFiveList = messageAnalysisRepository.findKafkaEndOffsetPerFiveBySchemaAndDateTime(schmName, year, month, date, fromHour, toHour);
				}
				if (!schmName.isEmpty() && !dbName.isEmpty()) {
					schmName = dbName.equals(dbName.toLowerCase(Locale.ROOT)) ? schmName.toLowerCase(Locale.ROOT) : schmName;
					totalAnalysisDtoList = analysisRepository.findAdvancedMessageAnalysisByDatabaseAndSchemaAndDateTime(dbName, schmName, year, month, date, fromHour, toHour);
					kafkaMessagePerFiveList = messageAnalysisRepository.findKafkaEndOffsetPerFiveByDatabaseAndSchemaAndDateTime(dbName, schmName, year, month, date, fromHour, toHour);
				}

				analyzeAndExtractMinuteResult(type, receivedMessageList, 0, 0, 0L, totalAnalysisDtoList, fromHour);
				dbName = dbName.isEmpty() ? "All_Database" : dbName;
				schmName = schmName.isEmpty() ? "All_Schema" : schmName;
				searchMap.put(dbName + "." + schmName + PROCESSED, receivedMessageList);
				if (type != ONE) {
					analyzeAndExtractKafkaOffsetResult(type, kafkaMessageList, 0, 0, 0L, kafkaMessagePerFiveList, fromHour);
					searchMap.put(dbName + "." + schmName + KAFKA, kafkaMessageList);
				}
				dbName = "";
				schmName = "";
				searchSize--;
			} while (searchSize > 0);
			return getDataAnalysisDto(searchMap, MINUTES, fromHour, toHour, type);
		} catch (Exception e) {
			logger.warn("Error occurs when retrieving data for message's analysis - {}", e.getMessage());
			return null;
		}
	}

	private void analyzeLatencyMinute(int type, List<Long> latencyList, List<MsgLatencyPerMinuteDto> latencyPerMinuteDtoList, int fromHour) {
		long pos = 0;
		int index = 0;
		long totalLatency = 0;
		long receivedMessage = 0;
		for (MsgLatencyPerMinuteDto dto : latencyPerMinuteDtoList) {
			if (dto == null) continue;
			int atMinute = dto.getAtMinute() + (dto.getAtHour() - fromHour) * 60;
			if (type == ONE) {
				latencyList.set(atMinute, dto.getTotalLatency() / dto.getReceivedMessage());
				continue;
			}
			while (pos < atMinute) {
				switch (type) {
					case FIVE:
						if (pos % FIVE == 0) {
							if (totalLatency > 0) {
								latencyList.set(index, totalLatency / receivedMessage);
							} else {
								latencyList.set(index, 0L);
							}
							receivedMessage = 0L;
							totalLatency = 0L;
							index++;
						}
						pos++;
						break;
					case FIFTEEN:
						if (pos % FIFTEEN == 0) {
							if (totalLatency > 0) {
								latencyList.set(index, totalLatency / receivedMessage);
							} else {
								latencyList.set(index, 0L);
							}
							receivedMessage = 0L;
							totalLatency = 0L;
							index++;
						}
						pos++;
						break;
					case THIRTY:
						if (pos % THIRTY == 0) {
							if (totalLatency > 0) {
								latencyList.set(index, totalLatency / receivedMessage);
							} else {
								latencyList.set(index, 0L);
							}
							receivedMessage = 0L;
							totalLatency = 0L;
							index++;
						}
						pos++;
						break;
					default:
						throw new RuntimeException();
				}
			}

			receivedMessage += dto.getReceivedMessage();
			totalLatency += dto.getTotalLatency();
			if (latencyPerMinuteDtoList.size() - 1 == latencyPerMinuteDtoList.lastIndexOf(dto)) {
				latencyList.set(index, totalLatency / receivedMessage);
			}
		}
	}

	private void analyzeAndExtractMinuteResult(int type, List<Long> receivedMessageList, int pos, int index, long receivedMessage, List<ProcessedMessagePerMinuteDto> messageAnalysisDtoList, int fromHour) {
		for (ProcessedMessagePerMinuteDto dto : messageAnalysisDtoList) {
			if (dto == null) continue;
			int atMinute = dto.getAtMinute() + (dto.getAtHour() - fromHour) * 60;
			if (type == ONE) {
				receivedMessageList.set(atMinute, dto.getReceivedMessage());
				continue;
			}
			switch (type) {
				case FIVE:
					while (pos < atMinute) {
						if (pos % FIVE == 0) {
							receivedMessageList.set(index, receivedMessage);
							receivedMessage = 0L;
							index++;
						}
						pos++;
					}
					break;
				case FIFTEEN:
					while (pos < atMinute) {
						if (pos % FIFTEEN == 0) {
							receivedMessageList.set(index, receivedMessage);
							receivedMessage = 0L;
							index++;
						}
						pos++;
					}
					break;
				case THIRTY:
					while (pos < atMinute) {
						if (pos % THIRTY == 0) {
							receivedMessageList.set(index, receivedMessage);
							receivedMessage = 0L;
							index++;
						}
						pos++;
					}
					break;
				default:
					throw new RuntimeException();
			}
			receivedMessage += dto.getReceivedMessage();
			if (messageAnalysisDtoList.size() - 1 == messageAnalysisDtoList.lastIndexOf(dto))
				receivedMessageList.set(index, receivedMessage);
		}
	}

	private void analyzeAndExtractKafkaOffsetResult(int type, List<Long> kafkaMessageList, int pos, int index, long kafkaMessage, List<ProcessedMessagePerMinuteDto> kafkaMessagePerFiveList, int fromHour) {
		for (ProcessedMessagePerMinuteDto dto : kafkaMessagePerFiveList) {
			if (dto == null) continue;
			int atMinute = (dto.getAtMinute() + (dto.getAtHour() - fromHour) * 60) / 5;
			if (type == FIVE) {
				kafkaMessageList.set(atMinute, dto.getReceivedMessage());
				continue;
			}
			switch (type) {
				case FIFTEEN:
					while (pos < atMinute) {
						if (pos % (FIFTEEN / FIVE) == 0) {
							kafkaMessageList.set(index, kafkaMessage);
							kafkaMessage = 0L;
							index++;
						}
						pos++;
					}
					break;
				case THIRTY:
					while (pos < atMinute) {
						if (pos % (THIRTY / FIVE) == 0) {
							kafkaMessageList.set(index, kafkaMessage);
							kafkaMessage = 0L;
							index++;
						}
						pos++;
					}
					break;
				default:
					throw new RuntimeException();
			}
			kafkaMessage += dto.getReceivedMessage();
			if (kafkaMessagePerFiveList.size() - 1 == kafkaMessagePerFiveList.lastIndexOf(dto))
				kafkaMessageList.set(index, kafkaMessage);
		}
	}

	private DataAnalysisDto getDataAnalysisDto(HashMap<String, List<Long>> searchMap, int byHourOrMinute, int fromHour, int toHour, int type) {
		long min = 0L, max = 0L;
		List<MessageDataForYAxisGraphDto> messageDataForYAxisGraphDtoList = new ArrayList<>();
		for (Map.Entry<String, List<Long>> cursor : searchMap.entrySet()) {
			messageDataForYAxisGraphDtoList.add(new MessageDataForYAxisGraphDto(cursor.getKey(), cursor.getValue()));
			long tempMin = cursor.getValue().stream().min(Long::compareTo).get();
			long tempMax = cursor.getValue().stream().max(Long::compareTo).get();
			min = Math.min(min, tempMin);
			max = Math.max(max, tempMax);
		}
		List<String> xAxis = new ArrayList<>();
		if (byHourOrMinute == DAYS) {
			for (int i = 0; i < byHourOrMinute + 1; i++) {
				xAxis.add((i < 10) ? ('0' + String.valueOf(i)) : String.valueOf((i)));
			}
		} else if (byHourOrMinute == HOURS) {
			for (int i = 0; i < byHourOrMinute; i++) {
				xAxis.add((i < 10) ? ('0' + String.valueOf(i) + ":00") : (i + ":00"));
			}
		} else {
			while (fromHour <= toHour) {
				for (int i = 0; i < byHourOrMinute; i++) {
					String s = (i < 10) ? '0' + String.valueOf(i) : String.valueOf(i);
					String e = (fromHour < 10) ? '0' + String.valueOf(fromHour) + ':' + s : String.valueOf(fromHour) + ':' + s;
					if (type == ONE) {
						xAxis.add(e);
					} else if (type == FIVE) {
						if (i % FIVE == 0) {
							xAxis.add(e);
						}
					} else if (type == FIFTEEN) {
						if (i % FIFTEEN == 0) {
							xAxis.add(e);
						}
					} else {
						if (i % THIRTY == 0) {
							xAxis.add(e);
						}
					}
				}
				fromHour++;
			}
		}
		DataAnalysisDto dataAnalysisDto = new DataAnalysisDto();
		dataAnalysisDto.setMessageDataForYAxisGraphDtoList(messageDataForYAxisGraphDtoList);
		dataAnalysisDto.setMessageDataForXAxisGraphDtoList(new MessageDataForXAxisGraphDto(xAxis));
		dataAnalysisDto.setMin(min);
		dataAnalysisDto.setMax(max);

		return dataAnalysisDto;
	}
}
