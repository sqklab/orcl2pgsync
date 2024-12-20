package com.lguplus.fleta.constant;

public class Constant {

	public static String DATABASE_HOST = "rds-devstp-iptv-datasync-01.cluster-c2as9ee4bg3m.ap-northeast-2.rds.amazonaws.com:5432";

	public static int SYNC_PER_DATASOURCE = 500; // The number of synchronizer used same datasource.

	public static int MESSAGE_PER_TOPIC = 1000; // The number of messages send to each topic

	public static int TIME_OUT = 5; // Minute . Time wait to finish synchronizer. It depend on the number of message

	public static boolean DATASOURCE_IS_DEFAULT = true; // TRUE: will be store to database

	public static String DATASOURCE_STATE = "ACTIVE";
}
