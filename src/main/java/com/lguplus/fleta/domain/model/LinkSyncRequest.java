package com.lguplus.fleta.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class LinkSyncRequest implements Serializable {
	private List<Long> ids = new ArrayList<>();

	public boolean contains(Long id) {
		return Objects.nonNull(ids) && ids.contains(id);
	}
}
