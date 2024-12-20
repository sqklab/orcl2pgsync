package com.lguplus.fleta.domain.service.exportImport;

import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.domain.model.comparison.DbComparisonInfoEntity;
import com.lguplus.fleta.domain.service.constant.Constants;
import com.lguplus.fleta.domain.service.constant.DivisionType;
import com.lguplus.fleta.domain.util.CommonUtils;
import com.lguplus.fleta.domain.util.DateUtils;
import com.lguplus.fleta.ports.service.SyncRequestImportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.apache.poi.ss.usermodel.CellType.STRING;

@Slf4j
@Service
public class SyncRequestImportServiceImpl implements SyncRequestImportService {

	public static final String COMMA = ",";

	@Value("${app.import.domain}")
	public String domainTypes;

	@Override
	public List<SyncRequestEntity> readDataFromExcelFile(InputStream inputStream) throws IOException {
		Map<String, SyncRequestEntity> mapSyncInfo = new HashMap<>();
		try (inputStream; XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
			int sheetNumber = workbook.getNumberOfSheets();
			int linkedSync = 0;
			String[] domains = domainTypes.split(COMMA);
			for (int i = 0; i < sheetNumber; i++) {
				Sheet sheet = workbook.getSheetAt(i);

				for (Row row : sheet) {
					if (row.getRowNum() < 2) { // ignore 2 row header
						continue;
					}
					Cell cellId = row.getCell(0);
					Cell cellSourceDB = row.getCell(1);
					Cell cellSourceSchema = row.getCell(2);
					Cell cellSourceTable = row.getCell(3);
					Cell cellTargetDivision = row.getCell(4);
					Cell cellTargetDB = row.getCell(5);
					Cell cellTargetSchema = row.getCell(6);
					Cell cellTargetTable = row.getCell(7);
					Cell cellEnableDML = row.getCell(10);
					Cell cellSyncName = row.getCell(8);
					Cell cellTopic = row.getCell(9);
					if (cellId == null && cellSourceDB == null && cellSourceSchema == null && cellSourceTable == null && cellTargetDivision == null && cellTargetDB == null && cellTargetSchema == null && cellTargetTable == null) {
						continue;
					}
					String division = getCellStringValue(cellTargetDivision);
					SyncRequestEntity syncInfo = getSyncRequestEntity(cellId, cellSourceDB, cellSourceSchema, cellSourceTable, cellTargetDB, cellTargetSchema, cellTargetTable, cellEnableDML, division);

					//Fixme: Modify on 09-29-2022. Use default instead of db.schema.table
					String defaultSyncName = getCellStringValue(cellSyncName);
					String defaultTopicName = getCellStringValue(cellTopic);
					String customTopicName = String.format("%s.%s.%s", syncInfo.getSourceDatabase(), syncInfo.getSourceSchema(), syncInfo.getSourceTable());
					if (isDomainType(domains, division)) {
						String topicName = StringUtils.isBlank(defaultTopicName) ? customTopicName : defaultTopicName;
						if (DivisionType.POSTGRES_TO_ORACLE.getDivisionStr().equalsIgnoreCase(division)) {
							topicName = Constants.REV_TOPIC_PREFIX + topicName;
						}
						syncInfo.setTopicName(topicName);
						syncInfo.setSynchronizerName(StringUtils.isBlank(defaultSyncName) ? syncInfo.getTopicName() : defaultSyncName);
						if (!mapSyncInfo.containsKey(syncInfo.getTopicName())) {
							mapSyncInfo.put(syncInfo.getTopicName(), syncInfo);
						} else {
							linkedSync++;
							String syncName = StringUtils.isBlank(defaultSyncName) ? syncInfo.getTopicName() + "_(" + linkedSync + ")" : defaultSyncName;
							syncInfo.setSynchronizerName(syncName);
							mapSyncInfo.put(syncInfo.getSynchronizerName(), syncInfo);
						}

						DbComparisonInfoEntity dbComparisonInfo = getDbComparisonInfoEntity(row, syncInfo);
						syncInfo.addComparisonEntity(dbComparisonInfo);
					}
				}
			}
		}
		return new ArrayList<>(mapSyncInfo.values());
	}

	private boolean isDomainType(String[] domains, String division) {
		return division != null && Arrays.stream(domains).anyMatch(division::equalsIgnoreCase);
	}

	private String getCellStringValue(Cell cell) {
		if (cell == null) {
			return null;
		}
		switch (cell.getCellType()) {
			case STRING:
			case NUMERIC:
				return cell.getRichStringCellValue().getString();
			case FORMULA:
				if (cell.getCachedFormulaResultType() == STRING) {
					return cell.getRichStringCellValue().getString();
				}
				return "";
			default:
				return "";
		}
	}

	private SyncRequestEntity getSyncRequestEntity(Cell cellId, Cell cellSourceDB, Cell cellSourceSchema, Cell cellSourceTable, Cell cellTargetDB,
												   Cell cellTargetSchema, Cell cellTargetTable, Cell cellEnableDML, String division) {
		SyncRequestEntity syncInfo = new SyncRequestEntity();
		if (cellId != null && ((XSSFCell) cellId).getRawValue() != null) {
			Long id = Double.valueOf(cellId.getNumericCellValue()).longValue();
			syncInfo.setId(id);
		}
		syncInfo.setSourceDatabase(getCellStringValue(cellSourceDB));
		syncInfo.setSourceSchema(getCellStringValue(cellSourceSchema));
		syncInfo.setSourceTable(getCellStringValue(cellSourceTable));

		syncInfo.setDivision(division);

		syncInfo.setTargetDatabase(getCellStringValue(cellTargetDB));
		syncInfo.setTargetSchema(getCellStringValue(cellTargetSchema));
		syncInfo.setTargetTable(getCellStringValue(cellTargetTable));
		if (syncInfo.getId() != null) {
			syncInfo.setConsumerGroup(CommonUtils.getKafkaGroupId(syncInfo.getId()));
		}
		syncInfo.setEnableTruncate(cellEnableDML != null && cellEnableDML.getBooleanCellValue());

		syncInfo.setCreatedAt(DateUtils.getDateTime());
		return syncInfo;
	}

	private DbComparisonInfoEntity getDbComparisonInfoEntity(Row row, SyncRequestEntity syncInfo) {
		DbComparisonInfoEntity dbComparisonInfo = new DbComparisonInfoEntity();
		dbComparisonInfo.setSyncInfo(syncInfo);
		dbComparisonInfo.setIsComparable(getCellStringValue(row.getCell(14)));
		Cell cellEnableColumnComparison = row.getCell(15);
		if (cellEnableColumnComparison == null) {
			dbComparisonInfo.setEnableColumnComparison(false);
		} else {
			dbComparisonInfo.setEnableColumnComparison(cellEnableColumnComparison.getBooleanCellValue());
		}
		dbComparisonInfo.setSourceCompareDatabase(getCellStringValue(row.getCell(16)));
		dbComparisonInfo.setTargetCompareDatabase(getCellStringValue(row.getCell(17)));
		dbComparisonInfo.setSourceQuery(getCellStringValue(row.getCell(18)));
		dbComparisonInfo.setTargetQuery(getCellStringValue(row.getCell(19)));
		return dbComparisonInfo;
	}

}
