package com.lguplus.fleta.columncomparison;

import com.lguplus.fleta.DbSyncServiceApplication;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.ports.service.DataSourceService;
import com.lguplus.fleta.ports.service.SynchronizerService;
import com.lguplus.fleta.ports.service.SyncRequestImportService;
import com.lguplus.fleta.ports.service.SyncRequestService;
import com.lguplus.fleta.util.TestTableHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.*;
import java.util.List;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = DbSyncServiceApplication.class
)
@ActiveProfiles("integrationTest")
public class ColumnComparisonIT {

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private SynchronizerService dbSyncService;

    @Autowired
    private SyncRequestImportService syncImportService;
    @Autowired
    private SyncRequestService syncRequestService;

    private static final String DS_NAME_ORA = "ORA";
    private static final String DS_NAME_PG = "PG";

    private static final String DS_SCHEMA_ORA = "TESTUSER";
    private static final String DS_SCHEMA_PG = "testuser";

    private static final String DS_ORA_SOURCE_TABLE = "TEST_TABLE_DIFFERENT_COLUMN";
    private static final String DS_PG_TARGET_TABLE = "test_table_different_column";

    private TestTableHelper ora;
    private TestTableHelper pg;

    @BeforeEach
    public void setUp() {
        this.cleanSyncData();
        dataSourceService.initialize();

        this.ora = new TestTableHelper(
                dataSourceService.findDatasourceByServerName(DS_NAME_ORA),
                DS_SCHEMA_ORA,
                true
        );
        this.pg = new TestTableHelper(
                dataSourceService.findDatasourceByServerName(DS_NAME_PG),
                DS_SCHEMA_PG,
                false
        );
    }

    @AfterEach
    public void cleanAllResource() {
        cleanSyncData();
    }

    private void cleanSyncData() {
        dbSyncService.doStop();
        this.syncRequestService.deleteAll();
    }

    @Test
    public void should_compare_with_different_columns_in_both_database() throws IOException {
        try {
            this.ora.createTableWitColumns(DS_ORA_SOURCE_TABLE, List.of("COL_BPCHAR VARCHAR2(30)", "COL_VARCHAR VARCHAR2(30)", "COL_FLOAT8 NUMBER", "COL_NUMERIC NUMBER"));
            this.pg.createTableWitColumns(DS_PG_TARGET_TABLE, List.of("col_bpchar bpchar NULL", "col_varchar varchar NULL"));
            this.importSync();

            SyncRequestEntity serviceById = syncRequestService.findById(1L);
            boolean comparisonError = syncRequestService.isColumnComparisonError(serviceById);
            Assertions.assertTrue(comparisonError); // has diff

        } finally {
            this.ora.dropTable(DS_ORA_SOURCE_TABLE);
            this.pg.dropTable(DS_PG_TARGET_TABLE);
        }
    }

    @Test
    public void should_detect_partition_table_success() throws IOException {
        this.importSync();

        SyncRequestEntity syncRequest = syncRequestService.findByTopicNameAndSynchronizerName("ORA.TESTUSER.DBZ_TEST_TABLE", "ORA.TESTUSER.DBZ_TEST_TABLE");

        boolean comparisonError = syncRequestService.isColumnComparisonError(syncRequest);

        Assertions.assertFalse(comparisonError); // because there is no diff
    }

    private void importSync() throws IOException {
        File initialFile = new File("src/integrationTest/java/com/lguplus/fleta/columncomparison/DBSynchronizerImportComparison.xlsx");
        InputStream targetStream = new FileInputStream(initialFile);
        List<SyncRequestEntity> syncRequestEntities = syncImportService.readDataFromExcelFile(targetStream);
        this.syncRequestService.saveAllSyncRequests(syncRequestEntities);
    }

}