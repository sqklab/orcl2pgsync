package com.lguplus.fleta.domain.dto.ui;

import com.lguplus.fleta.domain.model.comparison.ComparisonResultEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonResultForExport {
	private Map<String, ComparisonResultDto> map;
	private Map<LocalTime, List<ComparisonResultEntity>> rawValues;
	private List<String> headers;
}
