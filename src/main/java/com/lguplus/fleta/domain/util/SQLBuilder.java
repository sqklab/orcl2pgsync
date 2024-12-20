package com.lguplus.fleta.domain.util;

import ch.qos.logback.classic.Logger;
import com.lguplus.fleta.adapters.persistence.exception.InvalidSyncMessageRequestException;
import com.lguplus.fleta.domain.dto.DbSyncOperation;
import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class SQLBuilder {
	/**
	 * Use for parsing parameter names in "Read model" Sql
	 * query = delete from imcsuser.rd_vl_ab_album_image where test_id = :p_test_id and screen_type = case when :p_img_flag   --pk1:p_screen_type
	 * => [p_test_id, p_img_flag]
	 */
	private static final String RD_PARAMETER_PATTERN = "[=, ]\\s*(:[A-Za-z_0-9]+)";

	/**
	 * @param query an sql query
	 * @return list of parameter names
	 */
	public static List<String> getListParameters(String query) {
		if (StringUtils.isBlank(query)) {
			log.error("can't get parameters form query");
			return null;
		}
		Pattern pattern = Pattern.compile(RD_PARAMETER_PATTERN);
		Matcher matcher = pattern.matcher(query);
		List<String> result = new ArrayList<>();
		while (matcher.find()) {
			if (StringUtils.isBlank(matcher.group())) {
				continue;
			}
			String[] parts = matcher.group().split(":");
			if (parts.length < 2) {
				continue;
			}
			result.add(parts[1].trim());
		}
		return result;
	}

	/**
	 * @param query an sql query
	 * @return list of parameter names
	 */
	public static String changeParameterToQuestionMark(String query) {
		if (StringUtils.isBlank(query)) {
			log.error("can't get parameters form query");
			return null;
		}
		Pattern pattern = Pattern.compile(RD_PARAMETER_PATTERN);
		Matcher matcher = pattern.matcher(query);
		List<String> result = new ArrayList<>();
		while (matcher.find()) {
			if (StringUtils.isBlank(matcher.group())) {
				continue;
			}
			String[] parts = matcher.group().split(":");
			if (parts.length < 2) {
				continue;
			}
			result.add(parts[1].trim());
			query = query.replace(":" + parts[1], "?");
		}
		return query;
	}

	/**
	 * @param payload payload
	 * @return schema.tableName
	 * @throws InvalidSyncMessageRequestException e
	 */
	public static String getTargetTable(SyncRequestMessage.Payload payload) throws InvalidSyncMessageRequestException {
		if (Objects.isNull(payload) || StringUtils.isBlank(payload.getSegOwner()) || StringUtils.isBlank(payload.getTableName())) {
			throw new InvalidSyncMessageRequestException("Seg owner or table name is empty");
		}
		return payload.getSegOwner() + "." + payload.getTableName();
	}

	/**
	 * @param paramName Ex: p_CREATED_AT
	 * @return CREATED_AT
	 */
	public static String toColumnName(String paramName) {
		if (StringUtils.isBlank(paramName) || paramName.trim().length() < 3) {
			return null;
		}
		return paramName.trim().substring(2);
	}

	/**
	 * @param parameters            list of parameter names
	 * @param normalizedParamValues parameters and value of payload.data after normalized
	 * @return parameters
	 */
	public static Map<String, Object> buildParameters(List<String> parameters, Map<String, Object> normalizedParamValues) {
		if (Objects.isNull(parameters) || parameters.isEmpty()) {
			return new HashMap<>();
		}
		if (Objects.isNull(normalizedParamValues) || normalizedParamValues.isEmpty()) {
			return new HashMap<>();
		}
		Map<String, Object> lowerCaseNormalizedParamValues = new HashMap<>();
		normalizedParamValues.forEach((s, o) -> lowerCaseNormalizedParamValues.put(s.toLowerCase(), o));

		Map<String, Object> map = new HashMap<>();
		Set<String> paramSet = new HashSet<>(parameters);
		paramSet.forEach(parameterName -> {
			map.put(parameterName, lowerCaseNormalizedParamValues.get(Objects.requireNonNull(SQLBuilder.toColumnName(parameterName)).toLowerCase()));
		});

		return map;
	}

	/**
	 * @param columns col1, col2, col3
	 * @return (: col1, : col2, : col3)
	 */
	public static String toParamColumns(List<String> columns) {
		if (Objects.isNull(columns)) {
			return "";
		}
		return "(" + String.join(",",
				columns.stream().map(s -> ":" + s).collect(Collectors.toList())
		) + ")";
	}

	/**
	 * @param columns col1, col2, col3
	 * @return (col1, col2, col3)
	 */
	public static String toColumns(List<String> columns) {
		if (Objects.isNull(columns)) {
			return "";
		}
		return "(" + String.join(",", columns) + ")";
	}

	/**
	 * @param columnCount [A, B, C]
	 * @return " (?,?,?) "
	 */
	public static String toQuestionMarks(int columnCount) {
		if (columnCount <= 0) {
			return "";
		}
		String[] arr = new String[columnCount];
		Arrays.fill(arr, "?");
		return "(" + Arrays.stream(arr).map(s -> "?").collect(Collectors.joining(",")) + ")";
	}

	/**
	 * @param columns [A, B, C]
	 * @return " (?,?,?) "
	 */
	public static String toQuestionMarks(List<String> columns) {
		if (Objects.isNull(columns)) {
			return "";
		}
		return "(" + columns.stream().map(s -> "?").collect(Collectors.joining(",")) + ")";
	}

	public static String prepareSearchTerms(String keyword) {
		if (StringUtils.isEmpty(keyword)) keyword = StringUtils.EMPTY;
		if (StringUtils.isNotEmpty(keyword)) keyword = keyword.replace("'", "''");

		return "%" + keyword + "%";
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class QueryResult {
		private String query;
		private Set<String> params;


		public void setParams(List<String> params) {
			this.params = new HashSet<>(params);
		}

		public boolean invalid() {
			return StringUtils.isBlank(query) || Objects.isNull(params) || params.isEmpty();
		}
	}
}
