package com.lguplus.fleta.domain.service.synchronizer;

import ch.qos.logback.classic.Logger;
import com.lguplus.fleta.adapters.messagebroker.SynchronizerTaskExecutor;
import com.lguplus.fleta.adapters.persistence.exception.InvalidSyncMessageRequestException;
import com.lguplus.fleta.config.context.DbSyncContext;
import com.lguplus.fleta.domain.dto.ConnectionEvent;
import com.lguplus.fleta.domain.dto.ConnectionEvent.ConnectionStatus;
import com.lguplus.fleta.domain.dto.SyncRequestMessage;
import com.lguplus.fleta.domain.dto.command.TaskExecuteCommand;
import com.lguplus.fleta.domain.service.constant.CommonSqlState;
import com.lguplus.fleta.domain.service.constant.DivisionType;
import com.lguplus.fleta.domain.service.convertor.DebeziumConvertorFactory;
import com.lguplus.fleta.domain.service.convertor.IDebeziumConverter;
import com.lguplus.fleta.domain.service.exception.ConnectionTimeoutException;
import com.lguplus.fleta.domain.service.exception.DatasourceNotFoundException;
import com.lguplus.fleta.ports.service.DataSourceService;
import com.lguplus.fleta.ports.service.LoggerManager;
import com.lguplus.fleta.ports.service.LostConnectionPublisher;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DuplicateKeyException;

import java.net.ConnectException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLTransientConnectionException;
import java.util.List;

public abstract class DefaultSynchronizerTaskExecutor implements SynchronizerTaskExecutor {

	protected static final boolean CHANGE_INSERT_ON_FAILURE_UPDATE = true;
	protected final LostConnectionPublisher eventPublisher;

	protected final DataSourceService dataSourceService;
	protected final SynchronizerReportService synchronizerReportService;
	protected IDebeziumConverter datatypeConverter;

	protected TaskExecuteCommand command;
	protected int retryTime;
	protected int delayInMillis;

	protected Logger logger;

	public DefaultSynchronizerTaskExecutor(TaskExecuteCommand command) {
		this.dataSourceService = DbSyncContext.getBean("defaultDatasourceContainer", DataSourceService.class);
		this.eventPublisher = DbSyncContext.getBean(LostConnectionPublisher.class);
		this.synchronizerReportService = DbSyncContext.getBean(SynchronizerReportService.class);
		this.logger = DbSyncContext.getBean(LoggerManager.class).getLogger(command.getTopicName());
		this.datatypeConverter = DbSyncContext.getBean(DebeziumConvertorFactory.class)
				.getConverter(DivisionType.getDivision(command.getDivision()));
		this.retryTime = command.getRetryTime();
		this.delayInMillis = command.getDelayInMillis();
		this.command = command;
	}

	public void execute(List<SyncRequestMessage> messages) throws ConnectionTimeoutException, DuplicateKeyException, SQLException, InvalidSyncMessageRequestException, DatasourceNotFoundException {
		try {
			int retryTime = this.retryTime;
			while (retryTime > 0) {
				if ((this.retryTime > retryTime) && logger.isDebugEnabled()) {
					logger.debug("{} retry at {}", (this.retryTime - retryTime), System.currentTimeMillis());
				}
				try {
					// Execute synchronization task
					this.executeMessages(messages);
					// If everything is fine, there is no exception then break the loop
					break;
				} catch (DataSourceService.DisconnectedDataSourceException ex) {
					logger.warn("Cancel execute due to an disconnected connection. {}.", command.getTargetDatabase());
					throw ex;
				} catch (DatasourceNotFoundException ex) {
					retryTime--;
					if (retryTime == 0) {
						logger.warn("All retries are failed. Cannot establish connection to the {}.", command.getTargetDatabase());
						throw ex;
					}
					dataSourceService.retryInitializeDataSource(command.getTargetDatabase());
				} catch (SQLException ex) {
					logger.warn(ex.getMessage(), ex);

					retryTime--;
					if (retryTime <= 0) {
						logger.warn("All retries are failed. Cannot establish connection to the {}.", command.getTargetDatabase());
						throw ex;
					}
					if (ExceptionUtils.indexOfThrowable(ex, SQLTransientConnectionException.class) >= 0) {
						logger.debug("Sleep {} second(s)... due to connection timout", delayInMillis);

						Thread.sleep(delayInMillis);
						continue;
					}
					if (ExceptionUtils.indexOfThrowable(ex, ConnectException.class) >= 0) {
						logger.debug("Sleep {} second(s)... due to connection timout", delayInMillis);

						Thread.sleep(delayInMillis);
						continue;
					}
					throw ex;
				}
			}
		} catch (SQLException ex) {
			if (ExceptionUtils.indexOfThrowable(ex, ConnectException.class) >= 0) {
				ConnectionEvent event = new ConnectionEvent(this, command.getTargetDatabase(), ConnectionStatus.DISCONNECTED);
				eventPublisher.publishEvent(event);
				throw new ConnectionTimeoutException(ex.getMessage(), ex.getCause());
			}
			if (ExceptionUtils.indexOfThrowable(ex, SQLTransientConnectionException.class) >= 0) {
				ConnectionEvent event = new ConnectionEvent(this, command.getTargetDatabase(), ConnectionStatus.DISCONNECTED);
				eventPublisher.publishEvent(event);
				throw new SQLTransientConnectionException(ex.getMessage(), ex.getCause());
			}
			if (ExceptionUtils.indexOfThrowable(ex, SQLIntegrityConstraintViolationException.class) >= 0) {
				throw new DuplicateKeyException(ex.getMessage(), ex.getCause());
			}
			if (CommonSqlState.DUPLICATED_KEY.equals(ex.getSQLState())) {
				throw new DuplicateKeyException(ex.getMessage(), ex);
			}
			throw ex;
		} catch (InterruptedException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	protected abstract void executeMessages(List<SyncRequestMessage> messages) throws DuplicateKeyException, SQLException, InvalidSyncMessageRequestException, DatasourceNotFoundException;
}
