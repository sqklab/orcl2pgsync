package com.lguplus.fleta.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lguplus.fleta.domain.dto.Synchronizer;
import com.lguplus.fleta.domain.service.constant.Constants;
import com.lguplus.fleta.domain.util.CommonUtils;
import com.lguplus.fleta.domain.util.DateUtils;
import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@TypeDef(
		name = "json", typeClass = JsonType.class
)
public class BaseSyncRequestEntity extends BaseEntity implements Serializable {

	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@GenericGenerator(name = "CustomIdentityGenerator", strategy = "com.lguplus.fleta.domain.model.CustomIdentityGenerator")
	@GeneratedValue(generator = "CustomIdentityGenerator")
	@Column(unique = true, nullable = false)
	private Long id;

	@NotNull
	@Column(name = "source_database")
	private String sourceDatabase;

	@NotNull
	@Column(name = "source_schema")
	private String sourceSchema;

	@NotNull
	@Column(name = "source_table")
	private String sourceTable;

	@NotNull
	@Column(name = "target_database")
	private String targetDatabase;

	@NotNull
	@Column(name = "target_schema")
	private String targetSchema;

	@NotNull
	@Column(name = "target_table")
	private String targetTable;

	@NotNull
	@Column(name = "topic_name")
	private String topicName;

	@Column(name = "synchronizer_name")
	private String synchronizerName;

	@Column(name = "state")
	private Synchronizer.SyncState state = Synchronizer.SyncState.PENDING;

	@Column(name = "division")
	private String division;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonDeserialize(using = SyncRequestEntity.LocalDateTimeDeserializer.class)
	@JsonSerialize(using = SyncRequestEntity.LocalDateTimeSerializer.class)
	@Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime createdAt;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonDeserialize(using = SyncRequestEntity.LocalDateTimeDeserializer.class)
	@JsonSerialize(using = SyncRequestEntity.LocalDateTimeSerializer.class)
	@Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime updatedAt = DateUtils.getDateTime();
	
	@Column(name = "unique_keys")
	private String uniqueKeys;
	
	@Column(name = "primary_keys")
	private String primaryKeys;

	@Column(name = "is_partitioned")
	private Boolean isPartitioned;

	@Column(name = "consumer_group")
	private String consumerGroup;

	@Column(name = "is_batch", nullable = false)
	private boolean isBatch;

	@Column(name = "is_upsert", nullable = false)
	private boolean isUpsert;

	@Column(name = "is_change_insert", nullable = false)
	private boolean isChangeInsertOnFailureUpdate;

	@Column(name = "is_all_condition_update", nullable = false)
	private boolean isAllColumnConditionsOnUpdate;

	@Column(name = "enable_truncate", nullable = false)
	private boolean enableTruncate;

	@Column(name = "max_poll_records")
	private Integer maxPollRecords;


	public String getConsumerGroup() {
		if (!StringUtils.isEmpty(consumerGroup)) {
			return consumerGroup;
		}
		return this.getId() != null ? CommonUtils.getKafkaGroupId(this.getId()) : consumerGroup;
	}

	public Integer getMaxPollRecords() {
		return maxPollRecords == null ? Constants.DEFAULT_MAX_POLL_RECORDS : maxPollRecords;
	}

	@JsonIgnore
	public List<String> listPrimaryKeys() {
		return StringUtils.isBlank(primaryKeys) ? null :
				Arrays.stream(primaryKeys.split(","))
						.map(String::trim)
						.map(String::toUpperCase)
						.collect(Collectors.toList());
	}

	@JsonIgnore
	public boolean hasNoPrimaryKeys(){
		return StringUtils.isBlank(primaryKeys);
	}

	@JsonIgnore
	public List<String> listUniqueKeys() {
		return StringUtils.isBlank(uniqueKeys) ? null :
				Arrays.stream(uniqueKeys.split(","))
						.map(String::trim)
						.map(String::toUpperCase)
						.collect(Collectors.toList());
	}

	@JsonIgnore
	public boolean hasNoUniqueKeys(){
		return StringUtils.isBlank(primaryKeys);
	}
	
	@JsonIgnore
	public boolean hasNoConstraintKeys(){
		return hasNoPrimaryKeys() && hasNoUniqueKeys();
	}
}
