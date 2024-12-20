include .env

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------
DBZ_PG_NAME=dbz_postgres

define JQ_DBZ_PG
 ."database.hostname"="${PG_HOST}" \
| ."database.dbname"="${PG_SERVICE_NAME}"\
| ."database.port"="${PG_PORT}" \
| ."database.user"="${PG_USERNAME}" \
| ."database.password"="${PG_PASSWORD}" 
endef

DBZ_PG_CONFIG=$(shell jq '${JQ_DBZ_PG}' ./configs/connector_pg.json -crM)

s_tmp='temp.json'
s_target='./init/ssm_.json'

pg_connector_create :
	curl --location --request POST 'http://localhost:8083/connectors' \
	    --header 'Content-Type: application/json' \
	    --data-raw '{ \
	        "name": "${DBZ_PG_NAME}", \
	        "config": ${DBZ_PG_CONFIG} \
	    }'

pg_connector_delete: 
	curl -X DELETE 'http://localhost:8083/connectors/${DBZ_PG_NAME}'

pg_connector_restart:
	curl -X POST 'http://localhost:8083/connectors/${DBZ_PG_NAME}/restart'

pg_connector_restart_task:
	curl -X POST 'http://localhost:8083/connectors/${DBZ_PG_NAME}/tasks/0/restart'


# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

ORA_CONNECTOR_NAME=dbz_oracle
ORA_CONFIG_FILE=./configs/connector_ora.json

define JQ_DBZ_ORA
 ."database.hostname"="${ORA_HOST}" \
| ."database.port"="${ORA_PORT}" \
| ."database.user"="${ORA_DBZ_USERNAME}" \
| ."database.password"="${ORA_DBZ_PASSWORD}"
endef

ifndef ORA_CDB_NAME
JQ_DBZ_ORA:=${JQ_DBZ_ORA}| ."database.dbname"="${ORA_SERVICE_NAME}"
JQ_DBZ_ORA:=${JQ_DBZ_ORA}| del(."database.pdb.name")
else
JQ_DBZ_ORA:=${JQ_DBZ_ORA}| ."database.dbname"="${ORA_CDB_NAME}"
JQ_DBZ_ORA:=${JQ_DBZ_ORA}| ."database.pdb.name"="${ORA_SERVICE_NAME}"
endif

ifndef ORA_DBZ_SCHEMA_INCLUDE_LIST
else
JQ_DBZ_ORA:=${JQ_DBZ_ORA}| ."schema.include.list"="${ORA_DBZ_SCHEMA_INCLUDE_LIST}"
endif

ifndef ORA_DBZ_TABLE_INCLUDE_LIST
else
JQ_DBZ_ORA:=${JQ_DBZ_ORA}| ."table.include.list"="${ORA_DBZ_TABLE_INCLUDE_LIST}"
endif

define DBZ_ORA_CONFIG
$(shell jq '${JQ_DBZ_ORA}' ${ORA_CONFIG_FILE} -crM)
endef

define DBZ_ORA_CONFIG_RECOVERY
$(shell jq '${JQ_DBZ_ORA} | ."snapshot.mode"="schema_only_recovery"' ${ORA_CONFIG_FILE} -crM)
endef

s_tmp='temp.json'
s_target='./init/ssm_.json'

ora_connector_create:
	curl --location --request POST 'http://localhost:8083/connectors' \
	    --header 'Content-Type: application/json' \
	    --data-raw '{ \
	        "name": "${ORA_CONNECTOR_NAME}", \
	        "config": ${DBZ_ORA_CONFIG} \
	    }'

ora_connector_create_recovery:
	curl --location --request POST 'http://localhost:8083/connectors' \
	    --header 'Content-Type: application/json' \
	    --data-raw '{ \
	        "name": "${ORA_CONNECTOR_NAME}", \
	        "config": ${DBZ_ORA_CONFIG_RECOVERY} \
	    }'

ora_connector_delete:
	curl -X DELETE 'http://localhost:8083/connectors/${ORA_CONNECTOR_NAME}'

ora_connector_restart:
	curl -X POST 'http://localhost:8083/connectors/${ORA_CONNECTOR_NAME}/restart'

ora_connector_restart_task:
	curl -X POST 'http://localhost:8083/connectors/${ORA_CONNECTOR_NAME}/tasks/0/restart'

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

define JQ_LOCALSTACK
 ."oracle.host"="${ORA_HOST}" \
| ."oracle.port"="${ORA_PORT}" \
| ."oracle.user"="${ORA_USERNAME}" \
| ."oracle.pwd"="${ORA_PASSWORD}" \
| ."oracle.serviceName"="${ORA_SERVICE_NAME}" \
| ."oracle.serviceName2"="${ORA_SERVICE_NAME}"
endef

localstack_profile:
	jq '${JQ_LOCALSTACK}' ./init/ssm.json > ${s_tmp}
	mv ${s_tmp} ${s_target}

localstack_init:
	make localstack_profile
	chmod +x init/ssm_put.sh
	./init/ssm_put.sh

localstack_update:
	make localstack_profile
	chmod +x init/ssm_delete.sh
	./init/ssm_delete.sh
	chmod +x init/ssm_put.sh
	./init/ssm_put.sh

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

kafka_connect_clone:
	git clone git@github.com:LGUPLUS-IPTV-MSA/dbconnector-service.git ./connect

EXTRA_ARGS:=-name connectDistributed -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
export EXTRA_ARGS
kafka_connect_run:
	./connect/bin/connect-distributed.sh ./configs/connect-distributed.properties

# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------
# -----------------------------------------------------------------------------------

docker-compose-arm:
	docker-compose --profile localstack up -d
	docker-compose --profile kafka-cluster up -d
	docker-compose --profile postgres up -d
	make localstack_init || make localstack_update
	make kafka_connect_run

docker-compose:
	docker-compose --profile localstack up -d
	docker-compose --profile kafka-cluster up -d
	docker-compose --profile postgres up -d
	docker-compose --profile oracle up -d
	make localstack_init || make localstack_update
	make kafka_connect_run