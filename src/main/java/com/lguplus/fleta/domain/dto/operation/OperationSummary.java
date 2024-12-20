package com.lguplus.fleta.domain.dto.operation;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OperationSummary {

	private LocalDateTime operationDate;

	private String session;

	private Boolean state;

	private Long insert = 0L;

	private Long update = 0L;

	private Long delete = 0L;


	public OperationSummary(String session) {
		this.session = session;
	}
}
