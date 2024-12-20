package com.lguplus.fleta.ports.service.dbschedule;


import com.lguplus.fleta.domain.dto.ui.SimpleDbSchedulerResultDto;
import com.lguplus.fleta.domain.model.DbScheduler;
import com.lguplus.fleta.domain.model.DbSchedulerResult;

public interface DbSchedulerService {

	void register();

	void run(DbScheduler dbScheduleProcedure);

	void retry(DbSchedulerResult procedureResult, DbScheduler procedure);

	void testScript(DbScheduler procedure) throws Exception;

	SimpleDbSchedulerResultDto scheduleAll() throws Exception;

	long total();
}
