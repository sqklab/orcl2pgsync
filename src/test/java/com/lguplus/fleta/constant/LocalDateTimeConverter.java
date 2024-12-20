package com.lguplus.fleta.constant;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class LocalDateTimeConverter {

	private LocalDateTimeConverter() {
		throw new UnsupportedOperationException();
	}

	public static String to(final LocalDateTime localDateTime) {
		return LocalDateTimeConverter.to(localDateTime, LocalDateTimeConverter.buildFormatter());
	}

	public static String to(final LocalDateTime localDateTime, final DateTimeFormatter formatter) {
		if (localDateTime == null) {
			return null;
		}

		return localDateTime.format(formatter);
	}

	public static LocalDateTime from(final String str) {
		if (str == null || str.isEmpty()) {
			return null;
		}

		return LocalDateTime.parse(str, LocalDateTimeConverter.buildFormatter());
	}

	private static DateTimeFormatter buildFormatter() {
		return new DateTimeFormatterBuilder()
				.parseCaseInsensitive()
				.append(DateTimeFormatter.ISO_LOCAL_DATE)
				.appendLiteral(' ')
				.append(DateTimeFormatter.ISO_LOCAL_TIME)
				.toFormatter();
	}
}
