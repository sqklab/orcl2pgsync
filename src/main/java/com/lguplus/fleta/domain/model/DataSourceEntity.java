package com.lguplus.fleta.domain.model;

import com.lguplus.fleta.domain.dto.DataSourceInfo;
import com.lguplus.fleta.domain.dto.DataSourceState;
import com.lguplus.fleta.domain.dto.IDataSource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tbl_datasource", uniqueConstraints = {@UniqueConstraint(columnNames = {"server_name"})})
public class DataSourceEntity implements IDataSource, Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "server_name", unique = true)
	private String serverName;

	@Column(name = "url")
	private String url;

	@Column(name = "username")
	private String username;

	@Column(name = "password")
	private String password;

	@Column(name = "max_pool_size")
	private int maxPoolSize;

	@Column(name = "idle_timeout")
	private int idleTimeout;

	@Column(name = "status")
	private DataSourceState status;

	@Column(name = "driver_class_name")
	private String driverClassName;

	@Transient
	private boolean parserSecretValue;

	@CreatedBy
	@Column(name = "created_user" , nullable = false, updatable = false )
	private String createdUser;

	@LastModifiedBy
	@Column(name = "updated_user")
	private String updatedUser;

	@CreatedDate
	@Column(name = "created_at" , nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public LocalDateTime getCreatedTime() {return createdAt;}

	@Override
	public LocalDateTime getUpdatedTime() { return updatedAt; }

	@Override
	public String getCreatedBy() { return createdUser; }

	@Override
	public String getUpdatedBy() { return updatedUser; }

	@Override
	public void enableInUseState() {
		this.status = DataSourceState.IN_USE;
	}

	@Override
	public void disableInUseState() {
		this.status = DataSourceState.ACTIVE;
	}

	public DataSourceInfo toDataSourceInfo() {
		return DataSourceInfo.builder()
				.id(getId())
				.serverName(getServerName())
				.username(getUsername())
				.password(getPassword())
				.maxPoolSize(getMaxPoolSize())
				.idleTimeout(getIdleTimeout())
				.url(getUrl())
				.status(getStatus())
				.driverClassName(getDriverClassName()).isPending(DataSourceState.PENDING.equals(getStatus()))
				.createdUser(getCreatedUser())
				.updatedUser(getUpdatedUser())
				.createdAt(getCreatedAt())
				.updatedAt(getUpdatedAt())
				.build();
	}

	public DataSourceEntity toDataSourceEntity(IDataSource dataSource) {
		DataSourceEntity dataSourceInfo = new DataSourceEntity();
		dataSourceInfo.setId(dataSource.getId());
		dataSourceInfo.setServerName(dataSource.getServerName());
		dataSourceInfo.setUrl(dataSource.getUrl());
		dataSourceInfo.setUsername(dataSource.getUsername());
		dataSourceInfo.setPassword(dataSource.getPassword());
		dataSourceInfo.setMaxPoolSize(dataSource.getMaxPoolSize());
		dataSourceInfo.setIdleTimeout(dataSource.getIdleTimeout());
		dataSourceInfo.setStatus(dataSource.getStatus());
		dataSourceInfo.setDriverClassName(dataSource.getDriverClassName());
		dataSourceInfo.setCreatedUser(dataSource.getCreatedBy());
		dataSourceInfo.setUpdatedUser(dataSource.getUpdatedBy());
		dataSourceInfo.setCreatedAt(dataSource.getCreatedTime());
		dataSourceInfo.setUpdatedAt(dataSource.getUpdatedTime());
		return dataSourceInfo;
	}

	public boolean isNotIn(DataSourceState... states) {
		List<DataSourceState> dsStates = Arrays.asList(states);
		return !dsStates.contains(getStatus());
	}

	@Override
	public void setUrl(String driverName, String host, String port, String database, String params, boolean isSID) {
		if (StringUtils.isNoneEmpty(host) && StringUtils.isNoneEmpty(port) && StringUtils.isNoneEmpty(database)) {
			if (driverName.contains("oracle")) {
				this.url = (isSID) ? String.format("jdbc:%s:thin:@%s:%s:%s", driverName, host, port, database)
						: String.format("jdbc:%s:thin:@//%s:%s/%s", driverName, host, port, database);
			} else {
				this.url = String.format("jdbc:%s://%s:%s/%s", driverName, host, port, database);
			}
		}
	}

}
