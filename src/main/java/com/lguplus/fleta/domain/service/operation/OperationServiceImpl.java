package com.lguplus.fleta.domain.service.operation;

import com.lguplus.fleta.domain.dto.operation.OperationDto;
import com.lguplus.fleta.domain.dto.operation.OperationResult;
import com.lguplus.fleta.domain.dto.operation.OperationSummary;
import com.lguplus.fleta.domain.dto.ui.OperationResponse;
import com.lguplus.fleta.domain.model.operation.BaseOperationResultEntity;
import com.lguplus.fleta.domain.model.operation.OperationProcessEntity;
import com.lguplus.fleta.domain.service.exception.DatasourceNotFoundException;
import com.lguplus.fleta.domain.service.exception.InvalidPrimaryKeyException;
import com.lguplus.fleta.domain.service.mapper.OperationObjectMapper;
import com.lguplus.fleta.domain.util.DateUtils;
import com.lguplus.fleta.domain.util.JdbcUtil;
import com.lguplus.fleta.domain.util.SQLBuilder;
import com.lguplus.fleta.domain.util.TableConstraint;
import com.lguplus.fleta.ports.repository.operation.OpPtVoBuyRepository;
import com.lguplus.fleta.ports.repository.operation.OpPtVoWatchHistoryRepository;
import com.lguplus.fleta.ports.repository.operation.OpXcionRepository;
import com.lguplus.fleta.ports.repository.operation.OperationProcessRepository;
import com.lguplus.fleta.ports.service.DataSourceService;
import com.lguplus.fleta.ports.service.OperationService;
import com.lguplus.fleta.ports.service.operation.OperationBroadcastPublisher;
import com.zaxxer.hikari.pool.HikariProxyPreparedStatement;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.lguplus.fleta.domain.util.OperationUtil.isDiff;
import static com.lguplus.fleta.ports.service.OperationService.CompareDiffItem.CorrectOperation.INSERT;


@Service
public class OperationServiceImpl implements OperationService {

	private static final int POSTGRES_BATCH_MAX = 200;
	private static final Logger logger = LoggerFactory.getLogger(OperationServiceImpl.class);

	private final OperationManager operationManager;
	private final DataSourceService dataSourceService;
	private final OperationBroadcastPublisher broadcastPublisher;
	private final OpPtVoWatchHistoryRepository opPtVoWatchHistoryRepo;
	private final OpPtVoBuyRepository opPtVoBuyRepo;
	private final OpXcionRepository opXcionRepository;
	private final OperationProcessRepository operationProcessRepo;
	private final ThreadPoolTaskScheduler taskScheduler;

	public OperationServiceImpl(DataSourceService dataSourceService,
								OpPtVoWatchHistoryRepository opPtVoWatchHistoryRepo,
								OpPtVoBuyRepository opPtVoBuyRepo,
								OpXcionRepository opXcionRepository,
								OperationProcessRepository operationProcessRepo,
								@Qualifier("defaultThreadPool") ThreadPoolTaskScheduler taskScheduler,
								OperationBroadcastPublisher broadcastPublisher,
								OperationManager operationManager) {
		this.taskScheduler = taskScheduler;
		this.dataSourceService = dataSourceService;
		this.opPtVoWatchHistoryRepo = opPtVoWatchHistoryRepo;
		this.opPtVoBuyRepo = opPtVoBuyRepo;
		this.opXcionRepository = opXcionRepository;
		this.operationProcessRepo = operationProcessRepo;
		this.broadcastPublisher = broadcastPublisher;
		this.operationManager = operationManager;
	}

	public long count(String datasource, String schema, String table) throws SQLException, DatasourceNotFoundException {
		long start = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder("SELECT COUNT(*)");
		sb.append(" FROM ").append(schema).append(".").append(table);

		long total = 0;
		try (Connection connection = dataSourceService.findConnectionByServerName(datasource)) {
			if (Objects.isNull(connection)) {
				throw new DatasourceNotFoundException(String.format("Can't get connection to %s", datasource));
			}
			try (Statement statement = connection.createStatement()) {
				logger.info("Count sql: {}", sb);
				ResultSet rs = statement.executeQuery(sb.toString());
				while (rs.next()) {
					total = rs.getLong(1);
				}
				logger.info("Got count = {} after {} ms", total, (System.currentTimeMillis() - start));
			}
		}
		return total;
	}

