package com.lguplus.fleta.domain.dto.analysis;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDataForXAxisGraphDto implements Serializable {
	private List<String> data;
}
