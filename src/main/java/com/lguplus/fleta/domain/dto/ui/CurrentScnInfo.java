package com.lguplus.fleta.domain.dto.ui;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CurrentScnInfo {
	private String scn;
	private LocalDateTime findTime;
}
