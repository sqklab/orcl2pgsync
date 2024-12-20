package com.lguplus.fleta.domain.dto.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonResultDto {
	private List<Map<LocalTime, Long>> diffs;
	private Long diffCount;

	// do not change or delete it
	public Long getDiffCount() {
		if (this.getDiffs() == null) {
			return 0L;
		}
		Long count = 0L;
		for (Map<LocalTime, Long> diff : this.getDiffs()) {
			count += diff.values().stream().filter(value -> value != 0).count();
		}
		return count;
	}

}
