package com.lguplus.fleta.domain.service.convertor;

import com.lguplus.fleta.domain.service.constant.DbmsKind;
import com.lguplus.fleta.domain.service.constant.DivisionType;
import com.lguplus.fleta.domain.service.convertor.util.DebeziumVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DebeziumConvertorFactory {

	@Value("${debezium.version}")
	private String debeziumVersionStr;

	public IDebeziumConverter getConverter(DivisionType division) {
		DebeziumVersion version = new DebeziumVersion(debeziumVersionStr);

		final IDebeziumDataReceiverFromDB fromDB;
		final IDebeziumDataReceiverToDB toDB;
		final DebeziumDataReceiver receiver;

		switch (division.getSource()){
			case DbmsKind.ORACLE:
				fromDB = new DebeziumDataReceiverFromOracle(version);
				break;
			case DbmsKind.POSTGRES:
				fromDB = new DebeziumDataReceiverFromPostgres(version);
				break;
			default:
				log.warn("invalid source DB {}", division.getSource());
				fromDB = new DebeziumDataReceiverFromOracle(version);
		}

		switch (division.getTarget()){
			case DbmsKind.ORACLE:
				toDB = new DebeziumDataReceiverToOracle(version);
				receiver = new DebeziumDataReceiver(fromDB, toDB);
				return new DebeziumConverterToOracle(receiver);
			case DbmsKind.POSTGRES:
				toDB = new DebeziumDataReceiverToPostgres(version);
				receiver = new DebeziumDataReceiver(fromDB, toDB);
				return new DebeziumConverterToPostgres(receiver);
			default:
				log.warn("invalid target DB {}", division.getTarget());
				toDB = new DebeziumDataReceiverToPostgres(version);
				receiver = new DebeziumDataReceiver(fromDB, toDB);
				return new DebeziumConverterToPostgres(receiver);
		}

	}
}
