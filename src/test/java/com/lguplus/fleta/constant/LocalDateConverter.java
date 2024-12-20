package com.lguplus.fleta.constant;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateConverter {

	private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

	private LocalDateConverter() {
		throw new UnsupportedOperationException();
	}

	public static String to(final LocalDate localDate) {
		return LocalDateConverter.to(localDate, DEFAULT_FORMATTER);
	}

	public static String to(final LocalDate localDate, final DateTimeFormatter formatter) {
		if (localDate == null) {
			return null;
		}

		return localDate.format(formatter);
	}

	public static LocalDate from(final String str) {
		if (str == null || str.isEmpty()) {
			return null;
		}

		return LocalDate.parse(str, DEFAULT_FORMATTER);
	}
}
