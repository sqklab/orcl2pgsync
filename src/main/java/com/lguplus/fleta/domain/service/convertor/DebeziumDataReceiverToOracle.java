package com.lguplus.fleta.domain.service.convertor;

import com.lguplus.fleta.domain.service.convertor.util.DebeziumVersion;

class DebeziumDataReceiverToOracle extends DebeziumDataReceiverToDB {
    public DebeziumDataReceiverToOracle(DebeziumVersion debeziumVersion) {
        super(debeziumVersion);
    }
}
