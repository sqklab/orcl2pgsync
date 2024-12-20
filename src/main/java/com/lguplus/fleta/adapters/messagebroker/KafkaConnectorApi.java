package com.lguplus.fleta.adapters.messagebroker;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import okhttp3.ResponseBody;
import org.apache.commons.collections.CollectionUtils;
import retrofit2.Call;
import retrofit2.http.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Jan 2022
 */
public interface KafkaConnectorApi {

	@Headers({
			"Content-Type: application/json",
			"Accept: application/json"
	})
	@GET("/connectors?expand=status")
	Call<Map<String, Connector>> getConnectorStatus();

	@Headers({
			"Content-Type: application/json",
			"Accept: application/json"
	})
	@GET("/connectors/{connectorName}")
	Call<Map<String, Connector>> getConnectorStatusOfSpecificConnector(@Path("connectorName") String connectorName);

	@Headers({
			"Content-Type: application/json",
			"Accept: application/json"
	})
	@GET("/connectors/{connectorName}/status")
	Call<ConnectorStatus> getConnectorStatusOfSpecificConnector2(@Path("connectorName") String connectorName);

	@Headers({
			"Content-Type: application/json",
			"Accept: application/json"
	})
	@GET("/connectors/{connectorName}/config")
	Call<Map<String, String>> getConnectorConfigOfSpecificConnector(@Path("connectorName") String connectorName);

	@Headers({
			"Content-Type: application/json",
			"Accept: application/json"
	})
	@GET("/connectors?expand=status&expand=info")
	Call<Map<String, Connector>> getConnectorStatusWithExpandInfo();

	@Headers({
			"Content-Type: application/json",
			"Accept: application/json"
	})
	@POST("/connectors")
	Call<ResponseBody> createConnector(@Body ConnectorRequestParam requestBody);

	@Headers({
			"Content-Type: application/json",
			"Accept: application/json"
	})
	@PUT("/connectors/{connectorName}/config")
	Call<ResponseBody> update(@Path("connectorName") String connectorName, @Body Map<String, String> config);

	@Headers({
			"Content-Type: application/json",
			"Accept: application/json"
	})
	@POST("/connectors/{connectorName}/restart?includeTasks=true&onlyFailed=true")
	Call<ResponseBody> restartFailedConnector(@Path("connectorName") String connectorName);

	@Headers({
			"Content-Type: application/json",
			"Accept: application/json"
	})
	@POST("/connectors/{connectorName}/restart?includeTasks=true")
	Call<ResponseBody> forceRestartConnector(@Path("connectorName") String connectorName);

	@POST("/connectors/{connectorName}/tasks/{taskId}/restart")
	Call<ResponseBody> restartTask(@Path("connectorName") String connectorName, @Path("taskId") int taskId);

	@Headers({
			"Content-Type: application/json",
			"Accept: application/json"
	})
	@PUT("/connectors/{connectorName}/pause")
	Call<ResponseBody> stopConnector(@Path("connectorName") String connectorName);

	@Headers({
			"Content-Type: application/json",
			"Accept: application/json"
	})
	@PUT("/connectors/{connectorName}/resume")
	Call<ResponseBody> resumeConnector(@Path("connectorName") String connectorName);

	@Headers({
			"Content-Type: application/json",
			"Accept: application/json"
	})
	@DELETE("/connectors/{connectorName}")
	Call<ResponseBody> deleteConnector(@Path("connectorName") String connectorName);

	@Getter
	@Setter
	class ConnectorRequestParam implements Serializable {
		private String name;
		private Map<String, String> config;
	}

	@Getter
	class ConnectorState implements Serializable {
		private String state;
		private String worker_id;
	}

	@Getter
	class ConnectorTask implements Serializable {
		private int id;
		private String state;
		private String worker_id;
		private String trace;
	}

	@Getter
	class ConnectorTaskInfo implements Serializable {
		private String connector;
		private int task;
	}

	@Data
	class ConnectorInfo implements Serializable {
		private String name;
		private Map<String, String> config;
		private List<ConnectorTaskInfo> tasks;
		private String type;

		public boolean hasNoTask() {
			return Objects.isNull(tasks) || CollectionUtils.isEmpty(tasks);
		}
	}

	@Data
	class ConnectorStatus implements Serializable {
		private String name;
		private ConnectorState connector;
		private List<ConnectorTask> tasks;
		private String type;

		private String createdUser;
		private String updatedUser;
		public boolean isRunning() {
			return Objects.nonNull(connector) && "RUNNING".equals(connector.getState());
		}

		public boolean hasNoTask() {
			return Objects.isNull(tasks) || CollectionUtils.isEmpty(tasks);
		}
	}

	@Data
	class Connector implements Serializable {
		private Long id;
		private ConnectorStatus status;
		private ConnectorInfo info;
		private LocalDateTime createAt;
		private LocalDateTime updateAt;
		private String createdUser;
		private String updatedUser;

		public boolean isRunning() {
			return status.isRunning();
		}

		public boolean hasNoTask() {
			return status.hasNoTask() || info.hasNoTask();
		}
	}
}
