package com.lguplus.fleta.ports.service;

import com.lguplus.fleta.domain.dto.Synchronizer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface SyncRequestExportService {

	ByteArrayInputStream exportByConditions(LocalDate dateFrom, LocalDate dateTo, List<String> topicNames, Synchronizer.SyncState state, String division, String dbName, String schema) throws IOException;
}
