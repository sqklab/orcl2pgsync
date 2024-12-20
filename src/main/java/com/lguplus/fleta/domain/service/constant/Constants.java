package com.lguplus.fleta.domain.service.constant;

import java.time.ZoneId;

/**
 * DbSync constants.
 */
public interface Constants {

	/**
	 * Constant <code>KAFKA_ERROR_TOPIC_NAME="ERROR_TRACKING"</code>
	 */
	String KAFKA_ERROR_TOPIC_NAME = "DATASYNC.ERROR_TRACKING";

	/**
	 * Constant <code>KAFKA_ERROR_TOPIC_NAME="ERROR_TRACKING_GROUP"</code>
	 */
	String KAFKA_ERROR_GROUP_ID = "dbsync.ErrorTrackingConsumer";

	/**
	 * The suffix will append to the end of topic to send success message
	 */

	String IS_COMPARISON_RUNNABLE = "Y";

	ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

	String COMPARISON_LOG = "comparison";

	String ANALYSIS_LOG = "analysis";

	String PATTERN_DATE_FORMAT = "yyyy-MM-dd '/' HH:mm:ss";

	String REV_TOPIC_PREFIX = "REV_";

	String NOT_AVAILABLE = "N/A";

	String PREFIX_GROUP_DBSYNC = "dbsync.";

	Integer DEFAULT_MAX_POLL_RECORDS = 250;
}
