package com.lguplus.fleta.domain.dto;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class Synchronizer {

	@Getter
	public enum SyncState {
		PENDING(0), RUNNING(1), STOPPED(2);

		private final int state;

		SyncState(int state) {
			this.state = state;
		}

		public static SyncState getState(int id) {
			SyncState[] As = SyncState.values();
			for (SyncState a : As) {
				if (a.equal(id))
					return a;
			}
			return SyncState.PENDING;
		}

		public static int[] getIntValues() {
			SyncState[] states = SyncState.values();
			int[] ints = new int[states.length];
			for (int i = 0; i < states.length; i++) {
				ints[i] = states[i].state;
			}
			return ints;
		}

		public boolean equal(int i) {
			return state == i;
		}

		public boolean isIn(SyncState... states) {
			return Arrays.asList(states).contains(this);
		}
	}

	@Getter
	public enum ErrorState {
		ERROR(0), RESOLVED(1), PROCESSING(2);

		private final int state;

		ErrorState(int state) {
			this.state = state;
		}

		public static ErrorState getStateByName(String state) {
			for (ErrorState value : ErrorState.values()) {
				if (value.name().equals(state)) {
					return value;
				}
			}
			return null;
		}
	}

	public enum SortType {
		DESC, ASC
	}
}