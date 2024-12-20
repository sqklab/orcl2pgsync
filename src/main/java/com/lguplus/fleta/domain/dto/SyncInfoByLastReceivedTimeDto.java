package com.lguplus.fleta.domain.dto;

import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalTime;

public interface SyncInfoByLastReceivedTimeDto extends SyncInfoDto {
	@Value("#{target.scn}")
	Long getScn();

	@Value("#{target.commit_scn}")
	Long getCommitScn();

	@Value("#{target.msg_timestamp}")
	Long getMsgTimestamp();

	@Value("#{target.received_date}")
	LocalDate getReceivedDate();

	@Value("#{target.received_time}")
	LocalTime getReceivedTime();

	@Value("#{target.received_date_time}")
	String getReceivedDateTime();
}
