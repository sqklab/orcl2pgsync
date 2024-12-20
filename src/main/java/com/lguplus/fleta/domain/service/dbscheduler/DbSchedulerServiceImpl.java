package com.lguplus.fleta.domain.service.dbscheduler;

import ch.qos.logback.classic.Logger;
import com.lguplus.fleta.domain.dto.ui.SimpleDbSchedulerResultDto;
import com.lguplus.fleta.domain.model.DbScheduler;
import com.lguplus.fleta.domain.model.DbSchedulerResult;
import com.lguplus.fleta.domain.service.secret.SecretValueClient;
import com.lguplus.fleta.domain.service.constant.Constants;
import com.lguplus.fleta.domain.util.DateUtils;
import com.lguplus.fleta.ports.repository.DbScheduleProcedureRepository;
import com.lguplus.fleta.ports.repository.DbScheduleProcedureResultRepository;
import com.lguplus.fleta.ports.service.DataSourceService;
import com.lguplus.fleta.ports.service.LoggerManager;
import com.lguplus.fleta.ports.service.dbschedule.DbSchedulerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.security.SecureRandom;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DbSchedulerServiceImpl implements DbSchedulerService {

	public static final int TIME_ALIVE = 5;
	public static final String COMMA = ",";
	public static final String COLON = ":";
	public static final String LAST_DAY = "LASTDAY";
	public static final String DOT_COMMA = ";";

	private static final String DB_SCHEDULE_PROCEDURE_LOG = "dbschedule-procs";
	private static final String[] QUARTERS = {"1,4,7,10", "2,5,8,11", "3,6,9,12"};

	private final DbScheduleProcedureRepository scheduleRepository;

	private final DbScheduleProcedureResultRepository resultRepository;

	private final DataSourceService dataSourceService;

	private final TaskScheduler taskScheduler;

	private final Logger logger;

	// <id of schedule, future>
	private Map<Long, ScheduledFuture> jobs = new ConcurrentHashMap<>();

	@Value("${spring.profiles.active:dev}")
	private String secretName;

	public DbSchedulerServiceImpl(DbScheduleProcedureRepository scheduleRepository,
								  DbScheduleProcedureResultRepository resultRepository,
								  @Qualifier("defaultDatasourceContainer") DataSourceService dataSourceService,
								  @Qualifier("defaultThreadPool") ThreadPoolTaskScheduler taskScheduler,
								  LoggerManager loggerManager) {
		this.scheduleRepository = scheduleRepository;
		this.resultRepository = resultRepository;
		this.dataSourceService = dataSourceService;
		this.taskScheduler = taskScheduler;
		this.logger = loggerManager.getLogger(DB_SCHEDULE_PROCEDURE_LOG);
	}

	@PreDestroy
	public void releaseData() {
		logger.info("Shutting down DBSchedulerProcedureJobService...");
		List<Long> ids = new ArrayList<>(jobs.keySet());
		if (ids.isEmpty()) {
			return;
		}
		int releaseData = this.scheduleRepository.releaseData(ids, false);
		// remove all items
		this.jobs.values().forEach(future -> {
			if (!future.isCancelled() || !future.isDone()) {
				logger.info("Canceling Schedule task ...");
				future.cancel(true);
			}
		});
		this.jobs.clear();
		logger.info("*** Release data, effected {} rows", releaseData);
	}

	public void initData() {
		while (true) {
			List<DbScheduler> listToSchedule = this.scheduleRepository.findStoppedListToSchedule(false, 1000);
			if (listToSchedule.isEmpty()) {
				break;
			}
			logger.info("*** fetched {} rows to schedule ***", listToSchedule.size());
			listToSchedule.forEach(this::run);
		}
	}

	private void scheduleFixedDelayTask() {
		this.initData();
		// 1. update status still alive
		this.refreshJobData();
		// 2. load death job
		this.loadDeathJob();
	}

	@Override
	public void register() {
		this.taskScheduler.scheduleAtFixedRate(this::scheduleFixedDelayTask, 3 * 60000); // 3 minutes
	}

	/**
	 * 1. Refresh job and update lastRun
	 */
	private void refreshJobData() {
		if (this.jobs.keySet().isEmpty()) {
			return;
		}
		List<Long> ids = new ArrayList<>(this.jobs.keySet());
		Set<Long> existed = this.scheduleRepository.findExistedIDAndUpdateLastRun(ids, DateUtils.getDateTime());
		// remove deleted job-id out of jobs
		ids.stream().filter(idInJob -> !existed.contains(idInJob)).forEach(this::cancelJob);
	}

	/**
	 * 2. load other jobs which was hold by other instance, but the instance has crash!
	 */
	private void loadDeathJob() {
		LocalDateTime timeLive = DateUtils.getDateTime().minusMinutes(TIME_ALIVE);
		this.scheduleRepository.findAllByProcessStatusAndLastRunLessThanAndUpdateLastRunQuickly(true, timeLive, DateUtils.getDateTime()).forEach(item -> {
			if (!jobs.containsKey(item.getId())) {
				logger.info(">>>> loaded other death job id={}", item.getId());
				this.run(item);
			}
		});
	}

	private void cancelJob(Long removedId) {
		try {
			var scheduledFuture = this.jobs.get(removedId);
			if (scheduledFuture != null) {
				scheduledFuture.cancel(true);
			}
			this.jobs.remove(removedId);
			logger.info(" >>> updated Job, removed id={}", removedId);
		} catch (Exception ex) {
			logger.error("***** CANCEL JOB ERROR", ex);
		}
	}

	@Override
	public void run(DbScheduler dbScheduleProcedure) {
		if (dbScheduleProcedure == null || dbScheduleProcedure.getDb() == null || StringUtils.isEmpty(dbScheduleProcedure.getPlSQL())) {
			logger.warn(">>>>>>>>>>>>> INVALID DATA!!!<<<<<<<<< {}", dbScheduleProcedure);
			return;
		}
		if (dbScheduleProcedure.isDaily()) { // schedule by daily
			handleDaily(dbScheduleProcedure);
			return;
		}
		if (dbScheduleProcedure.isWeekly()) { // schedule by day of week
			handleWeekly(dbScheduleProcedure);
			return;
		}
		if (dbScheduleProcedure.isMonthly()) {
			handleMonthly(dbScheduleProcedure);
			return;
		}
		if (dbScheduleProcedure.isQuarterly()) {
			handleQuarterly(dbScheduleProcedure);
			return;
		}
		if (dbScheduleProcedure.isYearly()) {
			handleYearly(dbScheduleProcedure);
		}
	}

	@Override
	public void retry(DbSchedulerResult procedureResult, DbScheduler procedure) {
		String connectionName = procedure.getDb();
		String plSQL = procedure.getPlSQL();

		LocalDateTime startAt = DateUtils.getDateTime();

		if (connectionName == null) {
			this.saveResultForRetry(procedureResult, startAt, "Connection not found", false);
			return;
		}
		try {
			DataSource dataSource = dataSourceService.findDatasourceByServerName(connectionName);
			if (null == dataSource) {
				throw new NullPointerException("There is no dataSource available!!!");
			}
			executeSimpleJdbc(plSQL, procedure.getSchema(), procedure.getTable(), dataSource);
			this.saveResultForRetry(procedureResult, startAt, "", true);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			// Add log status
			this.saveResultForRetry(procedureResult, startAt, ex.getMessage(), false);
		}
	}

	@Override
	public void testScript(DbScheduler procedure) throws Exception {
		String connectionName = procedure.getDb();
		String plSQL = procedure.getPlSQL();
		if (connectionName == null) {
			throw new Exception("Connection not found!");
		}
		try {
			DataSource dataSource = dataSourceService.findDatasourceByServerName(connectionName);
			if (null == dataSource) {
				throw new Exception("Connection not found!");
			}
			executeSimpleJdbc(plSQL, procedure.getSchema(), procedure.getTable(), dataSource);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		}
	}

	@Override
	public SimpleDbSchedulerResultDto scheduleAll() {
		List<DbScheduler> allByStatus = this.scheduleRepository.findAllByStatus(true);
		LocalTime scheduleTime = DateUtils.getTime().withSecond(0);
		int numSuccess = 0;
		int numFailure = 0;
		for (DbScheduler dbScheduler : allByStatus) {
			LocalDateTime startAt = DateUtils.getDateTime();
			try {
				this.testScript(dbScheduler);
				numSuccess++;
				this.saveResult(dbScheduler, true, startAt, scheduleTime, "");
			} catch (Exception e) {
				logger.error("Run Schedule error {}", dbScheduler);
				numFailure++;
				this.saveResult(dbScheduler, false, startAt, scheduleTime, e.getMessage());
			}
		}
		return new SimpleDbSchedulerResultDto(numSuccess, numFailure);
	}

	@Override
	public long total() {
		return this.scheduleRepository.count();
	}

	private void saveResult(DbScheduler dbScheduleProcedure, boolean status, LocalDateTime startAt, LocalTime scheduleTime, String errorMsg) {
		DbSchedulerResult result = new DbSchedulerResult();
		result.setFkIdProcedure(dbScheduleProcedure.getId());
		result.setStatus(status);
		result.setScheduleTime(scheduleTime);
		result.setScheduleDate(DateUtils.getDate());
		result.setStartAt(startAt);
		result.setEndAt(DateUtils.getDateTime());
		result.setErrorMsg(errorMsg);
		this.resultRepository.save(result);
	}

	private void executeSimpleJdbc(String plSql, String schema, String table, DataSource dataSource) throws SQLException {
		String procedure;
		if (StringUtils.isEmpty(schema) && StringUtils.isEmpty(table)) {
			procedure = String.format("call %s", plSql);
		} else {
			procedure = String.format("call %s('%s.%s')", plSql, schema, table);
		}
		try (Connection connection = dataSource.getConnection();
			 CallableStatement statement = connection.prepareCall(procedure)) {
			statement.executeUpdate();
		}
	}

	private void saveResultForRetry(DbSchedulerResult result, LocalDateTime startAt, String errMsg, boolean status) {
		result.setStartAt(startAt);
		result.setEndAt(DateUtils.getDateTime());
		result.setErrorMsg(errMsg);
		result.setStatus(status);
		this.resultRepository.save(result);
	}

	private void handleWeekly(DbScheduler dbScheduleProcedure) {
		if (dbScheduleProcedure.getDayOfWeek() == null || dbScheduleProcedure.getTimesOfWeek() == null) {
			return;
		}
		String[] daysOfWeek = dbScheduleProcedure.getDayOfWeek().split(COMMA); // e.g: 2,3,5
		String toShortDays = Arrays.stream(daysOfWeek).map(day -> getShortDay(Integer.parseInt(day))).collect(Collectors.joining(COMMA));

		String[] times = dbScheduleProcedure.getTimesOfWeek().split(COMMA); // [HH:mm:ss,HH:mm:ss]
		for (String time : times) {
			String[] hourMinuteSecond = time.split(COLON);
			String cronExpression = time2WeeklyCronExpression(hourMinuteSecond, toShortDays);
			scheduleATask(dbScheduleProcedure.getId(), cronExpression, () -> executeQuery(dbScheduleProcedure));
		}
	}

	private void handleMonthly(DbScheduler dbScheduleProcedure) {
		if (StringUtils.isEmpty(dbScheduleProcedure.getMonthly())) {
			return;
		}
		String[] months = dbScheduleProcedure.getMonthly().split(DOT_COMMA);

		Arrays.stream(months).filter(month -> !StringUtils.isEmpty(month)).map(month -> month.split(COMMA)).forEach(dayTime -> {
			String day = dayTime[0];
			if (StringUtils.isEmpty(day)) {
				return;
			}
			if (LAST_DAY.equals(day)) {
				String[] hourMinuteSecond = dayTime[1].split(COLON);
				String cronExpression = this.time2LastDayCronExpression(hourMinuteSecond);
				scheduleATask(dbScheduleProcedure.getId(), cronExpression, () -> executeQueryForLastDay(dbScheduleProcedure));
			} else {
				String[] hourMinuteSecond = dayTime[1].split(COLON);
				String cronExpression = time2MonthlyCronExpression(hourMinuteSecond, day);
				scheduleATask(dbScheduleProcedure.getId(), cronExpression, () -> executeQuery(dbScheduleProcedure));
			}
		});
	}

	private void handleQuarterly(DbScheduler dbScheduleProcedure) {
		if (StringUtils.isEmpty(dbScheduleProcedure.getQuarterly())) {
			return;
		}
		String[] quarters = dbScheduleProcedure.getQuarterly().split(DOT_COMMA);

		Arrays.stream(quarters).filter(quarter -> !StringUtils.isEmpty(quarter)).map(quarter -> quarter.split(COMMA)).forEach(dayTime -> {
			String month = dayTime[0];
			String day = dayTime[1];
			String time = dayTime[2];
			if (StringUtils.isEmpty(day) || StringUtils.isEmpty(month)) {
				return;
			}
			int monthInt = Integer.parseInt(month);
			if (LAST_DAY.equals(day)) {
				String[] hourMinuteSecond = time.split(COLON);
				String quarterByMonth = QUARTERS[monthInt - 1];
				String cronExpression = this.time2LastDayQuarterlyCronExpression(hourMinuteSecond, quarterByMonth);
				scheduleATask(dbScheduleProcedure.getId(), cronExpression, () -> executeQueryForLastDay(dbScheduleProcedure));
			} else {
				String quarterByMonth = QUARTERS[monthInt - 1];
				String[] hourMinuteSecond = time.split(COLON);
				String cronExpression = time2QuarterlyCronExpression(hourMinuteSecond, day, quarterByMonth);
				scheduleATask(dbScheduleProcedure.getId(), cronExpression, () -> executeQuery(dbScheduleProcedure));
			}
		});
	}

	private void handleYearly(DbScheduler dbScheduleProcedure) {
		if (StringUtils.isEmpty(dbScheduleProcedure.getYearly())) {
			return;
		}
		String[] years = dbScheduleProcedure.getYearly().split(DOT_COMMA);

		Arrays.stream(years).filter(year -> !StringUtils.isEmpty(year)).map(year -> year.split(COMMA)).forEach(dayTime -> {
			String month = dayTime[0];
			String day = dayTime[1];
			String time = dayTime[2];
			if (StringUtils.isEmpty(day) || StringUtils.isEmpty(month)) {
				return;
			}
			if (LAST_DAY.equals(day)) {
				String[] hourMinuteSecond = time.split(COLON);
				String cronExpression = this.time2YearlyLastDayCronExpression(hourMinuteSecond, month);
				this.scheduleATask(dbScheduleProcedure.getId(), cronExpression, () -> executeQueryForLastDay(dbScheduleProcedure));
			} else {
				String[] hourMinuteSecond = time.split(COLON);
				String cronExpression = time2YearlyCronExpression(hourMinuteSecond, day, month);
				this.scheduleATask(dbScheduleProcedure.getId(), cronExpression, () -> executeQuery(dbScheduleProcedure));
			}
		});
	}

	private void executeQueryForLastDay(DbScheduler dbScheduleProcedure) {
		TimeZone zone = TimeZone.getTimeZone(Constants.ZONE_ID);
		final Calendar c = Calendar.getInstance(zone);
		if (c.get(Calendar.DATE) == c.getActualMaximum(Calendar.DATE)) {
			this.executeQuery(dbScheduleProcedure);
		}
	}

	private void executeQuery(DbScheduler dbScheduleProcedure) {
		DbScheduler existed = this.scheduleRepository.findById(dbScheduleProcedure.getId()).orElse(null);
		if (existed == null || !existed.getStatus()) {
			// remove the job in map
			logger.info("*** SQL was removed!, start to remove and cancel task");
			cancelJob(dbScheduleProcedure.getId());
			return;
		}

		LocalDateTime startAt = DateUtils.getDateTime();
		String connectionName = dbScheduleProcedure.getDb();
		if (connectionName == null) {
			this.saveResult(dbScheduleProcedure, false, startAt, "Connection not found");
			return;
		}
		try {
			DataSource dataSource = dataSourceService.findDatasourceByServerName(connectionName);
			if (null == dataSource) {
				throw new NullPointerException("There is no dataSource available!!!");
			}
			this.executeSimpleJdbc(dbScheduleProcedure.getPlSQL(), dbScheduleProcedure.getSchema(), dbScheduleProcedure.getTable(), dataSource);
			// Add log status
			this.saveResult(dbScheduleProcedure, true, startAt, "");
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			// Add log status
			this.saveResult(dbScheduleProcedure, false, startAt, ex.getMessage());
		}
	}

	private void saveResult(DbScheduler dbScheduleProcedure, boolean status, LocalDateTime startAt, String errorMsg) {
		DbSchedulerResult result = new DbSchedulerResult();
		result.setFkIdProcedure(dbScheduleProcedure.getId());
		result.setStatus(status);
		result.setScheduleTime(DateUtils.getTime().withSecond(0));
		result.setScheduleDate(DateUtils.getDate());
		result.setStartAt(startAt);
		result.setEndAt(DateUtils.getDateTime());
		result.setErrorMsg(errorMsg);
		this.resultRepository.save(result);
	}

	private void handleDaily(DbScheduler dbScheduleProcedure) {
		String[] times = dbScheduleProcedure.getTimeDaily().split(COMMA);
		Map<String, List<String>> hourSameMinute = getHourSameMinuteMap(times);
		for (String minute : hourSameMinute.keySet()) {
			String cronExpression = String.format("00 %s %s * * *", minute, String.join(",", hourSameMinute.get(minute)));
			this.scheduleATask(dbScheduleProcedure.getId(), cronExpression, () -> executeQuery(dbScheduleProcedure));
		}

//		for (String time : times) {
//			String[] hourMinuteSecond = time.split(COLON);
//			String cronExpression = time2DailyCronExpression(hourMinuteSecond);
//			this.scheduleATask(dbScheduleProcedure.getId(), cronExpression, () -> executeQuery(dbScheduleProcedure));
//		}
	}

	public Map<String, List<String>> getHourSameMinuteMap(String[] times) {
		Map<String, List<String>> minuteHours = new HashMap<>();
		for (String time : times) {
			String[] aTime = time.split(COLON);
			String hour = aTime[0];
			String minute = aTime[1];
			minuteHours.computeIfAbsent(minute, value -> new ArrayList<>()).add(hour);
		}
		return minuteHours;
	}

	private void scheduleATask(Long id, String cronExpression, Runnable task) {
		Trigger trigger = triggerContext -> {
			CronExpression expression = CronExpression.parse(cronExpression);
			ZonedDateTime dateTime = ZonedDateTime.ofInstant(new Date().toInstant(), Constants.ZONE_ID);
			ZonedDateTime next = expression.next(dateTime);

			assert next != null;
			logger.info("Create DbScheduler cron job {} at {}. The next execution time is {}",
					cronExpression, new Date(), next.toLocalDateTime());
			return Date.from(next.toInstant());
		};
		ScheduledFuture<?> schedule = taskScheduler.schedule(task, trigger);
		this.jobs.put(id, schedule);
	}

	/**
	 * e.g: hourMinuteSecond = [HH:mm:ss]
	 */
	private String time2DailyCronExpression(String[] hourMinuteSecond) {
		if (null == hourMinuteSecond) {
			return null;
		}
		String second = hourMinuteSecond[2];
		String minute = hourMinuteSecond[1];
		String hours = hourMinuteSecond[0];
		//		return "0 */1 * * * *"; // for testing
		return String.format("%s %s %s * * *", second, minute, hours);
	}


	private String time2LastDayCronExpression(String[] hourMinuteSecond) {
		if (null == hourMinuteSecond) {
			return null;
		}
		String second = hourMinuteSecond[2];
		String minute = hourMinuteSecond[1];
		String hours = hourMinuteSecond[0];
		return String.format("%s %s %s 28-31 * *", second, minute, hours);
	}

	private String time2WeeklyCronExpression(String[] hourMinuteSecond, String day) {
		if (null == hourMinuteSecond) {
			return null;
		}
		String second = hourMinuteSecond[2];
		String minute = hourMinuteSecond[1];
		String hours = hourMinuteSecond[0];
		return String.format("%s %s %s * * %s", second, minute, hours, day);
	}

	private String time2MonthlyCronExpression(String[] hourMinuteSecond, String day) {
		if (null == hourMinuteSecond) {
			return null;
		}
		String second = hourMinuteSecond[2];
		String minute = hourMinuteSecond[1];
		String hours = hourMinuteSecond[0];
		return String.format("%s %s %s %s * *", second, minute, hours, day);
	}

	private String time2LastDayQuarterlyCronExpression(String[] hourMinuteSecond, String month) {
		if (null == hourMinuteSecond) {
			return null;
		}
		String second = hourMinuteSecond[2];
		String minute = hourMinuteSecond[1];
		String hours = hourMinuteSecond[0];
		return String.format("%s %s %s 28-31 %s *", second, minute, hours, month);
	}

	private String time2QuarterlyCronExpression(String[] hourMinuteSecond, String day, String month) {
		if (null == hourMinuteSecond) {
			return null;
		}
		String second = hourMinuteSecond[2];
		String minute = hourMinuteSecond[1];
		String hours = hourMinuteSecond[0];
		return String.format("%s %s %s %s %s *", second, minute, hours, day, month);
	}

	private String time2YearlyCronExpression(String[] hourMinuteSecond, String day, String month) {
		if (null == hourMinuteSecond) {
			return null;
		}
		String second = hourMinuteSecond[2];
		String minute = hourMinuteSecond[1];
		String hours = hourMinuteSecond[0];
		return String.format("%s %s %s %s %s *", second, minute, hours, day, month);
	}

	private String time2YearlyLastDayCronExpression(String[] hourMinuteSecond, String month) {
		if (null == hourMinuteSecond) {
			return null;
		}
		String second = hourMinuteSecond[2];
		String minute = hourMinuteSecond[1];
		String hours = hourMinuteSecond[0];
		return String.format("%s %s %s 28-31 %s *", second, minute, hours, month);
	}

	/**
	 * day=2 -> Mon
	 */
	private String getShortDay(int day) {
		DayOfWeek dayOfWeek = DayOfWeek.of(day - 1); // because of DayOfWeek start at 1 (1 is Monday)
		return dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
	}
}
