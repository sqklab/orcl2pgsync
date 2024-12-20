package com.lguplus.fleta.domain.service.comparison;

import ch.qos.logback.classic.Logger;
import com.lguplus.fleta.adapters.messagebroker.KafkaConstants;
import com.lguplus.fleta.adapters.messagebroker.KafkaMessagesBehindApi;
import com.lguplus.fleta.adapters.messagebroker.KafkaProperties;
import com.lguplus.fleta.domain.dto.MessagesBehindInfo;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.domain.model.comparison.DbComparisonInfoEntity;
import com.lguplus.fleta.domain.service.constant.Constants;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Call;
import retrofit2.Response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Jan 2022
 */
@Slf4j
public class ComparisonBase {

	protected final Logger logger;
	private final KafkaMessagesBehindApi kafkaMessagesBehindCaller;

	public ComparisonBase(KafkaMessagesBehindApi kafkaMessagesBehindCaller, Logger logger) {
		this.kafkaMessagesBehindCaller = kafkaMessagesBehindCaller;

		// TODO: Make sure children class can use this one!!!
		this.logger = logger;
	}

	protected MessagesBehindInfo getKafkaMessagesBehind() {
		MessagesBehindInfo messagesBehind = new MessagesBehindInfo();
		List.of(KafkaConstants.MNT_CLUSTER).forEach(clusterId -> {
			try {
				Call<List<KafkaMessagesBehindApi.KafkaConsumerGroupMetaData>> caller = kafkaMessagesBehindCaller.getConsumerGroups(clusterId);
				Response<List<KafkaMessagesBehindApi.KafkaConsumerGroupMetaData>> response = caller.execute();
				if (response.isSuccessful()) {
					List<KafkaMessagesBehindApi.KafkaConsumerGroupMetaData> consumerGroupMetaData = response.body();
					assert consumerGroupMetaData != null;
					consumerGroupMetaData = consumerGroupMetaData.stream().filter(x -> x != null && x.getGroupId() != null && x.getGroupId().startsWith(Constants.PREFIX_GROUP_DBSYNC)).collect(Collectors.toList());
					if (KafkaConstants.MNT_CLUSTER.equals(clusterId)) {
						consumerGroupMetaData.forEach(msgBehind -> messagesBehind.addDtBehind(msgBehind.getMessagesBehind()));
					}
				}
			} catch (Exception ex) {
				if (logger.isDebugEnabled()) {
					logger.warn(ex.getMessage(), ex);
				} else {
					logger.warn("An error occurred while getting message behind from cluster {}. Error: {}",
							clusterId, ex.getMessage());
				}

				if (KafkaConstants.MNT_CLUSTER.equals(clusterId)) {
					messagesBehind.setMsgDtBehind(0L);
				}
			}
		});
		return messagesBehind;
	}
}
