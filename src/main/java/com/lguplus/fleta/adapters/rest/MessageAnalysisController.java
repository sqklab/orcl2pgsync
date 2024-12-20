package com.lguplus.fleta.adapters.rest;

import com.lguplus.fleta.domain.dto.analysis.DataAnalysisDto;
import com.lguplus.fleta.domain.dto.analysis.ChartType;
import com.lguplus.fleta.ports.service.MessageAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dbsync/analysis")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MessageAnalysisController {

	private final MessageAnalysisService messageAnalysisService;

	public MessageAnalysisController(MessageAnalysisService messageAnalysisService) {
		this.messageAnalysisService = messageAnalysisService;
	}

	@GetMapping("/viewDailyGraph")
	public ResponseEntity<DataAnalysisDto> viewDailyGraph(@RequestParam("dbName") List<String> dbName,
														  @RequestParam("schmName") List<String> schmName,
														  @RequestParam("division") String division,
														  @RequestParam("topic") String topic,
														  @RequestParam("year") int year,
														  @RequestParam("month") int month,
														  @RequestParam("date") int date,
														  @RequestParam("chartType") String chatType) {
		LocalDate dateTime = LocalDate.of(year, month, date);
		DataAnalysisDto dataAnalysisDto;
		if (chatType.equals(ChartType.MESSAGE_LATENCY.name())) {
			dataAnalysisDto = messageAnalysisService.getMsgLatencyDaily(dbName, schmName, division, topic, dateTime);
		} else {
			dataAnalysisDto = messageAnalysisService.getDailyAnalysis(dbName, schmName, division, topic, dateTime);
		}
		return ResponseEntity.ok(dataAnalysisDto);
	}

	@GetMapping("/viewHourlyGraph")
	public ResponseEntity<DataAnalysisDto> viewHourlyGraph(@RequestParam("dbName") List<String> dbName,
														   @RequestParam("schmName") List<String> schmName,
														   @RequestParam("division") String division,
														   @RequestParam("topic") String topic,
														   @RequestParam("year") int year,
														   @RequestParam("month") int month,
														   @RequestParam("date") int date,
														   @RequestParam("chartType") String chatType) {
		LocalDate dateTime = LocalDate.of(year, month, date);
		DataAnalysisDto dataAnalysisDto;
		if (chatType.equals(ChartType.MESSAGE_LATENCY.name())) {
			dataAnalysisDto = messageAnalysisService.getMsgLatencyHourly(dbName, schmName, division, topic, dateTime);
		} else {
			dataAnalysisDto = messageAnalysisService.getHourlyAnalysis(dbName, schmName, division, topic, dateTime);
		}

		return ResponseEntity.ok(dataAnalysisDto);
	}

	@GetMapping("/viewMinutelyGraph")
	public ResponseEntity<DataAnalysisDto> viewHourlyGraph(@RequestParam("dbName") List<String> dbName,
														   @RequestParam("schmName") List<String> schmName,
														   @RequestParam("topic") String topic,
														   @RequestParam("division") String division,
														   @RequestParam("year") int year,
														   @RequestParam("month") int month,
														   @RequestParam("date") int date,
														   @RequestParam("fromHour") int fromHour,
														   @RequestParam("toHour") int toHour,
														   @RequestParam("type") int type,
														   @RequestParam("chartType") String chatType) {
		LocalDate dateTime = LocalDate.of(year, month, date);
		DataAnalysisDto dataAnalysisDto;
		if (chatType.equals(ChartType.MESSAGE_LATENCY.name())) {
			 dataAnalysisDto = messageAnalysisService.getMsgLatencyByMinutely(dbName, schmName, division, topic, dateTime, fromHour, toHour, type);
		} else {
			dataAnalysisDto = messageAnalysisService.getMinutelyAnalysis(dbName, schmName, division, topic, dateTime, fromHour, toHour, type);
		}

		return ResponseEntity.ok(dataAnalysisDto);
	}

	@GetMapping("/viewTopicHourly")
	public ResponseEntity<DataAnalysisDto> viewTopicHourly(@RequestParam("topicNames") List<String> topicNames,
														   @RequestParam("year") int year,
														   @RequestParam("month") int month,
														   @RequestParam("date") int date) {
		return ResponseEntity.ok(messageAnalysisService.viewHourlyByTopics(topicNames, year, month, date));
	}
}
