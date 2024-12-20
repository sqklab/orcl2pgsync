package com.lguplus.fleta.domain.dto;

import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

public interface SyncInfoDto extends SyncInfoBase {

	@Value("#{target.source_database}")
	String getSourceDatabase();

	@Value("#{target.source_schema}")
	String getSourceSchema();

	@Value("#{target.source_table}")
	String getSourceTable();

	@Value("#{target.created_user}")
	String getCreatedUser();

	@Value("#{target.updated_user}")
	String getUpdatedUser();
	
	@Value("#{target.target_database}")
	String getTargetDatabase();

	@Value("#{target.target_schema}")
	String getTargetSchema();

	@Value("#{target.target_table}")
	String getTargetTable();

	@Value("#{target.state}")
	Integer getState();

	@Value("#{target.division}")
	String getDivision();

	@Value("#{target.enable_truncate}")
	Boolean getEnableTruncate();

	@Value("#{target.created_at}")
	LocalDateTime getCreatedAt();

	@Value("#{target.updated_at}")
	LocalDateTime getUpdatedAt();

	@Value("#{target.total_error}")
	Integer getNumberOfError();

	@Value("#{target.total_resolve}")
	Integer getNumberOfResolve();

	@Value("#{target.total}")
	Integer getNumberOfTotal();

	@Value("#{target.primary_keys}")
	String getPrimaryKeys();

	@Value("#{target.unique_keys}")
	String getUniqueKeys();

	@Value("#{target.is_partitioned}")
	Boolean isPartitioned();
}
