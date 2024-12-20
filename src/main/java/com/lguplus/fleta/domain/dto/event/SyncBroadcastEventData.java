package com.lguplus.fleta.domain.dto.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class SyncBroadcastEventData implements Serializable {

	private String topic;
	private String synchronizerName;
	private BroadcastAction action;

	public SyncBroadcastEventData(String topic, String syncName, BroadcastAction action) {
		this.topic = topic;
		this.synchronizerName = syncName;
		this.action = action;
	}

	public boolean inValid() {
		return StringUtils.isBlank(topic) || Objects.isNull(action);
	}

	public enum BroadcastAction {
		START, STOP
	}
}
