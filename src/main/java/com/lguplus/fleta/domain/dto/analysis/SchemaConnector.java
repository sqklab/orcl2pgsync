package com.lguplus.fleta.domain.dto.analysis;

import org.springframework.beans.factory.annotation.Value;

public interface SchemaConnector {

	@Value("#{target.dbName}")
	String getDbName();


	@Value("#{target.schemaName}")
	String getSchemaName();
}
