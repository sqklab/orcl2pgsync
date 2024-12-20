package com.lguplus.fleta.domain.dto.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class SyncComparisonBroadcastEventData implements Serializable {
	private LocalDate compareDate;
	private LocalTime time;
	private BroadcastAction action;

	public SyncComparisonBroadcastEventData(LocalDate compareDate, LocalTime time, BroadcastAction action) {
		this.compareDate = compareDate;
		this.time = time;
		this.action = action;
	}

	public boolean inValid() {
		return Objects.isNull(action);

	}

	public enum BroadcastAction {
		START, START_ONE_TIME
	}
}
