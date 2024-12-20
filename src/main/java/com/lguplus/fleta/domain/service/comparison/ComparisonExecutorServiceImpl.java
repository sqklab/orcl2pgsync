package com.lguplus.fleta.domain.service.comparison;

import com.lguplus.fleta.adapters.messagebroker.KafkaMessagesBehindApi;
import com.lguplus.fleta.domain.dto.MessagesBehindInfo;
import com.lguplus.fleta.domain.dto.SlackMessage;
import com.lguplus.fleta.domain.dto.comparison.ComparisonSummary;
import com.lguplus.fleta.domain.model.comparison.ComparisonResultEntity;
import com.lguplus.fleta.domain.model.comparison.DbComparisonInfoEntity;
import com.lguplus.fleta.domain.model.comparison.DbComparisonInfoEntity.CompareInfoState;
import com.lguplus.fleta.domain.model.comparison.DbComparisonResultEntity;
import com.lguplus.fleta.domain.model.comparison.DbComparisonResultSummaryEntity;
import com.lguplus.fleta.domain.service.constant.Constants;
import com.lguplus.fleta.domain.util.ShutdownHookUtils;
import com.lguplus.fleta.ports.repository.DbComparisonInfoRepository;
import com.lguplus.fleta.ports.repository.DbComparisonResultRepository;
import com.lguplus.fleta.ports.repository.DbComparisonResultSummaryRepository;
import com.lguplus.fleta.ports.service.LoggerManager;
import com.lguplus.fleta.ports.service.SlackService;
import com.lguplus.fleta.ports.service.comparison.ComparisonExecutorService;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.*;

import static com.lguplus.fleta.domain.service.constant.Constants.COMPARISON_LOG;
import static com.lguplus.fleta.domain.service.constant.Constants.IS_COMPARISON_RUNNABLE;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON) // TODO: important
public class ComparisonExecutorServiceImpl extends ComparisonBase implements ComparisonExecutorService {

	private final DbComparisonInfoRepository comparisonInfoRepository;

	private final DbComparisonResultRepository resultRepository;

	private final DbComparisonResultSummaryRepository summaryRepository;

	private final SlackService slackService;

	private final ComparerHelper compareHelper;

	public ComparisonExecutorServiceImpl(DbComparisonResultRepository resultRepository,
										 DbComparisonInfoRepository comparisonInfoRepository,
										 DbComparisonResultSummaryRepository summaryRepository,
										 SlackService slackService,
										 LoggerManager loggerManager,
										 KafkaMessagesBehindApi kafkaMessagesBehindCaller,
										 ComparerHelper compareHelper) {
		super(kafkaMessagesBehindCaller, loggerManager.getLogger(COMPARISON_LOG));

		this.comparisonInfoRepository = comparisonInfoRepository;
		this.resultRepository = resultRepository;
		this.summaryRepository = summaryRepository;
		this.slackService = slackService;
		this.compareHelper = compareHelper;
	}

