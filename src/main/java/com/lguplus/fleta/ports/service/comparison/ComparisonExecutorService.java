package com.lguplus.fleta.ports.service.comparison;

import com.lguplus.fleta.domain.model.comparison.DbComparisonInfoEntity;
import com.lguplus.fleta.domain.model.comparison.DbComparisonResultEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ComparisonExecutorService {
	DbComparisonResultEntity compareNew(DbComparisonInfoEntity comparisonInfo, LocalDate date, LocalTime time);

    void sendSlackMessage(LocalDate compareDate, LocalTime compareTime);

    void compareWithComparisonResultIds(List<Long> ids);

	DbComparisonResultEntity compareWithComparisonResultId(Long id);

	void createSummary(LocalDate date, LocalTime time);
}
