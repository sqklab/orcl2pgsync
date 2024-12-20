package com.lguplus.fleta.ports.service;

import com.lguplus.fleta.domain.model.SyncRequestEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface SyncRequestImportService {
	List<SyncRequestEntity> readDataFromExcelFile(InputStream inputStream) throws IOException;
}
