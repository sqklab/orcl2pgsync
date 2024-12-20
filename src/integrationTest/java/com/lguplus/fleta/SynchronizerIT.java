package com.lguplus.fleta;

import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.ports.service.DataSourceService;
import com.lguplus.fleta.ports.service.SyncRequestImportService;
import com.lguplus.fleta.ports.service.SyncRequestService;
import com.lguplus.fleta.ports.service.SynchronizerService;
import com.lguplus.fleta.util.TestTableHelper;
import com.lguplus.fleta.util.TestTableNameEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = DbSyncServiceApplication.class
)
@ActiveProfiles("integrationTest")
public class SynchronizerIT {

	@Autowired
	private DataSourceService dataSourceService;

	@Autowired
	private SynchronizerService defaultDbSyncContainer;

	@Autowired
	private SyncRequestService syncRequestService;

	@Autowired
	private SyncRequestImportService syncImportService;

	private static final String DS_SCHEMA_ORA = "TESTUSER";
	private static final String DS_SCHEMA_PG = "testuser";

	private TestTableHelper ora;
	private TestTableHelper pg;

	@BeforeEach
	public void setUp() throws IOException {
		cleanSyncData();
		dataSourceService.initialize();
		importSync();

		this.ora = new TestTableHelper(
				dataSourceService.findDatasourceByServerName(TestTableNameEnum.DBMS.ORA.getDbName()),
				DS_SCHEMA_ORA,
				true
		);
		this.pg = new TestTableHelper(
				dataSourceService.findDatasourceByServerName(TestTableNameEnum.DBMS.PG.getDbName()),
				DS_SCHEMA_PG,
				false
		);
	}

	@AfterEach
	public void cleanAllResource() {
		cleanSyncData();
	}

	private void importSync() {
		try {
			File initialFile = new File("src/integrationTest/java/com/lguplus/fleta/importSync/DBSynchronizerImport.xlsx");
			InputStream targetStream = new FileInputStream(initialFile);
			List<SyncRequestEntity> syncRequestEntities = syncImportService.readDataFromExcelFile(targetStream);
			this.syncRequestService.saveAllSyncRequests(syncRequestEntities);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startAll() throws IOException {
		this.defaultDbSyncContainer.startSynchronizer();
	}

	private void stopAll() {
		this.defaultDbSyncContainer.doStop();
	}

	private void cleanSyncData() {
		this.defaultDbSyncContainer.doStop();
		this.syncRequestService.deleteAll();
	}


	private static Stream<TestTableNameEnum> getAllKindOfTestTableNames() {
		return TestTableNameEnum.getAllKind();
	}

	private static Stream<TestTableNameEnum> getOraSourceKindOfTestTableNames() {
		return TestTableNameEnum.getOraSourceKind();
	}

	@MethodSource(value = "getAllKindOfTestTableNames")
	@ParameterizedTest
	public void cudTest(TestTableNameEnum testTables) throws IOException {
		startAll();

		// given
		TestTableHelper sourceTableHelper = this.getTestTableHelperByName(testTables.getSourceDBMS());
		TestTableHelper targetTableHelper = this.getTestTableHelperByName(testTables.getTargetDBMS());
		sourceTableHelper.setRandomData();
		targetTableHelper.setData(sourceTableHelper.getData());
		System.out.println(">>> " + testTables);

		// when 1
		sourceTableHelper.executeInsert(testTables.getSourceTable());
		// then 1
		final Integer cnt1 = getTargetRecordCount(0, testTables, targetTableHelper);
		assertThat(cnt1).isEqualTo(1);

		// when 2
		sourceTableHelper.resetColInt4(targetTableHelper.resetColInt4());
		sourceTableHelper.executeUpdateColInt4(testTables.getSourceTable());
		// then 2
		int cnt2 = getTargetRecordCount(0, testTables, targetTableHelper);
		assertThat(cnt2).isEqualTo(1);

		// when 3
		sourceTableHelper.executeDelete(testTables.getSourceTable());
		// then 3
		int cnt3 = getTargetRecordCount(1, testTables, targetTableHelper);
		assertThat(cnt3).isEqualTo(0);
	}

	@MethodSource(value = "getOraSourceKindOfTestTableNames")
	@ParameterizedTest
	public void truncateTest(TestTableNameEnum testTables) throws IOException {
		// enable truncate & start
		for (SyncRequestEntity entity : syncRequestService.findAll()) {
			entity.setEnableTruncate(true);
			syncRequestService.createOrUpdate(entity);
		}
		this.startAll();

		// given
		TestTableHelper sourceTableHelper = this.getTestTableHelperByName(testTables.getSourceDBMS());
		TestTableHelper targetTableHelper = this.getTestTableHelperByName(testTables.getTargetDBMS());
		sourceTableHelper.setRandomData();
		targetTableHelper.setData(sourceTableHelper.getData());
		System.out.println(">>> " + testTables);

		// when 1
		sourceTableHelper.executeInsert(testTables.getSourceTable());
		// then 1
		final Integer cnt1 = getTargetRecordCount(0, testTables, targetTableHelper);
		assertThat(cnt1).isEqualTo(1);

		// when 3
		sourceTableHelper.executeTruncate(testTables.getSourceTable());
		// then 3
		int cnt3 = getTargetRecordCount(1, testTables, targetTableHelper);
		assertThat(cnt3).isEqualTo(0);
	}

	private static int getTargetRecordCount(int currCnt, TestTableNameEnum testTables, TestTableHelper targetTableHelper) {
		int result = currCnt;
		try {
			for (int i = 0; i <= 100; i++) {
				if (result == currCnt) {
					result = targetTableHelper.executeCount(testTables.getTargetTable(), i % 10 == 0);
				}
				if (result == currCnt) {
					Thread.sleep(100);
				}
				if (i % 10 == 0) {
					System.out.println(">>> Cannot find, retrying...");
				}
				if (result != currCnt) {
					System.out.println(">>> Found target");
					break;
				}
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	private TestTableHelper getTestTableHelperByName(TestTableNameEnum.DBMS dbms) throws SynchronizerIT.TestTableHelperNotExistException {
		switch (dbms) {
			case ORA:
				return this.ora;
			case PG:
				return this.pg;
			default:
				throw new SynchronizerIT.TestTableHelperNotExistException(dbms.getDbName());
		}
	}

	private static class TestTableHelperNotExistException extends RuntimeException {
		public TestTableHelperNotExistException(String s) {
			super(String.format(">>> %s is not exist", s));
		}
	}
}