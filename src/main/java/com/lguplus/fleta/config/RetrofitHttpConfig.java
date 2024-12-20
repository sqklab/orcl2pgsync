package com.lguplus.fleta.config;

import com.lguplus.fleta.adapters.messagebroker.KafkaConnectorApi;
import com.lguplus.fleta.adapters.messagebroker.KafkaMessagesBehindApi;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

@Configuration
public class RetrofitHttpConfig {

	@Value("${spring.kafka.base-url}")
	private String BASE_URL_KAFKA_IPTV_MANAGEMENT;

	@Value("${spring.kafka.connector.base-url}")
	private String BASE_URL_KAFKA_ORACLE_CONNECTOR;

	@Value("${spring.kafka.connector.postgres.base-url}")
	private String BASE_URL_KAFKA_POSTGRES_CONNECTOR;

	private ConnectionPool connectionPool() {
		// Re-using connections for 5 minutes
		// keeps 100 idle connections at most
		return new ConnectionPool(100, 5, TimeUnit.MINUTES);
	}

	@Bean
	OkHttpClient okHttpClient() {
		OkHttpClient.Builder builder = new OkHttpClient.Builder()
			.connectionPool(connectionPool())
			.connectTimeout(20, TimeUnit.SECONDS)
			.writeTimeout(60, TimeUnit.SECONDS)
			.readTimeout(60, TimeUnit.SECONDS);
		return builder.build();
	}

	@Bean
	Converter.Factory converterFactory() {
		return JacksonConverterFactory.create();
	}

	@Bean
	Retrofit kafkaMessagesBehindRetrofit(OkHttpClient client, Converter.Factory converterFactory) {
		return new Retrofit.Builder()
			.baseUrl(BASE_URL_KAFKA_IPTV_MANAGEMENT)
			.addConverterFactory(converterFactory)
			.client(client)
			.build();
	}

	@Bean
	Retrofit kafkaOracleConnectorRetrofit(OkHttpClient client, Converter.Factory converterFactory) {
		return new Retrofit.Builder()
			.baseUrl(BASE_URL_KAFKA_ORACLE_CONNECTOR)
			.addConverterFactory(converterFactory)
			.client(client)
			.build();
	}

	@Bean
	Retrofit kafkaPostgresConnectorRetrofit(OkHttpClient client, Converter.Factory converterFactory) {
		return new Retrofit.Builder()
			.baseUrl(BASE_URL_KAFKA_POSTGRES_CONNECTOR)
			.addConverterFactory(converterFactory)
			.client(client)
			.build();
	}

	@Bean
	public KafkaMessagesBehindApi kafkaMessagesBehindApi(Retrofit kafkaMessagesBehindRetrofit) {
		return kafkaMessagesBehindRetrofit.create(KafkaMessagesBehindApi.class);
	}

	@Bean("kafkaOracleConnectorApi") // For Oracle Connector
	public KafkaConnectorApi kafkaOracleConnectorApi(Retrofit kafkaOracleConnectorRetrofit) {
		return kafkaOracleConnectorRetrofit.create(KafkaConnectorApi.class);
	}

	@Bean("kafkaPostgresConnectorApi") // For Postgres Connector
	public KafkaConnectorApi kafkaPostgresConnectorApi(Retrofit kafkaPostgresConnectorRetrofit) {
		return kafkaPostgresConnectorRetrofit.create(KafkaConnectorApi.class);
	}
}
