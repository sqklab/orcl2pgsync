package com.lguplus.fleta.latencyanalysis;

import com.lguplus.fleta.DbSyncServiceApplication;
import com.lguplus.fleta.domain.dto.LastMessageInfoDto;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.ports.repository.ReceivedMessageRepository;
import com.lguplus.fleta.ports.service.DataSourceService;
import com.lguplus.fleta.ports.service.SynchronizerService;
import com.lguplus.fleta.ports.service.SyncRequestImportService;
import com.lguplus.fleta.ports.service.SyncRequestService;
import com.lguplus.fleta.util.TestTableHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = DbSyncServiceApplication.class
)
@ActiveProfiles("integrationTest")
public class LatencyAnalysisIT {

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private SynchronizerService dbSyncService;

    @Autowired
    private ReceivedMessageRepository analysisRepository;
    @Autowired
    private SyncRequestImportService syncImportService;
    @Autowired
    private SyncRequestService syncRequestService;

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
    public void setUp() throws IOException, InterruptedException {
        cleanSyncData();

        dataSourceService.initialize();
        importSync();
        startAll();

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

    @CsvSource(value = {
            DS_NAME_PG  + "|" + DS_PG_SOURCE_TABLE_NPK  + "|" + DS_NAME_ORA + "|" + DS_ORA_TARGET_TABLE_NPK
    }, delimiter = '|')
    @ParameterizedTest
    public void should_show_latency_success(String sourceDB, String sourceTable, String targetDB, String targetTable) throws InterruptedException {
        // given
        TestTableHelper sourceTableHelper = this.getTestTableHelperByName(sourceDB);
        TestTableHelper targetTableHelper = this.getTestTableHelperByName(targetDB);

        // when
        sourceTableHelper.setRandomData();
        targetTableHelper.setData(sourceTableHelper.getData());
        while (true) {
            try {
                sourceTableHelper.executeInsert(sourceTable);
                break;
            } catch (DuplicateKeyException e){
                sourceTableHelper.setRandomData();
                System.out.println(">>> Duplicate key found; retry.");
            }
        }

        // then
        try{
            TimeUnit.SECONDS.sleep(5);
            LastMessageInfoDto lastMessageInfo = this.analysisRepository.findLastMessageInfo("REV_PG." + DS_SCHEMA_PG + "." + DS_PG_SOURCE_TABLE_NPK);
            Assertions.assertNotNull(lastMessageInfo);
        } finally {
            sourceTableHelper.executeDelete(sourceTable);
            sourceTableHelper.executeDelete(targetTable);
        }
    }

    private void importSync() {
        try {
            File initialFile = new File("src/integrationTest/java/com/lguplus/fleta/latencyanalysis/DBSynchronizerImport.xlsx");
            InputStream targetStream = new FileInputStream(initialFile);
            List<SyncRequestEntity> syncRequestEntities = syncImportService.readDataFromExcelFile(targetStream);
            this.syncRequestService.saveAllSyncRequests(syncRequestEntities);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startAll() throws IOException, InterruptedException {
        this.dbSyncService.startSynchronizer();
        TimeUnit.SECONDS.sleep(10);
    }


    private TestTableHelper getTestTableHelperByName(String testTableHelperName) throws GetTestTableHelperException {
        switch (testTableHelperName){
            case DS_NAME_ORA:
                return this.ora;
            case DS_NAME_PG:
                return this.pg;
            default:
                throw new GetTestTableHelperException(testTableHelperName);
        }
    }
    private static class GetTestTableHelperException extends RuntimeException {
        public GetTestTableHelperException(String s) {
            super(String.format(">>> %s is not exist", s));
        }
    }
}