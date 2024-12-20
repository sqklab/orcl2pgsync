package com.lguplus.fleta.domain.dto.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WrapLogMnrContent {
	private List<LogMnrContent> logContents;
	private Integer count;
}
