package com.lguplus.fleta.domain.util;

import com.lguplus.fleta.domain.service.constant.Constants;

import java.util.Map;

public class CommonUtils {

	public static boolean beforeEqualAfter(Map<String, Object> before, Map<String, Object> after) {
		if (null == before || after == null) return false;
		if (before.size() != after.size()) return false;

		return before.entrySet().stream()
				.allMatch(beforeEntry -> {
					Object beforeVal = beforeEntry.getValue();
					Object afterVal = after.get(beforeEntry.getKey());
					if (null == beforeVal && null == afterVal) {
						return true;
					}
					if (null == beforeVal || null == afterVal) {
						return false;
					}
					// both != null
					if (beforeVal instanceof Number) {
						return beforeVal.equals(afterVal);
					}
					return beforeVal.toString().equals(afterVal.toString());
				});
	}

	//[TO-BE] Default Consumer Group is dbsync.{tbl_sync_task_info.id}
	public static String getKafkaGroupId(Long id) {
		return Constants.PREFIX_GROUP_DBSYNC + id;
	}
}
