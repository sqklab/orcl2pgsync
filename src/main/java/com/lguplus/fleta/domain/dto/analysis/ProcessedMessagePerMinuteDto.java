package com.lguplus.fleta.domain.dto.analysis;

import org.springframework.beans.factory.annotation.Value;

public interface ProcessedMessagePerMinuteDto {

	@Value("#{target.atHour}")
	Integer getAtHour();

	@Value("#{target.atMinute}")
	Integer getAtMinute();

	@Value("#{target.receivedMessage}")
	Long getReceivedMessage();
}
