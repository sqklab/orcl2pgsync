package com.lguplus.fleta.domain.dto.ui;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SimpleDbSchedulerResultDto {
	private int numSuccess;
	private int numFailure;
}
