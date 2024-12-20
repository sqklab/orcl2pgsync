package com.lguplus.fleta.domain.service.connectors;

import com.lguplus.fleta.adapters.messagebroker.KafkaConnectorApi;
import com.lguplus.fleta.domain.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.*;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Feb 2022
 */
@Slf4j
@Component
public class LostConnectorHandler {

	private static final String TABLE_WHITELIST = "table.whitelist";
	private static final String TABLE_BLACKLIST = "table.blacklist";

	private static final String POSTGRES_TABLE_WHITELIST = "table.include.list";

	private final KafkaConnectorApi kafkaOracleConnectorApi;

	// For Postgres Connector
	private final KafkaConnectorApi kafkaPostgresConnectorApi;

	public LostConnectorHandler(KafkaConnectorApi kafkaOracleConnectorApi, KafkaConnectorApi kafkaPostgresConnectorApi) {
		this.kafkaOracleConnectorApi = kafkaOracleConnectorApi;
		this.kafkaPostgresConnectorApi = kafkaPostgresConnectorApi;
	}

	/**
	 * Force restarts a specific connector for give schema name
	 *
	 * @param schema
	 */
	public void restartLostConnector(final String dbName, final String schema) {
		Objects.requireNonNull(schema, "The schema name must not be null");

		Arrays.asList(kafkaOracleConnectorApi, kafkaPostgresConnectorApi).forEach((kafkaConnectorApi) -> {
			try {
				Call<Map<String, KafkaConnectorApi.Connector>> caller = kafkaConnectorApi.getConnectorStatusWithExpandInfo();
				Response<Map<String, KafkaConnectorApi.Connector>> response = caller.execute();
				if (response.isSuccessful()) {
					Map<String, KafkaConnectorApi.Connector> connectors = response.body();
					KafkaConnectorApi.Connector connector = getCorrespondingConnector(schema, connectors);
					if (Objects.nonNull(connector)) {
						String connectorName = connector.getStatus().getName();

						// Force restarts a specific connector
						Call<ResponseBody> restartCaller = kafkaConnectorApi.forceRestartConnector(connectorName);
						Response<ResponseBody> restartResponse = restartCaller.execute();
						if (restartResponse.isSuccessful()) {
							log.info("** The connector {} has just been restarted automatically at {} due to lost its session. \n\tDetails -> {}",
									connectorName, DateUtils.getDateTime(), connector);
						} else {
							log.error("** There is an error occurred while restarting connector {} at {} due to some reason.",
									connectorName, new Date());
						}
					}
				}
			} catch (IOException ex) {
				log.error(ex.getMessage(), ex);
			}
		});
	}

	private KafkaConnectorApi.Connector getCorrespondingConnector(final String schema, Map<String, KafkaConnectorApi.Connector> connectorMap) {
		if (Objects.isNull(connectorMap)) return null;
		List<KafkaConnectorApi.Connector> connectors = new ArrayList<>(connectorMap.values());
		for (KafkaConnectorApi.Connector connector : connectors) {
			KafkaConnectorApi.ConnectorInfo connectorInfo = connector.getInfo();
			if (Objects.nonNull(connectorInfo)) {
				Map<String, String> config = connectorInfo.getConfig();
				// E.g: "table.whitelist": "MCUSTUSER.*", or
				//		"table.whitelist": "VODUSER.PT_VO_BUY,VODUSER.PT_VO_BUY_DETAIL,VODUSER.PT_VO_WATCH_HISTORY_NSC,
				//		VODUSER.PT_VO_WATCH_HISTORY,VODUSER.PT_VO_SET_TIME_PTT,VODUSER.PT_VO_CATEGORY_MAP_UNITED,
				//		VODUSER.PT_VO_CATEGORY_MAP_HISTORY"
				String tableWhitelist = config.get(TABLE_WHITELIST);

				if (tableWhitelist == null || ("").equals(tableWhitelist)) {
					tableWhitelist = config.get(POSTGRES_TABLE_WHITELIST);
				}
				// E.g: "table.blacklist": ""
				// 		"table.blacklist": "VODUSER.PT_VO_BUY,VODUSER.PT_VO_BUY_DETAIL,VODUSER.PT_VO_WATCH_HISTORY_NSC,
				// 		VODUSER.PT_VO_WATCH_HISTORY,VODUSER.PT_VO_SET_TIME_PTT,VODUSER.PT_VO_CATEGORY_MAP_UNITED,
				// 		VODUSER.PT_VO_CATEGORY_MAP_HISTORY"
				String tableBlacklist = config.get(TABLE_BLACKLIST);
				if (Objects.isNull(tableWhitelist) || StringUtils.isEmpty(tableWhitelist)) continue;
				if (tableWhitelist.contains(schema)) {
					return connector;
				}
			}
		}
		return null;
	}
}
