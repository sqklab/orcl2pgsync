package com.lguplus.fleta.exportSync;

import com.lguplus.fleta.DbSyncServiceApplication;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.ports.service.SynchronizerService;
import com.lguplus.fleta.ports.service.SyncRequestImportService;
import com.lguplus.fleta.ports.service.SyncRequestExportService;
import com.lguplus.fleta.ports.service.SyncRequestService;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.test.context.ActiveProfiles;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = DbSyncServiceApplication.class
)
@ActiveProfiles("integrationTest")
public class ExportIT {

	private static final String DBSYNCHRONIZER_XLSX_CREATED = "src/integrationTest/java/com/lguplus/fleta/exportSync/DBSynchronizerImport.xlsx";
	public static final String DBSYNCHRONIZER_EXPORT_XLSX = "src/integrationTest/java/com/lguplus/fleta/exportSync/DBSynchronizerExport.xlsx";

	@Autowired
	private SynchronizerService dbSyncService;

	@Autowired
	private SyncRequestImportService syncImportService;

	@Autowired
	private SyncRequestService syncRequestService;

	@Autowired
	private SyncRequestExportService exportService;

	@BeforeEach
	public void setUp() {
		dbSyncService.doStop();
		this.syncRequestService.deleteAll();

		this.importData();
	}

	@AfterEach
	public void cleanAllResource() throws IOException {
		dbSyncService.doStop();
		this.syncRequestService.deleteAll();
		Files.deleteIfExists(Path.of(DBSYNCHRONIZER_EXPORT_XLSX));
	}

	private void importData() {
		File initialFile = new File(DBSYNCHRONIZER_XLSX_CREATED);
		try {
			InputStream targetStream = new FileInputStream(initialFile);
			List<SyncRequestEntity> syncRequestEntities = syncImportService.readDataFromExcelFile(targetStream);
			this.syncRequestService.saveAllSyncRequests(syncRequestEntities);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Test
	public void should_Export_ExcelFile_Success() throws IOException, InterruptedException {
		List<String> list = new ArrayList<>();
		list.add("REV_PG.testuser.dbz_test_table_npk");
		list.add("ORA.TESTUSER.DBZ_TEST_TABLE");
		list.add("ORA.TESTUSER.DBZ_TEST_TABLE_NPK");
		list.add("REV_PG.testuser.dbz_test_table");

		InputStreamResource file = new InputStreamResource(this.exportService.exportByConditions(null,null, list,null,"Oracle2Postgres","",""));
		InputStream inputStream = file.getInputStream();

		saveToFile(inputStream);
		Thread.sleep(1_000);
		// verify Excel file
		InputStream targetStream = new FileInputStream(DBSYNCHRONIZER_EXPORT_XLSX);
		List<SyncRequestEntity> syncRequestEntities = syncImportService.readDataFromExcelFile(targetStream);
		assertThat(syncRequestEntities.size() == 4); // 4 Synchronizers
	}

	private void saveToFile(InputStream inputStream) throws IOException {
		File targetFile = new File(DBSYNCHRONIZER_EXPORT_XLSX);
		try (OutputStream outStream = new FileOutputStream(targetFile)) {

			byte[] buffer = new byte[2 * 1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
			}
			IOUtils.closeQuietly(inputStream);
		}
	}
}