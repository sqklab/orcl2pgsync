package com.lguplus.fleta.adapters.rest;


import com.lguplus.fleta.adapters.messagebroker.KafkaConnectorApi;
import com.lguplus.fleta.domain.dto.rest.HttpResponse;
import com.lguplus.fleta.domain.model.KafkaConnectorEntity;
import com.lguplus.fleta.ports.service.ConnectorService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Slf4j
@RestController
@RequestMapping("/connectors")
@CrossOrigin
public class ConnectorController {
	private final ConnectorService connectorService;

	public ConnectorController(ConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	@PostMapping("/synchronize")
	public ResponseEntity<HttpResponse<List<KafkaConnectorApi.Connector>>> synchronize() {
		try {
			List<KafkaConnectorApi.Connector> list = this.connectorService.fetchConnectors();
			HttpResponse<List<KafkaConnectorApi.Connector>> response = new HttpResponse<>(HttpStatus.OK.value(), "OK", list);
			return ResponseEntity.ok(response);
		} catch (IOException ex) {
			HttpResponse<List<KafkaConnectorApi.Connector>> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			HttpResponse<List<KafkaConnectorApi.Connector>> response = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping(value = "/create")
	public ResponseEntity<HttpResponse<String>> create(@RequestBody KafkaConnectorApi.ConnectorRequestParam requestParam) {
		try {
			if(null == requestParam.getConfig() || StringUtils.isBlank(requestParam.getName())){
				HttpResponse<String> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), "Connector configuration format is invalid", null);
				return ResponseEntity.ok(response);
			}
			HttpResponse<String> httpResponse = this.connectorService.createConnector(requestParam);
			HttpResponse<String> response = new HttpResponse<>(httpResponse.getStatus(), httpResponse.getMessage(), httpResponse.getBody());
			return ResponseEntity.ok(response);
		} catch (IOException ex) {
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/update")
	public ResponseEntity<HttpResponse<Integer>> update(@RequestBody Map<String, String> config,
														@RequestParam("id") Long id) {
		if (id == null) {
			HttpResponse<Integer> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), "Connector id must not empty!", null);
			return ResponseEntity.ok(response);
		}
		try {
			HttpResponse<String> httpResponse = this.connectorService.updateConnectorByName(id, config);
			if (httpResponse == null) {
				HttpResponse<Integer> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), "Connector Not found!", HttpStatus.BAD_REQUEST.value());
				return ResponseEntity.ok(response);
			}
			HttpResponse<Integer> response = new HttpResponse<>(httpResponse.getStatus(), httpResponse.getMessage(), httpResponse.getStatus());
			return ResponseEntity.ok(response);
		} catch (IOException ex) {
			HttpResponse<Integer> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			HttpResponse<Integer> response = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/pause")
	public ResponseEntity<HttpResponse<Integer>> pause(@RequestParam("id") Long id) {
		if (id == null) {
			HttpResponse<Integer> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), "Connector name must not empty!", HttpStatus.BAD_REQUEST.value());
			return ResponseEntity.ok(response);
		}
		try {
			HttpResponse<String> pause = this.connectorService.pauseConnectorById(id);
			HttpResponse<Integer> response = new HttpResponse<>(pause.getStatus(), pause.getMessage(), pause.getStatus());
			return ResponseEntity.ok(response);
		} catch (IOException ex) {
			HttpResponse<Integer> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			HttpResponse<Integer> response = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/resume")
	public ResponseEntity<HttpResponse<Integer>> resume(@RequestParam("id") Long id) {
		if (id == null) {
			HttpResponse<Integer> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), "Connector name must not empty!", HttpStatus.BAD_REQUEST.value());
			return ResponseEntity.ok(response);
		}
		try {
			HttpResponse<String> pause = this.connectorService.resumeConnectorById(id);
			HttpResponse<Integer> response = new HttpResponse<>(pause.getStatus(), pause.getMessage(), pause.getStatus());
			return ResponseEntity.ok(response);
		} catch (IOException ex) {
			HttpResponse<Integer> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			HttpResponse<Integer> response = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		}
	}

	@GetMapping("/getById")
	public ResponseEntity<HttpResponse<Map<String, String>>> getById(@RequestParam("id") Long id) {
		if (id == null) {
			HttpResponse<Map<String, String>> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), "Connector id must not empty!", null);
			return ResponseEntity.ok(response);
		}
		try {
			Map<String, String> sourceRecordInfo = this.connectorService.viewConnectorById(id);
			HttpResponse<Map<String, String>> response = new HttpResponse<>(HttpStatus.OK.value(), "OK", sourceRecordInfo);
			return ResponseEntity.ok(response);
		} catch (IOException ex) {
			HttpResponse<Map<String, String>> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			HttpResponse<Map<String, String>> response = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/restart")
	public ResponseEntity<HttpResponse<Integer>> restart(@RequestParam("id") Long id,
														 @RequestParam("taskId") Integer taskId) {
		if (id == null || taskId == null) {
			HttpResponse<Integer> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), "Connector name or taskID must not empty!", HttpStatus.BAD_REQUEST.value());
			return ResponseEntity.ok(response);
		}
		try {
			HttpResponse<String> restart = this.connectorService.restartTaskOfSingleConnector(id, taskId);
			HttpResponse<Integer> response = new HttpResponse<>(restart.getStatus(), restart.getMessage(), restart.getStatus());
			return ResponseEntity.ok(response);
		} catch (IOException ex) {
			HttpResponse<Integer> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			HttpResponse<Integer> response = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/restartAll")
	public ResponseEntity<HttpResponse<List<HttpResponse<String>>>> restartAll() {
		try {
			List<HttpResponse<String>> restart = this.connectorService.restartAll();
			HttpResponse<List<HttpResponse<String>>> response = new HttpResponse<>(HttpStatus.OK.value(), "OK", restart);
			return ResponseEntity.ok(response);
		} catch (IOException ex) {
			HttpResponse<List<HttpResponse<String>>> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			HttpResponse<List<HttpResponse<String>>> response = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/deleteHistory")
	public ResponseEntity<?> deleteHistory(@RequestBody List<Long> ids){
		if (null == ids  || ids.isEmpty()) {
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(),
					"Connector id is required", "Connector id is required");
			return ResponseEntity.ok(response);
		}
		try {
			this.connectorService.deleteHistory(ids);
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.OK.value(),
					"delete history connector config successfully", "delete history connector config successfully");
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		}
	}


	@GetMapping("/revert")
	public ResponseEntity<?> revert(@RequestParam("currentId") Long currentId, @RequestParam("revertId") Long revertId) {
		if (null == currentId || null == revertId || currentId <= 0 || revertId <= 0) {
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(),
					"Connector id must be positive integer", "Connector id must be positive integer");
			return ResponseEntity.ok(response);
		}
		try {
			this.connectorService.revert(revertId, currentId);
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.OK.value(),
					"Revert connector config successfully", "Revert connector config successfully");
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/delete")
	public ResponseEntity<HttpResponse<String>> delete(@RequestParam("id") Long id,
													   @RequestParam("deleteKafkaConnectorAlso") boolean deleteKafkaConnectorAlso) {
		if (id == null) {
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), "Connector id must not empty!", "Connector id must not empty!");
			return ResponseEntity.ok(response);
		}
		try {
			HttpResponse delete = this.connectorService.deleteConnectorById(id, deleteKafkaConnectorAlso);
			return ResponseEntity.ok(delete);
		} catch (IOException ex) {
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/deleteTopicName")
	public ResponseEntity<HttpResponse<String>> deleteTopicName(@RequestParam("topicName") @NotBlank String topicName) {
		if (StringUtils.isEmpty(topicName)) {
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), "TopicName id must not empty!", "TopicName id must not empty!");
			return ResponseEntity.ok(response);
		}
		try {
			this.connectorService.deleteTopicName(topicName);
			return ResponseEntity.ok(null);
		} catch (Exception ex) {
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/stopAll")
	public ResponseEntity<HttpResponse<String>> stopAll() {
		try {
			this.connectorService.stopConnectors();
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.OK.value(), "OK", "OK");
			return ResponseEntity.ok(response);
		} catch (IOException ex) {
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/startAll")
	public ResponseEntity<HttpResponse<String>> startAll() {
		try {
			this.connectorService.startConnectors();
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.OK.value(), "OK", "OK");
			return ResponseEntity.ok(response);
		} catch (IOException ex) {
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			HttpResponse<String> response = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/history")
	public ResponseEntity<HttpResponse<?>> history(@RequestBody @Valid ConnectorHistoryParam param) {
		try {
			List<KafkaConnectorEntity> list = this.connectorService.history(param.getId(), param.getName(), param.type);
			HttpResponse<List<KafkaConnectorEntity>> response = new HttpResponse<>(HttpStatus.OK.value(), "OK", list);
			return ResponseEntity.ok(response);
		} catch (IOException ex) {
			HttpResponse<List<KafkaConnectorEntity>> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			HttpResponse<List<KafkaConnectorEntity>> response = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		}
	}

	@GetMapping("/refresh")
	public ResponseEntity<HttpResponse<List<KafkaConnectorApi.Connector>>> refresh() {
		try {
			List<KafkaConnectorApi.Connector> list = this.connectorService.refreshConnectors();
			HttpResponse<List<KafkaConnectorApi.Connector>> response = new HttpResponse<>(HttpStatus.OK.value(), "OK", list);
			return ResponseEntity.ok(response);
		} catch (IOException ex) {
			HttpResponse<List<KafkaConnectorApi.Connector>> response = new HttpResponse<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			HttpResponse<List<KafkaConnectorApi.Connector>> response = new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null);
			return ResponseEntity.ok(response);
		}
	}

	@Deprecated(since = "0.2.9")
	static class ConnectorHelper {

		public static KafkaConnectorApi.ConnectorRequestParam addStartSCNToConfig(KafkaConnectorApi.ConnectorRequestParam requestParam) {
			if (Objects.isNull(requestParam)) return requestParam;
			Map<String, String> defaultConfigs = requestParam.getConfig();
			if (Objects.isNull(defaultConfigs)) return requestParam;
			if (defaultConfigs.containsKey(OracleSourceConnectorConfig.START_SCN)) {
				return requestParam;
			} else {
				OracleSourceConnectorConfig config = new OracleSourceConnectorConfig(defaultConfigs);
				try (Connection conn = getConnection(config)) {
					PreparedStatement statement = conn.prepareCall("select min(current_scn) CURRENT_SCN from gv$database");
					ResultSet resultSet = statement.executeQuery();

					Long offsetScn = null;
					while (resultSet.next()) {
						offsetScn = resultSet.getLong("CURRENT_SCN");
					}

					if (Objects.nonNull(offsetScn)) {
						defaultConfigs.put(OracleSourceConnectorConfig.START_SCN, String.valueOf(offsetScn));
						requestParam.setConfig(defaultConfigs);
					}

					resultSet.close();
					statement.close();
				} catch (SQLException ex) {
					log.error(ex.getMessage(), ex);
				}
			}
			return requestParam;
		}

		private static Connection getConnection(OracleSourceConnectorConfig config) throws SQLException {
			return DriverManager.getConnection(
					"jdbc:oracle:thin:@" + config.getDbHostName() + ":" + config.getDbPort() + "/" + config.getDbName(),
					config.getDbUser(),
					config.getDbUserPassword());
		}
	}

	@Deprecated(since = "0.2.9")
	static class OracleSourceConnectorConfig extends AbstractConfig {

		public static final String DB_NAME_ALIAS = "db.name.alias";
		public static final String TOPIC_CONFIG = "topic";
		public static final String DB_NAME_CONFIG = "db.name";
		public static final String DB_HOST_NAME_CONFIG = "db.hostname";
		public static final String DB_PORT_CONFIG = "db.port";
		public static final String DB_USER_CONFIG = "db.user";
		public static final String DB_USER_PASSWORD_CONFIG = "db.user.password";
		public static final String TABLE_WHITELIST = "table.whitelist";
		public static final String PARSE_DML_DATA = "parse.dml.data";
		public static final String DB_FETCH_SIZE = "db.fetch.size";
		public static final String RESET_OFFSET = "reset.offset";
		public static final String START_SCN = "start.scn";
		public static final String MULTITENANT = "multitenant";
		public static final String TABLE_BLACKLIST = "table.blacklist";
		public static final String DML_TYPES = "dml.types";
		public static final String MAP_UNESCAPED_STRINGS = "map.unescaped.strings";


		public OracleSourceConnectorConfig(ConfigDef config, Map<String, String> parsedConfig) {
			super(config, parsedConfig);
		}

		public OracleSourceConnectorConfig(Map<String, String> parsedConfig) {
			this(conf(), parsedConfig);
		}

		public static ConfigDef conf() {
			return new ConfigDef()
					.define(DB_NAME_ALIAS, Type.STRING, Importance.HIGH, "Db Name Alias")
					.define(TOPIC_CONFIG, Type.STRING, Importance.HIGH, "Topic")
					.define(DB_NAME_CONFIG, Type.STRING, Importance.HIGH, "Db Name")
					.define(DB_HOST_NAME_CONFIG, Type.STRING, Importance.HIGH, "Db HostName")
					.define(DB_PORT_CONFIG, Type.INT, Importance.HIGH, "Db Port")
					.define(DB_USER_CONFIG, Type.STRING, Importance.HIGH, "Db User")
					.define(DB_USER_PASSWORD_CONFIG, Type.STRING, Importance.HIGH, "Db User Password")
					.define(TABLE_WHITELIST, Type.STRING, Importance.HIGH, "TAbles will be mined")
					.define(PARSE_DML_DATA, Type.BOOLEAN, Importance.HIGH, "Parse DML Data")
					.define(DB_FETCH_SIZE, Type.INT, Importance.HIGH, "Database Record Fetch Size")
					.define(RESET_OFFSET, Type.BOOLEAN, Importance.HIGH, "Reset Offset")
					.define(START_SCN, Type.STRING, "", Importance.LOW, "Start SCN")
					.define(MULTITENANT, Type.BOOLEAN, Importance.HIGH, "Database is multitenant (container)")
					.define(TABLE_BLACKLIST, Type.STRING, Importance.LOW, "Table will not be mined")
					.define(DML_TYPES, Type.STRING, "", Importance.LOW, "Types of DML to capture, CSV value of INSERT/UPDATE/DELETE")
					.define(MAP_UNESCAPED_STRINGS, Type.BOOLEAN, false, Importance.LOW, "Mapped values for data/before will have unescaped strings");
		}

		public String getDbNameAlias() {
			return this.getString(DB_NAME_ALIAS);
		}

		public String getTopic() {
			return this.getString(TOPIC_CONFIG);
		}

		public String getDbName() {
			return this.getString(DB_NAME_CONFIG);
		}

		public String getDbHostName() {
			return this.getString(DB_HOST_NAME_CONFIG);
		}

		public int getDbPort() {
			return this.getInt(DB_PORT_CONFIG);
		}

		public String getDbUser() {
			return this.getString(DB_USER_CONFIG);
		}

		public String getDbUserPassword() {
			return this.getString(DB_USER_PASSWORD_CONFIG);
		}

		public String getTableWhiteList() {
			return this.getString(TABLE_WHITELIST);
		}

		public Boolean getParseDmlData() {
			return this.getBoolean(PARSE_DML_DATA);
		}

		public int getDbFetchSize() {
			return this.getInt(DB_FETCH_SIZE);
		}

		public Boolean getResetOffset() {
			return this.getBoolean(RESET_OFFSET);
		}

		public String getStartScn() {
			return this.getString(START_SCN);
		}

		public Boolean getMultitenant() {
			return this.getBoolean(MULTITENANT);
		}

		public String getTableBlackList() {
			return this.getString(TABLE_BLACKLIST);
		}

		public String getDMLTypes() {
			return this.getString(DML_TYPES);
		}

		public Boolean getMapUnescapedStrings() {
			return this.getBoolean(MAP_UNESCAPED_STRINGS);
		}
	}


	@Getter
	@Setter
	@NoArgsConstructor
	public static class ConnectorHistoryParam{
		private long id;
		@NotBlank
		private String name;
		@NotBlank
		private String type;
	}
}
