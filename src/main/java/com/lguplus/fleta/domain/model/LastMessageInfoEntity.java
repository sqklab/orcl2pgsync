package com.lguplus.fleta.domain.model;

import com.lguplus.fleta.domain.dto.LastMessageInfoDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_last_received_message_info")
public class LastMessageInfoEntity implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;

	@Column(name = "topic")
	private String topic;

	@Column(name = "scn")
	private Long scn;

	@Column(name = "commit_scn")
	private Long commitScn;

	@Column(name = "msg_timestamp")
	private Long msgTimestamp;

	@Column(name = "received_date")
	private LocalDate receivedDate;

	@Column(name = "received_time")
	private LocalTime receivedTime;

	public LastMessageInfoEntity(String topic, LocalDate receivedDate, LocalTime receivedTime, Long commitScn, Long scn, Long timestamp) {
		this.topic = topic;
		this.receivedDate = receivedDate;
		this.receivedTime = receivedTime;
		this.commitScn = commitScn;
		this.scn = scn;
		this.msgTimestamp = timestamp;
	}

	public LastMessageInfoEntity(Long id, String topic, LastMessageInfoDto dto) {
		this.id = id;
		this.topic = topic;
		this.receivedDate = dto.getReceivedDateTime().toLocalDate();
		this.receivedTime = dto.getReceivedDateTime().toLocalTime();
		this.commitScn = dto.getCommitScn();
		this.scn = dto.getScn();
		this.msgTimestamp = dto.getMsgTimestamp();
	}
}
