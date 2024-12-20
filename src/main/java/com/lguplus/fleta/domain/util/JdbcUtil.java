package com.lguplus.fleta.domain.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.util.Assert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public final class JdbcUtil {

	/**
	 * @param sql    sql
	 * @param params parameters
	 */
	public static int saveWithParams(String sql, Connection connection, Map<String, Object> params) {
		Assert.notNull(connection, "connection required");
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(connection, true));
		return jdbcTemplate.update(sql, params);
	}

	/**
	 * @param connection connection
	 * @param schema     schema
	 * @param table      table
	 * @return list of primary keys
	 */
	public static List<String> detectColumnsFromPostgresTable(Connection connection, String schema, String table) {
		String query = "SELECT * " +
				"FROM information_schema.columns" +
				" WHERE table_schema = ?" +
				" AND table_name = ?;";

		List<String> columns = new LinkedList<>();
		try {
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, schema);
			statement.setString(2, table);
			ResultSet resultSet = statement.executeQuery();

			while (resultSet.next()) {
				columns.add(resultSet.getString("column_name"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return columns;
	}

	/**
	 * @param connection connection
	 * @param schema     schema
	 * @param table      table
	 * @return list of primary keys
	 */
	public static List<String> detectColumnsFromOracleTable(Connection connection, String schema, String table) {
		String query = "SELECT column_name " +
				"FROM all_tab_columns " +
				"WHERE owner = ? AND table_name = ?";

		List<String> columns = new LinkedList<>();
		try {
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, schema);
			statement.setString(2, table);
			ResultSet resultSet = statement.executeQuery();

			while (resultSet.next()) {
				columns.add(resultSet.getString(1));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return columns;
	}

	public static String detectPostgresPrimaryKeys(Connection connection, String schema, String table){
		return detectPostgresConstraintKeys(connection, schema, table, "PRIMARY KEY");
	}

	public static String detectPostgresUniqueKeys(Connection connection, String schema, String table){
		return detectPostgresConstraintKeys(connection, schema, table, "UNIQUE");
	}

	/**
	 * @param connection connection
	 * @param schema     schema
	 * @param table      table
	 * @return list of primary keys
	 */
	public static String detectPostgresConstraintKeys(Connection connection, String schema, String table, String constraint_type) {
		Set<String> constraintKeys = new HashSet<>();
		String detectKeySqlQuery = "" +
				"select c.column_name, c.data_type, ccu.constraint_name, c.is_nullable " +
				"from information_schema.table_constraints tc " +
				"join information_schema.constraint_column_usage as ccu using (constraint_schema, constraint_name) " +
				"join information_schema.columns as c " +
				"   on c.table_schema = tc.constraint_schema " +
				"  and tc.table_name = c.table_name " +
				"  and ccu.column_name = c.column_name " +
				"where tc.constraint_type = '"+constraint_type+"' " +
				"  and tc.table_schema = ? " +
				"  and tc.table_name = ? " +
				"order by ccu.constraint_name" +
				";";
		try {
			PreparedStatement statement = connection.prepareStatement(detectKeySqlQuery);
			statement.setString(1, schema);
			statement.setString(2, table);
			ResultSet resultSet = statement.executeQuery();

			while (resultSet.next()) {
				constraintKeys.add(resultSet.getString("column_name"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return StringUtils.join(constraintKeys, ",");
	}

	public static boolean detectPartition(Connection connection, String schema, String table) {
		boolean isPartitioned = false;
		String detectPartitionPostgresQuery = "SELECT nmsp_parent.nspname AS parent_schema," +
				"parent.relname      AS parent," +
				"nmsp_child.nspname  AS child_schema," +
				"child.relname       AS child " +
				"FROM pg_inherits " +
				"JOIN pg_class parent ON pg_inherits.inhparent = parent.oid " +
				"JOIN pg_class child ON pg_inherits.inhrelid = child.oid " +
				"JOIN pg_namespace nmsp_parent ON nmsp_parent.oid = parent.relnamespace " +
				"JOIN pg_namespace nmsp_child ON nmsp_child.oid = child.relnamespace " +
				"WHERE nmsp_parent.nspname = ? AND parent.relname = ?";
		try {
			PreparedStatement statement = connection.prepareStatement(detectPartitionPostgresQuery);
			statement.setString(1, schema.toLowerCase());
			statement.setString(2, table.toLowerCase());

			log.info(">>> Trying to detect partition for {}.{}.\n\t-> SQL Query: {}", schema, table, statement);
			ResultSet resultSet = statement.executeQuery();

			if (resultSet.next())
				isPartitioned = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return isPartitioned;
	}
	public static String detectOraclePrimaryKeys(Connection connection, String schema, String table){
		return detectOracleConstraintKeys(connection, schema, table, "P");
	}
	public static String detectOracleUniqueKeys(Connection connection, String schema, String table){
		return detectOracleConstraintKeys(connection, schema, table, "U");
	}
	public static String detectOracleConstraintKeys(Connection connection, String schema, String table, String constraint_type) {
		Set<String> constraintKeys = new HashSet<>();
		String detectKeySqlQuery = "" +
				"SELECT COLS.TABLE_NAME, COLS.COLUMN_NAME, COLS.POSITION, CONS.STATUS, CONS.OWNER " +
				"FROM ALL_CONSTRAINTS CONS, ALL_CONS_COLUMNS COLS " +
				"WHERE COLS.TABLE_NAME IN (?, ?) " +
				"AND CONS.CONSTRAINT_TYPE = '"+constraint_type+"' " +
				"AND CONS.CONSTRAINT_NAME = COLS.CONSTRAINT_NAME " +
				"AND CONS.OWNER = COLS.OWNER " +
				"AND COLS.OWNER IN (?, ?) " +
				"ORDER BY COLS.TABLE_NAME, COLS.POSITION";

		try {
			PreparedStatement statement = connection.prepareStatement(detectKeySqlQuery);
			statement.setString(1, table.toUpperCase());
			statement.setString(2, table.toLowerCase());
			statement.setString(3, schema.toUpperCase());
			statement.setString(4, schema.toLowerCase());

			log.info(">>> Trying to detect primary key for {}.{}.\n\t-> SQL Query: {}", schema, table, statement);
			ResultSet resultSet = statement.executeQuery();

			while (resultSet.next()) {
				constraintKeys.add(resultSet.getString(2));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return StringUtils.join(constraintKeys, ",");
	}

	/**
	 * @param before {col1: 'hello', col2: 3}
	 * @return col1=:col1, col2=:col2
	 */
	public static String createWhereClause(Map<String, Object> before, boolean upCaseParam, List<String> order) {
		if (Objects.isNull(before) || before.isEmpty()) {
			return null;
		}
		List<String> orderColumn = null == order ? new ArrayList<>(before.keySet()) : order;
		List<String> parts = new LinkedList<>();
		for (String field : orderColumn) {
			if (null == before.get(field)) {
				parts.add(field.toUpperCase() + " IS NULL");
			} else {
				if (upCaseParam) {
					parts.add(field.toUpperCase() + "=:" + field.toUpperCase());
				} else {
					parts.add(field.toUpperCase() + "=:" + field);
				}
			}
		}

		return String.join(" and ", parts);
	}


	/**
	 * @param fields col1,col2
	 * @return col1=:col1, col2=:col2
	 */
	public static String createSetValues(List<String> fields) {
		if (Objects.isNull(fields) || fields.isEmpty()) {
			return "";
		}
		List<String> parts = new LinkedList<>();
		for (String field : fields) {
			parts.add(field.toUpperCase() + "=?");
		}

		return String.join(",", parts);
	}

	/**
	 * @param alias  x
	 * @param before {COL1: 'hello', COL2: 3, COL3: null}
	 * @param order  [COL1, COL2, COL3]
	 * @return x.COL1=? and x.COL2=? and x.COL3 IS NULL
	 */
	public static String createWhereClauseQuestionMark(Map<String, Object> before, List<String> order, String alias) {
		if (Objects.isNull(before) || before.isEmpty()) {
			return null;
		}
		List<String> orderColumn = null == order || order.isEmpty() ? new ArrayList<>(before.keySet()) : order;
		List<String> parts = new LinkedList<>();
		for (String field : orderColumn) {
			if (null == before.get(field)) {
				parts.add(alias + "." + field.toUpperCase() + " IS NULL");
			} else {
				parts.add(alias + "." + field.toUpperCase() + "=?");
			}
		}

		return String.join(" and ", parts);
	}

	/**
	 * @param before {COL1: 'hello', COL2: 3, COL3: null}
	 * @param order  [COL1, COL2, COL3]
	 * @return COL1=? and COL2=? and COL3 IS NULL
	 */
	public static String createWhereClauseQuestionMark(Map<String, Object> before, List<String> order) {
		if (Objects.isNull(before) || before.isEmpty()) {
			return null;
		}
		List<String> orderColumn = null == order || order.isEmpty() ? new ArrayList<>(before.keySet()) : order;
		List<String> parts = new LinkedList<>();
		for (String field : orderColumn) {
			if (null == before.get(field)) {
				parts.add(field.toUpperCase() + " IS NULL");
			} else {
				parts.add(field.toUpperCase() + "=?");
			}
		}

		return String.join(" and ", parts);
	}

	/**
	 * @param list list
	 * @return lowercase list
	 */
	public static List<String> toLowerCase(List<String> list) {
		if (null == list) return null;
		return list.stream().map(String::toLowerCase).collect(Collectors.toList());
	}
}
