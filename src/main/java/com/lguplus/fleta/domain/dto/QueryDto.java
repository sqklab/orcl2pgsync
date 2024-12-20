package com.lguplus.fleta.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lguplus.fleta.domain.service.mapper.ObjectMapperFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Slf4j
@Setter
@Getter
public class QueryDto implements Serializable {

	private static final String PART1 = "#_select_data";
	private static final String PART2 = "#_select_where";

	@JsonProperty("select_data")
	String selectData;

	@JsonProperty("select_where")
	String selectWhere;

	@JsonProperty("query")
	String query;

	/**
	 * get operation from query
	 *
	 * @return update schema.table set ... => return UPDATE. insert into ... => return INSERT. delete from ... => return DELETE
	 */
	public DbSyncOperation getOperation() {
		if (StringUtils.isBlank(query)) {
			return null;
		}
		String[] parts = query.trim().split(" ");
		if (parts.length < 1) {
			return null;
		}
		String part1 = parts[0].trim();
		if (DbSyncOperation.UPDATE.toString().equalsIgnoreCase(part1)) {
			return DbSyncOperation.UPDATE;
		} else if (DbSyncOperation.DELETE.toString().equalsIgnoreCase(part1)) {
			return DbSyncOperation.DELETE;
		} else if (DbSyncOperation.INSERT.toString().equalsIgnoreCase(part1)) {
			return DbSyncOperation.INSERT;
		}

		return null;
	}

	/**
	 * Parse query return 2 part of query
	 * update imcsuser.rd_vl_ab_album_group set (album_group_nm, sys_mod_dt, sys_mod_id) = (#_select_data)
	 * where  1=1 and (test_id, variation_id, album_group_id, group_type) = (#_select_where)
	 *
	 * @return {
	 * part1: "update imcsuser.rd_vl_ab_album_group set (album_group_nm, sys_mod_dt, sys_mod_id)"
	 * part2: "where  1=1 and (test_id, variation_id, album_group_id, group_type)"
	 * }
	 */
	public QueryPart parseMainUpdateQuery() {
		if (StringUtils.isBlank(query)) {
			return null;
		}
		String[] parts = query.split(PART1);
		if (parts.length == 0) {
			return null;
		}
		QueryPart queryPart = new QueryPart();
		queryPart.setPart1(parts[0].trim());
		if (parts.length > 1 && StringUtils.isNoneBlank(parts[1])) {
			String[] part2 = parts[1].split(PART2);
			if (part2.length > 0) {
				queryPart.setPart2(removeLast(part2[0], "="));
			}
		}

		return queryPart;
	}

	/**
	 * remove last character from string
	 *
	 * @param text      "where  1=1 and (test_id, variation_id, album_group_id, group_type) = "
	 * @param character "+"
	 * @return "where  1=1 and (test_id, variation_id, album_group_id, group_type)"
	 */
	private String removeLast(String text, String character) {
		if (StringUtils.isBlank(text)) {
			return text;
		}
		int index = text.lastIndexOf(character);
		if (index >= 0) {
			return text.substring(0, index).trim();
		}
		return text;
	}

	public String toPrettyJson() {
		try {
			ObjectMapper mapper = ObjectMapperFactory.getInstance().getObjectMapper();
			return mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(this);
		} catch (Exception ex) {
			log.warn(ex.getMessage(), ex);
		}
		return null;
	}

	@Setter
	@Getter
	@NoArgsConstructor
	public static class QueryPart {
		private String part1;
		private String part2;
	}
}

