package com.lguplus.fleta.domain.service.synchronizer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lguplus.fleta.domain.dto.Synchronizer;

import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.domain.model.SynchronizerHistoryEntity;
import com.lguplus.fleta.domain.model.comparison.DbComparisonInfoEntity;
import com.lguplus.fleta.domain.service.mapper.ObjectMapperFactory;
import com.lguplus.fleta.ports.repository.DbComparisonInfoRepository;
import com.lguplus.fleta.ports.repository.SyncHistoryRepository;
import com.lguplus.fleta.ports.repository.SyncRequestRepository;
import com.lguplus.fleta.ports.service.SyncHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
public class SyncHistoryServiceImpl implements SyncHistoryService {

	private final SyncHistoryRepository syncHistoryRepository;
	private final SyncRequestRepository syncRequestRepository;
	private final DbComparisonInfoRepository dbComparisonInfoRepository;

	public SyncHistoryServiceImpl(SyncHistoryRepository syncHistoryRepository,
								  SyncRequestRepository syncRequestRepository,
								  DbComparisonInfoRepository dbComparisonInfoRepository) {
		this.syncHistoryRepository = syncHistoryRepository;
		this.syncRequestRepository = syncRequestRepository;
		this.dbComparisonInfoRepository = dbComparisonInfoRepository;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Transactional
	public boolean insertHistory(Long syncId, String topicName, Synchronizer.SyncState state, String operation, SyncRequestEntity synchronizer) {
		ObjectMapper mapper = ObjectMapperFactory.getInstance().getObjectMapper();
		Optional<SyncRequestEntity> syncEntityValue = syncRequestRepository.findById(syncId);
		try {
			String syncValue = mapper.writeValueAsString(syncEntityValue);
			SynchronizerHistoryEntity synchronizerHistoryEntity = SynchronizerHistoryEntity.builder()
					.synchronizerId(syncId)
					.topic(topicName)
					.syncJson(syncValue)
					.operation(operation)
					.state(syncEntityValue.get().getState())
					.time(LocalDateTime.now())
					.build();
			syncHistoryRepository.save(synchronizerHistoryEntity);
			return true;
		} catch (Exception ex) {
			log.warn(ex.getMessage(), ex);
			return false;
		}
	}

	@Override
	public List<SynchronizerHistoryEntity> getHistoryInfo(Long syncId) {
		List<SynchronizerHistoryEntity> historyEntities = syncHistoryRepository.getHistoryInfo(syncId);
		return historyEntities;
	}

	@Override
	@Transactional
	public boolean saveAllSyncHistory(List<SyncRequestEntity> historyEntity) {
		ObjectMapper mapper = ObjectMapperFactory.getInstance().getObjectMapper();
		List<SynchronizerHistoryEntity> historyEntities = new ArrayList<>();

		historyEntity.forEach(synchronizer -> {
			Optional<SyncRequestEntity> syncEntityValue = syncRequestRepository.findById(synchronizer.getId());
			try {
				String compare = mapper.writeValueAsString(syncEntityValue);
				SynchronizerHistoryEntity synchronizerHistoryEntity = SynchronizerHistoryEntity.builder()
						.synchronizerId(synchronizer.getId())
						.topic(synchronizer.getTopicName())
						.syncJson(compare)
						.operation("CREATED")
						.state(syncEntityValue.get().getState())
						.time(LocalDateTime.now())
						.build();
				historyEntities.add(synchronizerHistoryEntity);
			} catch (Exception ex) {
				log.warn(ex.getMessage(), ex);
			}
		});
		syncHistoryRepository.saveAll(historyEntities);
		return true;
	}


}
