package com.lguplus.fleta.detectpk;

import com.lguplus.fleta.DbSyncServiceApplication;
import com.lguplus.fleta.domain.service.constant.DivisionType;
import com.lguplus.fleta.domain.service.exception.DatasourceNotFoundException;
import com.lguplus.fleta.domain.util.TableConstraint;
import com.lguplus.fleta.ports.service.DataSourceService;
import com.lguplus.fleta.ports.service.SyncRequestService;
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
public class DetectPKIT {
    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    private SyncRequestService syncRequestService;

    @BeforeEach
    public void setUp() {
        dataSourceService.initialize();
    }

    @Test
    public void should_detectPK_success() throws DatasourceNotFoundException {
        String pks = syncRequestService.detectPrimaryKeys(DivisionType.POSTGRES_TO_POSTGRES.getDivisionStr(), "PG", "testuser", "dbz_test_table_target");
        Assertions.assertNotNull(pks);
        Assertions.assertEquals("col_bpchar,col_varchar", pks);
    }

    @Test
    public void should_detectPK_NotFound() throws DatasourceNotFoundException {
        String pks = syncRequestService.detectPrimaryKeys(DivisionType.POSTGRES_TO_POSTGRES.getDivisionStr(), "PG", "testuser", "dbz_test_table_npk_target");
        Assertions.assertEquals("", pks);
    }

}