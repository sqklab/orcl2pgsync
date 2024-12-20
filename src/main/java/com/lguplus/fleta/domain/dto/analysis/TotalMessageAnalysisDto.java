package com.lguplus.fleta.domain.dto.analysis;

import org.springframework.beans.factory.annotation.Value;

public interface TotalMessageAnalysisDto {

	@Value("#{target.dbName}")
	String getDbName();

	@Value("#{target.schmName}")
	String getSchmName();

	@Value("#{target.atTime}")
	Integer getAtTime();

	@Value("#{target.sumReceivedMessage}")
	Long getSumReceivedMessage();

	@Value("#{target.sumTotalMessage}")
	Long getSumTotalMessage();

}
