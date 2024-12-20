package com.lguplus.fleta.ports.service;

import com.lguplus.fleta.domain.model.LastMessageInfoEntity;

import java.util.List;

public interface LastMessageInfoService {

	List<LastMessageInfoEntity> getLastMessageInfoByListTopic(List<String> topic);
}
