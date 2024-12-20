package com.lguplus.fleta.adapters.messagebroker;

import com.lguplus.fleta.adapters.persistence.exception.InvalidSyncMessageRequestException;
import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import com.lguplus.fleta.domain.service.exception.ConnectionTimeoutException;
import com.lguplus.fleta.domain.service.exception.DatasourceNotFoundException;
import org.springframework.dao.DuplicateKeyException;

import java.sql.SQLException;
import java.util.List;

public interface SynchronizerTaskExecutor {

	void execute(List<SyncRequestMessage> messages) throws ConnectionTimeoutException, DuplicateKeyException, SQLException, InvalidSyncMessageRequestException, DatasourceNotFoundException;
}
