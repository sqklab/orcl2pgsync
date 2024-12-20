package com.lguplus.fleta.domain.service.operation;


import com.lguplus.fleta.domain.dto.operation.OperationDto;
import com.lguplus.fleta.ports.service.OperationService;
import com.lguplus.fleta.ports.service.operation.OperationCompareService;
import com.lguplus.fleta.ports.service.operation.OperationPostgresReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutorService;

@Service
public class OperationPostgresReaderServiceImpl implements OperationPostgresReaderService {
	private static final Logger logger = LoggerFactory.getLogger(OperationPostgresReaderServiceImpl.class);

	private final ExecutorService executorService;
	private final OperationService operationService;
	private final OperationCompareService compareService;

	public OperationPostgresReaderServiceImpl(@Qualifier("commonThreadPool") ExecutorService executorService,
											  OperationService operationService,
											  OperationCompareService compareService) {
		this.executorService = executorService;
		this.operationService = operationService;
		this.compareService = compareService;
	}

	@Override
	public void execute(String datasource, String sql, String primaryKeys, Map<String, Map<String, Object>> source, OperationDto param, String table) {
		executorService.submit(() -> {
			logger.info("[Operation][session {}]. Get rows from postgres", param.getSessionId());
			try {
				Map<String, Map<String, Object>> target = operationService.executePart(datasource, sql, primaryKeys, source.keySet(), table, false);
				compareService.diff(source, target, param);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		});
	}
}
