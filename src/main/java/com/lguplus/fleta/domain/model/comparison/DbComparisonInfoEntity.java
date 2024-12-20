package com.lguplus.fleta.domain.model.comparison;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "tbl_db_comparison_info")
public class DbComparisonInfoEntity implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "sync_id", referencedColumnName = "id")
	@JsonIgnoreProperties("dbComparisonInfoEntities")
	private SyncRequestEntity syncInfo;

	@Column(name = "source_query")
	private String sourceQuery;

	@Column(name = "target_query")
	private String targetQuery;

	@Column(name = "source_compare_database")
	private String sourceCompareDatabase;

	@Column(name = "target_compare_database")
	private String targetCompareDatabase;

	private CompareInfoState state;

	@Column(name = "is_comparable")
	private String isComparable;

	private Boolean enableColumnComparison;

	private LocalDateTime lastRun;

	@Column(name="comparison_order")
	private Integer comparisonOrder;

	@PrePersist
	void prePersist() {
		if (state == null) {
			state = CompareInfoState.NOT_RUNNING;
		}
		if (null != lastRun) {
			lastRun.truncatedTo(ChronoUnit.SECONDS);
		}
	}


	@Getter
	public enum CompareInfoState {
		NOT_RUNNING(0), RUNNING(1);

		private final int state;

		CompareInfoState(int state) {
			this.state = state;
		}
	}
}
