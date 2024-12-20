package com.lguplus.fleta.config;

/**
 * @author <a href="mailto:sondn@mz.co.kr">dangokuson</a>
 * @date Feb 2022
 */
public enum Profile {
	LOCAL("local"),
	STP("stp"),
	PRODUCTION("prd"),
	INTEGRATIONTEST("integrationTest");

	public final String value;

	Profile(String value) {
		this.value = value;
	}

	public static boolean isProduction(String profile) {
		// Temporary fix like that to deploy on dev, devstp and production environment
		return !Profile.LOCAL.value.equalsIgnoreCase(profile);
	}
}
