package com.lguplus.fleta.domain.dto.comparison;

import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ComparisonSummary {

	@Value("#{target.compare_date}")
	LocalDate getCompareDate();

	@Value("#{target.compare_time}")
	LocalTime getCompareTime();

	@Value("#{target.total}")
	Integer getTotal();

	@Value("#{target.equal}")
	Long getEqual();

	@Value("#{target.different}")
	Long getDifferent();

	@Value("#{target.fail}")
	Long getFail();

	@Value("#{target.sourceCount}")
	Long getSourceCount();

	@Value("#{target.targetCount}")
	Long getTargetCount();

	@Value("#{target.division}")
	String getDivision();
}
