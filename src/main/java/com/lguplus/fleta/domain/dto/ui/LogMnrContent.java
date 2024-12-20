package com.lguplus.fleta.domain.dto.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogMnrContent {
	private String scn;
	private String startScn;
	private String commitScn;
	private String timestamp;
	private String operation;
	private String segOwner;
	private String tableName;
	private String sqlRedo;
	private String sqlUndo;
}
