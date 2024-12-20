package com.lguplus.fleta.domain.service.operation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class OperationManager {
	private static final Logger logger = LoggerFactory.getLogger(OperationManager.class);

	/**
	 * key: session
	 * value: list of operation request
	 */
	private final Map<String, List<OperationReq>> operationRequests = new ConcurrentHashMap<>();

	/**
	 * @param session session
	 * @param where   where
	 * @param table   table
	 */
	public void addRequest(String session, String where, String table) {
		logger.info("Add new operation request session={}, table={}, where={}", session, table, where);
		if (StringUtils.isBlank(session) || StringUtils.isBlank(table) || StringUtils.isBlank(where)) return;
		if (null == operationRequests.get(session)) {
			List<OperationReq> list = new LinkedList<>();
			list.add(new OperationReq(session, table, where, false));
			operationRequests.put(session, list);
		} else {
			boolean isExist = Boolean.FALSE;
			List<OperationReq> lstBySession = operationRequests.get(session);
			for (OperationReq req : lstBySession) {
				if (table.equalsIgnoreCase(req.getTable()) && where.equals(req.getWhere())) {
					logger.info("Update operation cancel state to {}", false);
					isExist = true;
					req.setCancel(false);
				}
			}

			if (isExist == Boolean.FALSE) {
				lstBySession.add(new OperationReq(session, table, where, false));
			}
		}
	}


	/**
	 * @param session session
	 * @param where   where
	 * @param table   table
	 */
	public synchronized void doCancel(String session, String where, String table) {
		logger.info("Do cancel operation session: {}, table: {}, where: {}", session, table, where);
		if (StringUtils.isBlank(session)) return;
		List<OperationReq> list = operationRequests.get(session);
		if (null == list || list.isEmpty()) return;
		if (StringUtils.isBlank(table) || StringUtils.isBlank(where)) return;

		List<OperationReq> filter = list.stream()
				.filter(req -> table.equalsIgnoreCase(req.getTable()) && where.equals(req.getWhere()))
				.collect(Collectors.toList());

		if (filter.isEmpty()) return;
		if (filter.size() > 1) {
			logger.warn("User {} has 2 operation request has same condition {} in table {}.", session, where, table);
		}

		filter.forEach(req -> req.setCancel(true));
	}

	/**
	 * @param session session
	 * @param where   where
	 * @param table   table
	 * @return cancel status
	 */
	public boolean isCancel(String session, String where, String table) {
		if (StringUtils.isBlank(session)) return false;
		List<OperationReq> list = operationRequests.get(session);
		if (null == list || list.isEmpty()) return false;
		if (StringUtils.isBlank(table) || StringUtils.isBlank(where)) return false;

		List<OperationReq> filter = list.stream()
				.filter(req -> table.equalsIgnoreCase(req.getTable()) && where.equals(req.getWhere()))
				.collect(Collectors.toList());
		if (filter.isEmpty()) return false;
		if (filter.size() > 1) {
			logger.warn("User {} has 2 operation request has same condition {} in table {}.", session, where, table);
		}

		return filter.get(0).getCancel();
	}

	@Getter
	@Setter
	@NoArgsConstructor
	static class OperationReq {
		private String session;
		private String table;
		private String where;
		private boolean cancel;

		public OperationReq(String session, String table, String where, boolean cancel) {
			this.session = session;
			this.table = table;
			this.where = where;
			this.cancel = cancel;
		}

		public boolean getCancel() {
			return cancel;
		}
	}
}
