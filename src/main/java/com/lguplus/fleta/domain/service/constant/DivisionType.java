package com.lguplus.fleta.domain.service.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum DivisionType{
	ORACLE_TO_POSTGRES(DbmsKind.ORACLE, DbmsKind.POSTGRES),
	POSTGRES_TO_ORACLE(DbmsKind.POSTGRES, DbmsKind.ORACLE),
	POSTGRES_TO_POSTGRES(DbmsKind.POSTGRES, DbmsKind.POSTGRES);

	@Override
	public String toString() {
		return divisionStr;
	}

	@Getter private final String source;
	@Getter private final String target;
	@Getter private final String divisionStr;

	DivisionType(String source, String target) {
		this.source = source;
		this.target = target;
		this.divisionStr = source + "2" + target;
	}


	private static final String __ORACLE_TO_POSTGRES = DbmsKind.ORACLE+"2"+ DbmsKind.POSTGRES;
	private static final String __POSTGRES_TO_POSTGRES = DbmsKind.POSTGRES+"2"+ DbmsKind.POSTGRES;
	private static final String __POSTGRES_TO_ORACLE = DbmsKind.POSTGRES+"2"+ DbmsKind.ORACLE;
	public static DivisionType getDivision(String divisionStr) {
		switch (divisionStr) {
			case __ORACLE_TO_POSTGRES:
				return DivisionType.ORACLE_TO_POSTGRES;
			case __POSTGRES_TO_ORACLE:
				return DivisionType.POSTGRES_TO_ORACLE;
			case __POSTGRES_TO_POSTGRES:
				return DivisionType.POSTGRES_TO_POSTGRES;
			default:
				log.warn("invalid division {}", divisionStr);
				return DivisionType.ORACLE_TO_POSTGRES;
		}
	}

}
