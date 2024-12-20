package com.lguplus.fleta.ports.service;

import com.lguplus.fleta.domain.dto.ColumnCompare;
import com.lguplus.fleta.domain.dto.SlackErrorMessage;
import com.lguplus.fleta.domain.dto.SlackMessage;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public interface SlackService {
	ChatPostMessageResponse send(SlackMessage message) throws Exception;

	ChatPostMessageResponse send(SlackErrorMessage message) throws Exception;

	ChatPostMessageResponse send(ColumnComparisonMessage message) throws Exception;

	ChatPostMessageResponse send(InfiniteLoopMessage message) throws Exception;

	@Builder
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	class ColumnComparisonMessage {
		String topic;
		String errorMessage;
		LocalDate compareDate;
		LocalTime compareTime;
		String sourceDatabase;
		String sourceSchema;
		String sourceTable;
		String targetDatabase;
		String targetSchema;
		String targetTable;
		@Builder.Default
		List<ColumnCompare> diffColumns = new ArrayList<>();


		public String getDiffString() {
			if (null == diffColumns) return "";
			return diffColumns.stream().map(ColumnCompare::getNotBlankColumn).collect(Collectors.joining(", "));
		}

		public String getSourceTbl() {
			return String.format("%s.%s.%s", sourceDatabase, sourceSchema, sourceTable);
		}

		public String getTargetTbl() {
			return String.format("%s.%s.%s", targetDatabase, targetSchema, targetTable);
		}
	}

	@Builder
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	class InfiniteLoopMessage{
		String errorMessage;
		String topic;
		LocalTime compareTime;
	}
}
