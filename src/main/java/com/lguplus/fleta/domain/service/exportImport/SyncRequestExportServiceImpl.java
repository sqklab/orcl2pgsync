package com.lguplus.fleta.domain.service.exportImport;

import com.lguplus.fleta.domain.dto.SyncExportInfoDto;
import com.lguplus.fleta.domain.dto.Synchronizer;
import com.lguplus.fleta.domain.util.DateUtils;
import com.lguplus.fleta.ports.repository.SyncRequestExportRepository;
import com.lguplus.fleta.ports.service.SyncRequestExportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class SyncRequestExportServiceImpl implements SyncRequestExportService {

	private final SyncRequestExportRepository exportRepository;

	public SyncRequestExportServiceImpl(SyncRequestExportRepository exportRepository) {
		this.exportRepository = exportRepository;
	}

	@Override
	public ByteArrayInputStream exportByConditions(LocalDate dateFrom, LocalDate dateTo, List<String> topicNames, Synchronizer.SyncState state,
	                                           String division, String dbName, String schema) throws IOException {
		List<SyncExportInfoDto> synchronizes;
		int[] states = state == null ? Synchronizer.SyncState.getIntValues() : new int[]{state.getState()};
		String searchTopicNames = "%(" + String.join("|", topicNames) + ")%";
		if (dateFrom == null) {
			synchronizes = this.exportRepository.exportByConditions(division, searchTopicNames, dbName, schema, states);
		} else {
			if (dateTo == null) {
				dateTo = DateUtils.getDate();
			}
			dateTo = dateTo.plusDays(1);
			synchronizes = this.exportRepository.exportByPeriodTimeParams(division, searchTopicNames, dbName, schema, states, dateFrom, dateTo);
		}
		return writeExcel(synchronizes);
	}

	private ByteArrayInputStream writeExcel(List<SyncExportInfoDto> synchronizers) throws IOException {
		String[] header1 = {"SOURCE", "TARGET"};
		String[] header2 = {"ID", "DB", "Schema", "Table", "Division","DB", "Schema", "Table", "Name", "Topic", "Enable DML", "Consumer Group", "Primary Keys", "Sync Type", "Count Comparison",
				"Column Comparison", "Source Comparison DB", "Target Comparison DB", "Source Comparison Query", "Target Comparison Query"};

		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet("Sheet1");
			sheet.addMergedRegion(CellRangeAddress.valueOf("B1:D1"));
			sheet.addMergedRegion(CellRangeAddress.valueOf("F1:H1"));

			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 14);

			// Create a CellStyle with the font
			CellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);
			headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

			Row headerRow1 = sheet.createRow(0);
			// Create cells for row header 1
			Cell cellSource = headerRow1.createCell(1);
			cellSource.setCellValue(header1[0]);
			cellSource.setCellStyle(headerCellStyle);

			Cell cellTarget = headerRow1.createCell(5);
			cellTarget.setCellValue(header1[1]);
			cellTarget.setCellStyle(headerCellStyle);

			Row headerRow2 = sheet.createRow(1);
			// Create cells for row header 2
			for (int i = 0; i < header2.length; i++) {
				Cell cell = headerRow2.createCell(i);
				cell.setCellValue(header2[i]);
				cell.setCellStyle(headerCellStyle);
				CellUtil.setCellStyleProperty(cell, CellUtil.VERTICAL_ALIGNMENT, VerticalAlignment.CENTER);
			}

			int rowNum = 2;
			for (SyncExportInfoDto syncExportInfoDto : synchronizers) {
				Row row = sheet.createRow(rowNum++);
				row.createCell(0).setCellValue(syncExportInfoDto.getId());
				row.createCell(1).setCellValue(syncExportInfoDto.getSourceDatabase());
				row.createCell(2).setCellValue(syncExportInfoDto.getSourceSchema());
				row.createCell(3).setCellValue(syncExportInfoDto.getSourceTable());

				row.createCell(4).setCellValue(syncExportInfoDto.getDivision());

				row.createCell(5).setCellValue(syncExportInfoDto.getTargetDatabase());
				row.createCell(6).setCellValue(syncExportInfoDto.getTargetSchema());
				row.createCell(7).setCellValue(syncExportInfoDto.getTargetTable());
				
				row.createCell(8).setCellValue(syncExportInfoDto.getSynchronizerName());
				row.createCell(9).setCellValue(syncExportInfoDto.getTopicName());
				if (syncExportInfoDto.getEnableTruncate() == null) {
					row.createCell(10).setCellValue(false);
				} else {
					row.createCell(10).setCellValue(syncExportInfoDto.getEnableTruncate());
				}
				row.createCell(11).setCellValue(syncExportInfoDto.getConsumerGroup());

				row.createCell(12).setCellValue(syncExportInfoDto.getPrimaryKeys());
				row.createCell(13).setCellValue(syncExportInfoDto.getSyncType());

				row.createCell(14).setCellValue(syncExportInfoDto.isComparable());

				row.createCell(15).setCellValue(syncExportInfoDto.getEnableColumnComparison() != null && syncExportInfoDto.getEnableColumnComparison());

				row.createCell(16).setCellValue(syncExportInfoDto.getSourceCompareDatabase());
				row.createCell(17).setCellValue(syncExportInfoDto.getTargetCompareDatabase());
				row.createCell(18).setCellValue(syncExportInfoDto.getSourceQuery());
				row.createCell(19).setCellValue(syncExportInfoDto.getTargetQuery());
			}

			// Write the output to a file
			workbook.write(out);
			return new ByteArrayInputStream(out.toByteArray());
		}
	}
}
