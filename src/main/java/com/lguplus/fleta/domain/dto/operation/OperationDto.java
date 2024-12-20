package com.lguplus.fleta.domain.dto.operation;

import com.lguplus.fleta.domain.model.operation.BaseOperationResultEntity;
import com.lguplus.fleta.domain.model.operation.OpPtVoBuyEntity;
import com.lguplus.fleta.domain.model.operation.OpPtVoWatchHistoryEntity;
import com.lguplus.fleta.domain.model.operation.OpXcionEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class OperationDto {

	@NotBlank
	private String sourceSchema;
	@NotBlank
	private String sourceDatabase;
	@NotBlank
	private String sourceSql;

	@NotBlank
	private String targetSchema;
	@NotBlank
	private String targetDatabase;
	@NotBlank
	private String targetSql;

	@NotBlank
	private String table;

	private String whereStm;
	private String columnIdName;
	private String columnIdValue;

	private String sessionId;

	public boolean emptyWhere() {
		return null == whereStm || StringUtils.isBlank(whereStm);
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public SearchType getSearchType() {
		if (StringUtils.isBlank(columnIdValue) && (StringUtils.isBlank(whereStm))) {
			return SearchType.ALL;
		}
		if (!StringUtils.isBlank(columnIdValue) && StringUtils.isBlank(whereStm)) {
			return SearchType.BY_ID;
		}
		return SearchType.CUSTOM;
	}

	public Table getOpTable() {
		if (table.equalsIgnoreCase(Table.xcion_sbc_tbl_united.name())) {
			return Table.xcion_sbc_tbl_united;
		}
		if (table.equalsIgnoreCase(Table.pt_vo_buy.name())) {
			return Table.pt_vo_buy;
		}
		if (table.equalsIgnoreCase(Table.pt_vo_watch_history.name())) {
			return Table.pt_vo_watch_history;
		}
		return null;
	}

	public String pagingSql(int limit, int rowNum) {
		StringBuilder sb = new StringBuilder(
				String.format("SELECT m.*, rownum r FROM %s.%s m",
						getSourceSchema().toUpperCase(), getTable().toUpperCase()));
		if (!emptyWhere()) {
			sb.append(" ").append(getWhereStm());
		}

		return "SELECT * from (" + sb + String.format(" AND rownum <= %s) WHERE r > %s", rowNum + limit, rowNum);
	}

	public enum SearchType {
		ALL, BY_ID, CUSTOM
	}

	public enum Table {
		xcion_sbc_tbl_united, pt_vo_buy, pt_vo_watch_history;
	}
}
