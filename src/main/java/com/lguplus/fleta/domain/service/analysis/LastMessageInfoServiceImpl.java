package com.lguplus.fleta.domain.service.analysis;

import com.lguplus.fleta.domain.model.LastMessageInfoEntity;
import com.lguplus.fleta.ports.repository.LastMessageInfoRepository;
import com.lguplus.fleta.ports.service.LastMessageInfoService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LastMessageInfoServiceImpl implements LastMessageInfoService {

	private final LastMessageInfoRepository lastMessageInfoRepository;

	public LastMessageInfoServiceImpl(LastMessageInfoRepository lastMessageInfoRepository) {
		this.lastMessageInfoRepository = lastMessageInfoRepository;
	}

	@Override
	public List<LastMessageInfoEntity> getLastMessageInfoByListTopic(List<String> topic) {
		return lastMessageInfoRepository.findLastMessageInfoEntityByListTopic(topic);
	}
}
