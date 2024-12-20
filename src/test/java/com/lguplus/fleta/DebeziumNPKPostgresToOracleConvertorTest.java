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
//public class DebeziumNPKPostgresToOracleConvertorTest {
//	private static String pg_InsertMessage_NPK = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":true,\"field\":\"col_bpchar\"},{\"type\":\"string\",\"optional\":true,\"field\":\"col_varchar\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_float8\"},{\"type\":\"int32\",\"optional\":true,\"field\":\"col_int4\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_numeric\"},{\"type\":\"int16\",\"optional\":true,\"field\":\"col_int2\"},{\"type\":\"string\",\"optional\":true,\"field\":\"col_text\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"col_timestamp\"}],\"optional\":true,\"name\":\"REV_msa_mylgdb.imcsuser.hj_test_table_npk.Value\",\"field\":\"before\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":true,\"field\":\"col_bpchar\"},{\"type\":\"string\",\"optional\":true,\"field\":\"col_varchar\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_float8\"},{\"type\":\"int32\",\"optional\":true,\"field\":\"col_int4\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_numeric\"},{\"type\":\"int16\",\"optional\":true,\"field\":\"col_int2\"},{\"type\":\"string\",\"optional\":true,\"field\":\"col_text\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"col_timestamp\"}],\"optional\":true,\"name\":\"REV_msa_mylgdb.imcsuser.hj_test_table_npk.Value\",\"field\":\"after\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"version\"},{\"type\":\"string\",\"optional\":false,\"field\":\"connector\"},{\"type\":\"string\",\"optional\":false,\"field\":\"name\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"ts_ms\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.data.Enum\",\"version\":1,\"parameters\":{\"allowed\":\"true,last,false,incremental\"},\"default\":\"false\",\"field\":\"snapshot\"},{\"type\":\"string\",\"optional\":false,\"field\":\"db\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sequence\"},{\"type\":\"string\",\"optional\":false,\"field\":\"schema\"},{\"type\":\"string\",\"optional\":false,\"field\":\"table\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"txId\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"lsn\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"xmin\"}],\"optional\":false,\"name\":\"io.debezium.connector.postgresql.Source\",\"field\":\"source\"},{\"type\":\"string\",\"optional\":false,\"field\":\"op\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_ms\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"id\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"total_order\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"data_collection_order\"}],\"optional\":true,\"field\":\"transaction\"}],\"optional\":false,\"name\":\"REV_msa_mylgdb.imcsuser.hj_test_table_npk.Envelope\"},\"payload\":{\"before\":null,\"after\":{\"col_bpchar\":\"t\",\"col_varchar\":\"test\",\"col_float8\":1111.1111,\"col_int4\":1111,\"col_numeric\":1111.0,\"col_int2\":1111,\"col_text\":null,\"col_timestamp\":1655811474713},\"source\":{\"version\":\"1.9.0.Final\",\"connector\":\"postgresql\",\"name\":\"REV_msa_mylgdb\",\"ts_ms\":1655779074714,\"snapshot\":\"false\",\"db\":\"msa_mylgdb\",\"sequence\":\"[\\\"1279516173672\\\",\\\"1279516177792\\\"]\",\"schema\":\"imcsuser\",\"table\":\"hj_test_table_npk\",\"txId\":38018338,\"lsn\":1279516177792,\"xmin\":null},\"op\":\"c\",\"ts_ms\":1655779074937,\"transaction\":null}}";
//	private static String pg_UpdateMessage_NPK = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":true,\"field\":\"col_bpchar\"},{\"type\":\"string\",\"optional\":true,\"field\":\"col_varchar\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_float8\"},{\"type\":\"int32\",\"optional\":true,\"field\":\"col_int4\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_numeric\"},{\"type\":\"int16\",\"optional\":true,\"field\":\"col_int2\"},{\"type\":\"string\",\"optional\":true,\"field\":\"col_text\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"col_timestamp\"}],\"optional\":true,\"name\":\"REV_msa_mylgdb.imcsuser.hj_test_table_npk.Value\",\"field\":\"before\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":true,\"field\":\"col_bpchar\"},{\"type\":\"string\",\"optional\":true,\"field\":\"col_varchar\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_float8\"},{\"type\":\"int32\",\"optional\":true,\"field\":\"col_int4\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_numeric\"},{\"type\":\"int16\",\"optional\":true,\"field\":\"col_int2\"},{\"type\":\"string\",\"optional\":true,\"field\":\"col_text\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"col_timestamp\"}],\"optional\":true,\"name\":\"REV_msa_mylgdb.imcsuser.hj_test_table_npk.Value\",\"field\":\"after\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"version\"},{\"type\":\"string\",\"optional\":false,\"field\":\"connector\"},{\"type\":\"string\",\"optional\":false,\"field\":\"name\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"ts_ms\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.data.Enum\",\"version\":1,\"parameters\":{\"allowed\":\"true,last,false,incremental\"},\"default\":\"false\",\"field\":\"snapshot\"},{\"type\":\"string\",\"optional\":false,\"field\":\"db\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sequence\"},{\"type\":\"string\",\"optional\":false,\"field\":\"schema\"},{\"type\":\"string\",\"optional\":false,\"field\":\"table\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"txId\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"lsn\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"xmin\"}],\"optional\":false,\"name\":\"io.debezium.connector.postgresql.Source\",\"field\":\"source\"},{\"type\":\"string\",\"optional\":false,\"field\":\"op\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_ms\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"id\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"total_order\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"data_collection_order\"}],\"optional\":true,\"field\":\"transaction\"}],\"optional\":false,\"name\":\"REV_msa_mylgdb.imcsuser.hj_test_table_npk.Envelope\"},\"payload\":{\"before\":{\"col_bpchar\":\"t\",\"col_varchar\":\"test\",\"col_float8\":1111.1111,\"col_int4\":1111,\"col_numeric\":1111.0,\"col_int2\":1111,\"col_text\":null,\"col_timestamp\":1655811474713},\"after\":{\"col_bpchar\":\"t\",\"col_varchar\":\"test\",\"col_float8\":2222.2222,\"col_int4\":2222,\"col_numeric\":1111.0,\"col_int2\":2222,\"col_text\":null,\"col_timestamp\":1655811486702},\"source\":{\"version\":\"1.9.0.Final\",\"connector\":\"postgresql\",\"name\":\"REV_msa_mylgdb\",\"ts_ms\":1655779086703,\"snapshot\":\"false\",\"db\":\"msa_mylgdb\",\"sequence\":\"[\\\"1279516324784\\\",\\\"1279516338232\\\"]\",\"schema\":\"imcsuser\",\"table\":\"hj_test_table_npk\",\"txId\":38018461,\"lsn\":1279516338232,\"xmin\":null},\"op\":\"u\",\"ts_ms\":1655779086889,\"transaction\":null}}";
//	private static String pg_DeleteMessage_NPK = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":true,\"field\":\"col_bpchar\"},{\"type\":\"string\",\"optional\":true,\"field\":\"col_varchar\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_float8\"},{\"type\":\"int32\",\"optional\":true,\"field\":\"col_int4\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_numeric\"},{\"type\":\"int16\",\"optional\":true,\"field\":\"col_int2\"},{\"type\":\"string\",\"optional\":true,\"field\":\"col_text\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"col_timestamp\"}],\"optional\":true,\"name\":\"REV_msa_mylgdb.imcsuser.hj_test_table_npk.Value\",\"field\":\"before\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":true,\"field\":\"col_bpchar\"},{\"type\":\"string\",\"optional\":true,\"field\":\"col_varchar\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_float8\"},{\"type\":\"int32\",\"optional\":true,\"field\":\"col_int4\"},{\"type\":\"double\",\"optional\":true,\"field\":\"col_numeric\"},{\"type\":\"int16\",\"optional\":true,\"field\":\"col_int2\"},{\"type\":\"string\",\"optional\":true,\"field\":\"col_text\"},{\"type\":\"int64\",\"optional\":true,\"name\":\"org.apache.kafka.connect.data.Timestamp\",\"version\":1,\"field\":\"col_timestamp\"}],\"optional\":true,\"name\":\"REV_msa_mylgdb.imcsuser.hj_test_table_npk.Value\",\"field\":\"after\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"version\"},{\"type\":\"string\",\"optional\":false,\"field\":\"connector\"},{\"type\":\"string\",\"optional\":false,\"field\":\"name\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"ts_ms\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.data.Enum\",\"version\":1,\"parameters\":{\"allowed\":\"true,last,false,incremental\"},\"default\":\"false\",\"field\":\"snapshot\"},{\"type\":\"string\",\"optional\":false,\"field\":\"db\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sequence\"},{\"type\":\"string\",\"optional\":false,\"field\":\"schema\"},{\"type\":\"string\",\"optional\":false,\"field\":\"table\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"txId\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"lsn\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"xmin\"}],\"optional\":false,\"name\":\"io.debezium.connector.postgresql.Source\",\"field\":\"source\"},{\"type\":\"string\",\"optional\":false,\"field\":\"op\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_ms\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"id\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"total_order\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"data_collection_order\"}],\"optional\":true,\"field\":\"transaction\"}],\"optional\":false,\"name\":\"REV_msa_mylgdb.imcsuser.hj_test_table_npk.Envelope\"},\"payload\":{\"before\":{\"col_bpchar\":\"t\",\"col_varchar\":\"test\",\"col_float8\":2222.2222,\"col_int4\":2222,\"col_numeric\":1111.0,\"col_int2\":2222,\"col_text\":null,\"col_timestamp\":1655811486702},\"after\":null,\"source\":{\"version\":\"1.9.0.Final\",\"connector\":\"postgresql\",\"name\":\"REV_msa_mylgdb\",\"ts_ms\":1655779093407,\"snapshot\":\"false\",\"db\":\"msa_mylgdb\",\"sequence\":\"[\\\"1279516435880\\\",\\\"1279516442968\\\"]\",\"schema\":\"imcsuser\",\"table\":\"hj_test_table_npk\",\"txId\":38018539,\"lsn\":1279516442968,\"xmin\":null},\"op\":\"d\",\"ts_ms\":1655779093856,\"transaction\":null}}";
//	private static List<String> pkList = List.of("col_bpchar", "col_varchar");
//	ObjectMapper objectMapper = DebeziumDatatypeConvertorHelper.objectMapper;
//	IDatatypeConverter convertor = new DebeziumPostgresToOracleDatatypeConverter(DebeziumVersion.V1_9_6);
//
//	@Test
//	void testNPKTablePostgresToOracleInsert() throws InvalidSyncMessageRequestException, JsonProcessingException {
//
//		String after = objectMapper.writeValueAsString(JsonPath.read(pg_InsertMessage_NPK, "$.payload.after"));
//		System.out.println("======== testNPKTablePostgresToOracleInsert");
//		System.out.println("\n====== Postgres after");
//		System.out.println(after);
//
//		SyncRequestMessage syncRequestMessage = objectMapper.readValue(pg_InsertMessage_NPK, SyncRequestMessage.class);
//		Assertions.assertNotNull(syncRequestMessage);
//
//		DefaultDebeziumToPostgresDatatypeConverter.ExecutorSqlData sqlData = convertor.getSqlRedo(syncRequestMessage, TaskExecuteCommand.of(SyncRequestEntity.of("vod_programming", "HJ_TEST_TABLE_NPK", null, true)));
//		DebeziumDatatypeConvertorHelper.printSqlDate(sqlData);
//
//		Assertions.assertEquals(
//				"INSERT INTO VOD_PROGRAMMING.HJ_TEST_TABLE_NPK(COL_BPCHAR,COL_VARCHAR,COL_FLOAT8,COL_INT4,COL_NUMERIC,COL_INT2,COL_TEXT,COL_TIMESTAMP) VALUES (?,?,?,?,?,?,?,?)",
//				sqlData.getSql());
//
//		List<String> cols = Arrays.stream("col_bpchar, col_varchar, col_float8, col_int4, col_numeric, col_int2, col_text, col_timestamp"
//				.split(", ")).collect(Collectors.toList());
//		Assertions.assertEquals(cols, sqlData.getColumnOrder());
//
//
//		Assertions.assertEquals("'t'", sqlData.getParams().get("col_bpchar"));
//		Assertions.assertEquals("'test'", sqlData.getParams().get("col_varchar"));
//		Assertions.assertEquals("1111.1111", sqlData.getParams().get("col_float8"));
//		Assertions.assertEquals("1111", sqlData.getParams().get("col_int4"));
//		Assertions.assertEquals("1111.0", sqlData.getParams().get("col_numeric"));
//		Assertions.assertEquals("1111", sqlData.getParams().get("col_int2"));
//		Assertions.assertEquals(null, sqlData.getParams().get("col_text"));
//		Assertions.assertEquals("to_timestamp('06/21/2022 11:37:54.713', 'mm/dd/yyyy hh24:mi:ss.ff3')", sqlData.getParams().get("col_timestamp"));
//	}
//
//
//	@Test
//	void testNPKTablePostgresToOracleUpdate() throws InvalidSyncMessageRequestException, JsonProcessingException {
//		String before = objectMapper.writeValueAsString(JsonPath.read(pg_UpdateMessage_NPK, "$.payload.before"));
//		String after = objectMapper.writeValueAsString(JsonPath.read(pg_UpdateMessage_NPK, "$.payload.after"));
//		System.out.println("======== testNPKTablePostgresToOracleUpdate");
//		System.out.println("\n====== Postgres before");
//		System.out.println(before);
//
//		System.out.println("\n====== Postgres after");
//		System.out.println(after);
//
//		SyncRequestMessage syncRequestMessage = objectMapper.readValue(pg_UpdateMessage_NPK, SyncRequestMessage.class);
//		Assertions.assertNotNull(syncRequestMessage);
//
//		DefaultDebeziumToPostgresDatatypeConverter.ExecutorSqlData sqlData = convertor.getSqlRedo(syncRequestMessage, TaskExecuteCommand.of(SyncRequestEntity.of("vod_programming", "HJ_TEST_TABLE_NPK", null, true)));
//		DebeziumDatatypeConvertorHelper.printSqlDate(sqlData);
//
//		Assertions.assertEquals(
//				"UPDATE VOD_PROGRAMMING.HJ_TEST_TABLE_NPK SET COL_BPCHAR=?,COL_VARCHAR=?,COL_FLOAT8=?,COL_INT4=?,COL_NUMERIC=?,COL_INT2=?,COL_TEXT=?,COL_TIMESTAMP=? WHERE COL_BPCHAR=? and COL_VARCHAR=? and COL_FLOAT8=? and COL_INT4=? and COL_NUMERIC=? and COL_INT2=? and COL_TEXT IS NULL and COL_TIMESTAMP=?",
//				sqlData.getSql());
//
//		List<String> cols = Arrays.stream("col_bpchar, col_varchar, col_float8, col_int4, col_numeric, col_int2, col_text, col_timestamp, COL_BPCHAR, COL_VARCHAR, COL_FLOAT8, COL_INT4, COL_NUMERIC, COL_INT2, COL_TIMESTAMP"
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
//		Assertions.assertEquals("to_timestamp('06/21/2022 11:37:54.713', 'mm/dd/yyyy hh24:mi:ss.ff3')", sqlData.getParams().get("COL_TIMESTAMP"));
//
//		// Before (PG)
//		Assertions.assertEquals("'t'", sqlData.getParams().get("col_bpchar"));
//		Assertions.assertEquals("'test'", sqlData.getParams().get("col_varchar"));
//		Assertions.assertEquals("2222.2222", sqlData.getParams().get("col_float8"));
//		Assertions.assertEquals("2222", sqlData.getParams().get("col_int4"));
//		Assertions.assertEquals("1111.0", sqlData.getParams().get("col_numeric"));
//		Assertions.assertEquals("2222", sqlData.getParams().get("col_int2"));
//		Assertions.assertEquals(null, sqlData.getParams().get("col_text"));
//		Assertions.assertEquals("to_timestamp('06/21/2022 11:38:06.702', 'mm/dd/yyyy hh24:mi:ss.ff3')", sqlData.getParams().get("col_timestamp"));
//	}
//
//	@Test
//	void testNPKTablePostgresToOracleDelete() throws InvalidSyncMessageRequestException, JsonProcessingException {
//		String before = objectMapper.writeValueAsString(JsonPath.read(pg_DeleteMessage_NPK, "$.payload.before"));
//		System.out.println("======== testNPKTablePostgresToOracleDelete");
//		System.out.println("\n====== Postgres before");
//		System.out.println(before);
//
//		SyncRequestMessage syncRequestMessage = objectMapper.readValue(pg_DeleteMessage_NPK, SyncRequestMessage.class);
//		Assertions.assertNotNull(syncRequestMessage);
//
//		DefaultDebeziumToPostgresDatatypeConverter.ExecutorSqlData sqlData = convertor.getSqlRedo(syncRequestMessage, TaskExecuteCommand.of(SyncRequestEntity.of("vod_programming", "HJ_TEST_TABLE_NPK", null, true)));
//		DebeziumDatatypeConvertorHelper.printSqlDate(sqlData);
//
//		Assertions.assertEquals(
//				"DELETE FROM VOD_PROGRAMMING.HJ_TEST_TABLE_NPK WHERE COL_BPCHAR=? and COL_VARCHAR=? and COL_FLOAT8=? and COL_INT4=? and COL_NUMERIC=? and COL_INT2=? and COL_TEXT IS NULL and COL_TIMESTAMP=?",
//				sqlData.getSql());
//
//		List<String> cols = Arrays.stream("COL_BPCHAR, COL_VARCHAR, COL_FLOAT8, COL_INT4, COL_NUMERIC, COL_INT2, COL_TIMESTAMP".split(", ")).collect(Collectors.toList());
//		Assertions.assertEquals(cols, sqlData.getColumnOrder());
//
//
//		//2022-06-21T11:38:06.702
//		Assertions.assertEquals("'t'", sqlData.getParams().get("COL_BPCHAR"));
//		Assertions.assertEquals("'test'", sqlData.getParams().get("COL_VARCHAR"));
//		Assertions.assertEquals("2222.2222", sqlData.getParams().get("COL_FLOAT8"));
//		Assertions.assertEquals("2222", sqlData.getParams().get("COL_INT4"));
//		Assertions.assertEquals("1111.0", sqlData.getParams().get("COL_NUMERIC"));
//		Assertions.assertEquals("2222", sqlData.getParams().get("COL_INT2"));
//		Assertions.assertEquals(null, sqlData.getParams().get("COL_TEXT"));
//		Assertions.assertEquals("to_timestamp('06/21/2022 11:38:06.702', 'mm/dd/yyyy hh24:mi:ss.ff3')", sqlData.getParams().get("COL_TIMESTAMP"));
//	}
//}
