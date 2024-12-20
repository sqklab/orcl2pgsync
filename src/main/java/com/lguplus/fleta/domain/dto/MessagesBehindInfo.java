package com.lguplus.fleta.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessagesBehindInfo {
	private Long msgDtBehind = 0L;

	public void addDtBehind(Long dtBehind) {
		setMsgDtBehind(msgDtBehind + dtBehind);
	}
}
