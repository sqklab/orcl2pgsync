package com.lguplus.fleta.adapters.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lguplus.fleta.ports.service.KafkaHealthCheckService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/heath")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class HealthCheckController {
	public final ObjectMapper objectMapper;
	private final KafkaHealthCheckService kafkaHealthIndicator;

	public HealthCheckController(ObjectMapper objectMapper, KafkaHealthCheckService kafkaHealthIndicator) {
		this.objectMapper = objectMapper;
		this.kafkaHealthIndicator = kafkaHealthIndicator;
	}

	@GetMapping("/kafka/start")
	public SseEmitter startHeathCheck() {
		return kafkaHealthIndicator.startHeathCheck();
	}

	@GetMapping("/kafka/stop")
	public void stopHeathCheck() {
		kafkaHealthIndicator.stopHeathCheck();
	}

	@GetMapping("/memory/stats")
	public MemoryStats getMemoryStatistics() {
		MemoryStats stats = new MemoryStats();
		stats.setHeapSize(Runtime.getRuntime().totalMemory());
		stats.setHeapMaxSize(Runtime.getRuntime().maxMemory());
		stats.setHeapFreeSize(Runtime.getRuntime().freeMemory());
		return stats;
	}

	public static class Converter {
		static long kilo = 1024;
		static long mega = kilo * kilo;
		static long giga = mega * kilo;
		static long tera = giga * kilo;

		public static String getSize(long size) {
			String s = "";
			double kb = (double) size / kilo;
			double mb = kb / kilo;
			double gb = mb / kilo;
			double tb = gb / kilo;
			if (size < kilo) {
				s = size + " Bytes";
			} else if (size >= kilo && size < mega) {
				s = String.format("%.2f", kb) + " KB";
			} else if (size >= mega && size < giga) {
				s = String.format("%.2f", mb) + " MB";
			} else if (size >= giga && size < tera) {
				s = String.format("%.2f", gb) + " GB";
			} else if (size >= tera) {
				s = String.format("%.2f", tb) + " TB";
			}
			return s;
		}
	}

	@Setter
	static class MemoryStats {
		private long heapSize;
		private long heapMaxSize;
		private long heapFreeSize;

		public String getHeapSize() {
			return Converter.getSize(heapSize);
		}

		public String getHeapMaxSize() {
			return Converter.getSize(heapMaxSize);
		}

		public String getHeapFreeSize() {
			return Converter.getSize(heapFreeSize);
		}
	}
}
