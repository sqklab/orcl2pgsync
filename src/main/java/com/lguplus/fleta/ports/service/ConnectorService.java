package com.lguplus.fleta.ports.service;

import com.lguplus.fleta.adapters.messagebroker.KafkaConnectorApi;
import com.lguplus.fleta.domain.dto.rest.HttpResponse;
import com.lguplus.fleta.domain.model.KafkaConnectorEntity;
import com.lguplus.fleta.domain.service.exception.ResourceNotFoundException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ConnectorService {
	/**
	 * Synchronizer with kafka-rest api: store connectors to database
	 *
	 * @return
	 */
	List<KafkaConnectorApi.Connector> fetchConnectors() throws IOException;

	/**
	 * Get new-est status of connector
	 *
	 * @return
	 */
	List<KafkaConnectorApi.Connector> refreshConnectors() throws IOException;

	Map<String, String> viewConnectorById(long id) throws IOException;

	/**
	 * 201: created, other bad request
	 *
	 * @param requestParam
	 * @return
	 */
	HttpResponse<String> createConnector(KafkaConnectorApi.ConnectorRequestParam requestParam) throws IOException;

	HttpResponse<String> updateConnectorByName(long id, Map<String, String> config) throws IOException;

	HttpResponse<String> pauseConnectorById(long id) throws IOException;

	HttpResponse<String> resumeConnectorById(long id) throws IOException;

	void stopConnectors() throws IOException;

	void startConnectors() throws IOException;

	HttpResponse deleteConnectorById(long id, Boolean isDeleteKafkaConnectorAlso) throws IOException;

	void deleteTopicName(String topicName) throws IOException;

	HttpResponse<String> restartTaskOfSingleConnector(long id, int taskId) throws IOException;

	HttpResponse<String> restartTaskByNameAndApi(String connectorName, int taskId, KafkaConnectorApi connectorApi) throws IOException;

	List<HttpResponse<String>> restartAll() throws IOException;

	List<KafkaConnectorEntity> history(long id, String name, String type) throws IOException;

	void revert(long revertId, long currentId) throws Exception;

	void deleteHistory(List<Long> ids) throws Exception;

}
