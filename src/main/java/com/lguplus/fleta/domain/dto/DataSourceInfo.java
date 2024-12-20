package com.lguplus.fleta.domain.dto;

import com.lguplus.fleta.domain.model.DataSourceEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataSourceInfo implements IDataSource {

	private Long id;
	private String serverName;
	private String url;
	private String username;
	private String password;
	private int maxPoolSize;
	private int idleTimeout;
	private DataSourceState status;
	private String driverClassName;
	private boolean isPending;
	private String createdUser;
	private String updatedUser;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public DataSourceEntity toDataSourceInfo() {
		DataSourceEntity dataSourceInfo = new DataSourceEntity();
		dataSourceInfo.setId(id);
		dataSourceInfo.setServerName(serverName);
		dataSourceInfo.setUrl(url);
		dataSourceInfo.setUsername(username);
		dataSourceInfo.setPassword(password);
		dataSourceInfo.setMaxPoolSize(maxPoolSize);
		dataSourceInfo.setIdleTimeout(idleTimeout);
		dataSourceInfo.setStatus(status);
		dataSourceInfo.setDriverClassName(driverClassName);
		dataSourceInfo.setCreatedUser(createdUser);
		dataSourceInfo.setUpdatedUser(updatedUser);
		dataSourceInfo.setCreatedAt(createdAt);
		dataSourceInfo.setUpdatedAt(updatedAt);
		return dataSourceInfo;
	}

	@Override
	public LocalDateTime getCreatedTime() { return createdAt; }

	@Override
	public LocalDateTime getUpdatedTime() { return updatedAt;}

	@Override
	public String getCreatedBy() {return createdUser;}

	@Override
	public String getUpdatedBy() { return updatedUser; }

	@Override
	public DataSourceState getStatus() {
		return this.status;
	}

	@Override
	public void setStatus(DataSourceState status) {
		this.status = status;
	}

	public boolean getIsPending() {
		return this.isPending;
	}

	public void setIsPending(boolean isPending) {
		this.isPending = isPending;
	}

}
