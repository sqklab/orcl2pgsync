package com.lguplus.fleta.domain.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
@ToString
public class ConnectionEvent extends ApplicationEvent {

	private String datasource;

	private ConnectionStatus connectionStatus;

	public ConnectionEvent(Object source, String datasource, ConnectionStatus connectionStatus) {
		super(source);
		this.datasource = datasource;
		this.connectionStatus = connectionStatus;
	}

	public enum ConnectionStatus {
		DISCONNECTED, CONNECTED
	}
}
