package com.lguplus.fleta.domain.dto.ui;

import com.lguplus.fleta.ports.service.OperationService;
import lombok.Data;

import java.util.List;

@Data
public class OperationResponse {
	private List<OperationService.CompareDiffItem> entities;
	private int totalPage;
	private boolean state;
	private String primaryKeys;
}
