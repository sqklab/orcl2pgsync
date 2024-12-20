package com.lguplus.fleta.ports.service;

import com.lguplus.fleta.domain.model.comparison.DbComparisonSchedulerEntity;

import java.util.List;

public interface DbComparisonSchedulerService {

	List<DbComparisonSchedulerEntity> findByStateOrderByTime();

	long countByState();

	int start();

	int stop();
}
