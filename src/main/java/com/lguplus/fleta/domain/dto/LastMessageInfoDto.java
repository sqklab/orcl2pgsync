package com.lguplus.fleta.domain.dto;

import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;


public interface LastMessageInfoDto {

	@Value("#{target.topic}")
	String getTopic();

	@Value("#{target.receivedDateTime}")
	LocalDateTime getReceivedDateTime();

	@Value("#{target.scn}")
	Long getScn();

	@Value("#{target.commitScn}")
	Long getCommitScn();

	@Value("#{target.msgTimestamp}")
	Long getMsgTimestamp();
}
