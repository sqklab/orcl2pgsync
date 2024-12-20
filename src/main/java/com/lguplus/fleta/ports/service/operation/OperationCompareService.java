package com.lguplus.fleta.ports.service.operation;

import com.lguplus.fleta.domain.dto.operation.OperationDto;

import java.util.Map;

public interface OperationCompareService {
	void diff(Map<String, Map<String, Object>> source, Map<String, Map<String, Object>> target, OperationDto param);
}
