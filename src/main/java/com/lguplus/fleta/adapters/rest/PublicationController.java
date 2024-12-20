package com.lguplus.fleta.adapters.rest;

import com.lguplus.fleta.domain.dto.rest.HttpResponse;
import com.lguplus.fleta.domain.model.operation.PublicationDto;
import com.lguplus.fleta.ports.service.operation.PublicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/publication")
@CrossOrigin
public class PublicationController {

	private final PublicationService publicationService;

	public PublicationController(PublicationService publicationService) {
		this.publicationService = publicationService;
	}

	@GetMapping("/getPublications")
	public ResponseEntity<HttpResponse<List<String>>> getPublications(@RequestParam("db") String db) {
		HttpResponse<List<String>> response = new HttpResponse<>();
		try {
			List<String> publications = publicationService.getPublications(db);
			response.setStatus(HttpStatus.OK.value());
			response.setBody(publications);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(e.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	@GetMapping("/getPublicationTableByPublication")
	public ResponseEntity<HttpResponse<PublicationDto>> getPublicationTableByPublication(@RequestParam("db") String db,
	                                                                                           @RequestParam("publicationName") String publicationName,
	                                                                                           @RequestParam("tableSearch") String tableSearch,
	                                                                                           @RequestParam("pageNo") Integer pageNo,
	                                                                                           @RequestParam("pageSize") Integer pageSize) {
		HttpResponse<PublicationDto> response = new HttpResponse<>();
		try {
			PublicationDto publications = publicationService.getPublicationTableByPublication(publicationName, db,tableSearch, pageSize , (pageNo - 1) * pageSize);
			response.setStatus(HttpStatus.OK.value());
			response.setBody(publications);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(e.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	@GetMapping("/alterPublication")
	public ResponseEntity<HttpResponse<Integer>> alterPublication(@RequestParam("db") String db,
                                                                  @RequestParam("publicationName") String publicationName,
                                                                  @RequestParam("tables") String tables,
                                                                  @RequestParam("action") int action) {
		HttpResponse<Integer> response = new HttpResponse<>();
		try {
			PublicationService.PublicationAction act = getPublicationAction(action);
			int publications = publicationService.alterPublication(db, act, tables, publicationName);
			response.setStatus(HttpStatus.OK.value());
			response.setBody(publications);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(e.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	private PublicationService.PublicationAction getPublicationAction(int action) {
		switch (action) {
			case 1:
				return PublicationService.PublicationAction.ADD_TABLE;
			case 2:
				return PublicationService.PublicationAction.DROP_TABLE;
			default:
				throw new UnsupportedOperationException("do not support");
		}
	}
}
