package com.lguplus.fleta.domain.service.mapper;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class OperationObjectMapper implements RowMapper<Map<String, Map<String, Object>>> {

	String primaryKeys;
	boolean upCase;

	public OperationObjectMapper(String primaryKeys, boolean upCase) {
		this.primaryKeys = primaryKeys;
		this.upCase = upCase;
	}

	@Override
	public Map<String, Map<String, Object>> mapRow(ResultSet rs, int rowNum) throws SQLException {
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();

		Map<String, Map<String, Object>> result = new HashMap<>();
		Map<String, Object> map = parse(columnCount, metaData, rs);
		result.put(concatPk(primaryKeys, map), map);

		while (rs.next()) {
			map = parse(columnCount, metaData, rs);
			result.put(concatPk(primaryKeys, map), map);
		}

		return result;
	}


	private Map<String, Object> parse(int columnCount, ResultSetMetaData metaData, ResultSet rs) throws SQLException {
		Map<String, Object> map = new LinkedHashMap<>();
		for (int i = 0; i < columnCount; i++) {
			map.put(metaData.getColumnName(i + 1), rs.getObject(i + 1));
		}

		return map;
	}

	/**
	 * @param primaryKeys primaryKeys
	 * @param row         row
	 * @return concat primary key values
	 */
	private String concatPk(String primaryKeys, Map<String, Object> row) {
		List<String> pkValues = new ArrayList<>();
		String pKeys = upCase ? primaryKeys.toUpperCase() : primaryKeys;
		for (String col : pKeys.split(",")) {
			pkValues.add(String.valueOf(row.get(col)));
		}
		return String.join(",", pkValues);
	}
}