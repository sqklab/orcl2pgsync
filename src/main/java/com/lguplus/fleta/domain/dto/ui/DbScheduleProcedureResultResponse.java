package com.lguplus.fleta.domain.dto.ui;

import lombok.Data;

import java.util.List;

@Data
public class DbScheduleProcedureResultResponse {
	private List<DbSchedulerResultDto> entities;
	private int totalPage;
}
