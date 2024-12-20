package com.lguplus.fleta.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class SlackErrorMessage {
	LocalDate compareDate;
	LocalTime compareTime;
	String sql;
	String topic;
	String errorMessage;

	public SlackErrorMessage(LocalDate compareDate, LocalTime compareTime, String sql, String topic, String errorMessage) {
		this.compareDate = compareDate;
		this.compareTime = compareTime;
		this.sql = sql;
		this.topic = topic;
		this.errorMessage = errorMessage;
	}
}
