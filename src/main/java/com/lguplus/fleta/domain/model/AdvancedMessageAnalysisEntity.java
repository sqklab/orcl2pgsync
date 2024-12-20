package com.lguplus.fleta.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_analysis_message_per_minute")
public class AdvancedMessageAnalysisEntity implements Serializable {
	@Id
	@Column(name = "id", updatable = false, nullable = false)
	private String id;

	@NotNull
	@Column(name = "db_name")
	private String dbName;

	@NotNull
	@Column(name = "schm_name")
	private String schmName;

	@NotNull
	@Column(name = "topic")
	private String topic;

	@NotNull
	@Column(name = "received_message")
	private Long receivedMessage;

	@NotNull
	@Column(name = "at_year")
	private Integer atYear;

	@NotNull
	@Column(name = "at_month")
	private Integer atMonth;

	@NotNull
	@Column(name = "at_date")
	private Integer atDate;

	@NotNull
	@Column(name = "at_hour")
	private Integer atHour;

	@NotNull
	@Column(name = "at_minute")
	private Integer atMinute;

	@Column(name = "total_latency")
	private Long totalLatency;
}
