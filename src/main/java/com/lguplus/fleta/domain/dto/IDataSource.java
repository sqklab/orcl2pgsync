package com.lguplus.fleta.domain.dto;

import java.time.LocalDateTime;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public interface IDataSource {

	Long getId();

	void setId(Long id);

	String getServerName();

	void setServerName(String serverName);

	String getUrl();

	void setUrl(String url);

	LocalDateTime getCreatedTime();

	LocalDateTime getUpdatedTime();

	String getCreatedBy();

	String getUpdatedBy();

	String getUsername();

	void setUsername(String username);

	String getPassword();

	void setPassword(String password);

	int getMaxPoolSize();

	void setMaxPoolSize(int maxPoolSize);

	int getIdleTimeout();

	void setIdleTimeout(int idleTimeout);

	DataSourceState getStatus();

	void setStatus(DataSourceState status);

	default void setUrl(String driverName, String host, String port, String database, String params, boolean isSID) {
		if (isEmpty(host)) return;
		if (isEmpty(port)) return;
		if (isEmpty(database)) return;
		if (driverName.equals("oracle")) {
			setUrl((isSID) ? String.format("jdbc:%s:thin:@%s:%s:%s", driverName, host, port, database)
					: String.format("jdbc:%s:thin:@//%s:%s/%s", driverName, host, port, database));
		} else {
			setUrl(String.format("jdbc:%s://%s:%s/%s", driverName, host, port, database));
		}
	}

	default void enableInUseState() {
		this.setStatus(DataSourceState.IN_USE);
	}

	default void disableInUseState() {
		this.setStatus(DataSourceState.ACTIVE);
	}

	String getDriverClassName();
}