	@Override
	public DbComparisonResultEntity compareNew(DbComparisonInfoEntity comparisonInfo, LocalDate date, LocalTime time) {
		final DbComparisonResultEntity dbComparisonResultEntity = new DbComparisonResultEntity(
				comparisonInfo, date, time, LocalDateTime.of(date, time)
		);

		try {
			long totalComparison = comparisonInfoRepository.countListToCompare();
			if (logger.isDebugEnabled()) {
				logger.debug("Doing compare task. The totalComparison: {}, compare date: {}, compare time: {}", totalComparison, date, time);
			}

			Integer alreadyCompared = resultRepository.countBySyncCompareIdAndCompareDateAndCompareTime(
					comparisonInfo.getId().intValue(),
					date,
					time.truncatedTo(ChronoUnit.SECONDS)
			);
			if (alreadyCompared.equals(0)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Checking existed comparison result compare date: {}, compare time: {}, comparisonInfo: {}", date, time.truncatedTo(ChronoUnit.SECONDS), comparisonInfo.getId());
				}
				ComparerHelper.ComparisonResult result = compareHelper.comparison(comparisonInfo);
				dbComparisonResultEntity.updateCompare(result);
				dbComparisonResultEntity.setLastModified(LocalDateTime.now(Constants.ZONE_ID));
				resultRepository.save(dbComparisonResultEntity);
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("The comparison already executed date: {}, time: {}, compare info id: {}", date, time, comparisonInfo.getId());
				}
			}
		} catch (Exception ex) {
			logger.error("ERROR when compute comparison with time: {}, date: {}, msg: {}", time, date, ex.getMessage(), ex);
		} finally {
			comparisonInfo.setState(CompareInfoState.NOT_RUNNING);
			comparisonInfoRepository.save(comparisonInfo);
		}

		updateSummary(date, time);

		return dbComparisonResultEntity;
	}

	@Override
	public void sendSlackMessage(LocalDate compareDate, LocalTime compareTime){
		List<ComparisonSummary> comparisonResultDivision = resultRepository.getComparisonSummaryByDivision(compareDate, compareTime);
		List<DbComparisonResultEntity> notSucceedList = resultRepository.findAllByCompareDateAndCompareTimeAndComparisonStateNot(
				compareDate, compareTime, ComparisonResultEntity.ComparisonState.SAME
		);
		SlackMessage slackMessage = ComparisonUtil.toSlackMessage(comparisonResultDivision, notSucceedList);
		try {
			ChatPostMessageResponse response = slackService.send(slackMessage);
			if (response != null && response.getError() != null) {
				logger.error("ERROR during send notify to Slack, error : {}", response.getError());
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void compareWithComparisonResultIds(List<Long> ids) {
		for(Long id : ids){
			compareWithComparisonResultId(id);
		}
	}

	@Override
	public DbComparisonResultEntity compareWithComparisonResultId(Long id) {
		final DbComparisonResultEntity dbComparisonResult;
		try {
			dbComparisonResult = resultRepository.findById(id).orElseThrow(Exception::new);
		} catch (Exception e) {
			logger.error("There had no compared before (compareResultId={})", id);
			return null;
		}
		return compare(dbComparisonResult);
	}

	public DbComparisonResultEntity compare(DbComparisonResultEntity dbComparisonResult) {
		DbComparisonInfoEntity comparisonInfo = dbComparisonResult.getDbComparisonInfo();
		if (!(IS_COMPARISON_RUNNABLE).equals(comparisonInfo.getIsComparable())) {
			logger.warn("compare flag is not 'Y' (compareInfoId={})", comparisonInfo.getId());
			return dbComparisonResult;
		}

		LocalDate date = dbComparisonResult.getCompareDate();
		LocalTime time = dbComparisonResult.getCompareTime();

		ComparerHelper.ComparisonResult result = compareHelper.comparison(comparisonInfo);

		dbComparisonResult.updateCompare(result);
		resultRepository.save(dbComparisonResult);
		// update summary
		updateSummary(date, time);
		return dbComparisonResult;
	}

	@Override
	public void createSummary(LocalDate date, LocalTime time) {
		try {
			DbComparisonResultSummaryEntity summaryEntity = new DbComparisonResultSummaryEntity();
			summaryEntity.setCompareTime(time);
			summaryEntity.setCompareDate(date);
			summaryEntity.setTargetCount(0L);
			summaryEntity.setSourceCount(0L);

			summaryEntity.setTotal(0);
			summaryEntity.setEqual(0L);
			summaryEntity.setDifferent(0L);
			summaryEntity.setFail(0L);

			MessagesBehindInfo messageBehind = getKafkaMessagesBehind();
			if (messageBehind != null) {
				summaryEntity.setMsgDtBehind(messageBehind.getMsgDtBehind());
			}
			summaryRepository.saveAndFlush(summaryEntity);
		} catch (Exception ex) {
			logger.error("*** Save result entity error", ex);
		}
	}

	private void updateSummary(LocalDate date, LocalTime time) {
		ComparisonSummary summary = resultRepository.getComparisonSummary(date, time);
		if (summary == null) {
			return;
		}
		DbComparisonResultSummaryEntity summaryEntity = this.summaryRepository.findByCompareDateAndCompareTime(date, time);
		if (summaryEntity == null) {
			return;
		}
		summaryEntity.setEqual(summary.getEqual());
		summaryEntity.setDifferent(summary.getDifferent());
		summaryEntity.setFail(summary.getFail());
		summaryEntity.setSourceCount(summary.getSourceCount());
		summaryEntity.setTargetCount(summary.getTargetCount());
		this.summaryRepository.save(summaryEntity);
	}
}
