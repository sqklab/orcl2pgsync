package com.lguplus.fleta.domain.model.comparison;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Immutable;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "view_comparison_result")
public class ComparisonResultEntity {

	private Integer id;
	private Integer syncId;
	private String synchronizerName;
	private String topicName;
	private String sourceDatabase;
	private String sourceSchema;
	private String sourceTable;
	private String targetDatabase;
	private String targetSchema;
	private String targetTable;
	private String sourceQuery;
	private String targetQuery;
	private String sourceCompareDatabase;
	private String targetCompareDatabase;
	@Id
	private Integer compareResultId;
	private LocalDate compareDate;
	private LocalTime compareTime;
	private String division;
	private String errorMsgSource;
	private String errorMsgTarget;
	private LocalDateTime lastModified;
	private Long sourceCount;
	private ComparisonState comparisonState;
	private Long targetCount;
	@Transient
	private Long scn;
	@Transient
	private Long commitScn;
	@Transient
	private Long msgTimestamp;
	@Transient
	private LocalTime receivedTime;
	@Transient
	private LocalDate receivedDate;
	@Transient
	private String receivedDateTime;

	private Long numberDiff;

	public Long getNumberDiff() {
		return this.getSourceCount() - this.getTargetCount();
	}

	// do not remove it!
	public String getReceivedDateTime() {
		if (this.getReceivedDate() != null && this.getReceivedTime() != null) {
			this.setReceivedDateTime(this.getReceivedDate().toString() + " / " + this.getReceivedTime().toString());
		}
		return receivedDateTime;
	}

	@Getter
	public enum ComparisonState {
		SAME(0), DIFFERENT(1), FAILED(2);

		private final int state;

		ComparisonState(int state) {
			this.state = state;
		}

		public static ComparisonState checkState(long sourceCount, long targetCount) {
			if (sourceCount < 0 || targetCount < 0) {
				return FAILED;
			}
			if (sourceCount == targetCount) {
				return SAME;
			} else {
				return DIFFERENT;
			}
		}
	}

}
