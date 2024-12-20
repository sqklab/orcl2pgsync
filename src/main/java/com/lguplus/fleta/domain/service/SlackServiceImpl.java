package com.lguplus.fleta.domain.service;

import com.lguplus.fleta.domain.dto.SlackErrorMessage;
import com.lguplus.fleta.domain.dto.SlackMessage;
import com.lguplus.fleta.domain.model.comparison.ComparisonResultEntity;
import com.lguplus.fleta.domain.model.comparison.DbComparisonResultEntity;
import com.lguplus.fleta.ports.service.SlackService;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;

@Slf4j
@Service
public class SlackServiceImpl implements SlackService {

	@Value("${comparison.retry.message.limit}")
	private Integer NOT_SAME_LIMIT;

	private final Slack slack;

	private final String activeProfile;

	@Value("${app.slack.comparison-notify-channel}")
	public String channel;

	@Value("${app.slack.token}")
	public String token;

	@Value("${app.slack.mention:<!here>}")
	public String mention;

	private MethodsClient methodsClient;

	public SlackServiceImpl(Slack slack, @Value("${spring.profiles.active}") String activeProfile) {
		this.slack = slack;
		this.activeProfile = activeProfile;
	}

	@PostConstruct
	private void postConstructor() throws Exception {
		if (StringUtils.isBlank(token)) {
			throw new Exception("app.slack.token is required");
		}
		if (StringUtils.isBlank(channel)) {
			throw new Exception("app.slack.comparison-notify-channel is required");
		}

		methodsClient = slack.methods(token);
	}

	@Override
	public ChatPostMessageResponse send(SlackMessage message) throws Exception {
		if (null == message) {
			log.warn("Slack message is null");
			return null;
		}

		List<DbComparisonResultEntity> notSucceedList = message.getNotSucceedList();
		String notSameMessageList = notSucceedList.stream()
				.limit(Objects.requireNonNullElse(NOT_SAME_LIMIT, 0))
				.filter(e -> !e.getComparisonState().equals(ComparisonResultEntity.ComparisonState.SAME))
				.sorted(Comparator.comparing(
						(e)-> e.getDbComparisonInfo().getSyncInfo().getTopicName(),
						Comparator.reverseOrder()
				))
				.map(e -> {
					final String msgStr;
					ComparisonResultEntity.ComparisonState comparisonState = e.getComparisonState();
					String topicName = e.getDbComparisonInfo().getSyncInfo().getTopicName();
					if (comparisonState.equals(ComparisonResultEntity.ComparisonState.DIFFERENT)) {
						Long sourceCount = e.getSourceCount();
						Long targetCount = e.getTargetCount();
						msgStr = String.format("- (diff) %s: (sourceCount=%d, targetCount=%d)", topicName, sourceCount, targetCount);
					} else { // ComparisonResultEntity.ComparisonState.FAILED
						String errorMsgSource = e.getErrorMsgSource();
						String errorMsgTarget = e.getErrorMsgTarget();
						StringBuilder sb = new StringBuilder();
						sb.append(String.format("- (fail) %s: ", topicName));
						if (StringUtils.isNoneBlank(errorMsgSource)){
							sb.append(String.format("\n  - sourceMessage: `%s`)", errorMsgSource));
						}
						if (StringUtils.isNoneBlank(errorMsgTarget)){
							sb.append(String.format("\n  - targetMessage: `%s`)", errorMsgTarget));
						}
						msgStr = sb.toString();
					}
					return msgStr;
				})
				.collect(Collectors.joining("\n"));

		return methodsClient.chatPostMessage(req -> req
				.channel("#" + channel)
				.text("DB comparison notification")
				.blocks(asBlocks(
						section(section -> section.text(markdownText(mention))),
						section(section -> section.text(plainText(String.format("[IPTV MSA / DataSync System] - %s", activeProfile.toUpperCase())))),
						section(section -> section.text(markdownText(toTitle("DB Comparison 결과", message.getCompareDate(), message.getCompareTime())))),
						section(section -> section.text(markdownText(
										wrap("Total:\t              ", message.getTotalDetail(), message.getTotal()) + "\n" +
												wrap("Equal:\t             ", message.getEqualDetail(), message.getEqual()) + "\n" +
												wrap("Different:\t       ", message.getDifferenceDetail(), message.getDifference()) + "\n" +
												wrap("Failed:\t            ", message.getFailedDetail(), message.getFailed()) + "\n"
								))
						),
						section(section -> section.text(markdownText(Objects.requireNonNullElse(NOT_SAME_LIMIT, 0) > 0
								? (notSameMessageList + (notSucceedList.size() > NOT_SAME_LIMIT ? "\nmore..." : ""))
								: ""
						)))
				)));
	}

