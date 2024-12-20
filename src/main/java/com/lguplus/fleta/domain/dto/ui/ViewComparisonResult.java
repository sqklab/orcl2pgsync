package com.lguplus.fleta.domain.dto.ui;

import com.lguplus.fleta.domain.model.comparison.ComparisonResultEntity;
import lombok.Data;

import java.util.List;

@Data
public class ViewComparisonResult {
	private List<ComparisonResultEntity> entities;
	private int totalPage;
}
