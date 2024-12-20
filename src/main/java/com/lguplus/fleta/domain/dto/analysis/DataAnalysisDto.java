package com.lguplus.fleta.domain.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataAnalysisDto {
	private List<MessageDataForYAxisGraphDto> messageDataForYAxisGraphDtoList;
	private MessageDataForXAxisGraphDto messageDataForXAxisGraphDtoList;
	private Long min;
	private Long max;
}


