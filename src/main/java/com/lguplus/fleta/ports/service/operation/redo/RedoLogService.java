package com.lguplus.fleta.ports.service.operation.redo;

import com.lguplus.fleta.domain.dto.ui.CurrentScnInfo;
import com.lguplus.fleta.domain.dto.ui.WrapLogMnrContent;
import com.lguplus.fleta.domain.service.exception.DatasourceNotFoundException;

import java.sql.SQLException;
import java.util.List;

public interface RedoLogService {

	CurrentScnInfo getCurrentSCN(String db) throws SQLException, DatasourceNotFoundException;

	String getTimestampByScn(Long scn, String db) throws SQLException;

	String getScnByTimestamp(String dateTime, String db) throws SQLException;

	List<String> getTablesByDbAndSchema(String db, String schema) throws SQLException;

	WrapLogMnrContent searchLogMnrContents(String startScn, String endScn, List<Integer> operationTypes, List<String> tables, String schema, String db, Integer totalPage, Integer currentPage, Integer pageSize) throws SQLException;

}
