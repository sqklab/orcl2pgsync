package com.lguplus.fleta.domain.dto.ui;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogInfo {

	private String name;
	private String path;
	private long length;
}
