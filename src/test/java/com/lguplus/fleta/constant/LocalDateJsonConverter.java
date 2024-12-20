package com.lguplus.fleta.constant;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.time.LocalDate;

@JsonComponent
public class LocalDateJsonConverter {

	public static class Serializer extends JsonSerializer<LocalDate> {

		@Override
		public void serialize(
				LocalDate localDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
				throws IOException {
			jsonGenerator.writeString(LocalDateConverter.to(localDate));
		}
	}

	public static class Deserializer extends JsonDeserializer<LocalDate> {

		@Override
		public LocalDate deserialize(
				JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
			return LocalDateConverter.from(jsonParser.getText());
		}
	}
}
