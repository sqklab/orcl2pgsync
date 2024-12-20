package com.lguplus.fleta.adapters.rest;

import com.lguplus.fleta.domain.dto.LastMessageInfoDto;
import com.lguplus.fleta.domain.dto.SyncRequestParam;
import com.lguplus.fleta.domain.dto.Synchronizer.SyncState;
import com.lguplus.fleta.domain.model.LastMessageInfoEntity;
import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.domain.model.comparison.DbComparisonInfoEntity;
import com.lguplus.fleta.domain.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.kafka.common.internals.Topic;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.lguplus.fleta.domain.service.constant.Constants.IS_COMPARISON_RUNNABLE;

@Slf4j
public class SyncRequestHelper {


	private SyncRequestHelper() {
	}

	public static SyncRequestEntity toSyncEntity(SyncRequestParam param) {
		validateSyncRequestParam(param);
		Topic.validate(param.getTopicName());
		SyncRequestEntity entity = new SyncRequestEntity();
		try {
			BeanUtils.copyProperties(entity, param);
			if (param.getId() == null) {    // For create new synchronizer
				entity.setCreatedAt(DateUtils.getDateTime());
				entity.setUpdatedAt(DateUtils.getDateTime());
				entity.setState(SyncState.PENDING);
			} else {                        // For update existing synchronizer
				entity.setUpdatedAt(DateUtils.getDateTime());
				entity.setState(param.getState());
			}
			entity.setSynchronizerName(StringUtils.hasText(param.getSynchronizerName()) ? param.getSynchronizerName() : param.getTopicName());
			entity.setConsumerGroup(param.getConsumerGroup());
			entity.setBatch(param.getIsBatch());
			entity.setUpsert(param.getIsUpsert());
			entity.setChangeInsertOnFailureUpdate(param.getIsChangeInsertOnFailureUpdate());
			entity.setAllColumnConditionsOnUpdate(param.getIsAllColumnConditionsOnUpdate());
			entity.setEnableTruncate(param.getEnableTruncate());
			entity.setMaxPollRecords(param.getMaxPollRecords());

			DbComparisonInfoEntity comparisonInfo = new DbComparisonInfoEntity();
			comparisonInfo.setId(param.getComparisonInfoId());
			comparisonInfo.setSyncInfo(entity);
			comparisonInfo.setSourceQuery(param.getSourceQuery());
			comparisonInfo.setSourceCompareDatabase(param.getSourceCompareDatabase());
			comparisonInfo.setTargetQuery(param.getTargetQuery());
			comparisonInfo.setTargetCompareDatabase(param.getTargetCompareDatabase());
			comparisonInfo.setIsComparable(param.getIsComparable() ? IS_COMPARISON_RUNNABLE : "N");
			comparisonInfo.setEnableColumnComparison(SyncRequestHelper.getEnableColumnComparison(param.getEnableColumnComparison()));
			comparisonInfo.setState(DbComparisonInfoEntity.CompareInfoState.NOT_RUNNING);


			entity.addComparisonEntity(comparisonInfo);

		} catch (IllegalAccessException | InvocationTargetException e) {
			log.error(e.getMessage(), e);
		}
		return entity;
	}

	private static void validateSyncRequestParam(SyncRequestParam param) {
		boolean validTarget = StringUtils.hasText(param.getTargetDatabase())
				&& StringUtils.hasText(param.getTargetSchema())
				&& StringUtils.hasText(param.getTargetTable());
		boolean validSource = StringUtils.hasText(param.getSourceDatabase())
				&& StringUtils.hasText(param.getSourceSchema())
				&& StringUtils.hasText(param.getSourceTable());
		if (!validSource || !validTarget) {
			throw new IllegalArgumentException("Invalid parameters");
		}
	}

	public static boolean isComparable(String compare) {
		return IS_COMPARISON_RUNNABLE.equals(compare);
	}

	public static boolean getEnableColumnComparison(Boolean val) {
		return null != val && val;
	}

	public static Map<String, LastMessageInfoEntity> getStringLastMessageInfoEntityMap(Map<String, LastMessageInfoDto> receivedMessageMap, List<LastMessageInfoEntity> lastMessageList) {
		for (LastMessageInfoEntity entity : lastMessageList) {
			if (receivedMessageMap.containsKey(entity.getTopic())) {
				LastMessageInfoDto dto = receivedMessageMap.get(entity.getTopic());
				entity.setScn(dto.getScn());
				entity.setCommitScn(dto.getCommitScn());
				entity.setReceivedDate(dto.getReceivedDateTime().toLocalDate());
				entity.setReceivedTime(dto.getReceivedDateTime().toLocalTime());
				entity.setMsgTimestamp(dto.getMsgTimestamp());
				receivedMessageMap.remove(entity.getTopic());
			}
		}

		// In case tbl_last_message_info doesn't have last messages info of new topics.
		// This helps display the newest message info
		for (Map.Entry<String, LastMessageInfoDto> set : receivedMessageMap.entrySet()) {
			LastMessageInfoDto dto = set.getValue();
			lastMessageList.add(new LastMessageInfoEntity(
					dto.getTopic(),
					dto.getReceivedDateTime().toLocalDate(),
					dto.getReceivedDateTime().toLocalTime(),
					dto.getCommitScn(),
					dto.getScn(),
					dto.getMsgTimestamp()));
		}

		return lastMessageList.stream().collect(Collectors.toMap(LastMessageInfoEntity::getTopic, entity -> entity));
	}
}
