package com.lguplus.fleta.domain.service.convertor.util;

import java.text.SimpleDateFormat;

public class DebeziumDataTypeDict {
    public static final String KAFKACONNECT_DATE = "org.apache.kafka.connect.data.Date";
    public static final String KAFKACONNECT_TIME = "org.apache.kafka.connect.data.Time";
    public static final String KAFKACONNECT_TIMESTAMP = "org.apache.kafka.connect.data.Timestamp";
    public static final String DEBEZIUM_MICROTIMESTAMP = "io.debezium.time.MicroTimestamp";
    public static final String DEBEZIUM_MICROTIME = "io.debezium.time.MicroTime";
    public static final String STRUCT = "struct";
    public static final String STRING = "string";
    public static final String INT64 = "int64";
    public static final String BYTEA = "BYTEA";
    public static final String BOOLEAN = "boolean";
}
