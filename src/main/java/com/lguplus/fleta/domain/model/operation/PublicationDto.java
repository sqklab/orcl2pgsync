package com.lguplus.fleta.domain.model.operation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicationDto {
	private List<PublicationInfo> publicationInfoList;
	private int total;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PublicationInfo {
		private String name;
		private String schemaName;
		private String table;
	}
}
