package com.lguplus.fleta.domain.model.comparison;

import com.lguplus.fleta.domain.service.comparison.ComparerHelper;
import com.lguplus.fleta.domain.service.constant.Constants;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@Table(name = "tbl_db_comparison_result")
public class DbComparisonResultEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;

	@OneToOne
	@JoinColumn(name = "sync_compare_id", referencedColumnName = "id")
	private DbComparisonInfoEntity dbComparisonInfo;

	@Column(name = "compare_date")
	private LocalDate compareDate;
	@Column(name = "compare_time")
	private LocalTime compareTime;

	@Column(name = "source_count")
	private Long sourceCount;
	@Column(name = "target_count")
	private Long targetCount;
	@Column(name = "comparison_state")
	private ComparisonResultEntity.ComparisonState comparisonState;
	@Column(name = "last_modified")
	private LocalDateTime lastModified;
	@Column(name = "notified")
	private Boolean notified;
	@Column(name = "error_msg_source")
	private String errorMsgSource;
	@Column(name = "error_msg_target")
	private String errorMsgTarget;

	private DbComparisonResultEntity(Long id, DbComparisonInfoEntity dbComparisonInfo, LocalDate compareDate, LocalTime compareTime,
									Long sourceCount, Long targetCount, ComparisonResultEntity.ComparisonState comparisonState,
									LocalDateTime lastModified, String errorMsgSource, String errorMsgTarget) {
		this.id = id;
		this.dbComparisonInfo = dbComparisonInfo;
		this.compareDate = compareDate;
		this.compareTime = compareTime;
		this.sourceCount = sourceCount > 0 ? sourceCount : 0;
		this.comparisonState = comparisonState;
		this.targetCount = targetCount > 0 ? targetCount : 0;
		this.lastModified = lastModified;
		this.errorMsgSource = errorMsgSource;
		this.errorMsgTarget = errorMsgTarget;
	}

	public DbComparisonResultEntity(DbComparisonInfoEntity comparisonInfo, LocalDate date, LocalTime time, LocalDateTime runDate){
		this(null, comparisonInfo, date, time, -1L, -1L, ComparisonResultEntity.ComparisonState.FAILED, runDate, "", "");
	}

	public void updateCompare(ComparerHelper.ComparisonResult comparisonResult){
		final ComparerHelper.CountResult source = comparisonResult.getSourceCount();
		final ComparerHelper.CountResult target = comparisonResult.getTargetCount();

		comparisonState = ComparisonResultEntity.ComparisonState.checkState(source.getCount(), target.getCount());
		sourceCount = source.getCount();
		targetCount = target.getCount();
		errorMsgSource = source.getErrorMessage();
		errorMsgTarget = target.getErrorMessage();
		lastModified = LocalDateTime.now(Constants.ZONE_ID);
	}

	public void setSourceCount(Long sourceCount) {
		this.sourceCount = sourceCount > 0 ? sourceCount : 0;
	}

	public void setTargetCount(Long targetCount) {
		this.targetCount = targetCount > 0 ? targetCount : 0;
	}
}
