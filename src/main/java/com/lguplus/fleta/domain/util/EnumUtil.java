package com.lguplus.fleta.domain.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class EnumUtil {
	public static <T extends Enum<T>> List<T> getOrAll(Class<T> enumClass, String value) {
		EnumSet<T> ts = EnumSet.allOf(enumClass);
		return StringUtils.isEmpty(value) ? new ArrayList<>(ts) : List.of(T.valueOf(enumClass, value));
	}

	public static <T extends Enum<T>> List<T> getOrAll(Class<T> enumClass, List<String> values) {
		EnumSet<T> ts = EnumSet.allOf(enumClass);
		if (null == values || values.isEmpty()) {
			return new ArrayList<>(ts);
		} else {
			return values.stream().map(v -> T.valueOf(enumClass, v)).collect(Collectors.toList());
		}
	}

}
