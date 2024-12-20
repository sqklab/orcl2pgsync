package com.lguplus.fleta;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static java.time.temporal.ChronoUnit.MINUTES;

@RunWith(MockitoJUnitRunner.class)
public class CommonTest {
	@Test
	void test() throws UnknownHostException {
		System.out.println("time === " + LocalDateTime.now().toLocalTime().truncatedTo(ChronoUnit.SECONDS));
	}

	@Test
	void test2() {
		LocalTime l1 = LocalTime.parse("02:53:40");
		LocalTime l2 = LocalTime.parse("02:56:27");
		System.out.println(l1.until(l2, MINUTES));
		System.out.println(l2.until(l1, MINUTES));
		System.out.println(MINUTES.between(l1, l2));
		System.out.println(MINUTES.between(l2, l1));

		System.out.println(Duration.between(l1, l2).toMinutes());
	}

	@Test
	void test3() {
		Map<String, Object> before = new HashMap<>();
		before.put("col1", 10);
		before.put("col2", "Hello");
		before.put("col3", null);
		before.put("col4", 1647251795000L);
		Map<String, Object> after = new HashMap<>();
		after.put("col1", 10);
		after.put("col2", "Hello");
		after.put("col3", null);
		after.put("col4", 1647251795000L);

		boolean same = sameMap(before, after);

		Assertions.assertTrue(same);
	}

	private boolean sameMap(Map<String, Object> before, Map<String, Object> after) {
		if (null == before || after == null) return false;
		if (before.size() != after.size()) return false;

		return before.entrySet().stream()
				.allMatch(beforeEntry -> {
					Object beforeVal = beforeEntry.getValue();
					Object afterVal = after.get(beforeEntry.getKey());
					if (null == beforeVal && null == afterVal) {
						return true;
					}
					if (null == beforeVal || null == afterVal) {
						return false;
					}
					// both != null
					if (beforeVal instanceof Number) {
						return beforeVal.equals(afterVal);
					}
					return beforeVal.toString().equals(afterVal.toString());
				});
	}
}
