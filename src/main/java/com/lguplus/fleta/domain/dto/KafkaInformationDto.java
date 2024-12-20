package com.lguplus.fleta.domain.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Setter
@Getter
@Accessors(chain = true)
public class KafkaInformationDto {

	private String groupId;
	private String topics;
	private String state;
	private Long messagesBehind;
	private List<PartitionInfo> partitions;
}


