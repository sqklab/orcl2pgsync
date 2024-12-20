package com.lguplus.fleta.domain.dto;

import com.lguplus.fleta.domain.model.comparison.DbComparisonResultEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SlackMessage {
	LocalDate compareDate;
	LocalTime compareTime;
	Long total;
	Long equal;
	Long difference;
	Long failed;
	String totalDetail;
	String equalDetail;
	String differenceDetail;
	String failedDetail;
	List<DbComparisonResultEntity> notSucceedList;
}
