package com.lguplus.fleta.domain.model;

import com.lguplus.fleta.domain.dto.Synchronizer;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Getter
@NoArgsConstructor
@Table(name = "tbl_synchronizer_history")
public class SynchronizerHistoryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private long historyId;

	@Column(name = "synchronizer_id")
	private long synchronizerId;

	@Column(name = "topic")
	private String topic;

	@Column(name = "sync_json")
	private String syncJson;

	@Column(name = "operation")
	private String operation;

	@Column(name = "sync_state")
	private Synchronizer.SyncState state;

	@Column(name = "time")
	private LocalDateTime time;

	@Builder
	public SynchronizerHistoryEntity(long synchronizerId, String topic, String syncJson, String operation, Synchronizer.SyncState state, LocalDateTime time){
		this.synchronizerId = synchronizerId;
		this.topic = topic;
		this.syncJson = syncJson;
		this.operation = operation;
		this.state = state;
		this.time = time;
	}



}


