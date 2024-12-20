package com.lguplus.fleta.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class SyncRequestBatchCountDto {

	private List<SyncRequestMessage> messages;
	private int[] affectedRecords;
	private int[] generatedInsertedRecords;
	private boolean isRollback;
	private int batchSize;

	public SyncRequestBatchCountDto(List<SyncRequestMessage> messages) {
		this.messages = messages;
	}

	public static SyncRequestBatchCountDto of(List<SyncRequestMessage> messages) {
		return new SyncRequestBatchCountDto(messages);
	}

	public String[] getOperations() {
		return messages.stream()
				.map(SyncRequestMessage::getOperation).toArray(String[]::new);
	}

	public int getAffectedRecordsCount() {
		return isRollback || affectedRecords == null ? 0 : (Arrays.stream(affectedRecords).sum());
	}

	public long getExecutedCount() {
		return isRollback || affectedRecords == null ? 0 : Arrays.stream(affectedRecords).filter(r -> r == 1).count();
	}

	public long getNotExecutedCount() {
		return isRollback || affectedRecords == null ? 0 : Arrays.stream(affectedRecords).filter(r -> r == 0).count();
	}

	public long getMultipleExecutedCount() {
		return isRollback || affectedRecords == null ? 0 : Arrays.stream(affectedRecords).filter(r -> r > 1).count();
	}

	public int getGeneratedInsertedCount() {
		return isRollback || generatedInsertedRecords == null ? 0 : Arrays.stream(generatedInsertedRecords).sum();
	}
}
