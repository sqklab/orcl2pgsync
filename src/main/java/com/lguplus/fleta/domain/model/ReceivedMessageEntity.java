package com.lguplus.fleta.domain.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_received_message")
public class ReceivedMessageEntity implements Serializable {
	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
			name = "UUID",
			strategy = "org.hibernate.id.UUIDGenerator"
	)
	@Column(name = "id", updatable = false, nullable = false)
	private String id;

	@NotNull
	@Column(name = "topic")
	private String topic;

	@NotNull
	@Column(name = "received_date")
	private LocalDate receivedDate;

	@NotNull
	@Column(name = "received_time")
	private LocalTime receivedTime;

	@Column(name = "scn")
	private Long scn;

	@Column(name = "commit_scn")
	private Long commitScn;

	@Column(name = "msg_timestamp")
	private Long msgTimestamp;

	@Column(name = "msg_latency")
	private Long msgLatency;

	public ReceivedMessageEntity(String kafkaTopic, LocalDate receivedDate, LocalTime receivedTime) {
		this.topic = kafkaTopic;
		this.receivedDate = receivedDate;
		this.receivedTime = receivedTime;
	}

	public ReceivedMessageEntity(String kafkaTopic, LocalDate receivedDate, LocalTime receivedTime, Long commitScn, Long scn, Long timestamp) {
		this.topic = kafkaTopic;
		this.receivedDate = receivedDate;
		this.receivedTime = receivedTime;

		if (Objects.isNull(commitScn)) {
			this.commitScn = 0L;
		} else {
			this.commitScn = commitScn;
		}

		if (Objects.isNull(scn)) {
			this.scn = 0L;
		} else {
			this.scn = scn;
		}

		if (Objects.isNull(timestamp)) {
			this.msgTimestamp = 0L;
		} else {
			this.msgTimestamp = timestamp;
		}
	}

	public String getId() {
		return id;
	}

	public String getTopic() {
		return topic;
	}

	public LocalDate getReceivedDate() {
		return receivedDate;
	}

	public LocalTime getReceivedTime() {
		return receivedTime;
	}

	public Long getScn() {
		if (Objects.isNull(scn)) return 0L;
		return scn;
	}

	public Long getCommitScn() {
		if (Objects.isNull(commitScn)) return 0L;
		return commitScn;
	}

	public Long getMsgTimestamp() {
		if (Objects.isNull(msgTimestamp)) return 0L;
		return msgTimestamp;
	}
}
