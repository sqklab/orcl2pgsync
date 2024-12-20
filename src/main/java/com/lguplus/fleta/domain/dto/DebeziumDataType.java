package com.lguplus.fleta.domain.dto;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;

//@Setter
//@NoArgsConstructor
@Getter
public class DebeziumDataType {

	/**
	 * default value in database
	 */
	private final String defaultValue;
	/**
	 * datatype in database
	 */
	private final String dataType;
	/**
	 * literal type describes how the value is literally represented using Kafka Connect schema types:
	 * INT8, INT16, INT32, INT64, FLOAT32, FLOAT64, BOOLEAN, STRING, BYTES, ARRAY, MAP, and STRUCT.
	 */
	private final String literalType;
	/**
	 * semantic type describes how the Kafka Connect schema captures the meaning of the field using the name of the Kafka Connect schema for the field.
	 */
	private final String semanticType;

	private final String superType;

	public DebeziumDataType(DbField field) {
		this.dataType = field.getSourceType();
		this.literalType = field.getType();
		this.semanticType = field.getName();
		this.defaultValue = field.getDefaultValue();
		this.superType = StringUtils.isNoneBlank(this.semanticType)
				? this.semanticType
				: this.literalType;
	}
}
