package com.lguplus.fleta.domain.service.convertor;

import com.lguplus.fleta.domain.dto.DebeziumDataType;
import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import lombok.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.*;
import java.util.stream.Collectors;

public class DebeziumPayloadValues {

	private final DebeziumDataReceiver receiver;

	@Getter(AccessLevel.PRIVATE)
	private final boolean isPrimaryKeyConstraint;
	@Getter(AccessLevel.PRIVATE)
	private final boolean isUniqueKeyConstraint;
	@Getter(AccessLevel.PRIVATE)
	private final Map<String, DebeziumPayloadValue> before;
	@Getter(AccessLevel.PRIVATE)
	private final Map<String, DebeziumPayloadValue> after;

	@Getter(AccessLevel.PRIVATE)
	private final List<String> allColumnNameList;
	@Getter(AccessLevel.PRIVATE)
	private final List<String> constraintColumnNameList;
	@Getter(AccessLevel.PRIVATE)
	private final List<String> noneConstraintColumnNameList;
	@Getter(AccessLevel.PACKAGE)
	private final boolean isConstraint;
	private final boolean isAllColumnConditionsOnUpdate;


	public DebeziumPayloadValues(SyncRequestMessage message, DebeziumDataReceiver receiver, List<String> primaryKeys, List<String> uniqueKeys, boolean isAllColumnConditionsOnUpdate) {
		this.receiver = receiver;
		this.isAllColumnConditionsOnUpdate = isAllColumnConditionsOnUpdate;

		Map<String, DebeziumDataType> dataTypeMap = message.buildDataTypeMap("before");
		this.before = payloadValueMap(dataTypeMap, message.getPayload().getBefore());
		this.after = payloadValueMap(dataTypeMap, message.getPayload().getAfter());
		this.allColumnNameList = new ArrayList<>(dataTypeMap.keySet());

		this.isPrimaryKeyConstraint = CollectionUtils.isNotEmpty(primaryKeys);
		this.isUniqueKeyConstraint = CollectionUtils.isNotEmpty(uniqueKeys);
		this.constraintColumnNameList = isPrimaryKeyConstraint ? primaryKeys : uniqueKeys;

		if (CollectionUtils.isEmpty(this.constraintColumnNameList)) {
			this.noneConstraintColumnNameList = new ArrayList<>();
		} else {
			this.noneConstraintColumnNameList = this.allColumnNameList.stream()
					.filter(col -> constraintColumnNameList.stream().noneMatch(col::equalsIgnoreCase))
					.collect(Collectors.toCollection(ArrayList::new));
		}

		if (isPrimaryKeyConstraint) {
			this.isConstraint = true;
		} else if (isUniqueKeyConstraint && CollectionUtils.isNotEmpty(this.constraintColumnNameList)) {
			Map<String, DebeziumPayloadValue> valueMap = message.getOperation().equals("c") ? getAfter() : getBefore();
			this.isConstraint = constraintColumnNameList.stream()
					.map(valueMap::get)
					.anyMatch(Objects::nonNull);
		} else {
			this.isConstraint = false;
		}
	}

	private Map<String, DebeziumPayloadValue> payloadValueMap(Map<String, DebeziumDataType> dataTypeMap, Map<String, Object> payloadValues) {
		Map<String, DebeziumPayloadValue> result = new LinkedCaseInsensitiveMap<>();
		if (MapUtils.isNotEmpty(payloadValues)) {
			result.putAll(payloadValues.keySet().stream()
					.map(colName -> Map.entry(
							colName,
							new DebeziumPayloadValue(
									dataTypeMap.get(colName),
									payloadValues.get(colName)
							)
					))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
			);
		}
		return result;
	}

	public DebeziumSqlBuilder sqlBuild() {
		return new DebeziumSqlBuilder(this);
	}

	public String buildSqlClause(Map<String, DebeziumPayloadValue> payloadValueMap, List<String> columnOrder, boolean isDivByComma, boolean hasColName, boolean useIsNull) {
		return buildSqlClause(payloadValueMap, columnOrder, isDivByComma, hasColName, useIsNull, "");
	}

