package com.lguplus.fleta.partitiontable;

import com.lguplus.fleta.DbSyncServiceApplication;
import com.lguplus.fleta.domain.service.constant.DivisionType;
import com.lguplus.fleta.domain.service.exception.DatasourceNotFoundException;
import com.lguplus.fleta.ports.service.DataSourceService;
import com.lguplus.fleta.ports.service.SynchronizerService;
import com.lguplus.fleta.ports.service.SyncRequestService;
import com.lguplus.fleta.util.TestTableHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = DbSyncServiceApplication.class
)
@ActiveProfiles("integrationTest")
public class PartitionTableIT {

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private SynchronizerService dbSyncService;

    @Autowired
    private SyncRequestService syncRequestService;

    private static final String DS_NAME_PG = "PG";

    private static final String DS_SCHEMA_PG = "testuser";

    private static final String DS_PG_TARGET_TABLE = "test_table_parent_partition";

    private TestTableHelper pg;

    @BeforeEach
    public void setUp() {
        this.cleanSyncData();
        dataSourceService.initialize();
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
    public void should_detect_parittion_table_success() throws DatasourceNotFoundException, InterruptedException {
        try {
            this.pg.exeStm("CREATE TABLE " + DS_SCHEMA_PG + "." + DS_PG_TARGET_TABLE +
                    "(" +
                    "    id bigserial NOT NULL," +
                    "    first_name character varying(255) NOT NULL," +
                    "    last_name character varying(255) NOT NULL," +
                    "    gender smallint NOT NULL," +
                    "    country_id bigint NOT NULL," +
                    "    title character varying(255) NOT NULL," +
                    "    started_date date," +
                    "    created timestamp without time zone NOT NULL" +
                    ") PARTITION BY RANGE(started_date);");
            this.pg.exeStm("CREATE TABLE ENGINEER_Q1_2020 PARTITION OF " + DS_SCHEMA_PG + "." + DS_PG_TARGET_TABLE + " FOR VALUES" +
                    "    FROM ('2025-01-01') TO ('2025-04-01');");
            boolean detectPartition = syncRequestService.detectPartition(DivisionType.POSTGRES_TO_POSTGRES.getDivisionStr(), "PG", DS_SCHEMA_PG,
                    DS_PG_TARGET_TABLE, "PG", DS_SCHEMA_PG, DS_PG_TARGET_TABLE);
            Assertions.assertTrue(detectPartition);
        } finally {
            this.pg.dropTable(DS_PG_TARGET_TABLE);
        }
    }

    @Test
    public void should_detect_parittion_table_return_false_because_no_partition() throws DatasourceNotFoundException, InterruptedException {
        try {
            this.pg.exeStm("CREATE TABLE " + DS_SCHEMA_PG + "." + DS_PG_TARGET_TABLE +
                    "(" +
                    "    id bigserial NOT NULL," +
                    "    first_name character varying(255) NOT NULL," +
                    "    last_name character varying(255) NOT NULL," +
                    "    gender smallint NOT NULL," +
                    "    country_id bigint NOT NULL," +
                    "    title character varying(255) NOT NULL," +
                    "    started_date date," +
                    "    created timestamp without time zone NOT NULL" +
                    ");");
            boolean detectPartition = syncRequestService.detectPartition(DivisionType.POSTGRES_TO_POSTGRES.getDivisionStr(), "PG", DS_SCHEMA_PG,
                    DS_PG_TARGET_TABLE, "PG", DS_SCHEMA_PG, DS_PG_TARGET_TABLE);
            Assertions.assertFalse(detectPartition);
        } finally {
            this.pg.dropTable(DS_PG_TARGET_TABLE);
        }
    }
}