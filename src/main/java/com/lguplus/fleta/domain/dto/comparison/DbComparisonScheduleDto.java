package com.lguplus.fleta.domain.dto.comparison;

import com.lguplus.fleta.domain.model.comparison.DbComparisonSchedulerEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DbComparisonScheduleDto implements Serializable {
	private Long id;
	private DbComparisonSchedulerEntity.ComparisonScheduleState state;
	private LocalTime time;

	public DbComparisonSchedulerEntity toEntity() {
		return new DbComparisonSchedulerEntity(this.getId(), this.getState(), this.time);
	}
}
