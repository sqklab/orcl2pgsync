package com.lguplus.fleta.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lguplus.fleta.domain.service.mapper.ObjectMapperFactory;
import com.lguplus.fleta.domain.util.CommonUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Getter
@Setter
@ToString
@NoArgsConstructor
public class SyncRequestMessage implements Comparable<SyncRequestMessage>, Serializable {

	/**
	 * State of synchronization (ERROR / SUCCESS)
	 */
	private SyncRequestState state;

	/**
	 * Synchronization timestamp
	 */
	private Long timestamp;

	private DbSchema schema;

	private Payload payload;

	public SyncRequestMessage(SyncRequestMessage message) {
		if (null == message) return;

		this.state = message.getState();
		this.timestamp = message.getTimestamp();
		this.schema = message.getSchema();
		this.payload = message.getPayload();
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

	@JsonIgnore
	public boolean isInsertOperation() {
		if (null == payload) {
			throw new IllegalStateException("Unknown Operation");
		}
		return payload.isInsert();
	}

	@JsonIgnore
	public boolean isUpdateOperation() {
		if (null == payload) {
			throw new IllegalStateException("Unknown Operation");
		}
		return payload.isUpdate();
	}

	@JsonIgnore
	public boolean isDeleteOperation() {
		if (null == payload) {
			throw new IllegalStateException("Unknown Operation");
		}
		return payload.isDelete();
	}

	@JsonIgnore
	public boolean isTruncateOperation() {
		if (null == payload) {
			throw new IllegalStateException("Unknown Operation");
		}
		return payload.isTruncate();
	}

	public Map<String, DebeziumDataType> buildDataTypeMap(String fieldName) {
		if (StringUtils.isBlank(fieldName)) return null;
		return this.getSchema().buildDataTypeMap(fieldName);
	}

	@JsonIgnore
	public String getSqlRedo() {
		if (null != payload) {
			return payload.getSqlRedo();
		}
		return null;
	}

	@JsonIgnore
	public String getOperation() {
		return payload.getOperation();
	}

	@JsonIgnore
	public String toLog() {
		return payload.toLog();
	}

	public Long getTimestamp() {
		if (null == timestamp && null != payload) {
			timestamp = payload.getTimestamp();
		}
		return timestamp;
	}

	/**
	 * Get current status of sync message
	 *
	 * @return state
	 */
	public SyncRequestState getState() {
		if (null == state) {
			state = SyncRequestState.UNKNOWN;
		}
		return state;
	}

	@JsonIgnore
	public Long getScn() {
		if (Objects.isNull(payload)) return null;
		return payload.getScn();
	}

	@JsonIgnore
	public Long getCommitScn() {
		if (Objects.isNull(payload)) return null;
		return payload.getCommitScn();
	}

	@JsonIgnore
	public Long getProcessedTimestamp() {
		if (Objects.isNull(payload)) return null;
		return payload.getDebeTimestamp();
	}

	@Override
	public int compareTo(SyncRequestMessage syncReq) {
		// FIXME -> SCN can be duplicated.
		if (Objects.isNull(getScn()) || Objects.isNull(syncReq.getScn())) return 0;
		return getScn().compareTo(syncReq.getScn());
	}

	public boolean beforeEqualAfter() {
		if (!isUpdateOperation()) return false;

		Map<String, Object> before = payload.getBefore();
		Map<String, Object> after = payload.getAfter();

		return CommonUtils.beforeEqualAfter(before, after);
	}

	public void addSqlRedo(String sqlRedo) {
		getPayload().setSqlRedo(sqlRedo);
	}

	@Getter
	public enum SyncRequestState {
		SUCCESS,
		ERROR,
		/**
		 * TODO: For handling Exception org.springframework.kafka.listener.ListenerExecutionFailedException:
		 * 	Listener failed; nested exception is java.util.concurrent.RejectedExecutionException
		 */
		UNKNOWN
	}

	@Getter
	@Setter
	@ToString
	@NoArgsConstructor
	public static class Payload {

		@JsonProperty("SCN")
		private Long scn;

		@JsonProperty("COMMIT_SCN")
		private Long commitScn;

		@JsonProperty("SEG_OWNER")
		private String segOwner;

		@JsonProperty("TABLE_NAME")
		private String tableName;

		@JsonProperty("TIMESTAMP")
		private Long timestamp;

		@JsonProperty("SQL_REDO")
		private String sqlRedo;

		@JsonProperty("OPERATION")
		private String operation; // use enum cause bad request and lost message

		private Map<String, Object> before;

		private Map<String, Object> data = new HashMap<>();

		// Debezium
		@JsonProperty("ts_ms")
		private Long debeTimestamp;

		@JsonProperty("op")
		private String debeOperation; // use enum cause bad request and lost message

		private Map<String, Object> after = new HashMap<>();

		private DebeziumEventSource source;

		@JsonIgnore
		public boolean isDebezium() {
			return null != source && null != debeOperation;
		}

		public Long getScn() {
			if (isDebezium()) {
				return source.getScn();
			}
			return scn;
		}

		public Long getCommitScn() {
			if (isDebezium()) {
				return source.getCommitScn();
			}
			return commitScn;
		}

		public String getSegOwner() {
			if (isDebezium()) {
				return source.getSchema();
			}
			return segOwner;
		}

		public String getTableName() {
			if (isDebezium()) {
				return source.getTable();
			}
			return tableName;
		}

		public String getSqlRedo() {
			return sqlRedo;
		}

		public Long getTimestamp() {
			if (isDebezium()) {
				return debeTimestamp;
			}
			return timestamp;
		}

		public String getOperation() {
			if (isDebezium()) {
				return debeOperation;
			}
			return operation;
		}

		@JsonIgnore
		public boolean isInsert() {
			if (isDebezium()) {
				return DbSyncOperation.c.name().equals(debeOperation);
			}
			if (null == operation) {
				return false;
			}
			return DbSyncOperation.INSERT.name().equals(operation.toUpperCase());
		}

		@JsonIgnore
		public boolean isUpdate() {
			if (isDebezium()) {
				return DbSyncOperation.u.name().equals(debeOperation);
			}
			if (null == operation) {
				return false;
			}
			return DbSyncOperation.UPDATE.name().equals(operation.toUpperCase());
		}

		@JsonIgnore
		public boolean isDelete() {
			if (isDebezium()) {
				return DbSyncOperation.d.name().equals(debeOperation);
			}
			if (null == operation) {
				return false;
			}
			return DbSyncOperation.DELETE.name().equals(operation.toUpperCase());
		}

		@JsonIgnore
		public boolean isTruncate() {
			if (isDebezium()) {
				return DbSyncOperation.t.name().equals(debeOperation);
			}
			if (null == operation) {
				return false;
			}
			return DbSyncOperation.TRUNCATE.name().equals(operation.toUpperCase());
		}

		public Map<String, Object> getBefore() {
			return before;
		}

		public Map<String, Object> getData() {
			return data;
		}

		public Map<String, Object> getAfter() {
			return after;
		}

		public DebeziumEventSource getSource() {
			return source;
		}

		public String toLog() {
			if (isDebezium()) {
				return "operation=" + debeOperation + ", " + source.toLog() + ", timestamp=" + debeTimestamp;
			}
			return "operation=" + operation + ", scn=" + scn + ", timestamp=" + timestamp;
		}

		@Getter
		@Setter
		@NoArgsConstructor
		public static class DebeziumEventSource {
			private String version;
			private String connector;
			private String name;
			private String db;
			private String schema;
			private String table;
			private String sequence;
			@JsonProperty("ts_ms")
			private String tsMs;
			private String txId;
			private Long scn;
			@JsonProperty("commit_scn")
			private Long commitScn;
			private String snapshot;
			@JsonProperty("lcr_position")
			private String lcrPosition;
			private Long lsn;

			public Long getScn() {
				return null == scn ? lsn : scn;
			}

			public void setScn(String scn) {
				if (StringUtils.isBlank(scn)) return;
				try {
					this.scn = Long.parseLong(scn);
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}

			public void setCommitScn(String commitScn) {
				if (StringUtils.isBlank(commitScn)) return;
				try {
					this.commitScn = Long.parseLong(commitScn);
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}

			@JsonIgnore
			public String toLog() {
				return "scn=" + getScn() + ", commit_scn=" + commitScn + ", txId=" + txId;
			}
		}
	}
}
