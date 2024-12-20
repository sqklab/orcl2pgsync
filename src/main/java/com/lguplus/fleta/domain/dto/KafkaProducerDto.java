package com.lguplus.fleta.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class KafkaProducerDto {
	@NotBlank
	private String topic;
	private Object key;
	@NotNull
	private Object message;
}
