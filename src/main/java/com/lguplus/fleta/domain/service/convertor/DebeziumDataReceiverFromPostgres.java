package com.lguplus.fleta.domain.service.convertor;

import com.lguplus.fleta.domain.dto.DebeziumDataType;
import com.lguplus.fleta.domain.service.convertor.util.DebeziumVersion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static com.lguplus.fleta.domain.service.convertor.util.DebeziumDataTypeDict.*;

@Slf4j
class DebeziumDataReceiverFromPostgres extends DebeziumDataReceiverFromDB {
    DebeziumDataReceiverFromPostgres(DebeziumVersion debeziumVersion) {
        super(debeziumVersion);
    }

    @Override
    public Object debeziumValueToJavaObject(Object value, DebeziumDataType type){
        if (null == value) return null;

        switch (type.getSuperType()){
            case KAFKACONNECT_DATE:
                return org.apache.kafka.connect.data.Date.toLogical(
                                org.apache.kafka.connect.data.Date.SCHEMA,
                                Integer.parseInt(String.valueOf(value))
                        )
                        .toInstant()
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate();
            case KAFKACONNECT_TIME:
                return Instant.ofEpochMilli(Long.parseLong(String.valueOf(value)))
                        .atZone(ZoneId.of("UTC"))
                        .toLocalTime()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .toString();
            case KAFKACONNECT_TIMESTAMP:
                return Instant.ofEpochMilli(Long.parseLong(String.valueOf(value)))
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDateTime();
            case DEBEZIUM_MICROTIMESTAMP:
                return Instant.ofEpochMilli(Long.parseLong(String.valueOf(value)) / 1000);
            case DEBEZIUM_MICROTIME:
                return microtimeFormat.format(new Date(Long.parseLong(String.valueOf(value))));
            case BYTEA:
                if (StringUtils.isBlank(value.toString())) return value;
                try {
                    byte[] bytes = Hex.decodeHex(value.toString());
                    return new String(bytes, StandardCharsets.UTF_8);
                } catch (DecoderException e1) {
                    log.debug(e1.getMessage());
                } catch (Exception e2) {
                    log.debug(e2.getMessage(), e2);
                }
                return String.valueOf(value);
            default:
                return value;
        }
    }
}
