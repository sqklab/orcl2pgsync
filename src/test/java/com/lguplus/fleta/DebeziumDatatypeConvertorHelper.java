//package com.lguplus.fleta;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.lguplus.fleta.constant.ObjectMapperConfiguration;
//import com.lguplus.fleta.domain.service.convertor.DefaultDebeziumToPostgresDatatypeConverter;
//
//public class DebeziumDatatypeConvertorHelper {
//
//	public static ObjectMapper objectMapper = new ObjectMapperConfiguration().objectMapper().enable(SerializationFeature.INDENT_OUTPUT);
//
//	public static void printSqlDate(DefaultDebeziumToPostgresDatatypeConverter.ExecutorSqlData sqlData) throws JsonProcessingException {
//		System.out.println("\n====== Converted SQL");
//		System.out.println(sqlData.getSql());
//		System.out.println("\n====== Converted ColumnOrder");
//		System.out.println(sqlData.getColumnOrder());
//		System.out.println("\n====== Converted Params");
//		System.out.println(objectMapper.writeValueAsString(sqlData.getParams()));
//	}
//}
