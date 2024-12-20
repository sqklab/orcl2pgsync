package com.lguplus.fleta.domain.dto.ui;

import org.springframework.beans.factory.annotation.Value;

public interface OperationSummaryDto {

	@Value("#{target.insert}")
	Long getInsert();

	@Value("#{target.update}")
	Long getUpdate();

	@Value("#{target.delete}")
	Long getDelete();
}
