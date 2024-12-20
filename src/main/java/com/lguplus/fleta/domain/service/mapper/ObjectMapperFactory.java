package com.lguplus.fleta.domain.service.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Hold the ObjectMapper instance in a thread-safe singleton
 *
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Jan 2022
 */
public final class ObjectMapperFactory {

	private static final Object MUTEX = new Object();

	private static volatile ObjectMapperFactory INSTANCE;

	private final ObjectMapper objectMapper = createInstance();

	private ObjectMapperFactory() {
	}

	public static ObjectMapperFactory getInstance() {
		ObjectMapperFactory instance = INSTANCE;

		if (instance == null) {
			synchronized (MUTEX) {
				instance = INSTANCE;

				if (instance == null) {
					INSTANCE = instance = new ObjectMapperFactory();
				}
			}
		}

		return instance;
	}

	/**
	 * Create a new ObjectMapper configured with standard settings
	 *
	 * @return
	 */
	private static ObjectMapper createInstance() {
		ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
				.serializationInclusion(JsonInclude.Include.NON_NULL) // Don’t include null values
				.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) //ISODate
				.featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
				.build();

		mapper.registerModule(new JavaTimeModule());

		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
		return mapper;
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}
}
