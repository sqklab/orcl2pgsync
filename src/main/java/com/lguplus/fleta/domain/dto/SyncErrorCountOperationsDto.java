package com.lguplus.fleta.domain.dto;

import org.springframework.beans.factory.annotation.Value;

public interface SyncErrorCountOperationsDto {

	@Value("#{target.total_insert}")
	Long getTotalInsert();

	@Value("#{target.total_update}")
	Long getTotalUpdate();

	@Value("#{target.total_delete}")
	Long getTotalDelete();
}
