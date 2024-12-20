package com.lguplus.fleta.domain.dto;

import org.springframework.beans.factory.annotation.Value;

public interface SyncInfoBase {

	@Value("#{target.id}")
	Long getId();

	@Value("#{target.topic_name}")
	String getTopicName();

	@Value("#{target.synchronizer_name}")
	String getSynchronizerName();
}
