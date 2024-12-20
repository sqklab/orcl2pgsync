package com.lguplus.fleta.domain.dto.ui;

import com.lguplus.fleta.domain.dto.ErrorType;
import com.lguplus.fleta.domain.dto.Synchronizer;
import com.lguplus.fleta.domain.model.SyncErrorEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SyncErrorDto {
	private List<SyncErrorEntity> syncErrorEntities;
	private int totalPage;
	private String environment;
	private String errorVersion;
	private ErrorType[] allTypes;
	private Synchronizer.ErrorState[] errorStates;

	public Synchronizer.ErrorState[] getErrorStates() {
		return Synchronizer.ErrorState.values();
	}

	public ErrorType[] getAllTypes() {
		return ErrorType.values();
	}
}
