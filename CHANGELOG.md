+ Release `0.3.10`
  - sql converter 리팩토링 및 synchronizer uk 지원 [#83](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/83) 
    - 검색조건을 선택 후 동기화 목록 엑셀 내려받기 가능  [#84](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/84)
    - Auditor 적용  [#85](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/85)
    - Count Comparison 프로세스 개선 및 틀린테이블 목록 slack 첨부  [#86 #99](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/86)
    - 동기화 목록 덮어쓰기 방지 및 작업이력, 자동백업 기능 추가  [#91](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/91)
    - Truncate DML 처리 기능  [#92](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/92)
    - ConfigServer 적용과 동기화 대상 DB 정보 ParameterStore 사용 지원   [#97](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/97)
    - Max Poll Records 옵션 추가 지원  [#103](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/103)
+ Release `0.3.9`
    - 카운트 비교 시 읽기전용 DB 사용 기능 [#45](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/45)
    - 동기화 테이블 간 컬럼 비교 기능 [#46](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/46)
    - Truncate DML 처리 기능 [#49](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/49)
    - 파티션 테이블 여부 체크 기능 [#47](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/47)
    - 검색 UI 개선 [#44](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/44)
    - 엑셀 임포트/엑스포트 기능 [#52](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/52)
    - 컨슈머 그룹 개별 할당 기능 [#51](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/51)
    - 커넥터 설정 히스토리 관리 기능 [#48](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/48)
    - 레이턴시 측정 기능 [#60](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/60)
    - Publication 테이블 관리 기능 [#61](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/61)
    - Debezium 1.9.6 이상일 경우 Oracle 더블싱클쿼트 문자열 버그대응코드 처리하지 않음  [#75](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/75)
    - 싱크로나이저 무한루프 기동 방지로직 [#70](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/70)
    - 동기화 BATCH EXECUTE 동작이 옵션에 따라 다르게 처리 됨. [#71](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/71)
    - 동기화 INSERT/UPDATE 동작이 옵션에 따라 다르게 처리됨. [#74](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/74)
    - DBSync 장애 처리 (DLQ) 정책을 정리 적용.  [#77](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/77)
    - DBSync RD 모델 삭제 [#77](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/77)
    - DBSync 뷰어 권한추가 및 KeyClock 연동 [#80](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/80)
    - Integration Test Code 18 개 추가됨 [#82](https://github.com/LGUPLUS-IPTV-MSA/dbsync-service/pull/82)
+ Release `0.3.8`
    - Add the Redo_Log page: select sql_redo, sql_undo by start_scn, end_scn
    - Support reset schema of connector (delete history topic)
    - Fix a bug: cannot cancel compare in the Operation page
    - Fix bugs(2022_06_17): search dbscheduler, filter by dbname in the analysis page
+ Release `0.3.7`
    - Single kafka usage (Remove MSA Kafka). The `iptv-msa-01` will not be used in the future.
    - Oracle Connector Reset SCN. !!! Important thing is Postgres Connector Reset SCN will not be support this time.
    - Reverse direction (from Postgres to Oracle). Currently, supports two types: Oracle2Postgres (DMS1), Postgres2Oracle(REV)
      - Oracle2Postgres: Allow real-time synchronize (table to table) from Oracle-to-Postgres
      - Postgres2Oracle: Real-time synchronize data (table to table) from Postgres-to-Oracle
    - Keycloak Authentication Integration. The DataSync supports two types of authentications 1) JWT 2) Keycloak (default). The end-user can switch authentication type by using `spring.keycloak-enabled` key in the properties file.
    - Improve operation page: able to compare all data of table.
    - Improve analysis: Collect and display kafka end-offset per 5 minites
    - Fixed a bug: thread is waiting after compares a selected ids by don't use ExecutorService for calculating total
+ Release `0.3.6`
    - 2022-04-12 Merge code phase2 synchronizer from Postgres to Oracle
    - 2022-04-12 Turn off recommendation on ID column when typing ( use search button to search column ID)
    - 2022-04-12 Modify the Kafka Producer page (send a message to connect_offset topic)
    - 2022-04-12 Add configuration to authenticate with JWT or Keycloak (JWT as default)

+ Release `0.3.5`
    - 2022-04-07 Authentication (Integrate KeyCloak for authentication)
    - 2022-04-07 Operation page (Support autocomplete search with Id column (SA_ID/PVS_SBC_CONT_NO))
    - 2022-04-06 UI (Add a flag icon to language)