	@Override
	public void register() {
		taskScheduler.initialize();
		taskScheduler.scheduleAtFixedRate(this::deleteOutDateResult, Duration.ofHours(2));
		this.resetOperationState();
	}

	private void deleteOutDateResult() {
		try {
			LocalDateTime timeLive = DateUtils.getDateTime().minusDays(1);
			this.opPtVoWatchHistoryRepo.deleteOutDateResult(timeLive);
			this.opXcionRepository.deleteOutDateResult(timeLive);
			this.opPtVoBuyRepo.deleteOutDateResult(timeLive);
			this.operationProcessRepo.deleteOutDateResult(timeLive);
		} catch (Exception e) {
			logger.error("Clean operation tables have an error. ", e);
		}
	}

	private void resetOperationState() {
		this.operationProcessRepo.resetState();
	}

	@Override
	public void deleteDiffResult(String table, List<String> uuids) {
		OperationDto.Table tableEnum = OperationDto.Table.valueOf(table.toLowerCase());
		switch (tableEnum) {
			case xcion_sbc_tbl_united:
				opXcionRepository.deleteByUuids(uuids);
				break;
			case pt_vo_buy:
				opPtVoBuyRepo.deleteByUuids(uuids);
				break;
			case pt_vo_watch_history:
				opPtVoWatchHistoryRepo.deleteByUuids(uuids);
				break;
			default:
				break;
		}
	}

	@Override
	public void deleteDiffResult(String table, String session, String condition) {
		OperationDto.Table tableEnum = OperationDto.Table.valueOf(table.toLowerCase());
		switch (tableEnum) {
			case xcion_sbc_tbl_united:
				opXcionRepository.deleteBySessionAndWhereCondition(session, condition);
				break;
			case pt_vo_buy:
				opPtVoBuyRepo.deleteBySessionAndWhereCondition(session, condition);
				break;
			case pt_vo_watch_history:
				opPtVoWatchHistoryRepo.deleteBySessionAndWhereCondition(session, condition);
				break;
			default:
				break;
		}
	}

	@Override
	public void deleteProcess(String table, String session, String condition) {
		this.operationProcessRepo.deleteBySessionAndWhereCondition(table, session, condition);
	}

	/**
	 * @param datasource  datasource
	 * @param sql         sql
	 * @param primaryKeys combined primary
	 * @param pKeySet     set of combined primary key values
	 * @param table       table
	 * @return map
	 * @throws Exception e
	 */
	@Override
	public Map<String, Map<String, Object>> executePart(String datasource, String sql, String primaryKeys, Set<String> pKeySet, String table, boolean upCase) throws Exception {
		int size = pKeySet.size();
		if (size < POSTGRES_BATCH_MAX) {
			return getByIdIn(datasource, sql, primaryKeys, pKeySet, table, null, upCase);
		}
		logger.info("Separate to {} connections to get postgres data because number of rows from oracle is too large ({})", size / POSTGRES_BATCH_MAX, size);
		List<String> keys = new ArrayList<>(pKeySet);
		Map<String, Map<String, Object>> pkParam = new HashMap<>();
		int from = 0;
		int to = POSTGRES_BATCH_MAX;
		List<String> subKeys;
		while (from < size) {
			subKeys = keys.subList(from, to);
			pkParam.putAll(getByIdIn(datasource, sql, primaryKeys, pKeySet, table, subKeys, upCase));
			from = to;
			to = Math.min(from + POSTGRES_BATCH_MAX, size);
		}
		return pkParam;
	}

