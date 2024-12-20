package com.lguplus.fleta.domain.service.convertor;

import com.lguplus.fleta.domain.dto.DebeziumDataType;
import com.lguplus.fleta.domain.service.convertor.util.DebeziumVersion;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static com.lguplus.fleta.domain.service.convertor.util.DebeziumVersion.isBugExist_DBZ4891;
import static com.lguplus.fleta.domain.service.convertor.util.DebeziumDataTypeDict.*;

class DebeziumDataReceiverFromOracle extends DebeziumDataReceiverFromDB {
    public DebeziumDataReceiverFromOracle(DebeziumVersion debeziumVersion) {
        super(debeziumVersion);
    }

    @Override
    public Object debeziumValueToJavaObject(Object value, DebeziumDataType type) {
        if (null == value) return null;
        switch (type.getSuperType()){
            case KAFKACONNECT_DATE:
                return org.apache.kafka.connect.data.Date.toLogical(
                                org.apache.kafka.connect.data.Date.SCHEMA,
                                Integer.parseInt(String.valueOf(value))
                        )
                        .toInstant()
                        .atZone(zoneId)
                        .toLocalDate();
            case KAFKACONNECT_TIME:
                return Instant.ofEpochMilli(Long.parseLong(String.valueOf(value)))
                        .atZone(zoneId)
                        .toLocalTime()
                        .truncatedTo(ChronoUnit.SECONDS);
            case KAFKACONNECT_TIMESTAMP:
                return Instant.ofEpochMilli(Long.parseLong(String.valueOf(value)))
                        .atZone(zoneId)
                        .toLocalDateTime();
            case DEBEZIUM_MICROTIMESTAMP:
                return Instant.ofEpochMilli(Long.parseLong(String.valueOf(value)) / 1000);
            case DEBEZIUM_MICROTIME:
                Date microTime = new Date(Long.parseLong(String.valueOf(value)));
                return microtimeFormat.format(microTime);
            case STRING:
                if (isBugExist_DBZ4891(super.debeziumVersion)){
                    return value.toString().replaceAll("''", "'");
                } else {
                    return value.toString();
                }
            default:
                return value;
        }
    }
}
