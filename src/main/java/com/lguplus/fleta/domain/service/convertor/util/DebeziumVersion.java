package com.lguplus.fleta.domain.service.convertor.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class DebeziumVersion {
	public static final String UNKNOWN = "__UNKNOWN";
	public static final String V1_8_0 = "1.8.0.Final";
	public static final String V1_8_1 = "1.8.1.Final";
	public static final String V1_9_0 = "1.9.0.Final";
	public static final String V1_9_1 = "1.9.1.Final";
	public static final String V1_9_2 = "1.9.2.Final";
	public static final String V1_9_3 = "1.9.3.Final";
	public static final String V1_9_4 = "1.9.4.Final";
	public static final String V1_9_5 = "1.9.5.Final";
	public static final String V1_9_6 = "1.9.6.Final";
	public static final String V1_9_7 = "1.9.7.Final";
	public static final String V2_0_0 = "2.0.0.Final";

	public static final int __UNKNOWN = Integer.MAX_VALUE;
	public static final int __V1_8_0 = 1_008_000;
	public static final int __V1_8_1 = 1_008_001;
	public static final int __V1_9_0 = 1_009_000;
	public static final int __V1_9_1 = 1_009_001;
	public static final int __V1_9_2 = 1_009_002;
	public static final int __V1_9_3 = 1_009_003;
	public static final int __V1_9_4 = 1_009_004;
	public static final int __V1_9_5 = 1_009_005;
	public static final int __V1_9_6 = 1_009_006;
	public static final int __V1_9_7 = 1_009_007;
	public static final int __V2_0_0 = 2_000_000;


	@Getter(AccessLevel.PRIVATE)
	private final int version;

	@Getter(AccessLevel.PRIVATE)
	private final String versionStr;

	public DebeziumVersion(String versionStr){
		if (StringUtils.isEmpty(versionStr) || StringUtils.isBlank(versionStr)){
			log.warn("Property 'debezium.version' is empty, recommend debezium version is '1.9.6.Final'");
			this.versionStr = V1_9_6;
			this.version = versionStrToVersionDump(V1_9_6);
		} else {
			this.versionStr = versionStr;
			this.version = versionStrToVersionDump(versionStr);
		}
		this.warningMessage();
	}
	
	private static int versionStrToVersionDump(String versionStr){
		switch (versionStr) {
			case V2_0_0: return __V2_0_0;
			case V1_9_7: return __V1_9_7;
			case V1_9_6: return __V1_9_6;
			case V1_9_5: return __V1_9_5;
			case V1_9_4: return __V1_9_4;
			case V1_9_3: return __V1_9_3;
			case V1_9_2: return __V1_9_2;
			case V1_9_1: return __V1_9_1;
			case V1_9_0: return __V1_9_0;
			case V1_8_1: return __V1_8_1;
			case V1_8_0: return __V1_8_0;
			case UNKNOWN:
			default:
				log.warn("Recommend debezium version is '1.9.6.Final', or please check property, (debezium.version: '"+versionStr+"')");
				return __UNKNOWN;
		}
	}

	private void warningMessage(){
		if(this.version <= __V1_9_5){
			log.warn("debezium version '{}' has double pipe issue on oracle connector: When source DB update text value to two pipe('||'), then target DB will update to single pipe ('|')", this.versionStr);
		}
		if(this.version > __V1_9_7){
			log.warn("debezium version '{}' might not secure on this Datasync version", this.versionStr);
		}
	}

	// [DBZ-4891 Single quotes replication](https://issues.redhat.com/browse/DBZ-4891)
	public static boolean isBugExist_DBZ4891(DebeziumVersion dbzVersion) {
		return dbzVersion.getVersion() < __V1_9_0;
	}
}
