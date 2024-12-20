package com.lguplus.fleta.domain.service.convertor;

import com.lguplus.fleta.domain.dto.DebeziumDataType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class DebeziumDataReceiver {

    private final IDebeziumDataReceiverFromDB fromDB;
    private final IDebeziumDataReceiverToDB toDB;

    public DebeziumDataReceiver(IDebeziumDataReceiverFromDB fromDB, IDebeziumDataReceiverToDB toDB) {
        this.fromDB = fromDB;
        this.toDB = toDB;
    }

    public String valueToSqlClause(Object value, DebeziumDataType type){
        Object valueObject = fromDB.debeziumValueToJavaObject(value, type);
        return toDB.valueToSqlClause(valueObject, type);
    };
}
