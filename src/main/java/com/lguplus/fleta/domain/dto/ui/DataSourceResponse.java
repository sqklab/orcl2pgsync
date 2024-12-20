package com.lguplus.fleta.domain.dto.ui;

import com.lguplus.fleta.domain.dto.DataSourceInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DataSourceResponse {
	private List<DataSourceInfo> dataSourceDescriptions;
	private int totalPage;
}
