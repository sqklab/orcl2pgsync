package com.lguplus.fleta.domain.model.comparison;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@ToString
@Table(name = "tbl_db_comparison_result_summary")
public class DbComparisonResultSummaryEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;

	@Column(name = "compare_date")
	private LocalDate compareDate;

	@Column(name = "compare_time")
	private LocalTime compareTime;

	@Column(name = "source_count")
	private Long sourceCount;

	@Column(name = "target_count")
	private Long targetCount;

	@Column(name = "total")
	private Integer total;

	@Column(name = "fail")
	private Long fail;

	@Column(name = "equal")
	private Long equal;

	@Column(name = "different")
	private Long different;

	@Column(name = "msg_dt_behind")
	private Long msgDtBehind;

}
