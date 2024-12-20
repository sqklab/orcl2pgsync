package com.lguplus.fleta.domain.dto;

import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

public interface SyncExportInfoDto extends SyncInfoBase {

	@Value("#{target.source_database}")
	String getSourceDatabase();

	@Value("#{target.source_schema}")
	String getSourceSchema();

	@Value("#{target.source_table}")
	String getSourceTable();

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

	@Value("#{target.created_at}")
	LocalDateTime getCreatedAt();

	@Value("#{target.updated_at}")
	LocalDateTime getUpdatedAt();

	@Value("#{target.consumer_group}")
	String getConsumerGroup();

	@Value("#{target.is_comparable}")
	String isComparable();

	@Value("#{target.enable_truncate}")
	Boolean getEnableTruncate();

	@Value("#{target.enable_column_comparison}")
	Boolean getEnableColumnComparison();

	@Value("#{target.source_compare_database}")
	String getSourceCompareDatabase();

	@Value("#{target.target_compare_database}")
	String getTargetCompareDatabase();

	@Value("#{target.source_query}")
	String getSourceQuery();

	@Value("#{target.target_query}")
	String getTargetQuery();

	@Value("#{target.primary_keys}")
	String getPrimaryKeys();

	@Value("#{target.sync_type}")
	String getSyncType();
}
