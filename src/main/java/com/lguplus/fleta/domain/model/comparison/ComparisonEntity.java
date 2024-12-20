package com.lguplus.fleta.domain.model.comparison;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "view_comparison_info")
public class ComparisonEntity {
	@Id
	@Column(name = "id")
	private Integer id;
	@Column(name = "sync_id")
	private Integer syncId;
	@Column(name = "synchronizer_name")
	private String synchronizerName;
	@Column(name = "source_database")
	private String sourceDatabase;
	@Column(name = "source_schema")
	private String sourceSchema;
	@Column(name = "source_table")
	private String sourceTable;
	@Column(name = "target_database")
	private String targetDatabase;
	@Column(name = "target_schema")
	private String targetSchema;
	@Column(name = "target_table")
	private String targetTable;
	@Column(name = "source_compare_database")
	private String sourceCompareDatabase;
	@Column(name = "target_compare_database")
	private String targetCompareDatabase;
	@Column(name = "source_query")
	private String sourceQuery;
	@Column(name = "target_query")
	private String targetQuery;
	@Column(name = "is_comparable")
	private String isComparable;
	@Column(name = "enable_column_comparison")
	private Boolean enableColumnComparison;
}
