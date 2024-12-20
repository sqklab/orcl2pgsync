package com.lguplus.fleta.adapters.messagebroker;

import com.lguplus.fleta.domain.util.DateUtils;
import com.lguplus.fleta.domain.util.ShutdownHookUtils;
import com.lguplus.fleta.ports.service.KafkaHealthCheckService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class KafkaHealthCheckServiceImpl implements KafkaHealthCheckService {

	private static final int ADMIN_CLIENT_TIMEOUT_MS = 5000;

	private final KafkaAdmin kafkaAdmin;
	private final SseEmitter emitter = new SseEmitter();
	private final ExecutorService executorService;
	private final Map<String, Object> config = new HashMap<>();

	@Value("${spring.kafka.healthcheck-delay:10000}")
	private int delay;
	private boolean running = false;

	public KafkaHealthCheckServiceImpl(KafkaAdmin kafkaAdmin, @Qualifier("commonThreadPool") ExecutorService executorService) {
		this.kafkaAdmin = kafkaAdmin;
		this.executorService = executorService;
		ShutdownHookUtils.initExecutorServiceShutdownHook(this.executorService);
	}

	@PostConstruct
	private void init() {
		this.config.putAll(kafkaAdmin.getConfigurationProperties());
		this.config.put("request.timeout.ms", 5000);
	}


	/**
	 * start heath check
	 *
	 * @return event
	 */
	@Override
	public SseEmitter startHeathCheck() {
		if (this.running) {
			return emitter;
		}
		this.running = true;
		this.heathCheck();
		return emitter;
	}

	@Override
	public List<String> getTopics() {
		try (AdminClient client = KafkaAdminClient.create(config)) {
			ListTopicsOptions listTopicsOptions = new ListTopicsOptions().timeoutMs(ADMIN_CLIENT_TIMEOUT_MS);
			ListTopicsResult list = client.listTopics(listTopicsOptions);
			Set<String> names = list.names().get();
			return new ArrayList<>(names);
		} catch (Exception e) {
			log.warn("Kafka is disconnected");
			return Collections.emptyList();
		}
	}

	/**
	 * stop heath check
	 */
	@Override
	public void stopHeathCheck() {
		this.running = false;
		log.info("Stop kafka heath check");
	}

	/**
	 * check kafka status
	 *
	 * @return Status.UP or Status.DOWN
	 */
	private Status getKafkaStatus() {
		try (AdminClient client = KafkaAdminClient.create(config)) {
			ListTopicsOptions listTopicsOptions = new ListTopicsOptions().timeoutMs(ADMIN_CLIENT_TIMEOUT_MS);
			ListTopicsResult list = client.listTopics(listTopicsOptions);
			Set<String> names = list.names().get();
			return Status.UP;
		} catch (Exception e) {
			log.warn("Kafka is disconnected");
			log.warn(e.getMessage());
			return Status.DOWN;
		}
	}

	/**
	 * Check kafka status then emit event to client
	 */
	private void heathCheck() {
		log.debug("Start kafka heath check");

		executorService.execute(() -> {
			while (running) {
				try {
					org.springframework.boot.actuate.health.Status status = getKafkaStatus();
					SseEmitter.SseEventBuilder event = SseEmitter.event()
							.data(status)
							.id(String.valueOf(DateUtils.getDateTime()))
							.name("Kafka status");
					emitter.send(event);

				} catch (Exception ex) {
					emitter.completeWithError(ex);
				} finally {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						log.warn(e.getMessage());
					}
				}
			}
		});

		log.debug("Start kafka heath check end");
	}
}
