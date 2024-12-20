package com.lguplus.fleta.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.lguplus.fleta.adapters.rest.SyncRequestHelper;
import com.lguplus.fleta.domain.dto.SyncRequestParam;
import com.lguplus.fleta.domain.dto.Synchronizer.SyncState;
import com.lguplus.fleta.domain.model.comparison.DbComparisonInfoEntity;
import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Note: update SyncInfoCache if needed
 */

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_sync_task_info")
@TypeDef(
		name = "json", typeClass = JsonType.class
)
public class SyncRequestEntity extends BaseSyncRequestEntity implements Serializable {

	/**
	 * IMPORTANT: need to set null in SyncRequestServiceImpl.findByTopicNameFromCache to avoid Infinite recursion
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "syncInfo", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnoreProperties("syncInfo")
	private List<DbComparisonInfoEntity> dbComparisonInfoEntities = new ArrayList<>();

	public void addComparisonEntity(DbComparisonInfoEntity comparisonInfo) {
		dbComparisonInfoEntities.add(comparisonInfo);
	}

	public SyncRequestParam toSyncParam() {
		DbComparisonInfoEntity dbComparison = findDbComparison();
		Long comparisonId = dbComparison != null ? dbComparison.getId() : null;
		return SyncRequestParam.builder()
				.id(this.getId())
				.sourceDatabase(this.getSourceDatabase())
				.sourceSchema(this.getSourceSchema())
				.sourceTable(this.getSourceTable())
				.targetDatabase(getTargetDatabase())
				.targetSchema(this.getTargetSchema())
				.targetTable(this.getTargetTable())
				.state(this.getState())
				.division(this.getDivision())
				.createdAt(getCreatedAt())
				.updatedAt(getUpdatedAt())
				.topicName(this.getTopicName())
				.synchronizerName(this.getSynchronizerName())
				.isComparable(dbComparison != null && SyncRequestHelper.isComparable(dbComparison.getIsComparable()))
				.enableColumnComparison(dbComparison != null && SyncRequestHelper.getEnableColumnComparison(dbComparison.getEnableColumnComparison()))
				.sourceQuery(dbComparison != null ? dbComparison.getSourceQuery() : "")
				.sourceCompareDatabase(dbComparison != null ? dbComparison.getSourceCompareDatabase() : "")
				.targetCompareDatabase(dbComparison != null ? dbComparison.getTargetCompareDatabase() : "")
				.targetQuery(dbComparison != null ? dbComparison.getTargetQuery() : "")
				.comparisonInfoId(comparisonId)
				.primaryKeys(this.getPrimaryKeys())
				.uniqueKeys(this.getUniqueKeys())
				.isPartitioned(this.getIsPartitioned())
				.consumerGroup(this.getConsumerGroup())
				.isBatch(this.isBatch())
				.isUpsert(this.isUpsert())
				.isChangeInsertOnFailureUpdate(this.isChangeInsertOnFailureUpdate())
				.isAllColumnConditionsOnUpdate(this.isAllColumnConditionsOnUpdate())
				.enableTruncate(this.isEnableTruncate())
				.maxPollRecords(this.getMaxPollRecords())
				.createdUser(this.getCreatedUser())
				.updatedUser(this.getUpdatedUser())
				.build();
	}

	@JsonIgnore
	private DbComparisonInfoEntity findDbComparison() {
		return getDbComparisonInfoEntities().stream().findFirst().orElse(null);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		final SyncRequestEntity other = (SyncRequestEntity) obj;
		return Objects.equals(this.getTopicName(), other.getTopicName());
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 53 * hash + (this.getTopicName() != null ? this.getTopicName().hashCode() : 0);
		return hash;
	}

	public boolean isIn(SyncState... states) {
		return getState().isIn(states);
	}

	public boolean hasPrimaryKeys() {
		return Objects.nonNull(getPrimaryKeys()) && !getPrimaryKeys().isEmpty();
	}
	public boolean hasUniqueKeys() {
		return Objects.nonNull(getUniqueKeys()) && !getUniqueKeys().isEmpty();
	}

	public static class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

		@Override
		public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeString(value.toString());
		}
	}

	public static class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
		@Override
		public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
			return LocalDateTime.parse(parser.getText());
		}
	}

	public static SyncRequestEntity of(String targetSchema, String targetTable, String primaryKeys, boolean isUpsert) {
		SyncRequestEntity syncRequest = new SyncRequestEntity();
		syncRequest.setTargetSchema(targetSchema);
		syncRequest.setTargetTable(targetTable);
		syncRequest.setUpsert(isUpsert);
		syncRequest.setPrimaryKeys(primaryKeys);
		return syncRequest;
	}
}
