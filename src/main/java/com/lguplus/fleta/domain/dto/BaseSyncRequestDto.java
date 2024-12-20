package com.lguplus.fleta.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.domain.util.DateUtils;
import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@TypeDef(
		name = "json", typeClass = JsonType.class
)
public class BaseSyncRequestDto implements Serializable {

	private Long id;

	private String sourceDatabase;

	private String sourceSchema;

	private String sourceTable;

	private String targetDatabase;

	private String targetSchema;

	private String targetTable;

	private String topicName;

	private String synchronizerName;

	private Synchronizer.SyncState state = Synchronizer.SyncState.PENDING;

	private String division;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonDeserialize(using = SyncRequestEntity.LocalDateTimeDeserializer.class)
	@JsonSerialize(using = SyncRequestEntity.LocalDateTimeSerializer.class)
	private LocalDateTime createdAt;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonDeserialize(using = SyncRequestEntity.LocalDateTimeDeserializer.class)
	@JsonSerialize(using = SyncRequestEntity.LocalDateTimeSerializer.class)
	private LocalDateTime updatedAt = DateUtils.getDateTime();

	@Column(name = "primary_keys")
	private String primaryKeys;

	@Column(name = "is_partitioned")
	private Boolean isPartitioned;
}
