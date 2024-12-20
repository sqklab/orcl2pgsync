package com.lguplus.fleta.domain.service.convertor;

import com.lguplus.fleta.domain.dto.DebeziumDataType;

public interface IDebeziumDataReceiverFromDB {
    Object debeziumValueToJavaObject(Object value, DebeziumDataType type);
}
