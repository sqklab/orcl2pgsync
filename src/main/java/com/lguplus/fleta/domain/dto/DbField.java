package com.lguplus.fleta.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class DbField {

	public static final String DEBEZIUM_SOURCE_COLUMN_TYPE = "__debezium.source.column.type";
	public static final String DEBEZIUM_SOURCE_COLUMN_LENGTH = "__debezium.source.column.length";
	public static final String DEBEZIUM_SOURCE_COLUMN_SCALE = "__debezium.source.column.scale";

	private String type;
	private boolean optional;
	private String field;
	private String name;
	@JsonProperty("default")
	private String defaultValue;
	private List<DbField> fields;
	private Integer version;
	private Map<String, String> parameters;

	public Map<String, DebeziumDataType> buildDataTypeMap() {
		if (Objects.isNull(fields) || fields.isEmpty()) {
			return null;
		}
		Map<String, DebeziumDataType> map = new HashMap<>();
		for (DbField field : fields) {
			map.put(field.getField(), new DebeziumDataType(field));
		}
		return map;
	}

	/**
	 * get type of column in database
	 *
	 * @return datatype in database
	 */
	public String getSourceType() {
		if (null != parameters && StringUtils.isNotBlank(parameters.get(DEBEZIUM_SOURCE_COLUMN_TYPE))) {
			return parameters.get(DEBEZIUM_SOURCE_COLUMN_TYPE);
		}
		return null;
	}

	@Override
	public String toString() {
		return "DbField{" +
				"type='" + type + '\'' +
				", optional=" + optional +
				", field='" + field + '\'' +
				", name='" + name + '\'' +
				", fields=" + fields +
				'}';
	}
}
