package com.lguplus.fleta.domain.dto.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewKafkaConsumerGroup {
	private String topic;
	private Long msgBehind;
	private LocalDate receivedDate;
	private LocalTime receivedTime;
}
