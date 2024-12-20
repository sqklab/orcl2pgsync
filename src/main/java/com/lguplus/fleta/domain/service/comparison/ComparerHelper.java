package com.lguplus.fleta.domain.service.comparison;

import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.domain.model.comparison.DbComparisonInfoEntity;
import com.lguplus.fleta.domain.service.constant.Constants;
import com.lguplus.fleta.ports.service.DataSourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public
class ComparerHelper {
    private final ExecutorService executorService;
    private final DataSourceService dataSourceService;
    @Value("${comparison.query-timeout-seconds:60}")
    private int QUERY_TIMEOUT_SECONDS;

    public ComparerHelper(
            @Qualifier("defaultDatasourceContainer") DataSourceService dataSourceService,
            @Qualifier("commonThreadPool") ExecutorService executorService
    ) {
        this.dataSourceService = dataSourceService;
        this.executorService = executorService;
    }

    @AllArgsConstructor
    public class CountResult {
        @Getter(AccessLevel.PUBLIC)
        private final long count;
        @Getter(AccessLevel.PUBLIC)
        private final String errorMessage;
    }

    @AllArgsConstructor
    public class ComparisonResult {
        @Getter(AccessLevel.PUBLIC)
        private final CountResult sourceCount;
        @Getter(AccessLevel.PUBLIC)
        private final CountResult targetCount;

        public ComparisonResult(String errorMessage) {
            this.sourceCount = new CountResult(-1, errorMessage);
            this.targetCount = new CountResult(-1, errorMessage);
        }
    }

    private enum IndicateDB {
        SOURCE, TARGET
    }

    public ComparisonResult comparison(DbComparisonInfoEntity comparisonInfo) {
        if (Objects.isNull(comparisonInfo.getSyncInfo())) {
            log.error("Can't get source db and target db to compute count (comparisonId={})", comparisonInfo.getId());
            return new ComparisonResult("cannot find synchronizer");
        }

        CompletableFuture<Map.Entry<IndicateDB, CountResult>> sourceFuture = CompletableFuture.supplyAsync(
                () -> Map.entry(IndicateDB.SOURCE, getSourceResult(comparisonInfo)),
                executorService
        );
        CompletableFuture<Map.Entry<IndicateDB, CountResult>> targetFuture = CompletableFuture.supplyAsync(
                () -> Map.entry(IndicateDB.TARGET, getTargetResult(comparisonInfo)),
                executorService
        );
        Map<IndicateDB, CountResult> countResults = Stream.of(sourceFuture, targetFuture)
                .map(CompletableFuture::join)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new ComparisonResult(
                countResults.get(IndicateDB.SOURCE),
                countResults.get(IndicateDB.TARGET)
        );
    }

    private CountResult getSourceResult(DbComparisonInfoEntity comparisonInfo) {
        String compDbName = comparisonInfo.getSourceCompareDatabase();
        String syncDbName = comparisonInfo.getSyncInfo().getSourceDatabase();
        String datasourceName = (Objects.isNull(compDbName) || StringUtils.isBlank(compDbName)) ? syncDbName : compDbName;
        return getResult(datasourceName, comparisonInfo.getSourceQuery());
    }

    private CountResult getTargetResult(DbComparisonInfoEntity comparisonInfo) {
        String compDbName = comparisonInfo.getTargetCompareDatabase();
        String syncDbName = comparisonInfo.getSyncInfo().getTargetDatabase();
        String datasourceName = (Objects.isNull(compDbName) || StringUtils.isBlank(compDbName)) ? syncDbName : compDbName;
        return getResult(datasourceName, comparisonInfo.getTargetQuery());
    }

    private CountResult getResult(String dbName, String countSql) {
        long count;
        String errorMessage = "";
        try {
            count = executeCountQueryToTargetDb(dbName, countSql);
        } catch (Exception e) {
            count = -1;
            errorMessage = e.getMessage();
        }
        return new CountResult(count, errorMessage);
    }

    public long executeCountQueryToTargetDb(String targetDb, String sqlQuery) throws Exception {
        if (StringUtils.isBlank(sqlQuery) || sqlQuery.equals(Constants.NOT_AVAILABLE) || StringUtils.isBlank(targetDb)) {
            return -1;
        }
        if (log.isDebugEnabled()) {
            log.debug("Getting count from database {} by using sqlQuery {}", targetDb, sqlQuery);
        }

        // TODO: Fix bug ORA-00933: SQL command not properly ended
        //  because executeQuery() automatically adds a semicolon to a statement when executing it.
        sqlQuery = sqlQuery.trim();
        if (sqlQuery.endsWith(";")) {
            sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 1);
        }

        try (Connection connection = dataSourceService.findConnectionByServerName(targetDb)) {
            if (null == connection) {
                log.error("can't get connection for db {}", targetDb);
                throw new Exception("can't get connection");
            }
            try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
                statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS); // 60 seconds
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return rs.getLong(2);
                }
            }
        }
        return -1;
    }
}
