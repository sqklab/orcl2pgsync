package com.lguplus.fleta.domain.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
public class ColumnCompare {
	private String oracleColumn;
	private String postgresColumn;
	private int count;

	public void increase() {
		count = count + 1;
	}

	public String getNotBlankColumn() {
		return StringUtils.isBlank(oracleColumn) ? postgresColumn : oracleColumn;
	}
}
