package com.lguplus.fleta.adapters.rest;


import com.lguplus.fleta.domain.dto.rest.HttpResponse;
import com.lguplus.fleta.domain.dto.ui.CurrentScnInfo;
import com.lguplus.fleta.domain.dto.ui.WrapLogMnrContent;
import com.lguplus.fleta.domain.service.exception.DatasourceNotFoundException;
import com.lguplus.fleta.ports.service.operation.redo.RedoLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/redolog")
@CrossOrigin
public class RedoLogController {
	private final RedoLogService redoLogService;

	public RedoLogController(RedoLogService redoLogService) {
		this.redoLogService = redoLogService;
	}

	@GetMapping("/getCurrentSCN")
	public ResponseEntity<HttpResponse<CurrentScnInfo>> getCurrentSCN(@RequestParam("db") String db) {
		HttpResponse<CurrentScnInfo> response = new HttpResponse<>();
		if (StringUtils.isEmpty(db)) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.setMessage("Source DB is required!");
			return ResponseEntity.ok(response);
		}
		try {
			CurrentScnInfo currentSCN = redoLogService.getCurrentSCN(db);
			response.setStatus(HttpStatus.OK.value());
			response.setBody(currentSCN);
			return ResponseEntity.ok(response);
		} catch (SQLException | DatasourceNotFoundException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(e.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	@GetMapping("/getTimestampByScn")
	public ResponseEntity<HttpResponse<String>> getTimestampByScn(@RequestParam("db") String db, @RequestParam("scn") @NotNull Long scn) {
		HttpResponse<String> response = new HttpResponse<>();
		if (StringUtils.isEmpty(db)) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.setMessage("Source DB is required!");
			return ResponseEntity.ok(response);
		}
		try {
			String timestampByScn = redoLogService.getTimestampByScn(scn, db);
			response.setStatus(HttpStatus.OK.value());
			response.setBody(timestampByScn);
			return ResponseEntity.ok(response);
		} catch (SQLException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(e.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	@GetMapping("/getScnByTimestamp")
	public ResponseEntity<HttpResponse<String>> getScnByTimestamp(@RequestParam("db") @NotBlank @NotNull String db,
																  @RequestParam("dateTime") @NotBlank @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {
		HttpResponse<String> response = new HttpResponse<>();
		try {
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
			String formattedDate = dateTime.format(dateTimeFormatter);

			String timestampByScn = redoLogService.getScnByTimestamp(formattedDate, db);
			response.setStatus(HttpStatus.OK.value());
			response.setBody(timestampByScn);
			return ResponseEntity.ok(response);
		} catch (SQLException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(e.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	@GetMapping("/getTablesByDbAndSchema")
	public ResponseEntity<HttpResponse<List<String>>> getTablesByDbAndSchema(@RequestParam("db") @NotBlank @NotNull String db,
																			 @RequestParam("schema") @NotBlank @NotNull String schema) {
		HttpResponse<List<String>> response = new HttpResponse<>();
		try {
			List<String> tables = redoLogService.getTablesByDbAndSchema(db, schema);
			response.setStatus(HttpStatus.OK.value());
			response.setBody(tables);
			return ResponseEntity.ok(response);
		} catch (SQLException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(e.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	@GetMapping("/searchLogMnrContents")
	public ResponseEntity<HttpResponse<WrapLogMnrContent>> searchLogMnrContents(@RequestParam("startScn") @NotBlank @NotNull String startScn, @RequestParam("endScn") @NotBlank @NotNull String endScn,
																				@RequestParam("operationTypes") @NotBlank @NotNull List<Integer> operationTypes,
																				@RequestParam("tables") @NotBlank @NotNull List<String> tables,
																				@RequestParam("schema") @NotBlank @NotNull String schema,
																				@RequestParam("db") @NotBlank @NotNull String db, @RequestParam("totalPage") Integer totalPage,
																				@RequestParam("currentPage") Integer currentPage, @RequestParam("pageSize") Integer pageSize) {
		HttpResponse<WrapLogMnrContent> response = new HttpResponse<>();
		try {
			WrapLogMnrContent logMnrContents = redoLogService.searchLogMnrContents(startScn, endScn, operationTypes, tables, schema, db, totalPage, currentPage, pageSize);
			response.setStatus(HttpStatus.OK.value());
			response.setBody(logMnrContents);
			return ResponseEntity.ok(response);
		} catch (SQLException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(e.getMessage());
			return ResponseEntity.ok(response);
		}
	}
}
