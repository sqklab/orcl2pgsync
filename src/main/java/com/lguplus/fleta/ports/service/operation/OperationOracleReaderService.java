package com.lguplus.fleta.ports.service.operation;

import com.lguplus.fleta.domain.dto.operation.OperationDto;

public interface OperationOracleReaderService {
	void compare(OperationDto param, String primaryKeys);
}
