package com.lguplus.fleta.ports.service.operation;

import com.lguplus.fleta.domain.dto.operation.OperationDto;

import java.util.Map;

public interface OperationPostgresReaderService {
	void execute(String datasource, String sql, String primaryKeys, Map<String, Map<String, Object>> map, OperationDto param, String table);
}
