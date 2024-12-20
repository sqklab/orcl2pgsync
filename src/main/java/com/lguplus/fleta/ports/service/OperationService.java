package com.lguplus.fleta.ports.service;

import com.lguplus.fleta.domain.dto.operation.OperationSummary;
import com.lguplus.fleta.domain.dto.ui.OperationResponse;
import com.lguplus.fleta.domain.model.operation.OperationProcessEntity;
import com.lguplus.fleta.domain.service.exception.DatasourceNotFoundException;
import com.lguplus.fleta.domain.service.exception.InvalidPrimaryKeyException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Pageable;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface OperationService {

	void register();

	void deleteDiffResult(String table, List<String> ids);

	void deleteDiffResult(String table, String session, String condition);

	void deleteProcess(String table, String session, String condition);

	Map<String, Map<String, Object>> executePart(String datasource, String sql, String primaryKeys, Set<String> pKeySet, String table, boolean upCase) throws Exception;

	Map<String, Map<String, Object>> getByIdIn(String datasource, String sql, String primaryKeys, Set<String> pKeySet, String table, List<String> keys, boolean upCase) throws Exception;

	Map<String, Map<String, Object>> execute(String datasource, String sql, String primaryKeys) throws Exception;

	String getPrimaryKeys(String datasource, String schema, String table) throws SQLException, DatasourceNotFoundException;

	List<String> getValueByIds(String datasource, String schema, String table, String columnName, String value) throws SQLException, DatasourceNotFoundException;

	List<CorrectionResult> correct(List<CompareDiffItem> diffItems, String session, String whereStm, String database, String schema, String table, List<String> primaryKeys) throws DatasourceNotFoundException, InvalidPrimaryKeyException, SQLException;

	List<CompareDiffItem> getDiffData(List<CompareDiffItem> diffItems, String database, String schema, String targetDatabase, String targetSchema, String table, String primaryKeys) throws Exception;

	OperationResponse paggedLoadSearching(Pageable pageable,
										  String database, String schema, String targetDatabase, String targetSchema,
										  String table, String primaryKeys,
										  String session, String where) throws Exception;

	OperationProcessEntity checkOperation(String table, String sessionId, String whereCondition);

	OperationSummary getOperationSummary(String table, String sessionId, String whereCondition);

	void cancelOperation(String table, String sessionId, String whereCondition);

	void addOperation(String table, String sessionId, String whereCondition);

	@Getter
	@Setter
	@NoArgsConstructor
	class CompareResult {
		private boolean isSame;
		private List<Object[]> diff;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	class CompareDiffItem {
		private Map<String, Object> source;
		private Map<String, Object> target;
		private CorrectOperation operation;
		private UUID code;
		private String uuid;
		private String pKey;

		public CompareDiffItem(Map<String, Object> source, Map<String, Object> target, CorrectOperation operation, UUID code, String pKey) {
			this.source = source;
			this.target = target;
			this.operation = operation;
			this.code = code;
			this.pKey = pKey;
		}

		public enum CorrectOperation {
			DELETE, INSERT, UPDATE
		}
	}

	@Getter
	@Setter
	@NoArgsConstructor
	class CorrectionResult {
		private boolean success;
		private String errorMessage;
		private UUID code;
		private String pKey;

		public CorrectionResult(boolean success, String errorMessage, UUID code, String pKey) {
			this.success = success;
			this.errorMessage = errorMessage;
			this.code = code;
			this.pKey = pKey;
		}
	}

	@Getter
	@Setter
	@NoArgsConstructor
	class CorrectionBatchResult {
		private boolean success;
		private List<CorrectionResult> exceptions;
	}

}
