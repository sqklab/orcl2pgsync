package com.lguplus.fleta.domain.service.mapper;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class DefaultObjectMapper implements RowMapper<Object[]> {
	@Override
	public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Object[] objects = new Object[columnCount];

		for (int i = 0; i < columnCount; i++) {
			objects[i] = rs.getObject(i + 1);
		}

		return objects;
	}
}