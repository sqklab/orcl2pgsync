package com.lguplus.fleta.util;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.IncorrectResultSetColumnCountException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

public class TestTableHelper {

    private TestTableEntity testTableEntity;
    private final String schemaName;
    private final JdbcTemplate jdbcTemplate;
    private final boolean isOracle;

    public TestTableHelper(DataSource dataSource, String schemaName, boolean isOracle){
        this.schemaName = schemaName;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.isOracle = isOracle;
    }

    public Integer executeCount(String tableName, boolean logging){
        String countSql = String.format(
                "select count(1) from %s.%s where col_bpchar = '%s' and col_varchar = '%s' and col_int4 = %d",
                this.schemaName,
                tableName,
                this.testTableEntity.getBpchar(),
                this.testTableEntity.getVarchar(),
                this.testTableEntity.getInt4()
        );
        if (logging){
            System.out.println(">>>"+countSql);
        }
        return jdbcTemplate.queryForObject(countSql, Integer.class);
    }

    public void executeInsert(String tableName){
        String insertSQL = String.format(
                "insert into %s.%s" +
                        "    (col_bpchar, col_varchar, col_float8, col_int4, col_numeric, col_int2, col_text, col_timestamp)" +
                        "values ( '%s', '%s', %f, %d, %d, %d, '%s', %s)",
                this.schemaName,
                tableName,
                this.testTableEntity.getBpchar(),
                this.testTableEntity.getVarchar(),
                this.testTableEntity.getFloat8(),
                this.testTableEntity.getInt4(),
                this.testTableEntity.getNumeric(),
                this.testTableEntity.getInt2(),
                this.testTableEntity.getText(),
                this.isOracle ? "SYSDATE" : "now()::timestamp(0)"
        );
        System.out.println(">>> " + insertSQL);
        jdbcTemplate.execute(insertSQL);
    }

    public Integer executeUpdateColInt4(String tableName){
        String countSql = String.format(
                "update %s.%s set col_int4 = %d where col_bpchar = '%s' and col_varchar = '%s'",
                this.schemaName,
                tableName,
                this.testTableEntity.getInt4(),
                this.testTableEntity.getBpchar(),
                this.testTableEntity.getVarchar()
        );
        System.out.println(">>> " + countSql);
        try {
            return jdbcTemplate.queryForObject(countSql, Integer.class);
        } catch (IncorrectResultSetColumnCountException e) {
            return 0;
        } catch (DataIntegrityViolationException e) {
            return 0;
        }
    }

    public void exeStm(String query) {
        jdbcTemplate.execute(query);
    }

    public void executeDelete(String tableName){
        jdbcTemplate.execute(
                String.format(
                        "delete from %s.%s where col_bpchar = '%s' and col_varchar = '%s'",
                        this.schemaName,
                        tableName,
                        this.testTableEntity.getBpchar(),
                        this.testTableEntity.getVarchar()
                )
        );
    }

    public void executeTruncate(String tableName){
        jdbcTemplate.execute(
                String.format(
                        "truncate table %s.%s ",
                        this.schemaName,
                        tableName
                )
        );
    }

    public void createTableWitColumns(String tableName, List<String> columns) {
        String columnToQuery = String.join(",", columns);
        jdbcTemplate.execute(
            String.format(
                    "CREATE TABLE %s.%s (%s)",
                    this.schemaName,
                    tableName,
                    columnToQuery
            )
        );
    }

    public void dropTable(String tableName) {
        jdbcTemplate.execute(
                String.format(
                        "DROP TABLE %s.%s",
                        this.schemaName,
                        tableName
                )
        );
    }

    public void setData(TestTableEntity testTableEntity){
        this.testTableEntity = TestTableEntity.builder()
                .bpchar(testTableEntity.getBpchar())
                .varchar(testTableEntity.getVarchar())
                .int4(testTableEntity.getInt4())
                .float8(testTableEntity.getFloat8())
                .int2(testTableEntity.getInt2())
                .numeric(testTableEntity.getNumeric())
                .text(testTableEntity.getText())
                .build();

    }

    public void setRandomData(){
        this.testTableEntity = new TestTableEntity();
        this.testTableEntity.setRandomData();
    }

    public Integer resetColInt4(){
        int newColInt4 = (int) (Math.random()*10);
        while (testTableEntity.getInt4() == newColInt4){
            newColInt4 = (int) (Math.random()*10);
        }
        return this.resetColInt4(newColInt4);
    }

    public Integer resetColInt4(int n){
        this.testTableEntity.setInt4(n);
        return this.testTableEntity.getInt4();
    }

    public TestTableEntity getData(){
        return this.testTableEntity;
    }
}
