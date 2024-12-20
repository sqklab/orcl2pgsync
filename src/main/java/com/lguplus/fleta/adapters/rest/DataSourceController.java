package com.lguplus.fleta.adapters.rest;


import com.lguplus.fleta.domain.dto.DataSourceInfo;
import com.lguplus.fleta.domain.dto.DataSourceState;
import com.lguplus.fleta.domain.dto.rest.HttpResponse;
import com.lguplus.fleta.domain.dto.ui.DataSourceResponse;
import com.lguplus.fleta.domain.model.DataSourceEntity;
import com.lguplus.fleta.ports.service.DataSourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/datasource")
@CrossOrigin
public class DataSourceController {

	private static final String DEFAULT_SORT_FIELD = "id";

	private final DataSourceService dataSourceService;


	public DataSourceController(@Qualifier("defaultDatasourceContainer") DataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}

	@PostMapping("/add")
	public ResponseEntity<?> addDataSource(@RequestBody DataSourceInfo dsInfo) {
		if (dsInfo == null || dsInfo.getServerName() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		try {
			DataSourceEntity dataSourceEntity = dsInfo.toDataSourceInfo();
			dataSourceService.checkServerNameAndUrl(dsInfo);
			dataSourceService.initializeDataSource(dsInfo, true);
			dataSourceService.createNewDataSource(dataSourceEntity);
			dataSourceService.loadDataSourceForAddRuntimeCase(dsInfo, dataSourceEntity);
			return ResponseEntity.status(HttpStatus.OK).body(dsInfo);
		} catch (DataSourceService.DisconnectedDataSourceException ex) {
			log.warn(ex.getMessage(), ex);

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
		} catch (DataSourceService.DuplicateDataSourceException ex) {
			log.warn(ex.getMessage(), ex);

			return ResponseEntity.status(HttpStatus.INSUFFICIENT_STORAGE).body(ex.getMessage());
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
		}
	}

	@PostMapping("/testConnection")
	public ResponseEntity<Boolean> testConnection(@RequestBody DataSourceInfo dsInfo) {
		if (dsInfo == null || dsInfo.getServerName() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		return ResponseEntity.status(HttpStatus.OK).body(dataSourceService.testConnection(dsInfo));
	}


	@PutMapping("/update")
	public ResponseEntity<DataSourceInfo> updateDatasource(@RequestBody() DataSourceInfo dsInfo) {
		try {
			DataSourceEntity sourceInfo = dsInfo.toDataSourceInfo();
			if (sourceInfo.getStatus() == DataSourceState.IN_USE) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dsInfo);
			}

			if (dsInfo.getIsPending()) {
				dataSourceService.updateDataSource(sourceInfo, DataSourceState.PENDING);
			} else {
				dataSourceService.updateDataSource(sourceInfo, DataSourceState.ACTIVE);
				dataSourceService.initializeDataSource(sourceInfo, true);
			}

			return ResponseEntity.status(HttpStatus.OK).body(sourceInfo.toDataSourceInfo());
		} catch (DataSourceService.DisconnectedDataSourceException ex) {
			log.warn(ex.getMessage(), ex);

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(dsInfo);
		} catch (DataSourceService.DuplicateDataSourceException ex) {
			log.warn(ex.getMessage(), ex);

			return ResponseEntity.status(HttpStatus.INSUFFICIENT_STORAGE).body(dsInfo);
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dsInfo);
		}
	}


	@GetMapping("/view/{id}")
	public ResponseEntity<DataSourceInfo> viewDatasource(@PathVariable("id") long id) {
		if (id < 0) {
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
		}
		DataSourceInfo dto = dataSourceService.findDataSourceInfoById(id);
		if (dto == null) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(dto);
	}

	@PostMapping("/delete")
	public ResponseEntity<Boolean> deleteDatasource(@RequestBody List<Long> ids) {
		return ResponseEntity.status(HttpStatus.OK).body(dataSourceService.deleteByIdAndStatus(ids));
	}

	@GetMapping("/all")
	public ResponseEntity<DataSourceResponse> getTopics(@RequestParam("pageNo") int pageNo, @RequestParam("pageSize") Integer pageSize) {
		try {
			DataSourceResponse paginated = this.dataSourceService.findPaginated(pageNo, pageSize, DEFAULT_SORT_FIELD);
			return new ResponseEntity<>(paginated, HttpStatus.OK);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);

			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
