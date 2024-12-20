package com.lguplus.fleta.ports.service.operation;

import com.lguplus.fleta.domain.model.operation.PublicationDto;
import com.lguplus.fleta.domain.service.exception.DatasourceNotFoundException;

import java.sql.SQLException;
import java.util.List;

public interface PublicationService {
	List<String> getPublications(String db) throws SQLException, DatasourceNotFoundException;
	PublicationDto getPublicationTableByPublication(String publicationName, String db, String table, int pageSize, int offset) throws SQLException, DatasourceNotFoundException;

	// Alter
	int alterPublication(String db, PublicationAction action, String tables, String publicationName) throws SQLException, DatasourceNotFoundException;

	enum PublicationAction {
		ADD_TABLE(" ADD TABLE "),
		DROP_TABLE(" DROP TABLE ");

		final String value;

		PublicationAction(String s) {
			this.value = s;
		}

		public String getValue() {
			return this.value;
		}
	}
}
