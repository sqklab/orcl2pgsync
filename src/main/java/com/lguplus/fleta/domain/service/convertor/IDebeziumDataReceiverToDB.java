package com.lguplus.fleta.domain.service.convertor;

import com.lguplus.fleta.domain.dto.DebeziumDataType;

public interface IDebeziumDataReceiverToDB {
    String valueToSqlClause(Object value, DebeziumDataType type);
}
