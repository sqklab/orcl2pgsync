package com.lguplus.fleta.util;

import lombok.Getter;

import java.util.stream.Stream;

public enum TestTableNameEnum {
    ORA_NPK    (DBMS.ORA, DBMS.PG, IndexKind.NPK, false),
    ORA_PK     (DBMS.ORA, DBMS.PG, IndexKind.PK,  false),
    ORA_UK     (DBMS.ORA, DBMS.PG, IndexKind.UK,  false),
    ORA_NPK_PT (DBMS.ORA, DBMS.PG, IndexKind.NPK, true),
    ORA_PK_PT  (DBMS.ORA, DBMS.PG, IndexKind.PK,  true),
    ORA_UK_PT  (DBMS.ORA, DBMS.PG, IndexKind.UK,  true),
    PG_NPK     (DBMS.PG, DBMS.ORA, IndexKind.NPK, false),
    PG_PK      (DBMS.PG, DBMS.ORA, IndexKind.PK,  false),
    PG_UK      (DBMS.PG, DBMS.ORA, IndexKind.UK,  false),
    PG_NPK_PT  (DBMS.PG, DBMS.ORA, IndexKind.NPK, true),
    PG_PK_PT   (DBMS.PG, DBMS.ORA, IndexKind.PK,  true),
    PG_UK_PT   (DBMS.PG, DBMS.ORA, IndexKind.UK,  true);

    public enum IndexKind {
        PK(""), UK("_UK"), NPK("_NPK");
        @Getter
        private final String suffix;

        IndexKind(String suffix) {
            this.suffix = suffix;
        }
    }

    public enum DBMS {
        PG("PG"), ORA("ORA");
        @Getter
        private final String dbName;

        DBMS(String dbName) {
            this.dbName = dbName;
        }
    }

    @Getter
    private final DBMS sourceDBMS;
    @Getter
    private final DBMS targetDBMS;
    private final TestTableNameEnum.IndexKind indexKind;
    private final boolean isPartitionTable;

    TestTableNameEnum(DBMS sourceDBMS, DBMS targetDBMS, IndexKind indexKind, boolean isPartitionTable) {
        this.sourceDBMS = sourceDBMS;
        this.targetDBMS = targetDBMS;
        this.indexKind = indexKind;
        this.isPartitionTable = isPartitionTable;
    }

    public String getSourceDbName() {
        return sourceDBMS.getDbName();
    }

    public String getTargetDbName() {
        return targetDBMS.getDbName();
    }

    public String getSourceTable(){
        String tbName = "DBZ_TEST"+(isPartitionTable?"_PT":"")+"_TABLE"+this.indexKind.suffix;
        return (this.sourceDBMS == DBMS.ORA ? tbName.toUpperCase() : tbName.toLowerCase());
    }

    public String getTargetTable(){
        String tbName = "DBZ_TEST"+(isPartitionTable?"_PT":"")+"_TABLE"+this.indexKind.suffix+"_TARGET";
        return (this.targetDBMS == DBMS.ORA ? tbName.toUpperCase() : tbName.toLowerCase());
    }

    public String toString(){
        return getSourceDbName() + "." + getSourceTable() + " -> " + getTargetDbName() + "." + getTargetTable();
    }

    public static Stream<TestTableNameEnum> getAllKind(){
        return Stream.of(
                ORA_PK,
                ORA_UK,
                ORA_NPK,
                ORA_PK_PT,
                ORA_UK_PT,
                ORA_NPK_PT,
                PG_PK,
                PG_UK,
                PG_NPK,
                PG_PK_PT,
                PG_UK_PT,
                PG_NPK_PT
        );
    }

    public static Stream<TestTableNameEnum> getOraSourceKind(){
        return Stream.of(
                ORA_PK,
                ORA_UK,
                ORA_NPK,
                ORA_PK_PT,
                ORA_UK_PT,
                ORA_NPK_PT
        );
    }
}
