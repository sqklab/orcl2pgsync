package com.lguplus.fleta.domain.service.constant;

/**
 * https://db.apache.org/derby/docs/10.1/ref/rrefexcept71493.html
 * https://www.postgresql.org/docs/current/errcodes-appendix.html
 */
public interface CommonSqlState {

	/**
	 * duplicated key
	 */
	String DUPLICATED_KEY = "23505";
}
