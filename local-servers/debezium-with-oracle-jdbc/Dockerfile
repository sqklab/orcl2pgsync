ARG DEBEZIUM_VERSION
FROM quay.io/debezium/connect:$DEBEZIUM_VERSION
ENV KAFKA_CONNECT_JDBC_DIR=$KAFKA_CONNECT_PLUGINS_DIR/kafka-connect-jdbc

# These should point to the driver version to be used

ENV MAVEN_DEP_DESTINATION=$KAFKA_HOME/libs \
# ==============================================================
#     ORACLE_JDBC_REPO="com/oracle/database/jdbc" \
#     ORACLE_JDBC_GROUP="ojdbc11" \
#     ORACLE_JDBC_VERSION="21.7.0.0" \
#     ORACLE_JDBC_MD5=3b4ddbe215c5edead28d5a361f832a65
# ==============================================================
    ORACLE_JDBC_REPO="com/oracle/database/jdbc" \
    ORACLE_JDBC_GROUP="ojdbc8" \
    ORACLE_JDBC_VERSION="21.1.0.0" \
    ORACLE_JDBC_MD5=ebb69c8de3a83c08846b3fd47836ff15
# ==============================================================
#     ORACLE_JDBC_REPO="com/oracle/ojdbc" \
#     ORACLE_JDBC_GROUP="ojdbc8" \
#     ORACLE_JDBC_VERSION="19.3.0.0" \
#     ORACLE_JDBC_MD5=0b2a8e010df63e6feb396287d2ea7dbd
# ==============================================================
RUN docker-maven-download central "$ORACLE_JDBC_REPO" "$ORACLE_JDBC_GROUP" "$ORACLE_JDBC_VERSION" "$ORACLE_JDBC_MD5"

ENV MAVEN_DEP_DESTINATION=$KAFKA_HOME/libs \
    ORAI18N_JDBC_REPO="com/oracle/ojdbc" \
    ORAI18N_JDBC_GROUP="orai18n" \
    ORAI18N_JDBC_VERSION="19.3.0.0" \
    ORAI18N_JDBC_MD5=2d9c9f7c735548720650b20ee6d63bd3
RUN docker-maven-download central "$ORAI18N_JDBC_REPO" "$ORAI18N_JDBC_GROUP" "$ORAI18N_JDBC_VERSION" "$ORAI18N_JDBC_MD5"


USER kafka