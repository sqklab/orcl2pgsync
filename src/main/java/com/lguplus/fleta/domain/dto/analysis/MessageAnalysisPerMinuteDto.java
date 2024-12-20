package com.lguplus.fleta.domain.dto.analysis;

import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalTime;

public interface MessageAnalysisPerMinuteDto {

	@Value("#{target.atDate}")
	LocalDate getAtDate();

	@Value("#{target.atTime}")
	LocalTime getAtTime();

	@Value("#{target.receivedMessage}")
	Long getReceivedMessage();

	@Value("#{target.totalLatency}")
	Long getTotalLatency();
}
