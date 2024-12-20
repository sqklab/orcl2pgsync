package com.lguplus.fleta.domain.service.operation;


import com.lguplus.fleta.domain.dto.operation.OperationDto;
import com.lguplus.fleta.domain.model.operation.OperationProcessEntity;
import com.lguplus.fleta.domain.util.DateUtils;
import com.lguplus.fleta.ports.repository.operation.OperationProcessRepository;
import com.lguplus.fleta.ports.service.OperationService;
import com.lguplus.fleta.ports.service.operation.OperationOracleReaderService;
import com.lguplus.fleta.ports.service.operation.OperationPostgresReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutorService;

@Service
public class OperationOracleReaderServiceImpl implements OperationOracleReaderService {
	private static final Logger logger = LoggerFactory.getLogger(OperationOracleReaderServiceImpl.class);
	private static final int LIMIT = 1000;
	private final OperationManager operationManager;
	private final ExecutorService executorService;
	private final OperationService operationService;
	private final OperationPostgresReaderService postgresReader;
	private final OperationProcessRepository operationProcessRepository;

	public OperationOracleReaderServiceImpl(@Qualifier("commonThreadPool") ExecutorService executorService,
											OperationService operationService,
											OperationPostgresReaderService postgresReader,
											OperationProcessRepository processRepository,
											OperationManager operationManager) {
		this.executorService = executorService;
		this.operationService = operationService;
		this.postgresReader = postgresReader;
		this.operationProcessRepository = processRepository;
		this.operationManager = operationManager;
	}

	@Override
	public void compare(OperationDto param, String primaryKeys) {
		if (param.emptyWhere()) {
			logger.warn("[Operation][session {}]. There is no condition in the query, it takes a long time to compare all records of 2 tables.", param.getSessionId());
		}
		saveRequest(param);
		operationService.addOperation(param.getTable(), param.getSessionId(), param.getWhereStm());
		executorService.submit(() -> {
			try {
				Map<String, Map<String, Object>> source;
				int rowNum = 0;
				do {
					// Check cancel
					if (operationManager.isCancel(param.getSessionId(), param.getWhereStm(), param.getTable())) break;

					logger.info("[Operation][session {}]. Get rows from oracle rownum from {} to {}", param.getSessionId(), rowNum, rowNum + LIMIT);
					source = operationService.execute(
							param.getSourceDatabase(),
							param.pagingSql(LIMIT, rowNum),
							primaryKeys.toUpperCase()
					);

					// Check cancel
					if (operationManager.isCancel(param.getSessionId(), param.getWhereStm(), param.getTable())) break;
					postgresReader.execute(
							param.getTargetDatabase(),
							String.format("SELECT * FROM %s.%s WHERE 1=1 ", param.getTargetSchema(), param.getTable().toLowerCase()),
							primaryKeys,
							source,
							param,
							param.getTable());

					rowNum += LIMIT;

//					 For debug only, avoid scan all table
//					if (rowNum > 2 * LIMIT) break;


				} while (!source.isEmpty());
				logger.info("[Operation][session {}]. Stop scanning oracle table {}", param.getSessionId(), param.getTable());
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			} finally {
				updateProcessState(param);
			}
		});
	}

	/**
	 * @param param OperationDto
	 */
	private void updateProcessState(OperationDto param) {
		OperationProcessEntity updateEntity = operationProcessRepository.getBySessionAndOperationTableAndWhereCondition(param.getSessionId(), param.getTable(), param.getWhereStm());
		updateEntity.setState(false);
		updateEntity.setOperationEndDate(DateUtils.getDateTime());
		operationProcessRepository.save(updateEntity);
	}

	/**
	 * @param param param
	 */
	private void saveRequest(OperationDto param) {
		OperationProcessEntity opEntity = operationProcessRepository.getBySessionAndOperationTableAndWhereCondition(param.getSessionId(), param.getTable(), param.getWhereStm());
		if (opEntity == null) {
			opEntity = new OperationProcessEntity();
		}
		opEntity.setOperationDate(DateUtils.getDateTime());
		opEntity.setSession(param.getSessionId());
		opEntity.setOperationTable(param.getTable());
		opEntity.setWhereCondition(param.getWhereStm());
		opEntity.setSearchType(param.getSearchType().name());
		opEntity.setState(true);
		operationProcessRepository.save(opEntity);
	}
}
