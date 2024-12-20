package com.lguplus.fleta.domain.model;

import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tbl_db_schedule_procedure")
public class DbScheduler extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "pl_sql")
	private String plSQL;

	@Column(name = "db")
	private String db;

	@Column(name = "schedule_schema")
	private String schema;

	@Column(name = "schedule_table")
	private String table;

	@Column(name = "type")
	private Integer type;

	@Column(name = "day_of_week")
	private String dayOfWeek;

	@Column(name = "times_of_week")
	private String timesOfWeek;

	@Column(name = "time_daily")
	private String timeDaily;

	@Column(name = "monthly")
	private String monthly;

	@Column(name = "quarterly")
	private String quarterly;

	@Column(name = "yearly")
	private String yearly;

	@Column(name = "enable")
	private Boolean status;

	@Column(name = "process_status")
	private Boolean processStatus;

	@Column(name = "last_run")
	private LocalDateTime lastRun;

	public boolean isDaily() {
		return this.type == 0;
	}

	public boolean isWeekly() {
		return this.type == 1;
	}

	public boolean isMonthly() {
		return this.type == 2;
	}

	public boolean isQuarterly() {
		return this.type == 3;
	}

	public boolean isYearly() {
		return this.type == 4;
	}
}
