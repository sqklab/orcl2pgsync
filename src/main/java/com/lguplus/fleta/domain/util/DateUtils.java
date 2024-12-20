package com.lguplus.fleta.domain.util;

import com.lguplus.fleta.domain.service.constant.Constants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DateUtils {
	private DateUtils() {
	}

	public static LocalDateTime getDateTime() {
		return LocalDateTime.now(Constants.ZONE_ID);
	}

	public static LocalDate getDate() {
		return LocalDate.now(Constants.ZONE_ID);
	}

	public static LocalTime getTime() {
		return LocalTime.now(Constants.ZONE_ID);
	}

}
