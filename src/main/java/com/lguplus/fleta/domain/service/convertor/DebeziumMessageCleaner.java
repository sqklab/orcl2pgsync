package com.lguplus.fleta.domain.service.convertor;

import com.lguplus.fleta.domain.dto.DbSchema;
import com.lguplus.fleta.domain.dto.DebeziumDataType;
import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class fix some bug from Debezium
 */
public class DebeziumMessageCleaner {
	private static final Logger log = LoggerFactory.getLogger(DebeziumMessageCleaner.class);
	public static Pattern WRONG_DEFAULT_VALUE_PATTERN = Pattern.compile("^'[A-Za-z0-9]+'$");


	/**
	 * Remove NULL::character varying from payload
	 */
	public static void cleanPayload(SyncRequestMessage message) {
		if (null == message) return;
		DbSchema schema = message.getSchema();
		SyncRequestMessage.Payload payload = message.getPayload();
		if (null == schema) return;
		if (null == payload) return;

		Map<String, DebeziumDataType> fieldDataTypeMap = schema.buildDataTypeMap("before");
		if (null == fieldDataTypeMap) return;

		Map<String, Object> before = payload.getBefore();
		Map<String, Object> after = payload.getAfter();

		fieldDataTypeMap.forEach((field, datatype) -> {
			cleanNullCharacterVarying(before, after, field, datatype);
			cleanWrongDefaultCharacterValue(before, after, field, datatype);
		});
	}

	/**
	 * Remove NULL::character varying from payload
	 */
	private static void cleanNullCharacterVarying(Map<String, Object> before, Map<String, Object> after, String field, DebeziumDataType datatype) {
		if (null != before && isNullCharacterVarying(datatype)) {
			before.put(field, null);
		}
		if (null != after && isNullCharacterVarying(datatype)) {
			after.put(field, null);
		}
	}

	private static boolean isNullCharacterVarying(DebeziumDataType datatype) {
		return null != datatype && ConvertorHelper.NULL_CHARACTER_VARYING.equals(datatype.getDefaultValue());
	}

	/**
	 * * create table PT_VO_CATEGORY_UNITED (
	 * * VOD_PKG_YN              VARCHAR2(1) default ('N'),
	 * * VOD_POINT_I30           VARCHAR2(2) default ('0')
	 * * )
	 * * <p>
	 * * When column value is null, debezium set them to default value 'N' or '0' in kafka message.
	 * * This method check whether default value equal to value of column in before and after field in kafka message then set them to null
	 *
	 * @param before   before
	 * @param after    after
	 * @param field    field
	 * @param datatype datatype
	 */
	private static void cleanWrongDefaultCharacterValue(Map<String, Object> before, Map<String, Object> after, String field, DebeziumDataType datatype) {
		if (null != before && isCharacterDefaultValue(datatype, String.valueOf(before.get(field)), field)) {
			before.put(field, null);
		}
		if (null != after && isCharacterDefaultValue(datatype, String.valueOf(after.get(field)), field)) {
			after.put(field, null);
		}
	}

	/**
	 * create table PT_VO_CATEGORY_UNITED (
	 * VOD_PKG_YN              VARCHAR2(1) default ('N'),
	 * VOD_POINT_I30           VARCHAR2(2) default ('0')
	 * )
	 * check whether column like above
	 *
	 * @param datatype datatype
	 * @return
	 */
	private static boolean isCharacterDefaultValue(DebeziumDataType datatype, String columnValue, String field) {
		if (null == datatype || null == datatype.getDefaultValue()) return false;

		Matcher matcher = WRONG_DEFAULT_VALUE_PATTERN.matcher(datatype.getDefaultValue());
		if (matcher.find()) {
			if (StringUtils.isNotBlank(columnValue) && columnValue.equals(datatype.getDefaultValue())) {
				log.info("Clean wrong default value from debezium. Set value {} of field {} to null", matcher.group(0), field);
				return true;
			}
		}
		return false;
	}
}
