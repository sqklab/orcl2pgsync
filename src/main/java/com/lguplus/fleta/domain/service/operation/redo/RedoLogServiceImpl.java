package com.lguplus.fleta.domain.service.operation.redo;

import com.lguplus.fleta.domain.dto.ui.CurrentScnInfo;
import com.lguplus.fleta.domain.dto.ui.LogMnrContent;
import com.lguplus.fleta.domain.dto.ui.WrapLogMnrContent;
import com.lguplus.fleta.domain.service.exception.DatasourceNotFoundException;
import com.lguplus.fleta.domain.util.DateUtils;
import com.lguplus.fleta.ports.service.DataSourceService;
import com.lguplus.fleta.ports.service.operation.redo.RedoLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RedoLogServiceImpl implements RedoLogService {
	private final DataSourceService dataSourceService;

	public RedoLogServiceImpl(DataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}

	@Override
	public CurrentScnInfo getCurrentSCN(String db) throws SQLException, DatasourceNotFoundException {
		try (Connection connection = dataSourceService.findConnectionByServerName(db)) {
			if (connection == null) {
				throw new DatasourceNotFoundException(String.format("The datasource %s not found", db));
			}
			String query = "select CURRENT_SCN from v$database";
			try {
				PreparedStatement statement = connection.prepareStatement(query);
				ResultSet resultSet = statement.executeQuery();
				if (resultSet.next()) {
					return new CurrentScnInfo(resultSet.getString(1), DateUtils.getDateTime());
				}
			} catch (Exception ex) {
				log.error("Get current SCN error", ex);
				throw ex;
			}
		}
		return null;
	}

	@Override
	public String getTimestampByScn(Long scn, String db) throws SQLException {
		try (Connection connection = dataSourceService.findConnectionByServerName(db)) {
			String query = "select scn_to_timestamp(?) as timestamp from dual";
			try {
				PreparedStatement statement = connection.prepareStatement(query);
				statement.setLong(1, scn);
				ResultSet resultSet = statement.executeQuery();
				if (resultSet.next()) {
					return resultSet.getString(1);
				}
			} catch (Exception ex) {
				log.error("Get Time by SCN error", ex);
			}
		}
		return null;
	}

	/**
	 * @param dateTime: format should be: dd-mm-yyyy hh24:mi:ss  eg:'30-05-2022 03:34:12'
	 * @param db
	 * @return
	 * @throws SQLException
	 */
	@Override
	public String getScnByTimestamp(String dateTime, String db) throws SQLException {
		try (Connection connection = dataSourceService.findConnectionByServerName(db)) {
			String query = "select timestamp_to_scn(to_timestamp(?,'dd-mm-yyyy hh24:mi:ss')) scn from dual";
			try {
				PreparedStatement statement = connection.prepareStatement(query);
				statement.setString(1, dateTime);
				ResultSet resultSet = statement.executeQuery();
				if (resultSet.next()) {
					return resultSet.getString(1);
				}
			} catch (Exception ex) {
				log.error("Get Scn by time error", ex);
				throw ex;
			}
		}
		return null;
	}

	@Override
	public List<String> getTablesByDbAndSchema(String db, String schema) throws SQLException {
		List<String> tables = new LinkedList<>();
		try (Connection connection = dataSourceService.findConnectionByServerName(db)) {
			String query = "SELECT table_name FROM all_tables WHERE owner=?";
			try {
				PreparedStatement statement = connection.prepareStatement(query);
				statement.setString(1, schema);
				ResultSet resultSet = statement.executeQuery();
				while (resultSet.next()) {
					tables.add(resultSet.getString(1));
				}
			} catch (Exception ex) {
				log.error("Get tables error", ex);
			}
		}
		return tables;
	}

	@Override
	public WrapLogMnrContent searchLogMnrContents(String startScn, String endScn,
												  List<Integer> operationTypes, List<String> tables,
												  String schema, String db, Integer totalPage,
												  Integer currentPage, Integer pageSize) throws SQLException {
		WrapLogMnrContent wrapLogMnrContent = new WrapLogMnrContent();
		try (Connection connection = dataSourceService.findConnectionByServerName(db)) {
			connection.setAutoCommit(false);
			try {
				String enableLogQuery = "BEGIN SYS.DBMS_LOGMNR.START_LOGMNR ( " +
						" STARTSCN => ?," +
						" ENDSCN => ?, " +
						" OPTIONS => SYS.DBMS_LOGMNR.SKIP_CORRUPTION + SYS.DBMS_LOGMNR.NO_SQL_DELIMITER + SYS.DBMS_LOGMNR.NO_ROWID_IN_STMT + SYS.DBMS_LOGMNR.DICT_FROM_ONLINE_CATALOG + SYS.DBMS_LOGMNR.CONTINUOUS_MINE + SYS.DBMS_LOGMNR.COMMITTED_DATA_ONLY + SYS.DBMS_LOGMNR.STRING_LITERALS_IN_STMT " +
						"); " +
						"END;";
				try (PreparedStatement statement = connection.prepareStatement(enableLogQuery)) {
					statement.setLong(1, Long.parseLong(startScn));
					statement.setLong(2, Long.parseLong(endScn));
					statement.execute();
				}

				if (totalPage == 0) {
					String countQuery = buildCountLogMnrContentQuery(operationTypes, tables, schema);
					try (PreparedStatement statement = connection.prepareStatement(countQuery)) {
						int index = 1;
						if (!StringUtils.isEmpty(schema)) {
							statement.setString(index++, schema);
						}
						for (Integer type : operationTypes) {
							statement.setInt(index++, type);
						}
						for (String table : tables) {
							statement.setString(index++, table);
						}
						ResultSet resultSet = statement.executeQuery();
						while (resultSet.next()) {
							totalPage = resultSet.getInt(1);
						}
					}
				}
				String logQueryInfo = buildSelectLogMnrContentQuery(operationTypes, tables, currentPage, pageSize, schema);
				List<LogMnrContent> mnrContents = new LinkedList<>();
				try (PreparedStatement statement = connection.prepareStatement(logQueryInfo)) {
					int index = 1;
					if (!StringUtils.isEmpty(schema)) {
						statement.setString(index++, schema);
					}
					for (Integer type : operationTypes) {
						statement.setInt(index++, type);
					}
					for (String table : tables) {
						statement.setString(index++, table);
					}
					ResultSet resultSet = statement.executeQuery();
					while (resultSet.next()) {
						String scn = resultSet.getString(1);
						String startScnResult = resultSet.getString(2);
						String commitScn = resultSet.getString(3);
						String timestamp = resultSet.getString(4);
						String operation = resultSet.getString(5);
						String segOwner = resultSet.getString(6);
						String tableName = resultSet.getString(7);
						String sqlRedo = resultSet.getString(8);
						String sqlUndo = resultSet.getString(9);
						mnrContents.add(new LogMnrContent(scn, startScnResult, commitScn, timestamp, operation, segOwner, tableName, sqlRedo, sqlUndo));
					}
				}
				connection.commit();
				wrapLogMnrContent.setLogContents(mnrContents);
				wrapLogMnrContent.setCount(totalPage);
			} catch (Exception ex) {
				log.error("Get tables error", ex);
				throw ex;
			}
		}
		return wrapLogMnrContent;
	}

	private String buildSelectLogMnrContentQuery(List<Integer> operationTypes, List<String> tables, Integer currentPage, Integer pageSize, String schema) {
		StringBuilder sb = new StringBuilder("SELECT SCN,START_SCN,COMMIT_SCN,TIMESTAMP,OPERATION,SEG_OWNER,TABLE_NAME,SQL_REDO,SQL_UNDO FROM V$LOGMNR_CONTENTS ");
		boolean hasSchema = !StringUtils.isEmpty(schema);
		if (noConditions(operationTypes, tables, hasSchema)) {
			return sb.toString();
		}
		sb.append(buildConditions(operationTypes, tables, hasSchema));
		int offset = 0;
		if (currentPage > 1) {
			offset = ((currentPage - 1) * pageSize);
		}
		sb.append(String.format(" ORDER BY COMMIT_SCN OFFSET %s ROWS FETCH NEXT %s ROWS ONLY ", offset, pageSize));
		return sb.toString();
	}

	private boolean noConditions(List<Integer> operationTypes, List<String> tables, boolean hasSchema) {
		return !hasSchema && operationTypes.isEmpty() && tables.isEmpty();
	}

	private String buildCountLogMnrContentQuery(List<Integer> operationTypes, List<String> tables, String schema) {
		StringBuilder sb = new StringBuilder("SELECT COUNT(1) FROM V$LOGMNR_CONTENTS ");
		boolean hasSchema = !StringUtils.isEmpty(schema);
		if (noConditions(operationTypes, tables, hasSchema)) {
			return sb.toString();
		}
		sb.append(buildConditions(operationTypes, tables, hasSchema));
		return sb.toString();
	}

	private StringBuilder buildConditions(List<Integer> operationTypes, List<String> tables, boolean hasSchema) {
		StringBuilder sb = new StringBuilder(" WHERE ");
		List<String> conditions = new ArrayList<>();
		if (hasSchema) {
//			sb.append(" SEG_OWNER = ? ");
			conditions.add(" SEG_OWNER = ? ");
		}
		if (!operationTypes.isEmpty()) {
//			if (hasSchema) {
//				sb.append(" AND ");
//			}
			String operationCodeConditions = String.format(" OPERATION_CODE IN (%s) ",
					operationTypes.stream().map(v -> "?").collect(Collectors.joining(", ")));
			conditions.add(operationCodeConditions);
		}

		if (!tables.isEmpty()) {
//			if (!operationTypes.isEmpty()) {
//				sb.append(" AND ");
//			}
			String tableConditions = String.format(" TABLE_NAME in (%s) ",
					tables.stream().map(v -> "?").collect(Collectors.joining(", ")));
			conditions.add(tableConditions);
		}
		return sb.append(String.join(" AND ", conditions));
	}

}
