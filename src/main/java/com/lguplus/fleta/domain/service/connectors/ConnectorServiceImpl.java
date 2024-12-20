package com.lguplus.fleta.domain.service.connectors;

import com.lguplus.fleta.adapters.messagebroker.KafkaConnectorApi;
import com.lguplus.fleta.adapters.messagebroker.KafkaProperties;
import com.lguplus.fleta.domain.dto.rest.HttpResponse;
import com.lguplus.fleta.domain.model.KafkaConnectorEntity;
import com.lguplus.fleta.domain.service.exception.ResourceNotFoundException;
import com.lguplus.fleta.domain.util.DateUtils;
import com.lguplus.fleta.ports.repository.KafkaConnectorRepository;
import com.lguplus.fleta.ports.service.ConnectorService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConnectorServiceImpl implements ConnectorService {

	public static final String RUNNING = "RUNNING";

	public static final String POSTGRES = "postgres";

	// For Oracle Connector
	private final KafkaConnectorApi kafkaOracleConnectorApi;

	// For Postgres Connector
	private final KafkaConnectorApi kafkaPostgresConnectorApi;

	private final KafkaConnectorRepository connectorRepository;

	private final KafkaProperties kafkaProperties;

	public ConnectorServiceImpl(KafkaConnectorApi kafkaOracleConnectorApi,
								KafkaConnectorApi kafkaPostgresConnectorApi,
								KafkaConnectorRepository connectorRepository,
								KafkaProperties kafkaProperties) {
		this.kafkaOracleConnectorApi = kafkaOracleConnectorApi;
		this.kafkaPostgresConnectorApi = kafkaPostgresConnectorApi;
		this.connectorRepository = connectorRepository;
		this.kafkaProperties = kafkaProperties;
	}

	@Override
	public List<KafkaConnectorApi.Connector> fetchConnectors() throws IOException {
		List<KafkaConnectorApi.Connector> connectorInfos = new ArrayList<>();
		try {
			connectorInfos = getAllKafkaConnectorInfos(this.kafkaOracleConnectorApi);
		} catch (IOException ex) {
			log.error("Error during fetch Oracle connector {}", ex.getMessage());
		}

		try {
			connectorInfos.addAll(getAllKafkaConnectorInfos(this.kafkaPostgresConnectorApi));
		} catch (IOException ex) {
			log.error("Error during fetch Postgres connector {}", ex.getMessage());
		}

		for (KafkaConnectorApi.Connector connectorInfo : connectorInfos) {
			connectorInfo.setUpdateAt(DateUtils.getDateTime());
			connectorInfo.setCreateAt(DateUtils.getDateTime());
			saveConnectorInfoToDB(connectorInfo);
		}
		return connectorInfos;
	}

	private void saveConnectorInfoToDB(KafkaConnectorApi.Connector connectorInfo) {
		if (connectorInfo == null) {
			return;
		}
		try {
			List<KafkaConnectorEntity> list = this.connectorRepository.findByNameAndTypeAndDeletedFalse(connectorInfo.getInfo().getName(), connectorInfo.getInfo().getConfig().get("connector.class"));
			KafkaConnectorEntity existedEntity = null == list || list.isEmpty() ? null : list.get(0);
			KafkaConnectorEntity connectorEntity = new KafkaConnectorEntity();
			if (existedEntity != null) { // -> update
				connectorEntity = existedEntity;
			}

			connectorEntity.setName(connectorInfo.getStatus().getName());
			connectorEntity.setType(connectorInfo.getInfo().getConfig().get("connector.class"));
			connectorEntity.setStatus(connectorInfo.getStatus().getConnector().getState());
			connectorEntity.setConfig(connectorInfo.getInfo().getConfig());
			connectorEntity.setCreatedUser(connectorInfo.getStatus().getCreatedUser());
			connectorEntity.setUpdatedUser(connectorInfo.getStatus().getUpdatedUser());
			connectorEntity.setWorkerId(connectorInfo.getStatus().getConnector().getWorker_id());
			connectorEntity.setCreatedAt(existedEntity == null ? DateUtils.getDateTime() : existedEntity.getCreatedAt());
			connectorEntity.setUpdatedAt(DateUtils.getDateTime());
			this.connectorRepository.save(connectorEntity); // save each entity, because to check unique connector name
		} catch (Exception ex) {
			log.error("Save Connector error", ex);
		}
	}


	@Override
	public List<KafkaConnectorApi.Connector> refreshConnectors() throws IOException {
		List<KafkaConnectorEntity> connectorRepositoryAll = getConnectorRepositoryAll();
		// key=name + type
		Map<String, KafkaConnectorEntity> entityMap = new HashMap<>();
		for (KafkaConnectorEntity x : connectorRepositoryAll) {
			String key = x.getName() + x.getType();
			entityMap.putIfAbsent(key, x);
		}
		if (connectorRepositoryAll.size() > entityMap.size()) {
			log.error("Please check tbl_db_connectors, some connectors has same (name, type, deleted) value");
		}
		if (entityMap.isEmpty()) {
			return Collections.emptyList();
		}

		List<KafkaConnectorApi.Connector> connectorInfos = new ArrayList<>();
		try {
			connectorInfos = getAllKafkaConnectorInfos(this.kafkaOracleConnectorApi);
		} catch (Exception ex) {
			// no handle
		}
		try {
			connectorInfos.addAll(getAllKafkaConnectorInfos(this.kafkaPostgresConnectorApi));
		} catch (Exception ex) {
			// no handle
		}

		return connectorInfos.stream().filter(info -> {
			String nameAndType = info.getStatus().getName() + info.getInfo().getConfig().get("connector.class");
			if (entityMap.containsKey(nameAndType)) {
				KafkaConnectorEntity connectorEntity = entityMap.get(nameAndType);
				info.setCreateAt(connectorEntity.getCreatedAt());
				info.setUpdateAt(connectorEntity.getUpdatedAt());
				info.setId(connectorEntity.getId());
				info.setCreatedUser(connectorEntity.getCreatedUser());
				info.setUpdatedUser(connectorEntity.getUpdatedUser());
				return true;
			}
			return false;
		}).collect(Collectors.toList());
	}


	@Override
	public Map<String, String> viewConnectorById(long id) throws IOException {
		KafkaConnectorEntity byName = this.connectorRepository.findById(id);
		if (byName == null) {
			return null;
		}
		return byName.getConfig();
	}

	@Override
	public HttpResponse<String> createConnector(KafkaConnectorApi.ConnectorRequestParam requestParam) throws IOException {
		String type = requestParam.getConfig().get("connector.class");
		KafkaConnectorApi connectorApi = getKafkaConnectorByType(type);
		Call<ResponseBody> caller = connectorApi.createConnector(requestParam);
		Response<ResponseBody> response = caller.execute();
		if (response.isSuccessful()) {
			// store db
			KafkaConnectorApi.Connector recordInfoCreated = getAllKafkaConnectorInfos(connectorApi)
				.stream()
				.filter(info -> Objects.equals(info.getStatus().getName(), requestParam.getName()))
				.findAny()
				.orElse(null);
			saveConnectorInfoToDB(recordInfoCreated);
		}
		return parseResponse(response);
	}

	private KafkaConnectorApi getKafkaConnectorByType(String type) {
		return type.contains(POSTGRES) ? kafkaPostgresConnectorApi : kafkaOracleConnectorApi;
	}

	@Override
	public void stopConnectors() throws IOException {
		List<KafkaConnectorEntity> all = getConnectorRepositoryAll();
		for (KafkaConnectorEntity connectorEntity : all) {
			this.pauseConnectorById(connectorEntity.getId());
		}
	}

	@Override
	public void startConnectors() throws IOException {
		List<KafkaConnectorEntity> all = getConnectorRepositoryAll();
		for (KafkaConnectorEntity connectorEntity : all) {
			this.resumeConnectorById(connectorEntity.getId());
		}
	}

	@Override
	public HttpResponse deleteConnectorById(long id, Boolean isDeleteKafkaConnectorAlso) throws IOException {
		try {
			HttpResponse result = new HttpResponse(HttpStatus.NO_CONTENT.value(), "OK", "OK");
			KafkaConnectorEntity entity = this.connectorRepository.findById(id);
			String connectorName = entity.getName();
			if (isDeleteKafkaConnectorAlso) {
				KafkaConnectorApi connectorApi = getKafkaConnectorByType(entity.getType());
				Call<ResponseBody> caller = connectorApi.deleteConnector(connectorName);
				Response<ResponseBody> response = caller.execute();
				result = parseResponse(response);
			}
			entity.setDeleted(true);
			connectorRepository.save(entity);
			return result;
		} catch (Exception ex) {
			log.error("Delete Connector has an error", ex);
			throw ex;
		}
	}

	@Override
	public void deleteTopicName(String topicName) {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		try (AdminClient client = AdminClient.create(configProps)) {
			client.deleteTopics(Collections.singletonList(topicName));
		}
	}

	private HttpResponse<String> restartConnectorByName(String connectorName, KafkaConnectorApi connectorApi) throws IOException {
		Call<ResponseBody> caller = connectorApi.restartFailedConnector(connectorName);
		Response<ResponseBody> response = caller.execute();
		return parseResponse(response);
	}

	@Override
	public HttpResponse<String> restartTaskOfSingleConnector(long id, int taskId) throws IOException {
		KafkaConnectorEntity entity = this.connectorRepository.findById(id);
		KafkaConnectorApi connectorApi = getKafkaConnectorByType(entity.getType());
		Call<ResponseBody> caller;
		if (taskId >= 0) {
			caller = connectorApi.restartTask(entity.getName(), taskId);
		} else {
			// TODO: Use force restart connector
			caller = connectorApi.forceRestartConnector(entity.getName());
		}
		Response<ResponseBody> response = caller.execute();
		return parseResponse(response);
	}

	@Override
	public HttpResponse<String> restartTaskByNameAndApi(String connectorName, int taskId, KafkaConnectorApi connectorApi) throws IOException {
		Call<ResponseBody> caller = connectorApi.restartTask(connectorName, taskId);
		Response<ResponseBody> response = caller.execute();
		return parseResponse(response);
	}

	@Override
	public List<HttpResponse<String>> restartAll() throws IOException {
		List<HttpResponse<String>> result = new ArrayList<>();
		result.add(restartTasks(this.kafkaOracleConnectorApi));
		result.add(restartTasks(this.kafkaPostgresConnectorApi));

		return result;
	}

	@Override
	public List<KafkaConnectorEntity> history(long id, String name, String type) throws IOException {
		return connectorRepository.findByNameAndTypeAndDeletedTrue(name, type);
	}

	/**
	 * revert connector config
	 *
	 * @param revertId  revert id
	 * @param currentId current id
	 * @throws Exception e
	 */
	@Override
	public void revert(long revertId, long currentId) throws Exception {
		KafkaConnectorEntity config = connectorRepository.findById(revertId);
		KafkaConnectorEntity currentConfig = connectorRepository.findById(currentId);
		if (null == config || null == currentConfig) {
			throw new ResourceNotFoundException("connector config not found");
		}
		KafkaConnectorApi connectorApi = getKafkaConnectorByType(config.getType());
		Call<ResponseBody> caller = connectorApi.update(config.getName(), config.getConfig());
		Response<ResponseBody> response = caller.execute();
		if (response.isSuccessful()) {
			LocalDateTime now = DateUtils.getDateTime();
			currentConfig.setDeleted(true);
			currentConfig.setUpdatedAt(now);
			config.setDeleted(false);
			config.setUpdatedAt(now);
			List<KafkaConnectorEntity> list = Arrays.asList(config, currentConfig);
			connectorRepository.saveAll(list);
		} else {
			try (ResponseBody responseBody = response.errorBody()) {
				String body = responseBody != null ? responseBody.string() : "";
				throw new Exception(body);
			}
		}
	}

	@Override
	public void deleteHistory(List<Long> ids) throws Exception {
		connectorRepository.deleteAllById(ids);
	}

	private HttpResponse<String> restartTasks(KafkaConnectorApi connectorApi) throws IOException {
		HttpResponse<String> result = new HttpResponse<>();
		List<KafkaConnectorApi.Connector> connectors = this.getAllKafkaConnectorInfos(connectorApi);
		for (KafkaConnectorApi.Connector connector : connectors) {
			String connectorName = connector.getStatus().getName();
			if (isFailConnector(connector)) {
				HttpResponse<String> response = this.restartConnectorByName(connectorName, connectorApi);
				log.info("** The connector {} has just been restarted automatically at {}. \n\tDetails -> {}",
					connectorName, DateUtils.getDateTime(), response.getBody());
				continue;
			}
			List<KafkaConnectorApi.ConnectorTask> tasks = connector.getStatus().getTasks();
			for (KafkaConnectorApi.ConnectorTask task : tasks) {
				if (!RUNNING.equals(task.getState())) {
					try {
						HttpResponse<String> response = this.restartTaskByNameAndApi(connectorName, task.getId(), connectorApi);
						if (response.getStatus() != org.apache.http.HttpStatus.SC_NO_CONTENT) {
							log.info("** The Task {} of connector {} has just been restarted automatically at {}. \n\tDetails -> {}",
								task.getId(), connectorName, DateUtils.getDateTime(), response.getBody());
						}
						result = response;
					} catch (IOException e) {
						log.error("Restart the task error with task Id={}, of connector name={}. Detail: {}", task.getId(), connectorName, e);
					}
				}
			}
		}
		return result;
	}

	private boolean isFailConnector(KafkaConnectorApi.Connector connector) {
		KafkaConnectorApi.ConnectorStatus status = connector.getStatus();
		String stateConnector = status.getConnector().getState();
		if (!RUNNING.equals(stateConnector)) {
			return true;
		}
		return status.getTasks().isEmpty();
	}

	@Override
	public HttpResponse<String> updateConnectorByName(long id, Map<String, String> config) throws IOException {
		KafkaConnectorEntity byId = this.connectorRepository.findById(id);
		if (byId == null) {
			return null;
		}
		KafkaConnectorApi connectorApi = getKafkaConnectorByType(byId.getType());
		Call<ResponseBody> caller = connectorApi.update(byId.getName(), config);

		Response<ResponseBody> response = caller.execute();
		HttpResponse<String> update = parseResponse(response);
		// update db
		if (update.getStatus() != HttpStatus.INTERNAL_SERVER_ERROR.value()) {
			byId.setConfig(config);
			byId.setUpdatedAt(DateUtils.getDateTime());
			connectorRepository.save(byId);
		}
		return update;
	}

	@Override
	public HttpResponse pauseConnectorById(long id) throws IOException {
		KafkaConnectorEntity byId = this.connectorRepository.findById(id);
		KafkaConnectorApi connectorApi = getKafkaConnectorByType(byId.getType());

		Call<ResponseBody> caller = connectorApi.stopConnector(byId.getName());
		Response<ResponseBody> response = caller.execute();
		return parseResponse(response);
	}

	@Override
	public HttpResponse<String> resumeConnectorById(long id) throws IOException {
		KafkaConnectorEntity byId = this.connectorRepository.findById(id);
		KafkaConnectorApi connectorApi = getKafkaConnectorByType(byId.getType());

		Call<ResponseBody> caller = connectorApi.resumeConnector(byId.getName());
		Response<ResponseBody> response = caller.execute();
		return parseResponse(response);
	}

	private List<KafkaConnectorEntity> getConnectorRepositoryAll() {
		return this.connectorRepository.findAllActive();
	}

	private List<KafkaConnectorApi.Connector> getAllKafkaConnectorInfos(KafkaConnectorApi connectorApi) throws IOException {
		Call<Map<String, KafkaConnectorApi.Connector>> caller = connectorApi.getConnectorStatusWithExpandInfo();
		Response<Map<String, KafkaConnectorApi.Connector>> response = caller.execute();
		if (response.isSuccessful()) {
			Map<String, KafkaConnectorApi.Connector> body = response.body();
			return body != null ? new ArrayList<>(body.values()) : Collections.emptyList();
		}
		return Collections.emptyList();
	}

	private HttpResponse parseResponse(Response<ResponseBody> response) throws IOException {
		if (response.isSuccessful()) {
			try (ResponseBody responseBody = response.body()) {
				String body = responseBody != null ? responseBody.string() : "";
				return new HttpResponse(response.code(), response.message(), body);
			}
		} else {
			try (ResponseBody responseBody = response.errorBody()) {
				String body = responseBody != null ? responseBody.string() : "";
				return new HttpResponse(response.code(), body, body);
			}
		}
	}
}
