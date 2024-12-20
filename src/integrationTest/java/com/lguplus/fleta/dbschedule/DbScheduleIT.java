package com.lguplus.fleta.dbschedule;

import com.lguplus.fleta.DbSyncServiceApplication;
import com.lguplus.fleta.domain.model.DbScheduler;
import com.lguplus.fleta.domain.model.DbSchedulerResult;
import com.lguplus.fleta.domain.util.DateUtils;
import com.lguplus.fleta.ports.repository.DbScheduleProcedureResultRepository;
import com.lguplus.fleta.ports.service.DataSourceService;
import com.lguplus.fleta.ports.service.dbschedule.DbSchedulerManager;
import com.lguplus.fleta.ports.service.dbschedule.DbSchedulerService;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = DbSyncServiceApplication.class
)
@ActiveProfiles("integrationTest")
public class DbScheduleIT {

	public static final int SECOND = 60;

	@Autowired
	private DbSchedulerManager dbSchedulerManager;
	@Autowired
	private DbSchedulerService dbSchedulerService;
	@Autowired
	DbScheduleProcedureResultRepository resultRepository;

	@Test
	public void should_Execute_Db_Schedule_Success() throws Exception {
		DbScheduler dbScheduler = null;
		try {
			dbScheduler = createSchedule("testuser.demoPLSQL()");
			this.dbSchedulerService.scheduleAll();
			List<DbSchedulerResult> byScheduleDateOrderByScheduleTime = resultRepository.findByScheduleDateOrderByScheduleTime(LocalDate.now());
			Assert.assertEquals(1, byScheduleDateOrderByScheduleTime.size());
			DbSchedulerResult result = byScheduleDateOrderByScheduleTime.get(0);
			Assert.assertEquals("", result.getErrorMsg());
		} finally {
			if (dbScheduler != null) {
				this.dbSchedulerManager.deleteRegistrationByIds(List.of(dbScheduler.getId()));
			}
		}
	}

	@Test
	public void should_Execute_Db_Schedule_Failse_Because_InvalidPLSQL() throws Exception {
		DbScheduler dbScheduler = null;
		try {
			dbScheduler = createSchedule("notExist.invalidPLSQL()");
			this.dbSchedulerService.scheduleAll();
			List<DbSchedulerResult> byScheduleDateOrderByScheduleTime = resultRepository.findByScheduleDateOrderByScheduleTime(LocalDate.now());
			Assert.assertEquals(1, byScheduleDateOrderByScheduleTime.size());
			DbSchedulerResult result = byScheduleDateOrderByScheduleTime.get(0);
			Assert.assertNotNull(result.getErrorMsg());
		} finally {
			if (dbScheduler != null) {
				this.dbSchedulerManager.deleteRegistrationByIds(List.of(dbScheduler.getId()));
			}
		}
	}

	private DbScheduler createSchedule(String plSql) {
		DbScheduler dbScheduleProcedure = new DbScheduler();
		dbScheduleProcedure.setDb("PG");
		dbScheduleProcedure.setName("demoName1");
		dbScheduleProcedure.setPlSQL(plSql);
		dbScheduleProcedure.setStatus(Boolean.TRUE);
		dbScheduleProcedure.setType(0);
		dbScheduleProcedure.setProcessStatus(dbScheduleProcedure.getStatus());
		dbScheduleProcedure.setCreatedAt(DateUtils.getDateTime());
		dbScheduleProcedure.setUpdatedAt(DateUtils.getDateTime());
		dbScheduleProcedure.setLastRun(DateUtils.getDateTime());

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		String format = LocalDateTime.now().plusSeconds(SECOND).format(dateTimeFormatter);
		dbScheduleProcedure.setTimeDaily(format); // "18:52:00"
		return dbSchedulerManager.add(dbScheduleProcedure);
	}
}
