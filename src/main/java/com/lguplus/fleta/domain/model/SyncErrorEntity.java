package com.lguplus.fleta.domain.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lguplus.fleta.domain.dto.DbSyncOperation;
import com.lguplus.fleta.domain.dto.ErrorType;
import com.lguplus.fleta.domain.dto.Synchronizer.ErrorState;
import com.lguplus.fleta.domain.service.mapper.ObjectMapperFactory;
import com.lguplus.fleta.domain.util.DateUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Slf4j
@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@Table(name = "tbl_sync_task_error")
public class SyncErrorEntity implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;

	@Column(name = "topic_name")
	private String topicName;

	@Column(name = "sync_message")
	private String syncMessage;

	@Column(name = "error_message")
	private String errorMessage;

	@Column(name = "error_time", columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime errorTime;

	@Column(name = "state")
	private ErrorState state;

	@Enumerated(EnumType.STRING)
	private DbSyncOperation operation;

	@Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime updatedAt = DateUtils.getDateTime();

	@Enumerated(EnumType.STRING)
	private ErrorType errorType;

	@Column(name = "last_update")
	private LocalDateTime lastUpdate;

	@Transient
	private String sqlRedo;
	@Transient
	private String sqlRd;

	public SyncErrorEntity(String topicName, String syncMessage, String errorMessage, ErrorType errorType, DbSyncOperation operation) {
		this.topicName = topicName;
		this.syncMessage = syncMessage;
		this.errorMessage = errorMessage;
		this.state = ErrorState.ERROR;
		this.errorTime = DateUtils.getDateTime();
		this.errorType = errorType;
		this.operation = operation;
	}

	public String toJson() {
		ObjectMapper mapper = ObjectMapperFactory.getInstance().getObjectMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (Exception ex) {
			log.warn(ex.getMessage(), ex);
		}
		return null;
	}

	public String toPrettyJson() {
		try {
			ObjectMapper mapper = ObjectMapperFactory.getInstance().getObjectMapper();
			return mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(this);
		} catch (Exception ex) {
			log.warn(ex.getMessage(), ex);
		}
		return null;
	}
}
