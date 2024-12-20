package com.lguplus.fleta.domain.service.convertor;

import com.lguplus.fleta.adapters.persistence.exception.InvalidSyncMessageRequestException;
import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import com.lguplus.fleta.domain.dto.command.TaskExecuteCommand;
import lombok.*;

import java.util.List;
import java.util.Map;


public interface IDebeziumConverter {

	public static final String __debezium_unavailable_value = "__debezium_unavailable_value";

	String getSqlRedo(SyncRequestMessage syncRequestMessage, TaskExecuteCommand command) throws InvalidSyncMessageRequestException;

	@Getter
	@Setter
	@ToString
	@AllArgsConstructor
	@Builder
	class ExecutorSqlData {
		/**
		 * This field determines order of parameters in query
		 */
		private List<String> columnOrder;
		/**
		 * This sql contain parameter ? or :param_name
		 */
		private String sql;

		/**
		 * This sql not contain any parameter
		 */
		private String sqlRedo;

		/**
		 * This contains normalized parameters value.
		 */
		private Map<String, Object> params;

		private DebeziumPayloadValues debeziumPayloadValues;
	}
}
