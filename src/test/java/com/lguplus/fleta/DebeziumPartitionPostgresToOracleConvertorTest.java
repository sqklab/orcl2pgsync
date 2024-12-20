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
//public class DebeziumPartitionPostgresToOracleConvertorTest {
//	private static String pg_InsertMessage_Partition = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"struct\",\"fields\":[{\"type\":\"double\",\"optional\":false,\"field\":\"p_idx_sa\"},{\"type\":\"string\",\"optional\":false,\"field\":\"p_idx_mm\"},{\"type\":\"string\",\"optional\":false,\"field\":\"sa_id\"},{\"type\":\"string\",\"optional\":false,\"field\":\"mac_addr\"},{\"type\":\"string\",\"optional\":false,\"field\":\"buy_date\"},{\"type\":\"string\",\"optional\":false,\"field\":\"contents_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"contents_name\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_name\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_kind\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_type\"},{\"type\":\"string\",\"optional\":true,\"field\":\"album_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"hdcontent\"},{\"type\":\"string\",\"optional\":true,\"field\":\"rating_cd\"},{\"type\":\"string\",\"optional\":true,\"field\":\"cp_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"maximum_viewing_length\"},{\"type\":\"string\",\"optional\":true,\"field\":\"series_no\"}],\"optional\":true,\"name\":\"REV_msa_mylgdb.mcustuser.npt_vo_buy_meta.Value\",\"field\":\"before\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"double\",\"optional\":false,\"field\":\"p_idx_sa\"},{\"type\":\"string\",\"optional\":false,\"field\":\"p_idx_mm\"},{\"type\":\"string\",\"optional\":false,\"field\":\"sa_id\"},{\"type\":\"string\",\"optional\":false,\"field\":\"mac_addr\"},{\"type\":\"string\",\"optional\":false,\"field\":\"buy_date\"},{\"type\":\"string\",\"optional\":false,\"field\":\"contents_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"contents_name\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_name\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_kind\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_type\"},{\"type\":\"string\",\"optional\":true,\"field\":\"album_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"hdcontent\"},{\"type\":\"string\",\"optional\":true,\"field\":\"rating_cd\"},{\"type\":\"string\",\"optional\":true,\"field\":\"cp_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"maximum_viewing_length\"},{\"type\":\"string\",\"optional\":true,\"field\":\"series_no\"}],\"optional\":true,\"name\":\"REV_msa_mylgdb.mcustuser.npt_vo_buy_meta.Value\",\"field\":\"after\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"version\"},{\"type\":\"string\",\"optional\":false,\"field\":\"connector\"},{\"type\":\"string\",\"optional\":false,\"field\":\"name\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"ts_ms\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.data.Enum\",\"version\":1,\"parameters\":{\"allowed\":\"true,last,false,incremental\"},\"default\":\"false\",\"field\":\"snapshot\"},{\"type\":\"string\",\"optional\":false,\"field\":\"db\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sequence\"},{\"type\":\"string\",\"optional\":false,\"field\":\"schema\"},{\"type\":\"string\",\"optional\":false,\"field\":\"table\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"txId\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"lsn\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"xmin\"}],\"optional\":false,\"name\":\"io.debezium.connector.postgresql.Source\",\"field\":\"source\"},{\"type\":\"string\",\"optional\":false,\"field\":\"op\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_ms\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"id\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"total_order\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"data_collection_order\"}],\"optional\":true,\"field\":\"transaction\"}],\"optional\":false,\"name\":\"REV_msa_mylgdb.mcustuser.npt_vo_buy_meta.Envelope\"},\"payload\":{\"before\":null,\"after\":{\"p_idx_sa\":0,\"p_idx_mm\":\"0\",\"sa_id\":\"100121250233\",\"mac_addr\":\"v001.2125.0233\",\"buy_date\":\"\",\"contents_id\":\"\",\"contents_name\":\"\",\"product_id\":\"\",\"product_name\":\"\",\"product_kind\":\"\",\"product_type\":\"\",\"album_id\":\"\",\"hdcontent\":\"\",\"rating_cd\":\"\",\"cp_id\":\"\",\"maximum_viewing_length\":\"\",\"series_no\":\"\"},\"source\":{\"version\":\"1.9.4.Final\",\"connector\":\"postgresql\",\"name\":\"REV_msa_mylgdb\",\"ts_ms\":1656641458085,\"snapshot\":\"false\",\"db\":\"msa_mylgdb\",\"sequence\":\"[\\\"1298456060216\\\",\\\"1298456066944\\\"]\",\"schema\":\"mcustuser\",\"table\":\"npt_vo_buy_meta_nbuy_meta_ls_00_00\",\"txId\":45937853,\"lsn\":1298456066944,\"xmin\":null},\"op\":\"c\",\"ts_ms\":1656641458279,\"transaction\":null}}";
//	private static String pg_UpdateMessage_Partition = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"struct\",\"fields\":[{\"type\":\"double\",\"optional\":false,\"field\":\"p_idx_sa\"},{\"type\":\"string\",\"optional\":false,\"field\":\"p_idx_mm\"},{\"type\":\"string\",\"optional\":false,\"field\":\"sa_id\"},{\"type\":\"string\",\"optional\":false,\"field\":\"mac_addr\"},{\"type\":\"string\",\"optional\":false,\"field\":\"buy_date\"},{\"type\":\"string\",\"optional\":false,\"field\":\"contents_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"contents_name\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_name\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_kind\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_type\"},{\"type\":\"string\",\"optional\":true,\"field\":\"album_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"hdcontent\"},{\"type\":\"string\",\"optional\":true,\"field\":\"rating_cd\"},{\"type\":\"string\",\"optional\":true,\"field\":\"cp_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"maximum_viewing_length\"},{\"type\":\"string\",\"optional\":true,\"field\":\"series_no\"}],\"optional\":true,\"name\":\"REV_msa_mylgdb.mcustuser.npt_vo_buy_meta.Value\",\"field\":\"before\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"double\",\"optional\":false,\"field\":\"p_idx_sa\"},{\"type\":\"string\",\"optional\":false,\"field\":\"p_idx_mm\"},{\"type\":\"string\",\"optional\":false,\"field\":\"sa_id\"},{\"type\":\"string\",\"optional\":false,\"field\":\"mac_addr\"},{\"type\":\"string\",\"optional\":false,\"field\":\"buy_date\"},{\"type\":\"string\",\"optional\":false,\"field\":\"contents_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"contents_name\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_name\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_kind\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_type\"},{\"type\":\"string\",\"optional\":true,\"field\":\"album_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"hdcontent\"},{\"type\":\"string\",\"optional\":true,\"field\":\"rating_cd\"},{\"type\":\"string\",\"optional\":true,\"field\":\"cp_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"maximum_viewing_length\"},{\"type\":\"string\",\"optional\":true,\"field\":\"series_no\"}],\"optional\":true,\"name\":\"REV_msa_mylgdb.mcustuser.npt_vo_buy_meta.Value\",\"field\":\"after\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"version\"},{\"type\":\"string\",\"optional\":false,\"field\":\"connector\"},{\"type\":\"string\",\"optional\":false,\"field\":\"name\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"ts_ms\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.data.Enum\",\"version\":1,\"parameters\":{\"allowed\":\"true,last,false,incremental\"},\"default\":\"false\",\"field\":\"snapshot\"},{\"type\":\"string\",\"optional\":false,\"field\":\"db\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sequence\"},{\"type\":\"string\",\"optional\":false,\"field\":\"schema\"},{\"type\":\"string\",\"optional\":false,\"field\":\"table\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"txId\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"lsn\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"xmin\"}],\"optional\":false,\"name\":\"io.debezium.connector.postgresql.Source\",\"field\":\"source\"},{\"type\":\"string\",\"optional\":false,\"field\":\"op\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_ms\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"id\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"total_order\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"data_collection_order\"}],\"optional\":true,\"field\":\"transaction\"}],\"optional\":false,\"name\":\"REV_msa_mylgdb.mcustuser.npt_vo_buy_meta.Envelope\"},\"payload\":{\"before\":{\"p_idx_sa\":0.0,\"p_idx_mm\":\"0\",\"sa_id\":\"100121250233\",\"mac_addr\":\"v001.2125.0233\",\"buy_date\":\"\",\"contents_id\":\"\",\"contents_name\":\"\",\"product_id\":\"\",\"product_name\":\"\",\"product_kind\":\"\",\"product_type\":\"\",\"album_id\":\"\",\"hdcontent\":\"\",\"rating_cd\":\"\",\"cp_id\":\"\",\"maximum_viewing_length\":\"\",\"series_no\":\"\"},\"after\":{\"p_idx_sa\":0.0,\"p_idx_mm\":\"0\",\"sa_id\":\"100121250233\",\"mac_addr\":\"v001.2125.0233\",\"buy_date\":\"\",\"contents_id\":\"\",\"contents_name\":\"\",\"product_id\":\"\",\"product_name\":\"\",\"product_kind\":\"\",\"product_type\":\"\",\"album_id\":\"\",\"hdcontent\":\"\",\"rating_cd\":\"\",\"cp_id\":\"\",\"maximum_viewing_length\":\"\",\"series_no\":\"\"},\"source\":{\"version\":\"1.9.4.Final\",\"connector\":\"postgresql\",\"name\":\"REV_msa_mylgdb\",\"ts_ms\":1656641507045,\"snapshot\":\"false\",\"db\":\"msa_mylgdb\",\"sequence\":\"[\\\"1298456461056\\\",\\\"1298456461096\\\"]\",\"schema\":\"mcustuser\",\"table\":\"npt_vo_buy_meta_nbuy_meta_ls_00_00\",\"txId\":45938203,\"lsn\":1298456461096,\"xmin\":null},\"op\":\"u\",\"ts_ms\":1656641507078,\"transaction\":null}}";
//	private static String pg_DeleteMessage_Partition = "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"struct\",\"fields\":[{\"type\":\"double\",\"optional\":false,\"field\":\"p_idx_sa\"},{\"type\":\"string\",\"optional\":false,\"field\":\"p_idx_mm\"},{\"type\":\"string\",\"optional\":false,\"field\":\"sa_id\"},{\"type\":\"string\",\"optional\":false,\"field\":\"mac_addr\"},{\"type\":\"string\",\"optional\":false,\"field\":\"buy_date\"},{\"type\":\"string\",\"optional\":false,\"field\":\"contents_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"contents_name\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_name\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_kind\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_type\"},{\"type\":\"string\",\"optional\":true,\"field\":\"album_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"hdcontent\"},{\"type\":\"string\",\"optional\":true,\"field\":\"rating_cd\"},{\"type\":\"string\",\"optional\":true,\"field\":\"cp_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"maximum_viewing_length\"},{\"type\":\"string\",\"optional\":true,\"field\":\"series_no\"}],\"optional\":true,\"name\":\"REV_msa_mylgdb.mcustuser.npt_vo_buy_meta.Value\",\"field\":\"before\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"double\",\"optional\":false,\"field\":\"p_idx_sa\"},{\"type\":\"string\",\"optional\":false,\"field\":\"p_idx_mm\"},{\"type\":\"string\",\"optional\":false,\"field\":\"sa_id\"},{\"type\":\"string\",\"optional\":false,\"field\":\"mac_addr\"},{\"type\":\"string\",\"optional\":false,\"field\":\"buy_date\"},{\"type\":\"string\",\"optional\":false,\"field\":\"contents_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"contents_name\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_name\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_kind\"},{\"type\":\"string\",\"optional\":true,\"field\":\"product_type\"},{\"type\":\"string\",\"optional\":true,\"field\":\"album_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"hdcontent\"},{\"type\":\"string\",\"optional\":true,\"field\":\"rating_cd\"},{\"type\":\"string\",\"optional\":true,\"field\":\"cp_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"maximum_viewing_length\"},{\"type\":\"string\",\"optional\":true,\"field\":\"series_no\"}],\"optional\":true,\"name\":\"REV_msa_mylgdb.mcustuser.npt_vo_buy_meta.Value\",\"field\":\"after\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"version\"},{\"type\":\"string\",\"optional\":false,\"field\":\"connector\"},{\"type\":\"string\",\"optional\":false,\"field\":\"name\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"ts_ms\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.data.Enum\",\"version\":1,\"parameters\":{\"allowed\":\"true,last,false,incremental\"},\"default\":\"false\",\"field\":\"snapshot\"},{\"type\":\"string\",\"optional\":false,\"field\":\"db\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sequence\"},{\"type\":\"string\",\"optional\":false,\"field\":\"schema\"},{\"type\":\"string\",\"optional\":false,\"field\":\"table\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"txId\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"lsn\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"xmin\"}],\"optional\":false,\"name\":\"io.debezium.connector.postgresql.Source\",\"field\":\"source\"},{\"type\":\"string\",\"optional\":false,\"field\":\"op\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_ms\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"id\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"total_order\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"data_collection_order\"}],\"optional\":true,\"field\":\"transaction\"}],\"optional\":false,\"name\":\"REV_msa_mylgdb.mcustuser.npt_vo_buy_meta.Envelope\"},\"payload\":{\"before\":{\"p_idx_sa\":0.0,\"p_idx_mm\":\"0\",\"sa_id\":\"100121250233\",\"mac_addr\":\"v001.2125.0233\",\"buy_date\":\"\",\"contents_id\":\"\",\"contents_name\":\"\",\"product_id\":\"\",\"product_name\":\"\",\"product_kind\":\"\",\"product_type\":\"\",\"album_id\":\"\",\"hdcontent\":\"\",\"rating_cd\":\"\",\"cp_id\":\"\",\"maximum_viewing_length\":\"\",\"series_no\":\"\"},\"after\":null,\"source\":{\"version\":\"1.9.4.Final\",\"connector\":\"postgresql\",\"name\":\"REV_msa_mylgdb\",\"ts_ms\":1656641532548,\"snapshot\":\"false\",\"db\":\"msa_mylgdb\",\"sequence\":\"[\\\"1298460145304\\\",\\\"1298460150144\\\"]\",\"schema\":\"mcustuser\",\"table\":\"npt_vo_buy_meta_nbuy_meta_ls_00_00\",\"txId\":45938415,\"lsn\":1298460150144,\"xmin\":null},\"op\":\"d\",\"ts_ms\":1656641532679,\"transaction\":null}}";
//	private static List<String> pkList = List.of("p_idx_sa", "p_idx_mm,sa_id", "mac_addr", "buy_date", "contents_id");
//	ObjectMapper objectMapper = DebeziumDatatypeConvertorHelper.objectMapper;
//	IDatatypeConverter convertor = new DebeziumPostgresToOracleDatatypeConverter(DebeziumVersion.V1_9_6);
//
//	@Test
//	void testPartitionTablePostgresToOracleInsert() throws InvalidSyncMessageRequestException, JsonProcessingException {
//
//		String after = objectMapper.writeValueAsString(JsonPath.read(pg_InsertMessage_Partition, "$.payload.after"));
//		System.out.println("======== testPartitionTablePostgresToOracleInsert");
//		System.out.println("\n====== Postgres after");
//		System.out.println(after);
//
//		SyncRequestMessage syncRequestMessage = objectMapper.readValue(pg_InsertMessage_Partition, SyncRequestMessage.class);
//		Assertions.assertNotNull(syncRequestMessage);
//
//		DefaultDebeziumToPostgresDatatypeConverter.ExecutorSqlData sqlData = convertor.getSqlRedo(syncRequestMessage, TaskExecuteCommand.of(SyncRequestEntity.of("vod_programming", "HJ_TEST_TABLE_Partition", null, true)));
//		DebeziumDatatypeConvertorHelper.printSqlDate(sqlData);
//
//		Assertions.assertEquals(
//				"INSERT INTO VOD_PROGRAMMING.HJ_TEST_TABLE_PARTITION(P_IDX_SA,P_IDX_MM,SA_ID,MAC_ADDR,BUY_DATE,CONTENTS_ID,CONTENTS_NAME,PRODUCT_ID,PRODUCT_NAME,PRODUCT_KIND,PRODUCT_TYPE,ALBUM_ID,HDCONTENT,RATING_CD,CP_ID,MAXIMUM_VIEWING_LENGTH,SERIES_NO) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
//				sqlData.getSql());
//
//		List<String> cols = Arrays.stream("p_idx_sa, p_idx_mm, sa_id, mac_addr, buy_date, contents_id, contents_name, product_id, product_name, product_kind, product_type, album_id, hdcontent, rating_cd, cp_id, maximum_viewing_length, series_no"
//				.split(", ")).collect(Collectors.toList());
//		Assertions.assertEquals(cols, sqlData.getColumnOrder());
//
//
//		Assertions.assertEquals("0", sqlData.getParams().get("p_idx_sa"));
//		Assertions.assertEquals("'0'", sqlData.getParams().get("p_idx_mm"));
//		Assertions.assertEquals("'100121250233'", sqlData.getParams().get("sa_id"));
//		Assertions.assertEquals("'v001.2125.0233'", sqlData.getParams().get("mac_addr"));
//		Assertions.assertEquals("''", sqlData.getParams().get("buy_date"));
//		Assertions.assertEquals("''", sqlData.getParams().get("contents_id"));
//	}
//
//
//	@Test
//	void testPartitionTablePostgresToOracleUpdate() throws InvalidSyncMessageRequestException, JsonProcessingException {
//		String before = objectMapper.writeValueAsString(JsonPath.read(pg_UpdateMessage_Partition, "$.payload.before"));
//		String after = objectMapper.writeValueAsString(JsonPath.read(pg_UpdateMessage_Partition, "$.payload.after"));
//		System.out.println("======== testPartitionTablePostgresToOracleUpdate");
//		System.out.println("\n====== Postgres before");
//		System.out.println(before);
//
//		System.out.println("\n====== Postgres after");
//		System.out.println(after);
//
//		SyncRequestMessage syncRequestMessage = objectMapper.readValue(pg_UpdateMessage_Partition, SyncRequestMessage.class);
//		Assertions.assertNotNull(syncRequestMessage);
//
//		DefaultDebeziumToPostgresDatatypeConverter.ExecutorSqlData sqlData = convertor.getSqlRedo(syncRequestMessage, TaskExecuteCommand.of(SyncRequestEntity.of("vod_programming", "HJ_TEST_TABLE_Partition", null, true)));
//		DebeziumDatatypeConvertorHelper.printSqlDate(sqlData);
//
//		Assertions.assertEquals(
//				"UPDATE VOD_PROGRAMMING.HJ_TEST_TABLE_PARTITION SET P_IDX_SA=?,P_IDX_MM=?,SA_ID=?,MAC_ADDR=?,BUY_DATE=?,CONTENTS_ID=?,CONTENTS_NAME=?,PRODUCT_ID=?,PRODUCT_NAME=?,PRODUCT_KIND=?,PRODUCT_TYPE=?,ALBUM_ID=?,HDCONTENT=?,RATING_CD=?,CP_ID=?,MAXIMUM_VIEWING_LENGTH=?,SERIES_NO=? WHERE P_IDX_SA=? and P_IDX_MM=? and SA_ID=? and MAC_ADDR=? and BUY_DATE=? and CONTENTS_ID=? and CONTENTS_NAME=? and PRODUCT_ID=? and PRODUCT_NAME=? and PRODUCT_KIND=? and PRODUCT_TYPE=? and ALBUM_ID=? and HDCONTENT=? and RATING_CD=? and CP_ID=? and MAXIMUM_VIEWING_LENGTH=? and SERIES_NO=?",
//				sqlData.getSql());
//
//		List<String> cols = Arrays.stream("p_idx_sa, p_idx_mm, sa_id, mac_addr, buy_date, contents_id, contents_name, product_id, product_name, product_kind, product_type, album_id, hdcontent, rating_cd, cp_id, maximum_viewing_length, series_no, P_IDX_SA, P_IDX_MM, SA_ID, MAC_ADDR, BUY_DATE, CONTENTS_ID, CONTENTS_NAME, PRODUCT_ID, PRODUCT_NAME, PRODUCT_KIND, PRODUCT_TYPE, ALBUM_ID, HDCONTENT, RATING_CD, CP_ID, MAXIMUM_VIEWING_LENGTH, SERIES_NO"
//				.split(", ")).collect(Collectors.toList());
//		Assertions.assertEquals(cols, sqlData.getColumnOrder());
//
//		// After (Oracle)
//		Assertions.assertEquals("0.0", sqlData.getParams().get("p_idx_sa"));
//		Assertions.assertEquals("'0'", sqlData.getParams().get("p_idx_mm"));
//		Assertions.assertEquals("'100121250233'", sqlData.getParams().get("sa_id"));
//		Assertions.assertEquals("'v001.2125.0233'", sqlData.getParams().get("mac_addr"));
//		Assertions.assertEquals("''", sqlData.getParams().get("buy_date"));
//		Assertions.assertEquals("''", sqlData.getParams().get("contents_id"));
//
//		// Before (PG)
//		Assertions.assertEquals("0.0", sqlData.getParams().get("P_IDX_SA"));
//		Assertions.assertEquals("'0'", sqlData.getParams().get("P_IDX_MM"));
//		Assertions.assertEquals("'100121250233'", sqlData.getParams().get("SA_ID"));
//		Assertions.assertEquals("'v001.2125.0233'", sqlData.getParams().get("MAC_ADDR"));
//		Assertions.assertEquals("''", sqlData.getParams().get("BUY_DATE"));
//		Assertions.assertEquals("''", sqlData.getParams().get("CONTENTS_ID"));
//	}
//
//	@Test
//	void testPartitionTablePostgresToOracleDelete() throws InvalidSyncMessageRequestException, JsonProcessingException {
//		String before = objectMapper.writeValueAsString(JsonPath.read(pg_DeleteMessage_Partition, "$.payload.before"));
//		System.out.println("======== testPartitionTablePostgresToOracleDelete");
//		System.out.println("\n====== Postgres before");
//		System.out.println(before);
//
//		SyncRequestMessage syncRequestMessage = objectMapper.readValue(pg_DeleteMessage_Partition, SyncRequestMessage.class);
//		Assertions.assertNotNull(syncRequestMessage);
//
//		DefaultDebeziumToPostgresDatatypeConverter.ExecutorSqlData sqlData = convertor.getSqlRedo(syncRequestMessage, TaskExecuteCommand.of(SyncRequestEntity.of("vod_programming", "HJ_TEST_TABLE_Partition", null, true)));
//		DebeziumDatatypeConvertorHelper.printSqlDate(sqlData);
//
//		Assertions.assertEquals(
//				"DELETE FROM VOD_PROGRAMMING.HJ_TEST_TABLE_PARTITION WHERE P_IDX_SA=? and P_IDX_MM=? and SA_ID=? and MAC_ADDR=? and BUY_DATE=? and CONTENTS_ID=? and CONTENTS_NAME=? and PRODUCT_ID=? and PRODUCT_NAME=? and PRODUCT_KIND=? and PRODUCT_TYPE=? and ALBUM_ID=? and HDCONTENT=? and RATING_CD=? and CP_ID=? and MAXIMUM_VIEWING_LENGTH=? and SERIES_NO=?",
//				sqlData.getSql());
//
//		List<String> cols = Arrays.stream("P_IDX_SA, P_IDX_MM, SA_ID, MAC_ADDR, BUY_DATE, CONTENTS_ID, CONTENTS_NAME, PRODUCT_ID, PRODUCT_NAME, PRODUCT_KIND, PRODUCT_TYPE, ALBUM_ID, HDCONTENT, RATING_CD, CP_ID, MAXIMUM_VIEWING_LENGTH, SERIES_NO".split(", ")).collect(Collectors.toList());
//		Assertions.assertEquals(cols, sqlData.getColumnOrder());
//
//
//		//2022-06-21T11:38:06.702
//		Assertions.assertEquals("0.0", sqlData.getParams().get("P_IDX_SA"));
//		Assertions.assertEquals("'0'", sqlData.getParams().get("P_IDX_MM"));
//		Assertions.assertEquals("'100121250233'", sqlData.getParams().get("SA_ID"));
//		Assertions.assertEquals("'v001.2125.0233'", sqlData.getParams().get("MAC_ADDR"));
//		Assertions.assertEquals("''", sqlData.getParams().get("BUY_DATE"));
//		Assertions.assertEquals("''", sqlData.getParams().get("CONTENTS_ID"));
//	}
//}
