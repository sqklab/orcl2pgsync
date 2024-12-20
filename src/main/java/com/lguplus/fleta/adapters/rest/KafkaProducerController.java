package com.lguplus.fleta.adapters.rest;


import com.lguplus.fleta.domain.dto.KafkaProducerDto;
import com.lguplus.fleta.domain.dto.rest.HttpResponse;
import com.lguplus.fleta.ports.service.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/kafka-producer")
@CrossOrigin
public class KafkaProducerController {
	private final KafkaProducerService kafkaProducerService;

	public KafkaProducerController(KafkaProducerService kafkaProducerService) {
		this.kafkaProducerService = kafkaProducerService;
	}

	@GetMapping("getTopics")
	public ResponseEntity<HttpResponse<List<String>>> getTopic(@RequestParam("fetchBy") String fetchBy) {
		HttpResponse<List<String>> response = new HttpResponse<>();
		try {
			List<String> topics = this.kafkaProducerService.getTopics(fetchBy);
			response.setStatus(HttpStatus.OK.value());
			response.setMessage("OK");
			response.setBody(topics);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(ex.getMessage());
			return ResponseEntity.ok(response);
		}
	}

	@PostMapping("/producer")
	public ResponseEntity<HttpResponse<String>> send(@RequestBody @Valid KafkaProducerDto param) {
		HttpResponse<String> response = new HttpResponse<>();
		try {
			kafkaProducerService.send(param);
			response.setStatus(HttpStatus.OK.value());
			response.setBody("OK");
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(ex.getMessage());
			return ResponseEntity.ok(response);
		}
	}
}
