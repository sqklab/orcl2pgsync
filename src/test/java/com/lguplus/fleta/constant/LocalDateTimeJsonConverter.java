package com.lguplus.fleta.constant;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.time.LocalDateTime;

@JsonComponent
public class LocalDateTimeJsonConverter {

	public static class Serializer extends JsonSerializer<LocalDateTime> {

		@Override
		public void serialize(
				LocalDateTime dateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
				throws IOException {
			jsonGenerator.writeString(LocalDateTimeConverter.to(dateTime));
		}
	}

	public static class Deserializer extends JsonDeserializer<LocalDateTime> {

		@Override
		public LocalDateTime deserialize(
				JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
			return LocalDateTimeConverter.from(jsonParser.getText());
		}
	}
}
