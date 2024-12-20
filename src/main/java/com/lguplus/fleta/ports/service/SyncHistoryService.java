package com.lguplus.fleta.ports.service;

import com.lguplus.fleta.domain.dto.Synchronizer;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.domain.model.SynchronizerHistoryEntity;

import java.util.List;


public interface SyncHistoryService {

	boolean insertHistory(Long syncId, String topicName, Synchronizer.SyncState state, String operation, SyncRequestEntity synchronizer);

	List<SynchronizerHistoryEntity> getHistoryInfo(Long syncId);

	boolean saveAllSyncHistory(List<SyncRequestEntity> syncHistory);
}