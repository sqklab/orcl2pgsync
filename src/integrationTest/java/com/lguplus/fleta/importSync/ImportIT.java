package com.lguplus.fleta.importSync;

import com.lguplus.fleta.DbSyncServiceApplication;
import com.lguplus.fleta.domain.dto.Synchronizer;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.ports.service.SynchronizerService;
import com.lguplus.fleta.ports.service.SyncRequestImportService;
import com.lguplus.fleta.ports.service.SyncRequestService;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = DbSyncServiceApplication.class
)
@ActiveProfiles("integrationTest")
public class ImportIT {

	@Autowired
	private SynchronizerService dbSyncService;

	@Autowired
	private SyncRequestImportService syncImportService;
	@Autowired
	private SyncRequestService syncRequestService;


	@BeforeEach
	public void setUp() {
		// nothing
	}

	@AfterEach
	public void cleanAllResource() {
		dbSyncService.doStop();
		this.syncRequestService.deleteAll();
	}

	@Test
	public void should_Read_ExcelFile_Success() {
		File initialFile = new File("src/integrationTest/java/com/lguplus/fleta/importSync/DBSynchronizerImport.xlsx");
		try {
			InputStream targetStream = new FileInputStream(initialFile);
			List<SyncRequestEntity> syncRequestEntities = syncImportService.readDataFromExcelFile(targetStream);
			assertThat(syncRequestEntities.size() == 4); // 4 Synchronizers
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void should_Import_ExcelFile_Success() {
		File initialFile = new File("src/integrationTest/java/com/lguplus/fleta/importSync/DBSynchronizerImport.xlsx");
		try {
			InputStream targetStream = new FileInputStream(initialFile);
			List<SyncRequestEntity> syncRequestEntities = syncImportService.readDataFromExcelFile(targetStream);
			List<SyncRequestEntity> entities = this.syncRequestService.saveAllSyncRequests(syncRequestEntities);
			assertThat(entities.size() == 4); // 4 Synchronizers
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Assertion
		List<SyncRequestEntity> all = this.syncRequestService.findAll();
		all.stream().filter(x -> x.getId() == 2).findAny().ifPresent(syncRequestEntity -> {
			Assert.assertEquals(syncRequestEntity.getSourceDatabase(), "ORA");
			Assert.assertEquals(syncRequestEntity.getSourceSchema(), "TESTUSER");
			Assert.assertEquals(syncRequestEntity.getSourceTable(), "DBZ_TEST_TABLE_NPK");

			Assert.assertEquals(syncRequestEntity.getDivision(), "Oracle2Postgres");
			Assert.assertEquals(syncRequestEntity.getTargetDatabase(), "PG");
			Assert.assertEquals(syncRequestEntity.getTargetSchema(), "testuser");
			Assert.assertEquals(syncRequestEntity.getTargetTable(), "dbz_test_table_npk_target");

			Assert.assertEquals(syncRequestEntity.getTopicName(), "ORA.TESTUSER.DBZ_TEST_TABLE_NPK");
			Assert.assertEquals(syncRequestEntity.getSynchronizerName(), "ORA.TESTUSER.DBZ_TEST_TABLE_NPK");

			Assert.assertEquals(syncRequestEntity.isEnableTruncate(), false);
			Assert.assertEquals(syncRequestEntity.getConsumerGroup(), "dbsync.2");

		});

		all.stream().filter(x -> x.getId() == 8).findAny().ifPresent(syncRequestEntity -> {
			Assert.assertEquals(syncRequestEntity.getSourceDatabase(), "PG");
			Assert.assertEquals(syncRequestEntity.getSourceSchema(), "testuser");
			Assert.assertEquals(syncRequestEntity.getSourceTable(), "dbz_test_table_npk");

			Assert.assertEquals(syncRequestEntity.getDivision(), "Postgres2Oracle");
			Assert.assertEquals(syncRequestEntity.getTargetDatabase(), "ORA");
			Assert.assertEquals(syncRequestEntity.getTargetSchema(), "TESTUSER");
			Assert.assertEquals(syncRequestEntity.getTargetTable(), "DBZ_TEST_TABLE_NPK_TARGET");

			Assert.assertEquals(syncRequestEntity.getTopicName(), "REV_PG.testuser.dbz_test_table_npk");
			Assert.assertEquals(syncRequestEntity.getSynchronizerName(), "REV_PG.testuser.dbz_test_table_npk");

			Assert.assertEquals(syncRequestEntity.isEnableTruncate(), false);
			Assert.assertEquals(syncRequestEntity.getConsumerGroup(), "dbsync.8");

		});
	}


}