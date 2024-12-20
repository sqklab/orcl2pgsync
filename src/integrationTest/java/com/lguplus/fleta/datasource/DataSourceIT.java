package com.lguplus.fleta.datasource;

import com.lguplus.fleta.DbSyncServiceApplication;
import com.lguplus.fleta.domain.dto.DataSourceState;
import com.lguplus.fleta.domain.model.DataSourceEntity;
import com.lguplus.fleta.ports.repository.DataSourceRepository;
import com.lguplus.fleta.ports.service.DataSourceService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = DbSyncServiceApplication.class
)
@ActiveProfiles("integrationTest")
public class DataSourceIT {

    public static final String ORA_1 = "ORA1";
    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Test
    public void should_datasource_connected_success() throws SQLException {
        try {
            DataSourceEntity dataSource = new DataSourceEntity();
            dataSource.setServerName(ORA_1);
            dataSource.setUrl("jdbc:oracle:thin:@//oracle.host:oracle.port/oracle.serviceName2");
            dataSource.setUsername("oracle.user");
            dataSource.setPassword("oracle.pwd");
            dataSource.setMaxPoolSize(100);
            dataSource.setIdleTimeout(60000);
            dataSource.setStatus(DataSourceState.ACTIVE);
            dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");

            this.dataSourceService.createNewDataSource(dataSource);
            dataSourceService.initialize();

            DataSource byServerName = dataSourceService.findDatasourceByServerName(ORA_1);
            Connection connection = byServerName.getConnection();
            Assertions.assertNotNull(connection);
        } finally {
            dataSourceRepository.findByServerName(ORA_1).ifPresent(dataSourceEntity -> {
                this.dataSourceService.removeDataSourceFromMap(ORA_1, false);
                this.dataSourceRepository.deleteById(dataSourceEntity.getId());
            });

        }
    }

}