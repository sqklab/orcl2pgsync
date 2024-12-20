package com.lguplus.fleta.ports.service.comparison;


import java.time.LocalDate;
import java.time.LocalTime;

public interface ComparisonSchedulerService {

	void startOneTimeNow(LocalTime time, LocalDate compareDate);

}
