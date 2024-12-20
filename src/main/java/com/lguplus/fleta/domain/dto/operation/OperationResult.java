package com.lguplus.fleta.domain.dto.operation;

import org.springframework.beans.factory.annotation.Value;

public interface OperationResult {

	@Value("#{target.correction_type}")
	String getCorrectionType();

	@Value("#{target.total}")
	Long getTotal();
}
