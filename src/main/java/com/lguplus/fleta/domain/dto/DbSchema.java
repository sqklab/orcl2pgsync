package com.lguplus.fleta.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
public class DbSchema {

	private final int DB_INDEX = 0;
	private final int SCHEMA_INDEX = 1;
	private final int TABLE_INDEX = 2;
	private String type;
	private boolean optional;
	private String name;
	private List<DbField> fields;

	/**
	 * traverse schema field in kafka topic and return datatype info of fieldName
	 *
	 * @param fieldName fieldName
	 * @return datatype of fieldName
	 */
	public Map<String, DebeziumDataType> buildDataTypeMap(String fieldName) {
		DbField dataField = findDataField(fieldName);
		if (Objects.isNull(dataField)) {
			return null;
		}
		return dataField.buildDataTypeMap();
	}

	public DbField findDataField(String fieldName) {
		if (Objects.isNull(fields) || fields.isEmpty()) {
			return null;
		}
		Optional<DbField> dataField = fields.stream().filter(dbField -> fieldName.equals(dbField.getField())).findFirst();
		if (dataField.isEmpty()) {
			return null;
		}
		return dataField.get();
	}

	@Override
	public String toString() {
		return "DbSchema{" +
				"type='" + type + '\'' +
				", optional=" + optional +
				", name='" + name + '\'' +
				", fields=" + fields +
				'}';
	}
}