	@Override
	public ChatPostMessageResponse send(SlackErrorMessage message) throws Exception {
		if (null == message) {
			log.warn("Slack message is null");
			return null;
		}
		return methodsClient.chatPostMessage(req -> req
				.channel("#" + channel)
				.text("Synchronization error")
				.blocks(asBlocks(
								section(section -> section.text(markdownText(mention))),
								section(section -> section.text(plainText(String.format("[IPTV MSA / DataSync System] - %s", activeProfile.toUpperCase())))),
								section(section -> section.text(markdownText(toTitle("Synchronization Error", message.getCompareDate(), message.getCompareTime())))),
								section(section -> section.text(markdownText("*Kafka Topic*:\t" + message.getTopic()))),
								section(section -> section.text(markdownText("*Error Time*:\t" + message.getCompareDate() + " " + message.getCompareTime()))),
								section(section -> section.text(markdownText("*Error Message*:\t```" + message.getErrorMessage() + "```"))),
								section(section -> section.text(markdownText("*SQL Query*:\t```" + message.getSql() + "```"))),
								divider()
						)
				));
	}

	@Override
	public ChatPostMessageResponse send(ColumnComparisonMessage message) throws Exception {
		if (null == message) {
			log.warn("Slack message is null");
			return null;
		}
		return methodsClient.chatPostMessage(req -> req
				.channel("#" + channel)
				.text("Column comparison error")
				.blocks(asBlocks(
								section(section -> section.text(markdownText(mention))),
								section(section -> section.text(plainText(String.format("[IPTV MSA / DataSync System] - %s", activeProfile.toUpperCase())))),
								section(section -> section.text(markdownText(toTitle("*Column comparison error*", message.getCompareDate(), message.getCompareTime())))),

								section(section -> section.text(markdownText(
												"*Kafka Topic*:\t " + message.getTopic() + "\n" +
														"*Source table*:\t" + message.getSourceTbl() + "\n" +
														"*Target table*:\t " + message.getTargetTbl() + "\n" +
														"*Error*:\t\t        `" + message.getErrorMessage() + "`" +
														(StringUtils.isBlank(message.getDiffString()) ? "" : "\n*Different columns*:\t" + message.getDiffString())
										)
								)),
								divider()
						)
				));
	}

	public ChatPostMessageResponse send(InfiniteLoopMessage message) throws Exception {
		if (null == message) {
			log.warn("Slack message is null");
			return null;
		}
		return methodsClient.chatPostMessage(req -> req
				.channel("#" + channel)
				.text("Column comparison error")
				.blocks(asBlocks(
						section(section -> section.text(markdownText(mention))),
						section(section -> section.text(plainText(String.format("[IPTV MSA / DataSync System]")))),
						section(section -> section.text(markdownText(
										"*Kafka Topic*:\t " + message.getTopic() + "\n" +
												"*Error*:\t\t        `" + message.getErrorMessage() + "`\n" +
												"*DateTime*:\t        `" + message.getCompareTime() + "`"
								)
						)))
				));
	}

	private String wrap(String title, String text, Long total) {
		return title + total + " ( " + text + ")";
	}

	private String toTitle(String title, LocalDate date, LocalTime time) {
		if (Objects.isNull(date) || Objects.isNull(time)) {
			if (StringUtils.isBlank(title)) {
				return "";
			}
			return title;
		}
		return String.format(title + " / %s년 %s월 %s일 %s시 %s분 %s초",
				date.getYear(), date.getMonthValue(), date.getDayOfMonth(),
				time.getHour(), time.getMinute(), time.getSecond());
	}
}
