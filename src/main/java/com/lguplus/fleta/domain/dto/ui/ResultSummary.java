package com.lguplus.fleta.domain.dto.ui;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResultSummary {
	private long success;
	private long failure;
	private long total;
}
