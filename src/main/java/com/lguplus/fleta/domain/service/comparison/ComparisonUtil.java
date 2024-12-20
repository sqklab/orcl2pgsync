package com.lguplus.fleta.domain.service.comparison;

import com.lguplus.fleta.domain.dto.SlackMessage;
import com.lguplus.fleta.domain.dto.comparison.ComparisonSummary;
import com.lguplus.fleta.domain.model.comparison.DbComparisonResultEntity;

import java.util.ArrayList;
import java.util.List;

public class ComparisonUtil {

	public static SlackMessage toSlackMessage(List<ComparisonSummary> list) {
		return toSlackMessage(list, new ArrayList<>());
	}
	public static SlackMessage toSlackMessage(List<ComparisonSummary> list, List<DbComparisonResultEntity> notSucceedList) {
		if (null == list) return null;
		SlackMessage message = new SlackMessage();
		message.setCompareDate(list.get(0).getCompareDate());
		message.setCompareTime(list.get(0).getCompareTime());
		long total = 0;
		long equal = 0;
		long difference = 0;
		long failed = 0;

		List<String> totalDetail = new ArrayList<>();
		List<String> equalDetail = new ArrayList<>();
		List<String> differenceDetail = new ArrayList<>();
		List<String> failedDetail = new ArrayList<>();

		for (ComparisonSummary summary : list) {
			total += summary.getTotal();
			equal += summary.getEqual();
			difference += summary.getDifferent();
			failed += summary.getFail();

			String division = summary.getDivision();
			totalDetail.add(division + ":  " + summary.getTotal());
			equalDetail.add(division + ":  " + summary.getEqual());
			differenceDetail.add(division + ":  " + summary.getDifferent());
			failedDetail.add(division + ":  " + summary.getFail());
		}
		message.setTotal(total);
		message.setDifference(difference);
		message.setFailed(failed);
		message.setEqual(equal);
		message.setEqualDetail(String.join(",\t", equalDetail));
		message.setFailedDetail(String.join(",\t", failedDetail));
		message.setDifferenceDetail(String.join(",\t", differenceDetail));
		message.setTotalDetail(String.join(",\t", totalDetail));
		message.setNotSucceedList(notSucceedList);

		return message;
	}
}
