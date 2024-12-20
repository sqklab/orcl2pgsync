package com.lguplus.fleta.domain.dto;

import org.springframework.beans.factory.annotation.Value;

public interface SyncStateCountDto {

	@Value("#{target.pending}")
	Integer getPending();

	@Value("#{target.running}")
	Integer getRunning();

	@Value("#{target.stopped}")
	Integer getStopped();

	@Value("#{target.linked}")
	Integer getLinked();
}
