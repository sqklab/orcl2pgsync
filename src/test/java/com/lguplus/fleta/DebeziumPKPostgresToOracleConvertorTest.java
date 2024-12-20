//package com.lguplus.fleta;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.jayway.jsonpath.JsonPath;
//import com.lguplus.fleta.adapters.persistence.exception.InvalidSyncMessageRequestException;
//import com.lguplus.fleta.domain.dto.SyncRequestMessage;
//import com.lguplus.fleta.domain.dto.command.TaskExecuteCommand;
//import com.lguplus.fleta.domain.model.SyncRequestEntity;
//import com.lguplus.fleta.domain.service.convertor.DebeziumPostgresToOracleDatatypeConverter;
//import com.lguplus.fleta.domain.service.convertor.DebeziumVersion;
//import com.lguplus.fleta.domain.service.convertor.DefaultDebeziumToPostgresDatatypeConverter;
//import com.lguplus.fleta.domain.service.convertor.IDatatypeConverter;
//import groovy.util.logging.Slf4j;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RunWith(MockitoJUnitRunner.class)
//@Slf4j
//public class DebeziumPKPostgresToOracleConvertorTest {
//	private static String InsertMessage_PK = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"col_bpchar\"},{\"type\":\"string\",\"optional\":false,\"field\":\"col_varchar\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_float8\"},{\"type\":\"int32\",\"optional\":true,\"field\":\"col_int4\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_numeric\"},{\"type\":\"int16\",\"optional\":true,\"field\":\"col_int2\"},{\"type\":\"string\",\"optional\":true,\"field\":\"col_text\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"col_timestamp\"}],\"optional\":true,\"name\":\"REV_msa_mylgdb.imcsuser.hj_test_table.Value\",\"field\":\"before\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"col_bpchar\"},{\"type\":\"string\",\"optional\":false,\"field\":\"col_varchar\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_float8\"},{\"type\":\"int32\",\"optional\":true,\"field\":\"col_int4\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_numeric\"},{\"type\":\"int16\",\"optional\":true,\"field\":\"col_int2\"},{\"type\":\"string\",\"optional\":true,\"field\":\"col_text\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"col_timestamp\"}],\"optional\":true,\"name\":\"REV_msa_mylgdb.imcsuser.hj_test_table.Value\",\"field\":\"after\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"version\"},{\"type\":\"string\",\"optional\":false,\"field\":\"connector\"},{\"type\":\"string\",\"optional\":false,\"field\":\"name\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"ts_ms\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.data.Enum\",\"version\":1,\"parameters\":{\"allowed\":\"true,last,false,incremental\"},\"default\":\"false\",\"field\":\"snapshot\"},{\"type\":\"string\",\"optional\":false,\"field\":\"db\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sequence\"},{\"type\":\"string\",\"optional\":false,\"field\":\"schema\"},{\"type\":\"string\",\"optional\":false,\"field\":\"table\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"txId\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"lsn\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"xmin\"}],\"optional\":false,\"name\":\"io.debezium.connector.postgresql.Source\",\"field\":\"source\"},{\"type\":\"string\",\"optional\":false,\"field\":\"op\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_ms\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"id\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"total_order\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"data_collection_order\"}],\"optional\":true,\"field\":\"transaction\"}],\"optional\":false,\"name\":\"REV_msa_mylgdb.imcsuser.hj_test_table.Envelope\"},\"payload\":{\"before\":null,\"after\":{\"col_bpchar\":\"t\",\"col_varchar\":\"test2\",\"col_float8\":2222.2222,\"col_int4\":2222,\"col_numeric\":1111.0,\"col_int2\":2222,\"col_text\":null,\"col_timestamp\":1655809836076},\"source\":{\"version\":\"1.9.0.Final\",\"connector\":\"postgresql\",\"name\":\"REV_msa_mylgdb\",\"ts_ms\":1655777436077,\"snapshot\":\"false\",\"db\":\"msa_mylgdb\",\"sequence\":\"[\\\"1279470505112\\\",\\\"1279470516648\\\"]\",\"schema\":\"imcsuser\",\"table\":\"hj_test_table\",\"txId\":37998266,\"lsn\":1279470516648,\"xmin\":null},\"op\":\"c\",\"ts_ms\":1655777436228,\"transaction\":null}}";
//	private static String UpdateMessage_PK = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"col_bpchar\"},{\"type\":\"string\",\"optional\":false,\"field\":\"col_varchar\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_float8\"},{\"type\":\"int32\",\"optional\":true,\"field\":\"col_int4\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_numeric\"},{\"type\":\"int16\",\"optional\":true,\"field\":\"col_int2\"},{\"type\":\"string\",\"optional\":true,\"field\":\"col_text\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"col_timestamp\"}],\"optional\":true,\"name\":\"REV_msa_mylgdb.imcsuser.hj_test_table.Value\",\"field\":\"before\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"col_bpchar\"},{\"type\":\"string\",\"optional\":false,\"field\":\"col_varchar\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_float8\"},{\"type\":\"int32\",\"optional\":true,\"field\":\"col_int4\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_numeric\"},{\"type\":\"int16\",\"optional\":true,\"field\":\"col_int2\"},{\"type\":\"string\",\"optional\":true,\"field\":\"col_text\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"col_timestamp\"}],\"optional\":true,\"name\":\"REV_msa_mylgdb.imcsuser.hj_test_table.Value\",\"field\":\"after\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"version\"},{\"type\":\"string\",\"optional\":false,\"field\":\"connector\"},{\"type\":\"string\",\"optional\":false,\"field\":\"name\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"ts_ms\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.data.Enum\",\"version\":1,\"parameters\":{\"allowed\":\"true,last,false,incremental\"},\"default\":\"false\",\"field\":\"snapshot\"},{\"type\":\"string\",\"optional\":false,\"field\":\"db\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sequence\"},{\"type\":\"string\",\"optional\":false,\"field\":\"schema\"},{\"type\":\"string\",\"optional\":false,\"field\":\"table\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"txId\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"lsn\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"xmin\"}],\"optional\":false,\"name\":\"io.debezium.connector.postgresql.Source\",\"field\":\"source\"},{\"type\":\"string\",\"optional\":false,\"field\":\"op\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_ms\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"id\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"total_order\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"data_collection_order\"}],\"optional\":true,\"field\":\"transaction\"}],\"optional\":false,\"name\":\"REV_msa_mylgdb.imcsuser.hj_test_table.Envelope\"},\"payload\":{\"before\":{\"col_bpchar\":\"t\",\"col_varchar\":\"test\",\"col_float8\":1111.1111,\"col_int4\":1111,\"col_numeric\":1111.0,\"col_int2\":1111,\"col_text\":null,\"col_timestamp\":1655812728857},\"after\":{\"col_bpchar\":\"t\",\"col_varchar\":\"test\",\"col_float8\":2222.2222,\"col_int4\":2222,\"col_numeric\":1111.0,\"col_int2\":2222,\"col_text\":null,\"col_timestamp\":1655812731670},\"source\":{\"version\":\"1.9.0.Final\",\"connector\":\"postgresql\",\"name\":\"REV_msa_mylgdb\",\"ts_ms\":1655780331672,\"snapshot\":\"false\",\"db\":\"msa_mylgdb\",\"sequence\":\"[\\\"1279548720008\\\",\\\"1279548728592\\\"]\",\"schema\":\"imcsuser\",\"table\":\"hj_test_table\",\"txId\":38033428,\"lsn\":1279548728592,\"xmin\":null},\"op\":\"u\",\"ts_ms\":1655780331784,\"transaction\":null}}";
//	private static String DeleteMessage_PK = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"col_bpchar\"},{\"type\":\"string\",\"optional\":false,\"field\":\"col_varchar\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_float8\"},{\"type\":\"int32\",\"optional\":true,\"field\":\"col_int4\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_numeric\"},{\"type\":\"int16\",\"optional\":true,\"field\":\"col_int2\"},{\"type\":\"string\",\"optional\":true,\"field\":\"col_text\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"col_timestamp\"}],\"optional\":true,\"name\":\"REV_msa_mylgdb.imcsuser.hj_test_table.Value\",\"field\":\"before\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"col_bpchar\"},{\"type\":\"string\",\"optional\":false,\"field\":\"col_varchar\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_float8\"},{\"type\":\"int32\",\"optional\":true,\"field\":\"col_int4\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_numeric\"},{\"type\":\"int16\",\"optional\":true,\"field\":\"col_int2\"},{\"type\":\"string\",\"optional\":true,\"field\":\"col_text\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"col_timestamp\"}],\"optional\":true,\"name\":\"REV_msa_mylgdb.imcsuser.hj_test_table.Value\",\"field\":\"after\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"version\"},{\"type\":\"string\",\"optional\":false,\"field\":\"connector\"},{\"type\":\"string\",\"optional\":false,\"field\":\"name\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"ts_ms\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.data.Enum\",\"version\":1,\"parameters\":{\"allowed\":\"true,last,false,incremental\"},\"default\":\"false\",\"field\":\"snapshot\"},{\"type\":\"string\",\"optional\":false,\"field\":\"db\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sequence\"},{\"type\":\"string\",\"optional\":false,\"field\":\"schema\"},{\"type\":\"string\",\"optional\":false,\"field\":\"table\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"txId\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"lsn\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"xmin\"}],\"optional\":false,\"name\":\"io.debezium.connector.postgresql.Source\",\"field\":\"source\"},{\"type\":\"string\",\"optional\":false,\"field\":\"op\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_ms\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"id\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"total_order\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"data_collection_order\"}],\"optional\":true,\"field\":\"transaction\"}],\"optional\":false,\"name\":\"REV_msa_mylgdb.imcsuser.hj_test_table.Envelope\"},\"payload\":{\"before\":{\"col_bpchar\":\"t\",\"col_varchar\":\"test\",\"col_float8\":null,\"col_int4\":null,\"col_numeric\":null,\"col_int2\":null,\"col_text\":null,\"col_timestamp\":null},\"after\":null,\"source\":{\"version\":\"1.9.0.Final\",\"connector\":\"postgresql\",\"name\":\"REV_msa_mylgdb\",\"ts_ms\":1655777677289,\"snapshot\":\"false\",\"db\":\"msa_mylgdb\",\"sequence\":\"[\\\"1279477831072\\\",\\\"1279477831536\\\"]\",\"schema\":\"imcsuser\",\"table\":\"hj_test_table\",\"txId\":38001280,\"lsn\":1279477831536,\"xmin\":null},\"op\":\"d\",\"ts_ms\":1655777677835,\"transaction\":null}}";
//	private static final String pkList = "col_bpchar,col_varchar";
//	ObjectMapper objectMapper = DebeziumDatatypeConvertorHelper.objectMapper;
//	IDatatypeConverter convertor = new DebeziumPostgresToOracleDatatypeConverter(DebeziumVersion.V1_9_6);
//
//	@Test
//	void testPKTablePostgresToOracleInsert() throws InvalidSyncMessageRequestException, JsonProcessingException {
//
//		String after = objectMapper.writeValueAsString(JsonPath.read(InsertMessage_PK, "$.payload.after"));
//		System.out.println("======== testPKTablePostgresToOracleInsert");
//		System.out.println("\n====== Postgres after");
//		System.out.println(after);
//
//		SyncRequestMessage syncRequestMessage = objectMapper.readValue(InsertMessage_PK, SyncRequestMessage.class);
//		Assertions.assertNotNull(syncRequestMessage);
//
//		DefaultDebeziumToPostgresDatatypeConverter.ExecutorSqlData sqlData = convertor.getSqlRedo(syncRequestMessage, TaskExecuteCommand.of(SyncRequestEntity.of("vod_programming", "HJ_TEST_TABLE", pkList, false)));
//		DebeziumDatatypeConvertorHelper.printSqlDate(sqlData);
//
//		Assertions.assertEquals(
//				"INSERT INTO VOD_PROGRAMMING.HJ_TEST_TABLE(COL_BPCHAR,COL_VARCHAR,COL_FLOAT8,COL_INT4,COL_NUMERIC,COL_INT2,COL_TEXT,COL_TIMESTAMP) VALUES (?,?,?,?,?,?,?,?)",
//				sqlData.getSql());
//
//		List<String> cols = Arrays.stream("col_bpchar, col_varchar, col_float8, col_int4, col_numeric, col_int2, col_text, col_timestamp"
//				.split(", ")).collect(Collectors.toList());
//		Assertions.assertEquals(cols, sqlData.getColumnOrder());
//
//
//		Assertions.assertEquals("'t'", sqlData.getParams().get("col_bpchar"));
//		Assertions.assertEquals("'test2'", sqlData.getParams().get("col_varchar"));
//		Assertions.assertEquals("2222.2222", sqlData.getParams().get("col_float8"));
//		Assertions.assertEquals("2222", sqlData.getParams().get("col_int4"));
//		Assertions.assertEquals("1111.0", sqlData.getParams().get("col_numeric"));
//		Assertions.assertEquals("2222", sqlData.getParams().get("col_int2"));
//		Assertions.assertEquals(null, sqlData.getParams().get("col_text"));
//		Assertions.assertEquals("to_timestamp('06/21/2022 11:10:36.076', 'mm/dd/yyyy hh24:mi:ss.ff3')", sqlData.getParams().get("col_timestamp"));
//	}
//
//
//	@Test
//	void testPKTablePostgresToOracleUpdate() throws InvalidSyncMessageRequestException, JsonProcessingException {
//		String before = objectMapper.writeValueAsString(JsonPath.read(UpdateMessage_PK, "$.payload.before"));
//		String after = objectMapper.writeValueAsString(JsonPath.read(UpdateMessage_PK, "$.payload.after"));
//		System.out.println("======== testPKTablePostgresToOracleUpdate");
//		System.out.println("\n====== Postgres before");
//		System.out.println(before);
//
//		System.out.println("\n====== Postgres after");
//		System.out.println(after);
//
//		SyncRequestMessage syncRequestMessage = objectMapper.readValue(UpdateMessage_PK, SyncRequestMessage.class);
//		Assertions.assertNotNull(syncRequestMessage);
//
//		DefaultDebeziumToPostgresDatatypeConverter.ExecutorSqlData sqlData = convertor.getSqlRedo(syncRequestMessage, TaskExecuteCommand.of(SyncRequestEntity.of("vod_programming", "HJ_TEST_TABLE", pkList, true)));
//		DebeziumDatatypeConvertorHelper.printSqlDate(sqlData);
//
//		Assertions.assertEquals(
//				"UPDATE VOD_PROGRAMMING.HJ_TEST_TABLE SET COL_FLOAT8=?,COL_INT4=?,COL_NUMERIC=?,COL_INT2=?,COL_TEXT=?,COL_TIMESTAMP=? WHERE COL_BPCHAR=? and COL_VARCHAR=?",
//				sqlData.getSql());
//
//		List<String> cols = Arrays.stream("col_float8, col_int4, col_numeric, col_int2, col_text, col_timestamp, COL_BPCHAR, COL_VARCHAR"
//				.split(", ")).collect(Collectors.toList());
//		Assertions.assertEquals(cols, sqlData.getColumnOrder());
//
//		// After (Oracle)
//		Assertions.assertEquals("'t'", sqlData.getParams().get("COL_BPCHAR"));
//		Assertions.assertEquals("'test'", sqlData.getParams().get("COL_VARCHAR"));
//		Assertions.assertEquals("1111.1111", sqlData.getParams().get("COL_FLOAT8"));
//		Assertions.assertEquals("1111", sqlData.getParams().get("COL_INT4"));
//		Assertions.assertEquals("1111.0", sqlData.getParams().get("COL_NUMERIC"));
//		Assertions.assertEquals("1111", sqlData.getParams().get("COL_INT2"));
//		Assertions.assertEquals(null, sqlData.getParams().get("COL_TEXT"));
//		Assertions.assertEquals("to_timestamp('06/21/2022 11:58:48.857', 'mm/dd/yyyy hh24:mi:ss.ff3')", sqlData.getParams().get("COL_TIMESTAMP"));
//
//		// Before (PG)
//		Assertions.assertEquals("'t'", sqlData.getParams().get("col_bpchar"));
//		Assertions.assertEquals("'test'", sqlData.getParams().get("col_varchar"));
//		Assertions.assertEquals("2222.2222", sqlData.getParams().get("col_float8"));
//		Assertions.assertEquals("2222", sqlData.getParams().get("col_int4"));
//		Assertions.assertEquals("1111.0", sqlData.getParams().get("col_numeric"));
//		Assertions.assertEquals("2222", sqlData.getParams().get("col_int2"));
//		Assertions.assertEquals(null, sqlData.getParams().get("col_text"));
//		Assertions.assertEquals("to_timestamp('06/21/2022 11:58:51.670', 'mm/dd/yyyy hh24:mi:ss.ff3')", sqlData.getParams().get("col_timestamp"));
//	}
//
//	@Test
//	void testPKTablePostgresToOracleDelete() throws InvalidSyncMessageRequestException, JsonProcessingException {
//		String before = objectMapper.writeValueAsString(JsonPath.read(DeleteMessage_PK, "$.payload.before"));
//		System.out.println("======== testPKTablePostgresToOracleDelete");
//		System.out.println("\n====== Postgres before");
//		System.out.println(before);
//
//		SyncRequestMessage syncRequestMessage = objectMapper.readValue(DeleteMessage_PK, SyncRequestMessage.class);
//		Assertions.assertNotNull(syncRequestMessage);
//
//		DefaultDebeziumToPostgresDatatypeConverter.ExecutorSqlData sqlData = convertor.getSqlRedo(syncRequestMessage, TaskExecuteCommand.of(SyncRequestEntity.of("vod_programming", "HJ_TEST_TABLE", pkList, true)));
//		DebeziumDatatypeConvertorHelper.printSqlDate(sqlData);
//
//		Assertions.assertEquals(
//				"DELETE FROM VOD_PROGRAMMING.HJ_TEST_TABLE WHERE COL_BPCHAR=? and COL_VARCHAR=?",
//				sqlData.getSql());
//
//		List<String> cols = Arrays.stream("COL_BPCHAR, COL_VARCHAR".split(", ")).collect(Collectors.toList());
//		Assertions.assertEquals(cols, sqlData.getColumnOrder());
//
//
//		Assertions.assertEquals("'t'", sqlData.getParams().get("COL_BPCHAR"));
//		Assertions.assertEquals("'test'", sqlData.getParams().get("COL_VARCHAR"));
//	}
//}
