package com.lguplus.fleta.domain.dto.ui;

import com.lguplus.fleta.domain.dto.SyncRequestParam;
import lombok.Data;

import java.util.List;

@Data
public class SyncResponse {
	private List<SyncRequestParam> synchronizationParams;
	private int totalPage;
	/**
	 * if isToShow=true: show warning message, so user need to delete record error in tbl_sync_task_error table, should use UI
	 */
	private Boolean isToSlow;
}
