package com.lguplus.fleta.domain.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class PartitionInfo {
	private String topic;
	private Long endOffset;
	private Long messagesBehind;
}