	public String buildSqlClause(Map<String, DebeziumPayloadValue> payloadValueMap, List<String> columnOrder, boolean isDivByComma, boolean hasColName, boolean useIsNull, String alias) {
		final String divider = isDivByComma ? ", " : " and ";
		final String alias_ = StringUtils.isBlank(alias) ? "" : (alias + ".");
		return columnOrder.stream()
				.map(columnName -> {
					StringBuilder sb = new StringBuilder();
					String sqlClause = receiver.valueToSqlClause(
							payloadValueMap.get(columnName).debeziumValue,
							payloadValueMap.get(columnName).type
					);
					if (hasColName) {
						sb.append(alias_);
						sb.append(columnName);
						sb.append(Objects.isNull(sqlClause) && useIsNull ? " is " : "=");
					}
					sb.append(Objects.nonNull(sqlClause) ? sqlClause : "null");
					return sb;
				})
				.collect(Collectors.joining(divider));

	}

	public String getAllColumnNameStrList() {
		return String.join(", ", allColumnNameList);
	}

	public String getConstraintColumnNameStrList() {
		return String.join(", ", constraintColumnNameList);
	}

	@Override
	public String toString() {
		return "{after={" + after.toString() + ", before={" + before.toString() + "}}";
	}

	protected static class DebeziumPayloadValue {
		private final DebeziumDataType type;
		private final Object debeziumValue;

		public DebeziumPayloadValue(DebeziumDataType type, Object debeziumValue) {
			this.type = type;
			this.debeziumValue = debeziumValue;
		}

		@Override
		public String toString() {
			return "{type=" + type.getSuperType() + ", debeziumValue=" + debeziumValue + '}';
		}
	}

	@RequiredArgsConstructor
	public static class DebeziumSqlBuilder {
		private final DebeziumPayloadValues payload;

		public String getInsertValuesPart() {
			return payload.buildSqlClause(
					payload.getAfter(),
					payload.getAllColumnNameList(),
					true,
					false,
					false
			);
		}

		public String getUpdateSetSqlPart() {
			List<String> columnOrder;
			if (payload.isConstraint() && CollectionUtils.isNotEmpty(payload.getNoneConstraintColumnNameList())) {
				columnOrder = payload.getNoneConstraintColumnNameList();
			} else {
				columnOrder = payload.getAllColumnNameList();
			}
			return payload.buildSqlClause(
					payload.getAfter(),
					columnOrder,
					true,
					true,
					false
			);
		}

		public String getWhereSqlPart() {
			List<String> columnOrder;
			if (payload.isConstraint() && CollectionUtils.isNotEmpty(payload.getConstraintColumnNameList()) && !payload.isAllColumnConditionsOnUpdate) {
				columnOrder = payload.getConstraintColumnNameList();
			} else {
				columnOrder = payload.getAllColumnNameList();
			}
			return payload.buildSqlClause(
					payload.getBefore(),
					columnOrder,
					false,
					true,
					true
			);
		}


		public String getMergeIntoWhenMatchThenUpdateWhereSqlPart() { // for oracle merge into
			List<String> columnOrder;
			if (payload.isConstraint() && CollectionUtils.isNotEmpty(payload.getConstraintColumnNameList())) {
				columnOrder = payload.getConstraintColumnNameList();
			} else {
				columnOrder = payload.getAllColumnNameList();
			}
			return payload.buildSqlClause(
					payload.getAfter(),
					columnOrder,
					false,
					true,
					true
			);
		}

		public String getMergeIntoOnConditionSqlPart(String alias) { // for oracle merge into
			List<String> columnOrder;
			if (payload.isConstraint() && CollectionUtils.isNotEmpty(payload.getConstraintColumnNameList())) {
				columnOrder = payload.getConstraintColumnNameList();
			} else {
				columnOrder = payload.getAllColumnNameList();
			}
			return payload.buildSqlClause(
					payload.getAfter(),
					columnOrder,
					false,
					true,
					true,
					alias
			);
		}
	}
}
