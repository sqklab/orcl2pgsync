package com.lguplus.fleta.domain.service.operation;


import com.google.common.collect.Sets;
import com.lguplus.fleta.domain.dto.operation.OperationDto;
import com.lguplus.fleta.domain.model.operation.BaseOperationResultEntity;
import com.lguplus.fleta.domain.model.operation.OpPtVoBuyEntity;
import com.lguplus.fleta.domain.model.operation.OpPtVoWatchHistoryEntity;
import com.lguplus.fleta.domain.model.operation.OpXcionEntity;
import com.lguplus.fleta.domain.util.DateUtils;
import com.lguplus.fleta.ports.repository.operation.OpPtVoBuyRepository;
import com.lguplus.fleta.ports.repository.operation.OpPtVoWatchHistoryRepository;
import com.lguplus.fleta.ports.repository.operation.OpXcionRepository;
import com.lguplus.fleta.ports.service.OperationService;
import com.lguplus.fleta.ports.service.operation.OperationCompareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static com.lguplus.fleta.domain.util.OperationUtil.isDiff;

@Service
public class OperationCompareServiceImpl implements OperationCompareService {
	private static final Logger logger = LoggerFactory.getLogger(OperationCompareServiceImpl.class);

	private final OpXcionRepository xcionRepository;
	private final OpPtVoBuyRepository ptVoBuyRepository;
	private final OpPtVoWatchHistoryRepository ptVoWatchHistoryRepository;

	public OperationCompareServiceImpl(OpXcionRepository xcionRepository,
									   OpPtVoBuyRepository ptVoBuyRepository,
									   OpPtVoWatchHistoryRepository ptVoWatchHistoryRepository, OperationService operationService) {
		this.xcionRepository = xcionRepository;
		this.ptVoBuyRepository = ptVoBuyRepository;
		this.ptVoWatchHistoryRepository = ptVoWatchHistoryRepository;
	}


	@Override
	public void diff(Map<String, Map<String, Object>> source, Map<String, Map<String, Object>> target, OperationDto param) {
		try {
			logger.info("[Operation][session {}] Getting different between source ({} rows) and target ({} rows)", param.getSessionId(), source.size(), target.size());
			List<OperationService.CompareDiffItem> diff = checkDiff(source, target);
			// TODO save to DB
			if (diff.isEmpty()) {
				return;
			}
			logger.info("[Operation][session {}] Saving {} different to DB", param.getSessionId(), diff.size());
			List<BaseOperationResultEntity> diffItems = new ArrayList<>();
			LocalDateTime now = DateUtils.getDateTime();
			switch (param.getOpTable()) {
				case xcion_sbc_tbl_united:
					for (OperationService.CompareDiffItem item : diff) {
						OpXcionEntity xcionEntity = new OpXcionEntity();
						xcionEntity.setPrimaryKeys(item.getPKey());
						xcionEntity.setSession(param.getSessionId());
						xcionEntity.setWhereCondition(param.getWhereStm());
						xcionEntity.setCorrectionType(item.getOperation().name());
						xcionEntity.setOperationDate(now);
						diffItems.add(xcionEntity);
					}
					xcionRepository.saveAll(diffItems);
					break;
				case pt_vo_buy:
					for (OperationService.CompareDiffItem item : diff) {
						OpPtVoBuyEntity opPtVoBuyEntity = new OpPtVoBuyEntity();
						opPtVoBuyEntity.setPrimaryKeys(item.getPKey());
						opPtVoBuyEntity.setSession(param.getSessionId());
						opPtVoBuyEntity.setWhereCondition(param.getWhereStm());
						opPtVoBuyEntity.setCorrectionType(item.getOperation().name());
						opPtVoBuyEntity.setOperationDate(now);
						diffItems.add(opPtVoBuyEntity);
					}
					ptVoBuyRepository.saveAll(diffItems);
					break;
				case pt_vo_watch_history:
					for (OperationService.CompareDiffItem item : diff) {
						OpPtVoWatchHistoryEntity opPtVoWatchHistory = new OpPtVoWatchHistoryEntity();
						opPtVoWatchHistory.setPrimaryKeys(item.getPKey());
						opPtVoWatchHistory.setSession(param.getSessionId());
						opPtVoWatchHistory.setWhereCondition(param.getWhereStm());
						opPtVoWatchHistory.setCorrectionType(item.getOperation().name());
						opPtVoWatchHistory.setOperationDate(now);
						diffItems.add(opPtVoWatchHistory);
					}
					ptVoWatchHistoryRepository.saveAll(diffItems);
					break;
				default:
					break;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * @param source source
	 * @param target target
	 * @return list of diff items
	 */
	public List<OperationService.CompareDiffItem> checkDiff(Map<String, Map<String, Object>> source, Map<String, Map<String, Object>> target) {
		List<OperationService.CompareDiffItem> diffItems = new LinkedList<>();
		// diff
		Set<String> sourceKeys = source.keySet();
		Set<String> targetKeys = target.keySet();

		Set<String> targetMissing = Sets.difference(sourceKeys, targetKeys);
		Set<String> targetAddition = Sets.difference(targetKeys, sourceKeys);
		diffItems.addAll(buildDiffItems(targetMissing, source, target, OperationService.CompareDiffItem.CorrectOperation.INSERT));
		diffItems.addAll(buildDiffItems(targetAddition, source, target, OperationService.CompareDiffItem.CorrectOperation.DELETE));

		// same keys only
		sourceKeys.retainAll(targetKeys);
		diffItems.addAll(buildUpdateDiffItems(source, target, sourceKeys));

		return diffItems;
	}


	/**
	 * build diff item
	 *
	 * @param fields    fields
	 * @param source    source
	 * @param target    target
	 * @param operation operation
	 * @return list of diff items
	 */
	private List<OperationService.CompareDiffItem> buildDiffItems(Set<String> fields,
																  Map<String, Map<String, Object>> source,
																  Map<String, Map<String, Object>> target,
																  OperationService.CompareDiffItem.CorrectOperation operation) {
		if (Objects.isNull(fields) || Objects.isNull(target) || Objects.isNull(source)) {
			new ArrayList<>();
		}
		List<OperationService.CompareDiffItem> list = new LinkedList<>();
		for (String field : fields) {
			list.add(
					new OperationService.CompareDiffItem(
							source.get(field),
							target.get(field),
							operation,
							UUID.randomUUID(),
							field)
			);
		}
		return list;
	}

	/**
	 * @param source source
	 * @param target target
	 * @param pKeys  pKeys
	 * @return list of diff items
	 */
	private List<OperationService.CompareDiffItem> buildUpdateDiffItems(Map<String, Map<String, Object>> source,
																		Map<String, Map<String, Object>> target,
																		Set<String> pKeys) {
		List<OperationService.CompareDiffItem> list = new LinkedList<>();
		for (String field : pKeys) {
			if (isDiff(source.get(field), target.get(field))) {
				list.add(
						new OperationService.CompareDiffItem(
								source.get(field),
								target.get(field),
								OperationService.CompareDiffItem.CorrectOperation.UPDATE,
								UUID.randomUUID(),
								field)
				);
			}
		}

		return list;
	}
}
