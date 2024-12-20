package com.lguplus.fleta.domain.util;

import java.util.Map;
import java.util.Objects;

public class OperationUtil {

	/**
	 * Check if 2 row is diff
	 *
	 * @param sourceRow sourceRow
	 * @param targetRow targetRow
	 * @return true if 2 row is diff
	 */
	public static boolean isDiff(Map<String, Object> sourceRow, Map<String, Object> targetRow) {
		if (sourceRow == null || targetRow == null) {
			return true;
		}
		for (String field : sourceRow.keySet()) {
			Object sourceVal = sourceRow.get(field);
			Object targetVal = targetRow.get(field.toLowerCase());
			if (Objects.isNull(sourceVal) && Objects.isNull(targetVal)) {
				continue;
			}
			if (Objects.nonNull(sourceVal) && Objects.nonNull(targetVal) && !sourceVal.equals(targetVal)) {
				return true;
			}
		}
		return false;
	}
}