	@Override
	public Map<String, Map<String, Object>> getByIdIn(String datasource, String sql, String primaryKeys, Set<String> pKeySet, String table, List<String> keys, boolean upCase) throws Exception {
		try (Connection connection = dataSourceService.findConnectionByServerName(datasource)) {
			if (Objects.isNull(connection)) {
				throw new DatasourceNotFoundException(String.format("Can't get connection to %s", datasource));
			}
			sql = setParams(sql, primaryKeys);
			logger.info("get data from {}, pKeys = {}, sql = {}", datasource, primaryKeys, sql);
			Map<String, Object> pkParam = new HashMap<>();
			pkParam.put("primaryKeys", buildPkParam(
					null == keys ? pKeySet : keys, table)
			);
			NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(connection, true));
			List<Map<String, Map<String, Object>>> result = jdbcTemplate.query(sql, pkParam, new OperationObjectMapper(primaryKeys, upCase));

			if (!result.isEmpty()) {
				return result.get(0);
			}

			return new HashMap<>();
		}
	}

	/**
	 * @param combinedPrimaryKeys {'pk1_value1,pk2_value1', 'pk1_value2,pk2_value2'}
	 * @return list of primary keys
	 */
	private List<Object[]> buildPkParam(Collection<String> combinedPrimaryKeys, String table) {
		if (null == combinedPrimaryKeys || combinedPrimaryKeys.isEmpty()) return null;

		List<Object[]> list = new LinkedList<>();

		if (!"PT_VO_WATCH_HISTORY".equals(table)) {
			for (String combinedKey : combinedPrimaryKeys) {
				String[] keys = combinedKey.split(",");
				list.add(keys);
			}
			return list;
		}

		for (String combinedKey : combinedPrimaryKeys) {
			String[] keys = combinedKey.split(",");
			Object[] items = new Object[keys.length];
			for (int i = 0; i < keys.length; i++) {
				if (i == 0) {
					items[0] = Integer.parseInt(String.valueOf(keys[0]));//P_IDX_SA
				} else {
					items[i] = keys[i];
				}
			}
			list.add(items);
		}

		return list;
	}

	/**
	 * @param sql         sql
	 * @param primaryKeys primaryKeys
	 * @return sql
	 */
	private String setParams(String sql, String primaryKeys) {
		if (StringUtils.isBlank(sql)) return sql;
		StringBuilder sb = new StringBuilder(sql);
		if (sql.toUpperCase().contains("WHERE")) {
			sb.append(" AND ");
		} else {
			sb.append(" WHERE ");
		}
		sb.append("(").append(primaryKeys).append(")").append(" IN (:primaryKeys)");

		return sb.toString();
	}

	@Override
	public Map<String, Map<String, Object>> execute(String datasource, String sql, String primaryKeys) throws Exception {
		long start = System.currentTimeMillis();
		try (Connection connection = dataSourceService.findConnectionByServerName(datasource)) {
			if (Objects.isNull(connection)) {
				throw new DatasourceNotFoundException(String.format("Can't get connection to %s", datasource));
			}
			try (Statement statement = connection.createStatement()) {
				logger.info("get data from {}, pKeys = {}, sql = {}", datasource, primaryKeys, sql);
				try (ResultSet rs = statement.executeQuery(sql)) {
					ResultSetMetaData resultSetMetaData = rs.getMetaData();
					int columnCount = resultSetMetaData.getColumnCount();

					Map<String, Map<String, Object>> result = new HashMap<>();
					while (rs.next()) {
						Map<String, Object> map = new LinkedHashMap<>();
						for (int i = 0; i < columnCount; i++) {
							map.put(resultSetMetaData.getColumnName(i + 1), rs.getObject(i + 1));
						}
						result.put(concatPk(primaryKeys, map), map);
					}

					logger.info("Got {} rows from Db after {} ms", result.size(), (System.currentTimeMillis() - start));
					return result;
				}
			}
		}
	}

	@Override
	public String getPrimaryKeys(String datasource, String schema, String table) throws SQLException, DatasourceNotFoundException {
		if (StringUtils.isBlank(datasource) || StringUtils.isBlank(schema) || StringUtils.isBlank(table)) {
			return null;
		}
		try (Connection connection = dataSourceService.findConnectionByServerName(datasource)) {
			if (Objects.isNull(connection)) {
				throw new DatasourceNotFoundException(String.format("Can't get connection to %s", datasource));
			}
			return JdbcUtil.detectPostgresPrimaryKeys(connection, schema, table);
		}
	}

	@Override
	public List<String> getValueByIds(String datasource, String schema, String table, String columnName, String value) throws SQLException, DatasourceNotFoundException {
		if (StringUtils.isBlank(datasource) || StringUtils.isBlank(schema) || StringUtils.isBlank(table)
				|| StringUtils.isBlank(columnName)) {
			return Collections.emptyList();
		}
		String query = buildCustomQuery(schema, table, columnName, value);
		logger.info("Query: {}", query);
		List<String> result = new ArrayList<>();
		try (Connection connection = dataSourceService.findConnectionByServerName(datasource)) {
			if (Objects.isNull(connection)) {
				throw new DatasourceNotFoundException(String.format("Can't get connection to %s", datasource));
			}
			try (Statement statement = connection.createStatement()) {
				ResultSet resultSet = statement.executeQuery(query);
				while (resultSet.next()) {
					result.add(resultSet.getString(1));
				}
			}
		}
		logger.info("Result: {}", String.join(",", result));
		if (StringUtils.isBlank(value) && result.size() == 1) {
			result = getValueByIds(datasource, schema, table, columnName, result.get(0));
		}
		return result;
	}

	/**
	 * @param schema     schema
	 * @param table      table
	 * @param columnName column
	 * @param value      value
	 * @return sql
	 */
	private String buildCustomQuery(String schema, String table, String columnName, String value) {
		if (StringUtils.isBlank(value)) {
			// This query returns a list of first character
			return String.format("select distinct substr(%s, 1, 1) as COLUMN_ID FROM %s.%s \n" +
					"where %s in (SELECT distinct(%s)  FROM %s.%s) order by COLUMN_ID ", columnName, schema, table, columnName, columnName, schema, table);
		} else {
			return String.format("SELECT distinct %s FROM %s.%s WHERE %s like '%s' order by %s", columnName, schema, table, columnName, value + '%', columnName);
		}
	}

	@Override
	public List<CorrectionResult> correct(List<CompareDiffItem> diffItems, String session, String whereStm, String database, String schema, String table, List<String> primaryKeys) throws SQLException {
		List<String> queries = new LinkedList<>();
		List<CorrectionResult> exceptions = new LinkedList<>();
		for (CompareDiffItem diffItem : diffItems) {
			try {
				switch (diffItem.getOperation()) {
					case INSERT:
						String sqlInsert = correctInsertBatch(diffItem.getSource(), database, schema, table);
						queries.add(sqlInsert);
						break;
					case DELETE:
						String sqlDelete = correctDeleteBatch(diffItem.getTarget(), database, schema, table, primaryKeys);
						queries.add(sqlDelete);
						break;
					case UPDATE:
						String sqlUpdate = correctUpdateBatch(diffItem.getSource(), diffItem.getTarget(), database, schema, table, primaryKeys);
						queries.add(sqlUpdate);
						break;
					default:
						break;
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				exceptions.add(
						new CorrectionResult(false, e.getMessage(), diffItem.getCode(), diffItem.getPKey())
				);
			}
		}
		try (Connection connection = dataSourceService.findConnectionByServerName(database)) {
			connection.setAutoCommit(false);
			try (Statement statement = connection.createStatement()) {
				for (String sql : queries) {
					statement.addBatch(sql);
				}
				statement.executeBatch();
				connection.commit();
			} catch (Exception ex) {
				if (!connection.isClosed()) { // Fix bug: java.sql.SQLException: Connection is closed
					connection.rollback();
				}
				throw ex;
			}
		}

		return exceptions;
	}

	/**
	 * @param diffItems   diffItems
	 * @param database    database
	 * @param schema      schema
	 * @param table       table
	 * @param primaryKeys primaryKeys
	 * @return diffItems
	 * @throws Exception e
	 */
	@Override
	public List<CompareDiffItem> getDiffData(List<CompareDiffItem> diffItems,
											 String database, String schema,
											 String targetDatabase, String targetSchema,
											 String table, String primaryKeys) throws Exception {
		if (null == diffItems || diffItems.isEmpty()) {
			throw new Exception("Diff items is empty");
		}

		// Get postgres data
		String postgresSql = String.format("SELECT * FROM %s.%s WHERE 1=1 ", targetSchema.toLowerCase(), table.toLowerCase());
		Set<String> pKeyValues = diffItems.stream()
				.filter(item -> INSERT != item.getOperation())
				.map(CompareDiffItem::getPKey).collect(Collectors.toSet());
		Map<String, Map<String, Object>> postgresData = executePart(targetDatabase, postgresSql, primaryKeys.toLowerCase(), pKeyValues, table, false);
		for (CompareDiffItem diff : diffItems) {
			diff.setTarget(postgresData.get(diff.getPKey()));
		}
		// Get oracle data
		Set<String> oraclePKeyValues = diffItems.stream()
				.map(CompareDiffItem::getPKey).collect(Collectors.toSet());
		String oracleSql = String.format("SELECT * FROM %s.%s WHERE 1=1 ", schema.toUpperCase(), table.toUpperCase());
		Map<String, Map<String, Object>> oracleData = executePart(database, oracleSql, primaryKeys.toUpperCase(), oraclePKeyValues, table, true);
		for (CompareDiffItem diff : diffItems) {
			diff.setSource(oracleData.get(diff.getPKey()));
		}

		return diffItems;
	}

	@Override
	public OperationResponse paggedLoadSearching(Pageable pageable, String sourceDatabase, String sourceSchema, String targetDatabase,
												 String targetSchema, String table, String primaryKeys,
												 String session, String where) throws Exception {
		OperationResponse operationResponse = new OperationResponse();
		List<OperationService.CompareDiffItem> result = new ArrayList<>();
		int pageSize = pageable.getPageSize();
		int pageNo = pageable.getPageNumber();
		long total;
		List<OperationService.CompareDiffItem> sameItems = new ArrayList<>();
		do {
			// (1) Fetch a list of different Primary keys
			List<OperationService.CompareDiffItem> temp = new ArrayList<>();
			Pageable pageRequest = PageRequest.of(pageNo++, pageSize);
			Page<BaseOperationResultEntity> tempPage = (Page<BaseOperationResultEntity>) this.getOperationResultByTable(pageRequest, table, session, where);
			for (BaseOperationResultEntity entity : tempPage) {
				OperationService.CompareDiffItem item = new OperationService.CompareDiffItem();
				item.setUuid(entity.getUuid());
				item.setPKey(entity.getPrimaryKeys());
				item.setOperation(CompareDiffItem.CorrectOperation.valueOf(entity.getCorrectionType()));
				temp.add(item);
			}
			total = tempPage.getTotalElements();
			if (tempPage.getContent().size() == 0) {
				break;
			}
			// (2) Get current data from Oracle and Postgres database
			temp = getDiffData(temp, sourceDatabase, sourceSchema, targetDatabase, targetSchema, table, primaryKeys);

			// (3) Remove same item
			sameItems.addAll(filterSame(temp));
			result.addAll(temp);
			if (tempPage.getContent().size() < 100) {
				break;
			}
		} while (pageSize - result.size() > 0);

		int same = sameItems.size();
		if (same > 0) {
			logger.info("There are {} same items in the list of different", same);
			updateSameItems(sameItems, table);
		}
		int totalPage = (int) ((total - same) / pageSize);

		operationResponse.setEntities(result.size() < pageSize ? result : result.subList(0, pageSize));
		operationResponse.setTotalPage(totalPage);
		return operationResponse;
	}

	private void updateSameItems(List<OperationService.CompareDiffItem> sameItems, String table) {
		List<String> uuids = sameItems.stream().map(a -> a.getUuid()).collect(Collectors.toList());
		OperationDto.Table tableEnum = OperationDto.Table.valueOf(table.toLowerCase());
		switch (tableEnum) {
			case xcion_sbc_tbl_united:
				opXcionRepository.deleteByUuids(uuids);
				break;
			case pt_vo_buy:
				opPtVoBuyRepo.deleteByUuids(uuids);
				break;
			case pt_vo_watch_history:
				opPtVoWatchHistoryRepo.deleteByUuids(uuids);
				break;
			default:
				break;
		}

	}

	private List<OperationService.CompareDiffItem> filterSame(List<OperationService.CompareDiffItem> diffItems) {
		List<OperationService.CompareDiffItem> same = new LinkedList<>();
		ListIterator<OperationService.CompareDiffItem> iter = diffItems.listIterator();
		while (iter.hasNext()) {
			OperationService.CompareDiffItem diff = iter.next();
			if (!isDiff(diff.getSource(), diff.getTarget())) {
				same.add(diff);
				iter.remove();
			}
		}

		return same;
	}


	private Page<? extends BaseOperationResultEntity> getOperationResultByTable(Pageable pageable, String table, String session, String where) {
		switch (table) {
			case "PT_VO_WATCH_HISTORY":
				return opPtVoWatchHistoryRepo.pagedResults(session, where, pageable);
			case "XCION_SBC_TBL_UNITED":
				return opXcionRepository.pagedResults(session, where, pageable);
			case "PT_VO_BUY":
				return opPtVoBuyRepo.pagedResults(session, where, pageable);
			default:
				throw new IllegalArgumentException("Only support 3 tables: ['PT_VO_WATCH_HISTORY', 'XCION_SBC_TBL_UNITED', 'PT_VO_BUY']");
		}
	}

	/**
	 * return true: running, false: finish or not searching yet
	 *
	 * @param table     table
	 * @param sessionId sessionId
	 * @return o
	 */
	@Override
	public OperationProcessEntity checkOperation(String table, String sessionId, String whereCondition) {
		OperationProcessEntity process = this.operationProcessRepo.getBySessionAndOperationTableAndWhereCondition(sessionId, table, whereCondition);
		if (process != null && process.getOperationEndDate() != null && !process.getState()) {
			long duration = Duration.between(process.getOperationEndDate(), DateUtils.getDateTime()).toMinutes();
			process.setIsOutDate(duration > 30); // 30 minutes:
		}
		return process;
	}

	@Override
	public OperationSummary getOperationSummary(String table, String sessionId, String whereCondition) {
		OperationSummary operationSummary = new OperationSummary(sessionId);
		operationSummary.setState(operationProcessRepo.getOperationState(sessionId, table, whereCondition));

		OperationDto.Table tableEnum = OperationDto.Table.valueOf(table.toLowerCase());
		List<OperationResult> operationResults = null;
		switch (tableEnum) {
			case xcion_sbc_tbl_united:
				operationResults = opXcionRepository.countDifferent(sessionId, whereCondition);
				break;
			case pt_vo_buy:
				operationResults = opPtVoBuyRepo.countDifferent(sessionId, whereCondition);
				break;
			case pt_vo_watch_history:
				operationResults = opPtVoWatchHistoryRepo.countDifferent(sessionId, whereCondition);
				break;
			default:
				break;
		}
		if (operationResults != null) {
			for (OperationResult result : operationResults) {
				long total = result.getTotal();
				CompareDiffItem.CorrectOperation opType = CompareDiffItem.CorrectOperation.valueOf(result.getCorrectionType());
				switch (opType) {
					case INSERT:
						operationSummary.setInsert(total);
						break;
					case UPDATE:
						operationSummary.setUpdate(total);
						break;
					case DELETE:
						operationSummary.setDelete(total);
						break;
					default:
						break;
				}
			}
		}
		return operationSummary;
	}

	@Override
	public void cancelOperation(String table, String sessionId, String whereCondition) {
		broadcastPublisher.broadcast(
				new OperationBroadcastPublisher
						.BroadcastEventData(sessionId, whereCondition, table)
		);
	}

	@Override
	public void addOperation(String table, String sessionId, String whereCondition) {
		operationManager.addRequest(sessionId, whereCondition, table);
	}

	/**
	 * @param primaryKeys primaryKeys
	 * @param row         row
	 * @return concat primary key values
	 */
	private String concatPk(String primaryKeys, Map<String, Object> row) {
		List<String> pkValues = new ArrayList<>();
		for (String col : primaryKeys.split(",")) {
			pkValues.add(String.valueOf(row.get(col)));
		}

		return String.join(",", pkValues);
	}


	/**
	 * @param row      row
	 * @param database database
	 * @param schema   schema
	 * @param table    table
	 * @return insert
	 */
	private String correctInsertBatch(Map<String, Object> row, String database, String schema, String table) throws DatasourceNotFoundException, SQLException {
		List<String> columns = new ArrayList<>(row.keySet());
		StringBuilder sb = new StringBuilder("INSERT INTO ")
				.append(schema.toLowerCase()).append(".").append(table.toLowerCase()) // SCHEMA.TABLE_NAME
				.append(SQLBuilder.toColumns(columns.stream().map(String::toLowerCase).collect(Collectors.toList()))) // (col1, col2, col3)
				.append(" VALUES ");
		sb.append(SQLBuilder.toQuestionMarks(columns)); // (?, ?, ?)
		sb.append(" ON CONFLICT DO NOTHING");

		return getRawSql(database, row, columns, sb.toString());
	}

	/**
	 * @param database     database
	 * @param row          data map
	 * @param columnsOrder columns list
	 * @param sql          sql has param ?
	 * @return sql
	 * @throws DatasourceNotFoundException e
	 * @throws SQLException                e
	 */
	private String getRawSql(String database, Map<String, Object> row, List<String> columnsOrder, String sql) throws DatasourceNotFoundException, SQLException {
		try (Connection connection = dataSourceService.findConnectionByServerName(database);
			 PreparedStatement statement = Objects.isNull(connection) ? null : connection.prepareStatement(sql)) {
			if (Objects.isNull(statement)) {
				throw new DatasourceNotFoundException(String.format("Can't get connection to %s", database));
			}
			int index = 1;
			for (String key : columnsOrder) {
				statement.setObject(index++, row.get(key));
			}
			String sqlRedo = null;
			HikariProxyPreparedStatement proxyPreparedStatement = (HikariProxyPreparedStatement) statement;
			if (proxyPreparedStatement.isWrapperFor(PreparedStatement.class)) {
				// TODO: There is no asSql method in PreparedStatement for PostgreSQL
				org.postgresql.jdbc.PgStatement postgresStatement = proxyPreparedStatement.unwrap(org.postgresql.jdbc.PgStatement.class);
				sqlRedo = postgresStatement.toString();
			}
			logger.info("** SQL: {}", sqlRedo);

			return sqlRedo;
		}
	}

	/**
	 * @param row         data map
	 * @param database    database
	 * @param schema      schema
	 * @param table       table
	 * @param primaryKeys primaryKeys
	 * @return sql
	 * @throws DatasourceNotFoundException e
	 * @throws InvalidPrimaryKeyException  e
	 */
	private String correctDeleteBatch(Map<String, Object> row, String database, String schema, String table, List<String> primaryKeys) throws DatasourceNotFoundException, InvalidPrimaryKeyException, SQLException {

		if (Objects.isNull(primaryKeys) || primaryKeys.isEmpty()) {
			throw new InvalidPrimaryKeyException("Primary key not found");
		}
		StringBuilder sb = new StringBuilder("DELETE FROM ")
				.append(schema.toLowerCase()).append(".").append(table.toLowerCase()) // SCHEMA.TABLE_NAME
				.append(" WHERE ")
				.append(JdbcUtil.createWhereClauseQuestionMark(row, primaryKeys));// col3=:col3 and col4 is null

		return getRawSql(database, row, primaryKeys, sb.toString());
	}

	/**
	 * @param source      source
	 * @param target      target
	 * @param database    database
	 * @param schema      schema
	 * @param table       table
	 * @param primaryKeys primaryKeys
	 * @return sql
	 * @throws DatasourceNotFoundException e
	 * @throws InvalidPrimaryKeyException  e
	 * @throws SQLException                e
	 */
	private String correctUpdateBatch(Map<String, Object> source,
									  Map<String, Object> target,
									  String database,
									  String schema,
									  String table,
									  List<String> primaryKeys) throws DatasourceNotFoundException, InvalidPrimaryKeyException, SQLException {

		if (Objects.isNull(primaryKeys) || primaryKeys.isEmpty()) {
			throw new InvalidPrimaryKeyException("Primary key not found");
		}
		List<String> setColumns = new ArrayList<>(source.keySet());
		StringBuilder sb = new StringBuilder("UPDATE ")
				.append(schema.toLowerCase()).append(".").append(table.toLowerCase()) // SCHEMA.TABLE_NAME
				.append(" SET ")
				.append(JdbcUtil.createSetValues(setColumns)) // col1=:col1, col2=:col2 hoac col1=?, col2=?
				.append(" WHERE ");
		sb.append(JdbcUtil.createWhereClauseQuestionMark(target, primaryKeys));// col3=:col3
		Map<String, Object> params = new HashMap<>(source);
		params.putAll(target);
		List<String> order = new ArrayList<>(setColumns);
		order.addAll(primaryKeys);

		return getRawSql(database, params, order, sb.toString());
	}
}
