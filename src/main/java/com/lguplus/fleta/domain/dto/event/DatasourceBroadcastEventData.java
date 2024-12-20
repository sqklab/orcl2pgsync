package com.lguplus.fleta.domain.dto.event;

import com.lguplus.fleta.domain.dto.DataSourceInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class DatasourceBroadcastEventData implements Serializable {
	private String currentServerName;
	private BroadcastAction action;
	private DataSourceInfo dataSource;

	public DatasourceBroadcastEventData(String currentServerName, DataSourceInfo dataSource, BroadcastAction action) {
		this.currentServerName = currentServerName;
		this.action = action;
		this.dataSource = dataSource;
	}

	public boolean inValid() {
		return StringUtils.isBlank(currentServerName) || Objects.isNull(action);
	}

	@Override
	public String toString() {
		return "DatasourceBroadcastEventData{" +
				"currentServerName='" + currentServerName + '\'' +
				"dataSource='" + dataSource + '\'' +
				", action=" + action +
				'}';
	}

	public enum BroadcastAction {
		ADD, DELETE
	}
}
