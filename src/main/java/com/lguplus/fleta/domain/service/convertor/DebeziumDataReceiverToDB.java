package com.lguplus.fleta.domain.service.convertor;

import com.lguplus.fleta.domain.dto.DebeziumDataType;
import com.lguplus.fleta.domain.service.convertor.util.DebeziumVersion;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.lguplus.fleta.domain.service.convertor.util.DebeziumDataTypeDict.*;

abstract class DebeziumDataReceiverToDB implements IDebeziumDataReceiverToDB {
    protected final DebeziumVersion debeziumVersion;

    DebeziumDataReceiverToDB(DebeziumVersion debeziumVersion) {
        this.debeziumVersion = debeziumVersion;
    }

    @Override
    public String valueToSqlClause(Object value, DebeziumDataType type) {
        if (null == value) return null;

        switch (type.getSuperType()){
            case KAFKACONNECT_DATE:
                return "to_date('" + new SimpleDateFormat("MM/dd/yyyy").format(value) + "', 'mm/dd/yyyy')";
            case KAFKACONNECT_TIME:
                return "to_date('" + new SimpleDateFormat("HH:mm:ss").format(value) + "', 'HH:mm:ss')";
            case KAFKACONNECT_TIMESTAMP:
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss.SSS");
                return "to_timestamp('" + formatter.format((LocalDateTime) value) + "', 'mm/dd/yyyy hh24:mi:ss.ff3')";
            case BOOLEAN:
                return (Boolean) value ? "1" : "0";
            case STRING:
                return "'" + ((String) value).replace("'", "''") + "'";
            case DEBEZIUM_MICROTIME:
            case BYTEA:
            default:
                return String.valueOf(value);
        }
    }
}
