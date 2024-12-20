# Prepare work
pull datasync repository and go to local server workpath
and please work on below process on `local-servers` folder
```sh
git clone git@github.com:LGUPLUS-IPTV-MSA/dbsync-service.git ./datasync-service
cd ./datasync-service/local-servers
```
## tools
- [jq](https://stedolan.github.io/jq/)
- [awscli](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)
- [docker desktop](https://www.docker.com/products/docker-desktop/)
- NONE-ARM architecture device (for local oracle container)
    - [VirtualBox](https://www.virtualbox.org/)
    - [Vagrant](https://www.vagrantup.com/downloads)

## Set docker-compose env
1. NONE-ARM machine
    ```sh
    cp './configs/.env.nonarm' '.env' 
    ```
2. ARM machine (like M1 or M2 mac device)
    ```sh
    cp './configs/.env.arm' '.env' 
    ```
    Please fill environment on `.env` file

---

# Kafka cluster
1. change `/etc/hosts` file
    add `kafka1` and `kafka2` on `127.0.0.1`
    ```sh
    # /etc/hosts
    127.0.0.1 localhost kafka1 kafka2
    ```
2. start kafka cluster
    start kafka cluster with docker-compose
    ```sh
    docker-compose --profile kafka-cluster up -d
    ```

---

# postgres
```
docker-compose --profile postgres up -d
```

---

# Oracle Database server
- [A. NONARM](#a-none-arm-machine)
- [B. ARM](#b-arm-machine-like-m1-or-m2-mac-device)

---

## A. NONE-ARM machine

ARM 이 아닌 머신에서는 아래 절차로 수행합니다.

### A-1. Prepare docker image on vagrant

vagrant 환경에서 oracle 도커 이미지를 빌드합니다.

1. get `Oracle Database 12c Enterprise Edition 12.2.0.1` archive file using [oracle download](https://edelivery.oracle.com/osdc/faces/SoftwareDelivery) page
    
2. move archive file to `./vagrant/vagrant_data/12.2.0.1/linuxx64_12201_database.zip`
3. start vagrant
    ```sh
    # local
    cd vagrant
    vagrant up
    vagrant ssh
    ```
4. build image on vagrant vagrant vm
    ```sh
    # vagrnat VM
    cd /vagrant_data
    systemctl start docker #if docker command dose not work
    ./buildContainerImage.sh -v 12.2.0.1 -i -e
    docker save -o oracle_database.12.2.0.1-ee.tar oracle/database:12.2.0.1-ee
    exit
    ```
5. save image on local docker registry
    ```sh
    # local
    vagrant halt
    vagrant destroy
    docker load -i vagrant_volume/oracle_database.12.2.0.1-ee.tar
    ```

### A-2. Start container
```
docker-compose --profile oracle up -d
```

> **important) Please try when using oracle container first time**
> initialize oracle
> 1. Check the log of oracle container
>   ```sh
>   docker logs -f oracle
>   ```
> 2. Initialize Oracle containers(*You may run on another terminal)
>   <br>Run below command, when you see the `DATABASE IS READY TO USE!` on oracle container log
>   ```sh
>   cat ./init/setup-oracle-container.sh | docker exec -i oracle bash
>   ```

---

## B. ARM machine (like M1 or M2 mac device)

M1 또는 M2 머신에서는 oracle db 가 실행되지 않으므로, 별도의 환경에서 아래 설명을 따라 oracle db 를 준비해야 합니다.

- Prepare oracle server
  - Please prepare oracle server with [debezium document](https://debezium.io/documentation/reference/1.9/connectors/oracle.html#_preparing_the_database)
  - Add require schema and table
    - CDB
      - `$REPO_ROOT/local-servers/init/ora.sql`
    - NONCDB
      - `$REPO_ROOT/local-servers/init/ora-noncdb.sql`

## C. AWS RDS

AWS RDS 를 통해 oracle db 를 사용하시려는 경우, 아래 설명을 따라 oracle db 를 준비 해 주세요

- 로그마이너 설정을 위해 Oracle Enterprise Edition 으로 인스턴스 생성이 필요합니다.

- 구동 후, 다음의 로그마이너 유저를 생성 해 주세요.

```sql
--- CALL 호출은 DB 접속프로그램에 따라 EXEC 를 사용할 수도 있음. 
CALL rdsadmin.rdsadmin_util.alter_supplemental_logging('ADD');
CALL rdsadmin.rdsadmin_util.set_configuration('archivelog retention hours',24);

CREATE TABLESPACE dbzuser DATAFILE SIZE 1G AUTOEXTEND ON MAXSIZE 10G;
CREATE USER dbzuser IDENTIFIED BY 패스워드
    DEFAULT TABLESPACE dbzuser
    QUOTA UNLIMITED ON dbzuser;
   
GRANT CREATE SESSION TO dbzuser; 
GRANT SELECT ANY TRANSACTION to dbzuser;
GRANT SELECT on DBA_TABLESPACES to dbzuser;
GRANT EXECUTE on rdsadmin.rdsadmin_util to dbzuser;
GRANT LOGMINING to dbzuser;
GRANT FLASHBACK ANY TABLE TO dbzuser; 
GRANT SELECT ANY TABLE TO dbzuser; 
GRANT SELECT_CATALOG_ROLE TO dbzuser; 
GRANT EXECUTE_CATALOG_ROLE TO dbzuser; 
GRANT CREATE TABLE TO dbzuser; 
GRANT LOCK ANY TABLE TO dbzuser; 
GRANT CREATE SEQUENCE TO dbzuser; 
GRANT SELECT ANY dictionary to dbzuser;
```

- 다음, 생성한 `dbzuser` 유저는 `.env` 파일의 `ORA_DBZ_USERNAME` 에 기입합니다.

```shell
ORA_DBZ_USERNAME=DBZUSER
ORA_DBZ_PASSWORD=패스워드
```

---

## grant tables on Oracle Database (b)
1. please add table with `./init/ora.sql`
2. please grant to debezium user `ORA_DBZ_USERNAME` was defined on `./.env`
    ```sql
    GRANT SELECT ON TESTUSER.DBZ_TEST_TABLE TO ${ORA_DBZ_USERNAME};
    GRANT SELECT ON TESTUSER.DBZ_TEST_TABLE_TARGET TO ${ORA_DBZ_USERNAME};
    GRANT SELECT ON TESTUSER.DBZ_TEST_TABLE_NPK TO ${ORA_DBZ_USERNAME};
    GRANT SELECT ON TESTUSER.DBZ_TEST_TABLE_NPK_TARGET TO ${ORA_DBZ_USERNAME};
    ```

---

# localStack

Create ssm file and add local env

1. Create ssm file
    ```sh
    docker-compose --profile localstack up -d
    ```
2. initialize localstack
    ```
    make localstack_profile
    ```

> *info) ssm script*
> ```sh
> aws ssm put-parameter \
>         --endpoint-url=http://localhost:4566 \
>         --name $key \
>         --value $value \
>         --type String
> ```
> ```sh
> aws ssm delete-parameter \
>     --endpoint-url=http://localhost:4566 \
>     --name $key
> ```
> ```sh
> aws ssm get-parameter \
>     --endpoint-url=http://localhost:4566 \
>     --name $key
> ```


---

# Debezium connector

1. git clone kafka connect repository
    ```sh
    git clone git@github.com:LGUPLUS-IPTV-MSA/dbconnector-service.git ./connect
    ```
2. Run kafka-connect with java
    <br> You may run on another terminal
    ```sh
    ./connect/bin/connect-distributed.sh ./configs/connect-distributed.properties
    ```
3. start debezium connector on kafka connect
    ```sh
    make ora_connector_create
    make pg_connector_create
    ```

---

# start datasync application
```shell
cd ..
./gradlew bootRun
```

---

# Stop local servers
```shell
docker-compose down
```

---

# start servers next time
## NON-ARM
```
make docker-compose
make ora_connector_create
make pg_connector_create
```
## ARM
```
make docker-compose-arm
make ora_connector_create
make pg_connector_create
```

## Recreate connector 
```
make ora_connector_delete
make pg_connector_delete
make ora_connector_create
make pg_connector_create
```
