package com.lguplus.fleta.domain.dto;

import com.lguplus.fleta.domain.dto.Synchronizer.SyncState;
import com.lguplus.fleta.domain.model.LinkSyncRequest;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SyncRequestParam {

	private Long id;

	private String sourceDatabase;

	private String sourceSchema;

	private String sourceTable;

	private String targetDatabase;

	private String targetSchema;

	private String targetTable;

	private String topicName;

	private String synchronizerName;

	private String logFile;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private SyncState state;

	private String division;

	private Integer numberOfError;
	private Integer numberOfResolve;
	private Integer numberOfToTal;

	@Builder.Default
	private String syncType = "DT";

	private String sourceQuery;

	private String targetQuery;

	private String sourceCompareDatabase;

	private String targetCompareDatabase;

	private Integer comparisonId;

	private Boolean isComparable;

	private Boolean enableColumnComparison;

	private Long comparisonInfoId;

	@Builder.Default
	private List<Map<String, String>> syncRdRequestParams = new ArrayList<>();

	private LinkSyncRequest linkSyncRequest;

	private Long scn;

	private Long commitScn;

	private String operation;

	private Long msgTimestamp;

	private LocalTime receivedTime;

	private LocalDate receivedDate;

	private String receivedDateTime;

	private String createdUser;

	private String updatedUser;

	private String primaryKeys;

	private String uniqueKeys;

	private Boolean isPartitioned;

	private String consumerGroup;

	private Boolean isBatch;

	private Boolean isUpsert;

	private Boolean isChangeInsertOnFailureUpdate;

	private Boolean isAllColumnConditionsOnUpdate;

	private Boolean enableTruncate;

	private Integer maxPollRecords;

	public static SyncRequestParam buildFromProjection(SyncInfoDto syncInfo) {
		try {
			SyncRequestParam response = new SyncRequestParam();
			response.setId(syncInfo.getId());
			response.setSourceDatabase(syncInfo.getSourceDatabase());
			response.setSourceSchema(syncInfo.getSourceSchema());
			response.setSourceTable(syncInfo.getSourceTable());
			response.setTargetDatabase(syncInfo.getTargetDatabase());
			response.setTargetSchema(syncInfo.getTargetSchema());
			response.setTargetTable(syncInfo.getTargetTable());
			response.setTopicName(syncInfo.getTopicName());
			response.setCreatedAt(syncInfo.getCreatedAt());
			response.setUpdatedAt(syncInfo.getUpdatedAt());
			//TOTO: Modified by Thong on 13/10/2021
			response.setState(SyncState.getState(syncInfo.getState()));
			response.setDivision(syncInfo.getDivision());
			response.setNumberOfError(syncInfo.getNumberOfError());
			response.setNumberOfResolve(syncInfo.getNumberOfResolve());
			response.setNumberOfToTal(syncInfo.getNumberOfTotal());
			response.setSynchronizerName(Objects.nonNull(syncInfo.getSynchronizerName()) ? syncInfo.getSynchronizerName() : syncInfo.getTopicName());
			response.setPrimaryKeys(syncInfo.getPrimaryKeys());
			response.setUniqueKeys(syncInfo.getUniqueKeys());
			response.setIsPartitioned(syncInfo.isPartitioned());
			response.setEnableTruncate(syncInfo.getEnableTruncate());
			response.setCreatedUser(syncInfo.getCreatedUser());
			response.setUpdatedUser(syncInfo.getUpdatedUser());
			if (syncInfo instanceof SyncInfoByLastReceivedTimeDto) {
				SyncInfoByLastReceivedTimeDto lastReceivedTimeDto = (SyncInfoByLastReceivedTimeDto) syncInfo;
				response.setScn(lastReceivedTimeDto.getScn());
				response.setCommitScn(lastReceivedTimeDto.getCommitScn());
				response.setMsgTimestamp(lastReceivedTimeDto.getMsgTimestamp());
				response.setReceivedTime(lastReceivedTimeDto.getReceivedTime());
				response.setReceivedDate(lastReceivedTimeDto.getReceivedDate());
			}
			return response;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	// do not remove it!
	public String getReceivedDateTime() {
		if (this.getReceivedDate() != null && this.getReceivedTime() != null) {
			this.setReceivedDateTime(this.getReceivedDate().toString() + " / " + this.getReceivedTime().toString());
		}
		return receivedDateTime;
	}

	public Boolean getIsComparable() {
		if (this.isComparable == null) {
			return false;
		}
		return isComparable;
	}

	public Boolean getIsPartitioned() {
		if (this.isPartitioned == null) {
			return false;
		}
		return isPartitioned;
	}

	public Boolean getIsBatch() {
		if (this.isBatch == null) {
			return false;
		}
		return isBatch;
	}

	public Boolean getIsUpsert() {
		if (this.isUpsert == null) {
			return false;
		}
		return isUpsert;
	}

	public Boolean getIsChangeInsertOnFailureUpdate() {
		if (this.isChangeInsertOnFailureUpdate == null) {
			return false;
		}
		return isChangeInsertOnFailureUpdate;
	}

	public Boolean getIsAllColumnConditionsOnUpdate() {
		if (this.isAllColumnConditionsOnUpdate == null) {
			return false;
		}
		return isAllColumnConditionsOnUpdate;
	}

	public Boolean getEnableTruncate() {
		if (this.enableTruncate == null) {
			return false;
		}
		return enableTruncate;
	}
}
