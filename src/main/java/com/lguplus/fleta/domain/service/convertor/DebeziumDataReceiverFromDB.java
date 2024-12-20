package com.lguplus.fleta.domain.service.convertor;

import com.lguplus.fleta.domain.service.convertor.util.DebeziumVersion;

import java.text.SimpleDateFormat;
import java.time.ZoneId;

abstract class DebeziumDataReceiverFromDB implements IDebeziumDataReceiverFromDB {
    protected static final SimpleDateFormat microtimeFormat = new SimpleDateFormat("HH:mm:ss");
    protected static final ZoneId zoneId = ZoneId.of("UTC");

    protected final DebeziumVersion debeziumVersion;

    DebeziumDataReceiverFromDB(DebeziumVersion debeziumVersion) {
        this.debeziumVersion = debeziumVersion;
    }
}
