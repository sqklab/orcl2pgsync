//package com.lguplus.fleta;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.jayway.jsonpath.JsonPath;
//import com.lguplus.fleta.adapters.persistence.exception.InvalidSyncMessageRequestException;
//import com.lguplus.fleta.domain.dto.SyncRequestMessage;
//import com.lguplus.fleta.domain.dto.command.TaskExecuteCommand;
//import com.lguplus.fleta.domain.model.SyncRequestEntity;
//import com.lguplus.fleta.domain.service.convertor.DebeziumOracleToPostgresDatatypeConverter;
//import com.lguplus.fleta.domain.service.convertor.DebeziumVersion;
//import com.lguplus.fleta.domain.service.convertor.DefaultDebeziumToPostgresDatatypeConverter;
//import com.lguplus.fleta.domain.service.convertor.IDatatypeConverter;
//import groovy.util.logging.Slf4j;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RunWith(MockitoJUnitRunner.class)
//@Slf4j
//public class DebeziumNPKOracleToPostgresConvertorTest {
//	ObjectMapper objectMapper = DebeziumDatatypeConvertorHelper.objectMapper;
//
//	IDatatypeConverter convertor = new DebeziumOracleToPostgresDatatypeConverter(DebeziumVersion.V1_9_6);
//
//	String Ora_InsertMessage_NPK = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":true,\"field\":\"COL_CHAR\"},{\"type\":\"string\",\"optional\":true,\"field\":\"COL_VARCHAR2\"},{\"type\":\"string\",\"optional\":true,\"field\":\"COL_NVARCHAR2\"},{\"type\":\"double\",\"optional\":true,\"field\":\"COL_FLOAT\"},{\"type\":\"double\",\"optional\":true,\"field\":\"COL_NUMBER\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_DATE\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP2\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP4\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP6\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.time.ZonedTimestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP_TZ\"}],\"optional\":true,\"name\":\"MYLGDB.IMCSUSER.HJ_TEST_TABLE_NPK_ORA.Value\",\"field\":\"before\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":true,\"field\":\"COL_CHAR\"},{\"type\":\"string\",\"optional\":true,\"field\":\"COL_VARCHAR2\"},{\"type\":\"string\",\"optional\":true,\"field\":\"COL_NVARCHAR2\"},{\"type\":\"double\",\"optional\":true,\"field\":\"COL_FLOAT\"},{\"type\":\"double\",\"optional\":true,\"field\":\"COL_NUMBER\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_DATE\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP2\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP4\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP6\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.time.ZonedTimestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP_TZ\"}],\"optional\":true,\"name\":\"MYLGDB.IMCSUSER.HJ_TEST_TABLE_NPK_ORA.Value\",\"field\":\"after\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"version\"},{\"type\":\"string\",\"optional\":false,\"field\":\"connector\"},{\"type\":\"string\",\"optional\":false,\"field\":\"name\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"ts_ms\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.data.Enum\",\"version\":1,\"parameters\":{\"allowed\":\"true,last,false,incremental\"},\"default\":\"false\",\"field\":\"snapshot\"},{\"type\":\"string\",\"optional\":false,\"field\":\"db\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sequence\"},{\"type\":\"string\",\"optional\":false,\"field\":\"schema\"},{\"type\":\"string\",\"optional\":false,\"field\":\"table\"},{\"type\":\"string\",\"optional\":true,\"field\":\"txId\"},{\"type\":\"string\",\"optional\":true,\"field\":\"scn\"},{\"type\":\"string\",\"optional\":true,\"field\":\"commit_scn\"},{\"type\":\"string\",\"optional\":true,\"field\":\"lcr_position\"}],\"optional\":false,\"name\":\"io.debezium.connector.oracle.Source\",\"field\":\"source\"},{\"type\":\"string\",\"optional\":false,\"field\":\"op\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_ms\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"id\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"total_order\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"data_collection_order\"}],\"optional\":true,\"field\":\"transaction\"}],\"optional\":false,\"name\":\"MYLGDB.IMCSUSER.HJ_TEST_TABLE_NPK_ORA.Envelope\"},\"payload\":{\"before\":null,\"after\":{\"COL_CHAR\":\"t\",\"COL_VARCHAR2\":\"test\",\"COL_NVARCHAR2\":\"test1\",\"COL_FLOAT\":111.111,\"COL_NUMBER\":111.0,\"COL_DATE\":1655814204000,\"COL_TIMESTAMP2\":1655814204000,\"COL_TIMESTAMP4\":1655814204000,\"COL_TIMESTAMP6\":1655814204000,\"COL_TIMESTAMP_TZ\":\"2022-06-21T12:23:24+09:00\"},\"source\":{\"version\":\"1.9.0.Final\",\"connector\":\"oracle\",\"name\":\"MYLGDB\",\"ts_ms\":1655749404000,\"snapshot\":\"false\",\"db\":\"MYLGDB2\",\"sequence\":null,\"schema\":\"IMCSUSER\",\"table\":\"HJ_TEST_TABLE_NPK_ORA\",\"txId\":\"050019002ff30000\",\"scn\":\"1542570990\",\"commit_scn\":\"1542571352\",\"lcr_position\":null},\"op\":\"c\",\"ts_ms\":1655781805123,\"transaction\":null}}";
//	String Ora_UpdateMessage_NPK = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":true,\"field\":\"COL_CHAR\"},{\"type\":\"string\",\"optional\":true,\"field\":\"COL_VARCHAR2\"},{\"type\":\"string\",\"optional\":true,\"field\":\"COL_NVARCHAR2\"},{\"type\":\"double\",\"optional\":true,\"field\":\"COL_FLOAT\"},{\"type\":\"double\",\"optional\":true,\"field\":\"COL_NUMBER\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_DATE\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP2\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP4\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP6\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.time.ZonedTimestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP_TZ\"}],\"optional\":true,\"name\":\"MYLGDB.IMCSUSER.HJ_TEST_TABLE_NPK_ORA.Value\",\"field\":\"before\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":true,\"field\":\"COL_CHAR\"},{\"type\":\"string\",\"optional\":true,\"field\":\"COL_VARCHAR2\"},{\"type\":\"string\",\"optional\":true,\"field\":\"COL_NVARCHAR2\"},{\"type\":\"double\",\"optional\":true,\"field\":\"COL_FLOAT\"},{\"type\":\"double\",\"optional\":true,\"field\":\"COL_NUMBER\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_DATE\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP2\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP4\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP6\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.time.ZonedTimestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP_TZ\"}],\"optional\":true,\"name\":\"MYLGDB.IMCSUSER.HJ_TEST_TABLE_NPK_ORA.Value\",\"field\":\"after\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"version\"},{\"type\":\"string\",\"optional\":false,\"field\":\"connector\"},{\"type\":\"string\",\"optional\":false,\"field\":\"name\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"ts_ms\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.data.Enum\",\"version\":1,\"parameters\":{\"allowed\":\"true,last,false,incremental\"},\"default\":\"false\",\"field\":\"snapshot\"},{\"type\":\"string\",\"optional\":false,\"field\":\"db\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sequence\"},{\"type\":\"string\",\"optional\":false,\"field\":\"schema\"},{\"type\":\"string\",\"optional\":false,\"field\":\"table\"},{\"type\":\"string\",\"optional\":true,\"field\":\"txId\"},{\"type\":\"string\",\"optional\":true,\"field\":\"scn\"},{\"type\":\"string\",\"optional\":true,\"field\":\"commit_scn\"},{\"type\":\"string\",\"optional\":true,\"field\":\"lcr_position\"}],\"optional\":false,\"name\":\"io.debezium.connector.oracle.Source\",\"field\":\"source\"},{\"type\":\"string\",\"optional\":false,\"field\":\"op\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_ms\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"id\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"total_order\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"data_collection_order\"}],\"optional\":true,\"field\":\"transaction\"}],\"optional\":false,\"name\":\"MYLGDB.IMCSUSER.HJ_TEST_TABLE_NPK_ORA.Envelope\"},\"payload\":{\"before\":{\"COL_CHAR\":\"t\",\"COL_VARCHAR2\":\"test\",\"COL_NVARCHAR2\":\"test1\",\"COL_FLOAT\":111.111,\"COL_NUMBER\":111.0,\"COL_DATE\":1655814204000,\"COL_TIMESTAMP2\":1655814204000,\"COL_TIMESTAMP4\":1655814204000,\"COL_TIMESTAMP6\":1655814204000,\"COL_TIMESTAMP_TZ\":\"2022-06-21T12:23:24+09:00\"},\"after\":{\"COL_CHAR\":\"t\",\"COL_VARCHAR2\":\"test\",\"COL_NVARCHAR2\":\"test2\",\"COL_FLOAT\":222.0,\"COL_NUMBER\":222.222,\"COL_DATE\":1655814206000,\"COL_TIMESTAMP2\":1655814206000,\"COL_TIMESTAMP4\":1655814206000,\"COL_TIMESTAMP6\":1655814206000,\"COL_TIMESTAMP_TZ\":\"2022-06-21T12:23:26+09:00\"},\"source\":{\"version\":\"1.9.0.Final\",\"connector\":\"oracle\",\"name\":\"MYLGDB\",\"ts_ms\":1655749406000,\"snapshot\":\"false\",\"db\":\"MYLGDB2\",\"sequence\":null,\"schema\":\"IMCSUSER\",\"table\":\"HJ_TEST_TABLE_NPK_ORA\",\"txId\":\"0c000f00b0c30100\",\"scn\":\"1542570990\",\"commit_scn\":\"1542571602\",\"lcr_position\":null},\"op\":\"u\",\"ts_ms\":1655781808218,\"transaction\":null}}";
//	String Ora_DeleteMessage_NPK = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":true,\"field\":\"COL_CHAR\"},{\"type\":\"string\",\"optional\":true,\"field\":\"COL_VARCHAR2\"},{\"type\":\"string\",\"optional\":true,\"field\":\"COL_NVARCHAR2\"},{\"type\":\"double\",\"optional\":true,\"field\":\"COL_FLOAT\"},{\"type\":\"double\",\"optional\":true,\"field\":\"COL_NUMBER\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_DATE\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP2\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP4\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP6\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.time.ZonedTimestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP_TZ\"}],\"optional\":true,\"name\":\"MYLGDB.IMCSUSER.HJ_TEST_TABLE_NPK_ORA.Value\",\"field\":\"before\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":true,\"field\":\"COL_CHAR\"},{\"type\":\"string\",\"optional\":true,\"field\":\"COL_VARCHAR2\"},{\"type\":\"string\",\"optional\":true,\"field\":\"COL_NVARCHAR2\"},{\"type\":\"double\",\"optional\":true,\"field\":\"COL_FLOAT\"},{\"type\":\"double\",\"optional\":true,\"field\":\"COL_NUMBER\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_DATE\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP2\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP4\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP6\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.time.ZonedTimestamp\",\"version\":1,\"field\":\"COL_TIMESTAMP_TZ\"}],\"optional\":true,\"name\":\"MYLGDB.IMCSUSER.HJ_TEST_TABLE_NPK_ORA.Value\",\"field\":\"after\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"version\"},{\"type\":\"string\",\"optional\":false,\"field\":\"connector\"},{\"type\":\"string\",\"optional\":false,\"field\":\"name\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"ts_ms\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.data.Enum\",\"version\":1,\"parameters\":{\"allowed\":\"true,last,false,incremental\"},\"default\":\"false\",\"field\":\"snapshot\"},{\"type\":\"string\",\"optional\":false,\"field\":\"db\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sequence\"},{\"type\":\"string\",\"optional\":false,\"field\":\"schema\"},{\"type\":\"string\",\"optional\":false,\"field\":\"table\"},{\"type\":\"string\",\"optional\":true,\"field\":\"txId\"},{\"type\":\"string\",\"optional\":true,\"field\":\"scn\"},{\"type\":\"string\",\"optional\":true,\"field\":\"commit_scn\"},{\"type\":\"string\",\"optional\":true,\"field\":\"lcr_position\"}],\"optional\":false,\"name\":\"io.debezium.connector.oracle.Source\",\"field\":\"source\"},{\"type\":\"string\",\"optional\":false,\"field\":\"op\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_ms\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"id\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"total_order\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"data_collection_order\"}],\"optional\":true,\"field\":\"transaction\"}],\"optional\":false,\"name\":\"MYLGDB.IMCSUSER.HJ_TEST_TABLE_NPK_ORA.Envelope\"},\"payload\":{\"before\":{\"COL_CHAR\":\"t\",\"COL_VARCHAR2\":\"test\",\"COL_NVARCHAR2\":\"test2\",\"COL_FLOAT\":222.0,\"COL_NUMBER\":222.222,\"COL_DATE\":1655814206000,\"COL_TIMESTAMP2\":1655814206000,\"COL_TIMESTAMP4\":1655814206000,\"COL_TIMESTAMP6\":1655814206000,\"COL_TIMESTAMP_TZ\":\"2022-06-21T12:23:26+09:00\"},\"after\":null,\"source\":{\"version\":\"1.9.0.Final\",\"connector\":\"oracle\",\"name\":\"MYLGDB\",\"ts_ms\":1655749408000,\"snapshot\":\"false\",\"db\":\"MYLGDB2\",\"sequence\":null,\"schema\":\"IMCSUSER\",\"table\":\"HJ_TEST_TABLE_NPK_ORA\",\"txId\":\"0500050010f60000\",\"scn\":\"1542570990\",\"commit_scn\":\"1542571763\",\"lcr_position\":null},\"op\":\"d\",\"ts_ms\":1655781811115,\"transaction\":null}}";
//
//	@Test
//	void testNPKTableOracleToPostgresInsert() throws InvalidSyncMessageRequestException, JsonProcessingException {
//
//		String after = objectMapper.writeValueAsString(JsonPath.read(Ora_InsertMessage_NPK, "$.payload.after"));
//		System.out.println("======== testNPKTableOracleToPostgresInsert");
//		System.out.println("\n====== Oracle after");
//		System.out.println(after);
//
//		SyncRequestMessage syncRequestMessage = objectMapper.readValue(Ora_InsertMessage_NPK, SyncRequestMessage.class);
//		Assertions.assertNotNull(syncRequestMessage);
//
//		DefaultDebeziumToPostgresDatatypeConverter.ExecutorSqlData sqlData = convertor.getSqlRedo(syncRequestMessage, TaskExecuteCommand.of(SyncRequestEntity.of("vod_programming", "HJ_TEST_TABLE_NPK_ORA", null, true)));
//		DebeziumDatatypeConvertorHelper.printSqlDate(sqlData);
//
//		Assertions.assertEquals(
//				"INSERT INTO VOD_PROGRAMMING.HJ_TEST_TABLE_NPK_ORA(COL_CHAR,COL_VARCHAR2,COL_NVARCHAR2,COL_FLOAT,COL_NUMBER,COL_DATE,COL_TIMESTAMP2,COL_TIMESTAMP4,COL_TIMESTAMP6,COL_TIMESTAMP_TZ) VALUES (?,?,?,?,?,?,?,?,?,?)",
//				sqlData.getSql());
//
//		List<String> cols = Arrays.stream("COL_CHAR, COL_VARCHAR2, COL_NVARCHAR2, COL_FLOAT, COL_NUMBER, COL_DATE, COL_TIMESTAMP2, COL_TIMESTAMP4, COL_TIMESTAMP6, COL_TIMESTAMP_TZ"
//				.split(", ")).collect(Collectors.toList());
//		Assertions.assertEquals(cols, sqlData.getColumnOrder());
//
//
//		Assertions.assertEquals("t", sqlData.getParams().get("COL_CHAR"));
//		Assertions.assertEquals("test", sqlData.getParams().get("COL_VARCHAR2"));
//		Assertions.assertEquals("test1", sqlData.getParams().get("COL_NVARCHAR2"));
//		Assertions.assertEquals(111.111, sqlData.getParams().get("COL_FLOAT"));
//		Assertions.assertEquals(111.0, sqlData.getParams().get("COL_NUMBER"));
//		Assertions.assertEquals(LocalDateTime.parse("2022-06-21T12:23:24"), sqlData.getParams().get("COL_DATE"));
//		Assertions.assertEquals(LocalDateTime.parse("2022-06-21T12:23:24"), sqlData.getParams().get("COL_TIMESTAMP2"));
//		Assertions.assertEquals(LocalDateTime.parse("2022-06-21T12:23:24"), sqlData.getParams().get("COL_TIMESTAMP4"));
//		Assertions.assertEquals(LocalDateTime.parse("2022-06-21T12:23:24"), sqlData.getParams().get("COL_TIMESTAMP6"));
//	}
//
//
//	@Test
//	void testNPKTableOracleToPostgresUpdate() throws InvalidSyncMessageRequestException, JsonProcessingException {
//		String before = objectMapper.writeValueAsString(JsonPath.read(Ora_UpdateMessage_NPK, "$.payload.before"));
//		String after = objectMapper.writeValueAsString(JsonPath.read(Ora_UpdateMessage_NPK, "$.payload.after"));
//		System.out.println("======== testNPKTableOracleToPostgresUpdate");
//		System.out.println("\n====== Oracle before");
//		System.out.println(before);
//
//		System.out.println("\n====== Oracle after");
//		System.out.println(after);
//
//		SyncRequestMessage syncRequestMessage = objectMapper.readValue(Ora_UpdateMessage_NPK, SyncRequestMessage.class);
//		Assertions.assertNotNull(syncRequestMessage);
//
//		DefaultDebeziumToPostgresDatatypeConverter.ExecutorSqlData sqlData = convertor.getSqlRedo(syncRequestMessage, TaskExecuteCommand.of(SyncRequestEntity.of("vod_programming", "HJ_TEST_TABLE_NPK_ORA", null, true)));
//		DebeziumDatatypeConvertorHelper.printSqlDate(sqlData);
//
//		Assertions.assertEquals(
//				"UPDATE vod_programming.HJ_TEST_TABLE_NPK_ORA SET COL_CHAR=?,COL_VARCHAR2=?,COL_NVARCHAR2=?,COL_FLOAT=?,COL_NUMBER=?,COL_DATE=?,COL_TIMESTAMP2=?,COL_TIMESTAMP4=?,COL_TIMESTAMP6=?,COL_TIMESTAMP_TZ=? WHERE COL_CHAR=? and COL_VARCHAR2=? and COL_NVARCHAR2=? and COL_FLOAT=? and COL_NUMBER=? and COL_DATE=? and COL_TIMESTAMP2=? and COL_TIMESTAMP4=? and COL_TIMESTAMP6=? and COL_TIMESTAMP_TZ=?",
//				sqlData.getSql());
//
//		List<String> cols = Arrays.stream("col_char, col_varchar2, col_nvarchar2, col_float, col_number, col_date, col_timestamp2, col_timestamp4, col_timestamp6, col_timestamp_tz, COL_CHAR, COL_VARCHAR2, COL_NVARCHAR2, COL_FLOAT, COL_NUMBER, COL_DATE, COL_TIMESTAMP2, COL_TIMESTAMP4, COL_TIMESTAMP6, COL_TIMESTAMP_TZ"
//				.split(", ")).collect(Collectors.toList());
//		Assertions.assertEquals(cols, sqlData.getColumnOrder());
//
//		// After (PG)
//		Assertions.assertEquals("t", sqlData.getParams().get("COL_CHAR"));
//		Assertions.assertEquals("test", sqlData.getParams().get("COL_VARCHAR2"));
//		Assertions.assertEquals("test1", sqlData.getParams().get("COL_NVARCHAR2"));
//		Assertions.assertEquals(111.111, sqlData.getParams().get("COL_FLOAT"));
//		Assertions.assertEquals(111.0, sqlData.getParams().get("COL_NUMBER"));
//		Assertions.assertEquals(LocalDateTime.parse("2022-06-21T12:23:24"), sqlData.getParams().get("COL_DATE"));
//		Assertions.assertEquals(LocalDateTime.parse("2022-06-21T12:23:24"), sqlData.getParams().get("COL_TIMESTAMP2"));
//		Assertions.assertEquals(LocalDateTime.parse("2022-06-21T12:23:24"), sqlData.getParams().get("COL_TIMESTAMP4"));
//		Assertions.assertEquals(LocalDateTime.parse("2022-06-21T12:23:24"), sqlData.getParams().get("COL_TIMESTAMP6"));
//
//		// Before (Oracle)
//		Assertions.assertEquals("t", sqlData.getParams().get("col_char"));
//		Assertions.assertEquals("test", sqlData.getParams().get("col_varchar2"));
//		Assertions.assertEquals("test2", sqlData.getParams().get("col_nvarchar2"));
//		Assertions.assertEquals(222.0, sqlData.getParams().get("col_float"));
//		Assertions.assertEquals(222.222, sqlData.getParams().get("col_number"));
//		Assertions.assertEquals(LocalDateTime.parse("2022-06-21T12:23:26"), sqlData.getParams().get("col_date"));
//		Assertions.assertEquals(LocalDateTime.parse("2022-06-21T12:23:26"), sqlData.getParams().get("col_timestamp2"));
//		Assertions.assertEquals(LocalDateTime.parse("2022-06-21T12:23:26"), sqlData.getParams().get("col_timestamp4"));
//		Assertions.assertEquals(LocalDateTime.parse("2022-06-21T12:23:26"), sqlData.getParams().get("col_timestamp6"));
//	}
//
//	@Test
//	void testNPKTableOracleToPostgresDelete() throws InvalidSyncMessageRequestException, JsonProcessingException {
//		String before = objectMapper.writeValueAsString(JsonPath.read(Ora_DeleteMessage_NPK, "$.payload.before"));
//		System.out.println("======== testNPKTableOracleToPostgresDelete");
//		System.out.println("\n====== Oracle before");
//		System.out.println(before);
//
//		SyncRequestMessage syncRequestMessage = objectMapper.readValue(Ora_DeleteMessage_NPK, SyncRequestMessage.class);
//		Assertions.assertNotNull(syncRequestMessage);
//
//		DefaultDebeziumToPostgresDatatypeConverter.ExecutorSqlData sqlData = convertor.getSqlRedo(syncRequestMessage, TaskExecuteCommand.of(SyncRequestEntity.of("vod_programming", "HJ_TEST_TABLE_NPK_ORA", null, true)));
//		DebeziumDatatypeConvertorHelper.printSqlDate(sqlData);
//
//		Assertions.assertEquals(
//				"DELETE FROM vod_programming.HJ_TEST_TABLE_NPK_ORA WHERE COL_CHAR=? and COL_VARCHAR2=? and COL_NVARCHAR2=? and COL_FLOAT=? and COL_NUMBER=? and COL_DATE=? and COL_TIMESTAMP2=? and COL_TIMESTAMP4=? and COL_TIMESTAMP6=? and COL_TIMESTAMP_TZ=?",
//				sqlData.getSql());
//
//		List<String> cols = Arrays.stream("COL_CHAR, COL_VARCHAR2, COL_NVARCHAR2, COL_FLOAT, COL_NUMBER, COL_DATE, COL_TIMESTAMP2, COL_TIMESTAMP4, COL_TIMESTAMP6, COL_TIMESTAMP_TZ".split(", ")).collect(Collectors.toList());
//		Assertions.assertEquals(cols, sqlData.getColumnOrder());
//
//
//		Assertions.assertEquals("t", sqlData.getParams().get("COL_CHAR"));
//		Assertions.assertEquals("test", sqlData.getParams().get("COL_VARCHAR2"));
//		Assertions.assertEquals("test2", sqlData.getParams().get("COL_NVARCHAR2"));
//		Assertions.assertEquals(222.0, sqlData.getParams().get("COL_FLOAT"));
//		Assertions.assertEquals(222.222, sqlData.getParams().get("COL_NUMBER"));
//		Assertions.assertEquals(LocalDateTime.parse("2022-06-21T12:23:26"), sqlData.getParams().get("COL_DATE"));
//		Assertions.assertEquals(LocalDateTime.parse("2022-06-21T12:23:26"), sqlData.getParams().get("COL_TIMESTAMP2"));
//		Assertions.assertEquals(LocalDateTime.parse("2022-06-21T12:23:26"), sqlData.getParams().get("COL_TIMESTAMP4"));
//		Assertions.assertEquals(LocalDateTime.parse("2022-06-21T12:23:26"), sqlData.getParams().get("COL_TIMESTAMP6"));
//	}
//}
