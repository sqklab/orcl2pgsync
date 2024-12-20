package com.lguplus.fleta.domain.service.convertor;

import com.lguplus.fleta.domain.service.convertor.util.DebeziumVersion;

class DebeziumDataReceiverToPostgres extends DebeziumDataReceiverToDB {
    public DebeziumDataReceiverToPostgres(DebeziumVersion debeziumVersion) {
        super(debeziumVersion);
    }
}
