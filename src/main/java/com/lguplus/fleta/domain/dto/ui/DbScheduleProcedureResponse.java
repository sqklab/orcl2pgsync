package com.lguplus.fleta.domain.dto.ui;

import com.lguplus.fleta.domain.model.DbScheduler;
import lombok.Data;

import java.util.List;

@Data
public class DbScheduleProcedureResponse {
	private List<DbScheduler> entities;
	private int totalPage;
}
