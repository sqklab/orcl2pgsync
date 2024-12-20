package com.lguplus.fleta.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_db_schedule_procedure_result")
public class DbSchedulerResult implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "fk_id_schedule_procedure")
	private Long fkIdProcedure;

	@Column(name = "error_msg")
	private String errorMsg;

	@Column(name = "schedule_time")
	private LocalTime scheduleTime;

	@Column(name = "schedule_date")
	private LocalDate scheduleDate;

	@Column(name = "status")
	private Boolean status;

	@Column(name = "start_at")
	private LocalDateTime startAt;

	@Column(name = "end_at")
	private LocalDateTime endAt;
}
