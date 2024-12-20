package com.lguplus.fleta.adapters.rest;


import com.lguplus.fleta.domain.dto.operation.OperationDto;
import com.lguplus.fleta.domain.dto.operation.OperationSummary;
import com.lguplus.fleta.domain.dto.rest.HttpResponse;
import com.lguplus.fleta.domain.dto.ui.OperationResponse;
import com.lguplus.fleta.domain.model.operation.OperationProcessEntity;
import com.lguplus.fleta.domain.service.exception.DatasourceNotFoundException;
import com.lguplus.fleta.domain.service.exception.InvalidPrimaryKeyException;
import com.lguplus.fleta.ports.service.OperationService;
import com.lguplus.fleta.ports.service.operation.OperationOracleReaderService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/operation")
@CrossOrigin
public class OperationController {
	private final OperationService operationService;
	private final OperationOracleReaderService oracleReaderService;

	public OperationController(OperationService operationService,
							   OperationOracleReaderService oracleReaderService) {
		this.operationService = operationService;
		this.oracleReaderService = oracleReaderService;
	}

	@PostMapping("/getColumnIdValue")
	public ResponseEntity<?> getColumnIdValue(@RequestBody OperationDto RequestParam) {
		HttpResponse<List<String>> response = new HttpResponse<>();
		try {
			List<String> values = this.operationService.getValueByIds(RequestParam.getSourceDatabase(), RequestParam.getSourceSchema(), RequestParam.getTable(), RequestParam.getColumnIdName(), RequestParam.getColumnIdValue());
			response.setBody(values);
			response.setMessage("OK");
			response.setStatus(HttpStatus.OK.value());
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response.setMessage(e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return ResponseEntity.ok(response);
		}
	}

	@GetMapping("/checkOperation")
	public ResponseEntity<HttpResponse<OperationProcessEntity>> checkOperation(@RequestParam("table") String table, @RequestParam("sessionId") String sessionId, @RequestParam("whereCondition") String whereCondition) {
		HttpResponse<OperationProcessEntity> response = new HttpResponse<>();
		try {
			OperationProcessEntity operationProcess = operationService.checkOperation(table, sessionId, whereCondition);
			response.setStatus(HttpStatus.OK.value());
			response.setBody(operationProcess);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(e.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	@GetMapping("/getOperationSummary")
	public ResponseEntity<HttpResponse<OperationSummary>> getOperationSummary(@RequestParam("table") String table,
																			  @RequestParam("sessionId") String sessionId,
																			  @RequestParam("whereCondition") String whereCondition) {
		HttpResponse<OperationSummary> response = new HttpResponse<>();
		try {
			OperationSummary operationSummaryDto = operationService.getOperationSummary(table, sessionId, whereCondition);
			response.setStatus(HttpStatus.OK.value());
			response.setBody(operationSummaryDto);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(e.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/deleteDiff")
	public ResponseEntity<?> deleteDiff(@RequestBody @Valid DiffDeleteReq req) {
		HttpResponse<?> response = new HttpResponse<>();
		try {
			operationService.deleteDiffResult(req.getTable(), req.getSession(), req.getWhere());
			response.setStatus(HttpStatus.OK.value());
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(e.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/deleteProcess")
	public ResponseEntity<?> deleteProcess(@RequestBody @Valid DiffDeleteReq req) {
		HttpResponse<?> response = new HttpResponse<>();
		try {
			operationService.deleteProcess(req.getTable(), req.getSession(), req.getWhere());
			response.setStatus(HttpStatus.OK.value());
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(e.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	@GetMapping("/loadSearching")
	public ResponseEntity<HttpResponse<OperationResponse>> loadSearching(@RequestParam(name = "sourceDatabase") @Valid @NotBlank String sourceDatabase,
																		 @RequestParam(name = "sourceSchema") @Valid @NotBlank String sourceSchema,
																		 @RequestParam(name = "targetDatabase") @Valid @NotBlank String targetDatabase,
																		 @RequestParam(name = "targetSchema") @Valid @NotBlank String targetSchema,
																		 @RequestParam("table") String table,
																		 @RequestParam("session") String session,
																		 @RequestParam("whereStm") String whereStm,
																		 @RequestParam("pageNo") Integer pageNo,
																		 @RequestParam("primaryKeys") String primaryKeys,
																		 @RequestParam("pageSize") Integer pageSize) {
		HttpResponse<OperationResponse> response = new HttpResponse<>();
		try {
			primaryKeys = StringUtils.isBlank(primaryKeys) ? operationService.getPrimaryKeys(targetDatabase, targetSchema, table.toLowerCase()) : primaryKeys;
			if (StringUtils.isBlank(primaryKeys)) {
				response.setStatus(HttpStatus.NOT_FOUND.value());
				response.setMessage(String.format("Can't detect primary keys for table %s.%s. Please sure that table %s is in schema %s.",
						targetSchema, table.toLowerCase(), table.toLowerCase(), targetSchema));
				return ResponseEntity.ok(response);
			}

			Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
			OperationResponse compareDiffItems = operationService.paggedLoadSearching(pageable, sourceDatabase, sourceSchema,
					targetDatabase, targetSchema, table, primaryKeys, session, whereStm);
			compareDiffItems.setPrimaryKeys(primaryKeys);
			response.setStatus(HttpStatus.OK.value());
			response.setBody(compareDiffItems);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(e.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/compare")
	public ResponseEntity<HttpResponse<List<OperationService.CompareDiffItem>>> compare(@RequestBody @Valid OperationDto requestParam) {
		HttpResponse<List<OperationService.CompareDiffItem>> response = new HttpResponse<>();
		try {
			String primaryKeys = operationService.getPrimaryKeys(requestParam.getTargetDatabase(), requestParam.getTargetSchema(), requestParam.getTable().toLowerCase());
			if (StringUtils.isBlank(primaryKeys)) {
				response.setStatus(HttpStatus.NOT_FOUND.value());
				response.setMessage(String.format("Can't detect primary keys for table %s.%s. Please sure that table %s is in schema %s.",
						requestParam.getTargetSchema(), requestParam.getTable().toLowerCase(),
						requestParam.getTable().toLowerCase(), requestParam.getTargetSchema()));
				return ResponseEntity.ok(response);
			}
			OperationProcessEntity operationProcess = operationService.checkOperation(requestParam.getTable(), requestParam.getSessionId(), requestParam.getWhereStm());
			if (operationProcess != null && operationProcess.getState()) {
				response.setStatus(HttpStatus.OK.value());
				response.setBody(null);
				response.setMessage("System is finding different between 2 tables");
				return ResponseEntity.ok(response);
			}

			oracleReaderService.compare(requestParam, primaryKeys);

			response.setStatus(HttpStatus.OK.value());
			response.setBody(null);
			response.setMessage("System is finding different between 2 tables");
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(ex.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/correctBatch")
	public HttpResponse<OperationService.CorrectionBatchResult> correctBatch(@RequestBody List<OperationService.CompareDiffItem> diffItems,
																			 @RequestParam(name = "sourceDatabase") @Valid @NotBlank String sourceDatabase,
																			 @RequestParam(name = "sourceSchema") @Valid @NotBlank String sourceSchema,
																			 @RequestParam(name = "targetDatabase") @Valid @NotBlank String targetDatabase,
																			 @RequestParam(name = "targetSchema") @Valid @NotBlank String targetSchema,
																			 @RequestParam(name = "targetTable") @Valid @NotBlank String targetTable,
																			 @RequestParam("session") String session,
																			 @RequestParam("whereStm") String whereStm) {
		HttpResponse<OperationService.CorrectionBatchResult> response = new HttpResponse<>();
		if (Objects.isNull(diffItems) || diffItems.isEmpty()) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.setMessage("Invalid RequestParameters!");
			return response;
		}
		try {
			String primaryKeys = operationService.getPrimaryKeys(targetDatabase, targetSchema, targetTable.toLowerCase());
			if (StringUtils.isBlank(primaryKeys)) {
				response.setStatus(HttpStatus.NOT_FOUND.value());
				response.setMessage(String.format("Can't detect primary keys for table %s.%s. Please sure that table %s is in schema %s.",
						targetSchema, targetTable.toLowerCase(), targetTable.toLowerCase(), targetSchema));
				return response;
			}
			log.info("Correct list {} items to {}.{}.{}", diffItems.size(), targetDatabase, targetSchema, targetTable);
			diffItems = operationService.getDiffData(diffItems, sourceDatabase, sourceSchema, targetDatabase, targetSchema, targetTable, primaryKeys);
			OperationService.CorrectionBatchResult batchResult = new OperationService.CorrectionBatchResult();
			try {
				List<OperationService.CorrectionResult> exceptions = operationService.correct(diffItems, session, whereStm, targetDatabase, targetSchema, targetTable, Arrays.asList(primaryKeys.split(",")));
				List<String> fail = exceptions.stream().map(OperationService.CorrectionResult::getPKey).collect(Collectors.toList());
				List<String> needDelete = diffItems.stream()
						.map(OperationService.CompareDiffItem::getUuid)
						.filter(s -> fail.isEmpty() || !fail.contains(s)).collect(Collectors.toList());
				operationService.deleteDiffResult(targetTable, needDelete);

				batchResult.setSuccess(exceptions.size() < diffItems.size());
				batchResult.setExceptions(exceptions);
				response.setStatus(HttpStatus.OK.value());
				response.setBody(batchResult);
			} catch (DatasourceNotFoundException | InvalidPrimaryKeyException e1) {
				log.error(e1.getMessage(), e1);
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				response.setMessage(e1.getMessage());
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(ex.getMessage());
		}

		return response;
	}

	@PostMapping("/cancelSearching")
	public HttpResponse<?> cancelSearching(@RequestParam("table") @Valid @NotBlank String table,
										   @RequestParam("session") @Valid @NotBlank String session,
										   @RequestParam("whereStm") @Valid @NotBlank String whereStm) {

		HttpResponse<?> response = new HttpResponse<>();
		try {
			operationService.cancelOperation(table, session, whereStm);

			response.setStatus(HttpStatus.OK.value());
			return response;
		} catch (Exception e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(e.getMessage());
			return response;
		}
	}

	@Getter
	@Setter
	@NoArgsConstructor
	static class DiffDeleteReq {
		@NotBlank
		private String table;
		@NotBlank
		private String session;
		@NotBlank
		private String where;
	}
}
