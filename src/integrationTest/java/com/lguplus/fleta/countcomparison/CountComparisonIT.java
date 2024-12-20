package com.lguplus.fleta.countcomparison;

import com.lguplus.fleta.DbSyncServiceApplication;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.domain.model.comparison.ComparisonResultEntity;
import com.lguplus.fleta.ports.repository.DbComparisonResultRepository;
import com.lguplus.fleta.ports.service.DataSourceService;
import com.lguplus.fleta.ports.service.SynchronizerService;
import com.lguplus.fleta.ports.service.SyncRequestImportService;
import com.lguplus.fleta.ports.service.SyncRequestService;
import com.lguplus.fleta.ports.service.comparison.ComparisonSchedulerService;
import com.lguplus.fleta.util.TestTableHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = DbSyncServiceApplication.class
)
@ActiveProfiles("integrationTest")
public class CountComparisonIT {

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private ComparisonSchedulerService schedulerService;

    @Autowired
    private SynchronizerService dbSyncService;

    @Autowired
    private SyncRequestImportService syncImportService;
    @Autowired
    private SyncRequestService syncRequestService;
    @Autowired
    private DbComparisonResultRepository resultRepository;

    private static final String DS_NAME_ORA = "ORA";
    private static final String DS_NAME_PG = "PG";

    private static final String DS_SCHEMA_ORA = "TESTUSER";
    private static final String DS_SCHEMA_PG = "testuser";

    private static final String DS_ORA_SOURCE_TABLE = "DBZ_TEST_TABLE";
    private static final String DS_PG_TARGET_TABLE = "dbz_test_table_target";

    private static final String DS_PG_SOURCE_TABLE = "dbz_test_table";
    private static final String DS_ORA_TARGET_TABLE = "DBZ_TEST_TABLE_TARGET";

    private static final String DS_ORA_SOURCE_TABLE_NPK = "DBZ_TEST_TABLE_NPK";
    private static final String DS_PG_TARGET_TABLE_NPK = "dbz_test_table_npk_target";

    private static final String DS_PG_SOURCE_TABLE_NPK = "dbz_test_table_npk";
    private static final String DS_ORA_TARGET_TABLE_NPK = "DBZ_TEST_TABLE_NPK_TARGET";

    private TestTableHelper ora;
    private TestTableHelper pg;

    @BeforeEach
    public void setUp() throws IOException {
        cleanSyncData();
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

    private void truncateTableORA(String schema, String table) {
        this.ora.exeStm("TRUNCATE TABLE " + schema + "." + table);
    }

    private void truncateTablePG(String schema, String table) {
        this.pg.exeStm("TRUNCATE TABLE " + schema + "." + table);
    }

    @Test
    public void should_count_success() throws InterruptedException {
        this.truncateTableORA(DS_SCHEMA_ORA, DS_ORA_SOURCE_TABLE);
        this.truncateTableORA(DS_SCHEMA_ORA, DS_ORA_SOURCE_TABLE_NPK);
        this.truncateTableORA(DS_SCHEMA_ORA, DS_ORA_TARGET_TABLE_NPK);
        this.truncateTableORA(DS_SCHEMA_ORA, DS_ORA_TARGET_TABLE);

        this.truncateTablePG(DS_SCHEMA_PG, DS_PG_SOURCE_TABLE_NPK);
        this.truncateTablePG(DS_SCHEMA_PG, DS_PG_SOURCE_TABLE);
        this.truncateTablePG(DS_SCHEMA_PG, DS_PG_TARGET_TABLE_NPK);
        this.truncateTablePG(DS_SCHEMA_PG, DS_PG_TARGET_TABLE);

        importSync();
        this.genDataORA(10, DS_ORA_SOURCE_TABLE);
        this.genDataPG(10, DS_PG_TARGET_TABLE);

        this.genDataORA(40, DS_ORA_SOURCE_TABLE_NPK);
        this.genDataPG(40, DS_PG_TARGET_TABLE_NPK);

        this.genDataORA(20, DS_ORA_TARGET_TABLE_NPK);
        this.genDataPG(20, DS_PG_SOURCE_TABLE_NPK);

        schedulerService.startOneTimeNow(LocalTime.now(), LocalDate.now());

        Thread.sleep(5_000);
        resultRepository.findAll().forEach(x -> Assertions.assertEquals(x.getComparisonState(), ComparisonResultEntity.ComparisonState.SAME));
    }

    private void genDataORA(int numRow, String table) {
        for (int i = 0; i < numRow; i++) {
            this.ora.setRandomData();
            this.ora.executeInsert(table);
        }
    }

    private void genDataPG(int numRow, String table) {
        for (int i = 0; i < numRow; i++) {
            this.pg.setRandomData();
            this.pg.executeInsert(table);
        }
    }

    private void importSync() {
        try {
            File initialFile = new File("src/integrationTest/java/com/lguplus/fleta/countcomparison/DBSynchronizerTestCountComparison.xlsx");
            InputStream targetStream = new FileInputStream(initialFile);
            List<SyncRequestEntity> syncRequestEntities = syncImportService.readDataFromExcelFile(targetStream);
            this.syncRequestService.saveAllSyncRequests(syncRequestEntities);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}