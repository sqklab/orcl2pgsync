package com.lguplus.fleta.domain.service.exception;

import com.lguplus.fleta.domain.dto.ErrorType;
import org.springframework.dao.DuplicateKeyException;

import java.sql.SQLException;

import static org.apache.commons.lang3.exception.ExceptionUtils.indexOfThrowable;

public class ExceptionHelper {
	public static ErrorType getCorrespondingErrorType(Throwable throwable) {
		if (indexOfThrowable(throwable, ConnectionTimeoutException.class) != -1 ||
				indexOfThrowable(throwable, ConnectionTimeoutException.class) != -1) {
			return ErrorType.CONNECTION_TIMEOUT;
		} else if (indexOfThrowable(throwable, DuplicateKeyException.class) != -1) {
			return ErrorType.DUPLICATED_KEY;
		} else if (indexOfThrowable(throwable, SQLException.class) != -1) {
			return ErrorType.SQL_ERROR;
		} else if (indexOfThrowable(throwable, DatasourceNotFoundException.class) != -1) {
			return ErrorType.DATASOURCE_NOT_FOUND;
		}
		return ErrorType.GENERAL_ERROR;
	}
}
