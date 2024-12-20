package com.lguplus.fleta;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;
import java.util.TimeZone;

@Slf4j
@SpringBootApplication
public class DbSyncServiceApplication {

	public static void main(String[] args) {
		// Setting Spring Boot SetTimeZone (FIXME: Post constructs don't work sometimes)
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));   // It will set Asia/Seoul timezone
		System.out.println("Spring boot application running in GMT+9:00 timezone :" + new Date());   // It will print Asia/Seoul timezone

		// TODO: IMPORTANT
		//	DO NOT CHANGE THE INPUT ARGUMENTS
		SpringApplication.run(DbSyncServiceApplication.class, args);
	}
}
