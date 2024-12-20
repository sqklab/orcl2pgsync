package com.lguplus.fleta.ports.service.operation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public interface OperationBroadcastPublisher {

	void broadcast(BroadcastEventData eventData);


	@Getter
	@Setter
	@NoArgsConstructor
	class BroadcastEventData implements Serializable {
		private String session;
		private String where;
		private String table;

		public BroadcastEventData(String session, String where, String table) {
			this.session = session;
			this.where = where;
			this.table = table;
		}


		public boolean inValid() {
			return StringUtils.isBlank(table) || StringUtils.isBlank(session) || StringUtils.isBlank(where);
		}
	}
}