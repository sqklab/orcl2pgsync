package com.lguplus.fleta.domain.service.operation;


import com.lguplus.fleta.domain.model.operation.PublicationDto;
import com.lguplus.fleta.domain.service.exception.DatasourceNotFoundException;
import com.lguplus.fleta.ports.service.DataSourceService;
import com.lguplus.fleta.ports.service.operation.PublicationService;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PublicationServiceImpl implements PublicationService {

	public static final String DATA_SOURCE_NOT_FOUND = "Data source not found";
	private final DataSourceService dataSourceService;

	public PublicationServiceImpl(DataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}

	@Override
	public List<String> getPublications(String db) throws SQLException, DatasourceNotFoundException {
		List<String> results = new ArrayList<>();
		try (Connection connection = dataSourceService.findConnectionByServerName(db)) {
			if (connection == null) {
				throw new DatasourceNotFoundException(DATA_SOURCE_NOT_FOUND);
			}
			try (PreparedStatement statement = connection.prepareCall("select * from pg_publication")) {
				ResultSet resultSet = statement.executeQuery();
				while (resultSet.next()) {
					String name = resultSet.getString("pubname");
					results.add(name);
				}
			}
		}
		return results;
	}

	@Override
	public PublicationDto getPublicationTableByPublication(String publicationName, String db, String tableSearch,
	                                                       int pageSize, int offset) throws SQLException, DatasourceNotFoundException {
		PublicationDto publicationDto = new PublicationDto();
		try (Connection connection = dataSourceService.findConnectionByServerName(db)) {
			if (connection == null) {
				throw new DatasourceNotFoundException(DATA_SOURCE_NOT_FOUND);
			}
			try (PreparedStatement statement = connection.prepareCall("select *, count(*) OVER() AS total from pg_publication_tables WHERE tablename like ? and pubname = ? LIMIT ? OFFSET ?")) {
				statement.setString(1, "%" + tableSearch + "%");
				statement.setString(2, publicationName);
				statement.setInt(3, pageSize);
				statement.setInt(4, offset);

				ResultSet resultSet = statement.executeQuery();
				int total = 0;
				List<PublicationDto.PublicationInfo> infos = new ArrayList<>();
				while (resultSet.next()) {
					String name = resultSet.getString("pubname");
					String schema = resultSet.getString("schemaname");
					String table = resultSet.getString("tablename");
					total = resultSet.getInt("total");
					PublicationDto.PublicationInfo info = new PublicationDto.PublicationInfo(name, schema, table);
					infos.add(info);
				}
				publicationDto.setPublicationInfoList(infos);
				publicationDto.setTotal(total);
			}
		}
		return publicationDto;
	}

	@Override
	public int alterPublication(String db, PublicationAction action, String tables, String publicationName) throws SQLException, DatasourceNotFoundException {
		String query = String.format("ALTER PUBLICATION %s %s %s", publicationName, action.getValue(), tables);
		try (Connection connection = dataSourceService.findConnectionByServerName(db)) {
			if (connection == null) {
				throw new DatasourceNotFoundException(DATA_SOURCE_NOT_FOUND);
			}
			try (PreparedStatement statement = connection.prepareCall(query)) {
				return statement.executeUpdate();
			}
		}
	}
}
