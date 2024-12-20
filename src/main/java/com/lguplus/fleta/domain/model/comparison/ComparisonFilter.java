package com.lguplus.fleta.domain.model.comparison;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComparisonFilter {
	private LocalDate compareDate;
	private LocalTime compareTime;
	private String sourceDB;
	private String sourceSchema;
	private String targetDB;
	private String targetSchema;
	private String sourceTable;
	private String targetTable;
	private List<ComparisonResultEntity.ComparisonState> state;
	private String division;
}
