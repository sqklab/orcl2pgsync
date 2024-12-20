package com.lguplus.fleta.domain.model.comparison;

import com.lguplus.fleta.domain.dto.comparison.DbComparisonScheduleDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_db_comparison_schedule")
public class DbComparisonSchedulerEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;

	@Column(name = "state")
	@Enumerated(EnumType.ORDINAL)
	private ComparisonScheduleState state;

	@Column(name = "time")
	private LocalTime time;

	public DbComparisonScheduleDto toDbComparisonScheduleDto() {
		return new DbComparisonScheduleDto(this.getId(), this.getState(), this.getTime());
	}

	@Override
	public String toString() {
		return "DbComparisonSchedulerEntity{" +
				"id=" + id +
				", state=" + state +
				", time=" + time +
				'}';
	}

	@Getter
	public enum ComparisonScheduleState {
		DISABLED(0), ENABLED(1);

		private final int state;

		ComparisonScheduleState(int state) {
			this.state = state;
		}

	}
}
