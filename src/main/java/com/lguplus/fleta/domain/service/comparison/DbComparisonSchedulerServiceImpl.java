package com.lguplus.fleta.domain.service.comparison;

import com.lguplus.fleta.domain.model.comparison.DbComparisonSchedulerEntity;
import com.lguplus.fleta.ports.repository.DbComparisonScheduleRepository;
import com.lguplus.fleta.ports.service.DbComparisonSchedulerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DbComparisonSchedulerServiceImpl implements DbComparisonSchedulerService {

	private final DbComparisonScheduleRepository scheduleRepo;

	public DbComparisonSchedulerServiceImpl(DbComparisonScheduleRepository scheduleRepo) {
		this.scheduleRepo = scheduleRepo;
	}

	@Override
	public List<DbComparisonSchedulerEntity> findByStateOrderByTime() {
		return scheduleRepo.findByStateOrderByTime(DbComparisonSchedulerEntity.ComparisonScheduleState.ENABLED);
	}

	@Override
	public long countByState() {
		return scheduleRepo.countByState(DbComparisonSchedulerEntity.ComparisonScheduleState.ENABLED);
	}

	@Override
	public int start() {
		return scheduleRepo.updateState(DbComparisonSchedulerEntity.ComparisonScheduleState.ENABLED);
	}

	@Override
	public int stop() {
		return scheduleRepo.updateState(DbComparisonSchedulerEntity.ComparisonScheduleState.DISABLED);
	}
}
