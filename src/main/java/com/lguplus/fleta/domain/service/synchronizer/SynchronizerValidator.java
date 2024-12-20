package com.lguplus.fleta.domain.service.synchronizer;

import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.domain.service.constant.Constants;
import com.lguplus.fleta.ports.service.SlackService;
import com.lguplus.fleta.ports.service.SyncRequestService;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class SynchronizerValidator {
	private final SyncRequestService syncRequestService;

	private final SlackService slackService;

	public boolean syncValidation(String kafkaTopic, String syncName) {

		SyncRequestEntity syncRequest = syncRequestService.findByTopicNameAndSynchronizerName(kafkaTopic, syncName);

		return recursionValidator(
				syncRequest.getSourceDatabase(), syncRequest.getSourceSchema(), syncRequest.getSourceTable(),
				syncRequest.getTargetDatabase(), syncRequest.getTargetSchema(), syncRequest.getTargetTable()
		);
	}

	private boolean recursionValidator(
			String sourceDB, String sourceSchema, String sourceTable, String targetDB, String targetSchema, String targetTable
	) {
		List<SyncRequestEntity> synchronizerList = syncRequestService.findAllRunningSynchronizerWithSourceInformation(
				targetDB, targetSchema, targetTable
		);

		if (synchronizerList.size() == 0) {
			return false;
		}

		List<Boolean> isLoopList = new ArrayList<>();

		for (SyncRequestEntity synchronizer : synchronizerList) {
			boolean isSameDB = Objects.equals(sourceDB, synchronizer.getTargetDatabase());
			boolean isSameSchema = Objects.equals(sourceSchema, synchronizer.getTargetSchema());
			boolean isSameTable = Objects.equals(sourceTable, synchronizer.getTargetTable());
			if (isSameDB && isSameSchema && isSameTable) {
				isLoopList.add(true);
				// call slack message here
				slackMessageCaller(synchronizer);
			} else {
				isLoopList.add(recursionValidator(
						sourceDB, sourceSchema, sourceTable,
						synchronizer.getTargetDatabase(),
						synchronizer.getTargetSchema(),
						synchronizer.getTargetTable()
				));
			}
		}

		return isLoopList.contains(true);
	}

	private void slackMessageCaller(SyncRequestEntity synchronizer) {
		LocalDateTime now = LocalDateTime.now(Constants.ZONE_ID);
		String alertMessage = "have potential infiniteLoop. Please Check!";
		SlackService.InfiniteLoopMessage message = SlackService.InfiniteLoopMessage.builder()
				.compareTime(now.toLocalTime())
				.topic(synchronizer.getTopicName())
				.errorMessage(alertMessage)
				.build();

		try {
			ChatPostMessageResponse response = slackService.send(message);
			if (response != null && response.getError() != null) {
				log.error("ERROR during send notify to Slack, error : {}", response.getError());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
