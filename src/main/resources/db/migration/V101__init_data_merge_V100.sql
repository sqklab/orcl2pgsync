-- tbl_datasource
INSERT INTO tbl_datasource (
    "server_name",
    "url",
    "username",
    "password",
    "max_pool_size",
    "idle_timeout",
    "status",
    "driver_class_name"
) VALUES (
             'ORA',
             'jdbc:oracle:thin:@//oracle.host:oracle.port/oracle.serviceName',
             'oracle.user',
             'oracle.pwd',
             100,
             60000,
             0,
             'oracle.jdbc.driver.OracleDriver'
         );
INSERT INTO tbl_datasource (
    server_name,
    url,
    username,
    password,
    max_pool_size,
    idle_timeout,
    status,
    driver_class_name
) VALUES (
             'PG',
             'jdbc:postgresql://postgres.host:postgres.port/postgres.db',
             'postgres.username',
             'postgres.password',
             100,
             60000,
             0,
             'org.postgresql.Driver'
         );

-- tbl_sync_task_info
INSERT INTO tbl_sync_task_info
(synchronizer_name, source_database, source_schema, source_table, topic_name, target_database, target_schema, target_table, state, created_at, updated_at, sync_type, link_sync_request, division, primary_keys, consumer_group, is_partitioned, enable_truncate, is_batch, is_upsert, is_change_insert, is_all_condition_update)
VALUES('ORA.TESTUSER.DBZ_TEST_PT_TABLE', 'ORA', 'TESTUSER', 'DBZ_TEST_PT_TABLE', 'ORA.TESTUSER.DBZ_TEST_PT_TABLE', 'PG', 'testuser', 'dbz_test_pt_table_target', 2, '2022-11-14 10:12:41.527', '2022-11-14 10:22:34.792', 'DT', '{"ids":[]}', 'Oracle2Postgres', 'col_bpchar,col_varchar', 'dbsync', NULL, NULL, true, true, true, false);
INSERT INTO tbl_sync_task_info
(synchronizer_name, source_database, source_schema, source_table, topic_name, target_database, target_schema, target_table, state, created_at, updated_at, sync_type, link_sync_request, division, primary_keys, consumer_group, is_partitioned, enable_truncate, is_batch, is_upsert, is_change_insert, is_all_condition_update)
VALUES('ORA.TESTUSER.DBZ_TEST_PT_TABLE_UK', 'ORA', 'TESTUSER', 'DBZ_TEST_PT_TABLE_UK', 'ORA.TESTUSER.DBZ_TEST_PT_TABLE_UK', 'PG', 'testuser', 'dbz_test_pt_table_uk_target', 2, '2022-11-14 10:12:41.527', '2022-11-14 10:22:34.804', 'DT', '{"ids":[]}', 'Oracle2Postgres', 'col_bpchar,col_varchar', 'dbsync', NULL, NULL, true, true, true, false);
INSERT INTO tbl_sync_task_info
(synchronizer_name, source_database, source_schema, source_table, topic_name, target_database, target_schema, target_table, state, created_at, updated_at, sync_type, link_sync_request, division, primary_keys, consumer_group, is_partitioned, enable_truncate, is_batch, is_upsert, is_change_insert, is_all_condition_update)
VALUES('REV_PG.testuser.dbz_test_table_npk', 'PG', 'testuser', 'dbz_test_table_npk', 'REV_PG.testuser.dbz_test_table_npk', 'ORA', 'TESTUSER', 'DBZ_TEST_TABLE_NPK_TARGET', 2, '2022-11-14 10:12:41.425', '2022-11-14 10:22:34.698', 'DT', '{"ids":[]}', 'Postgres2Oracle', '', 'dbsync', NULL, NULL, true, true, true, false);
INSERT INTO tbl_sync_task_info
(synchronizer_name, source_database, source_schema, source_table, topic_name, target_database, target_schema, target_table, state, created_at, updated_at, sync_type, link_sync_request, division, primary_keys, consumer_group, is_partitioned, enable_truncate, is_batch, is_upsert, is_change_insert, is_all_condition_update)
VALUES('ORA.TESTUSER.DBZ_TEST_PT_TABLE_NPK', 'ORA', 'TESTUSER', 'DBZ_TEST_PT_TABLE_NPK', 'ORA.TESTUSER.DBZ_TEST_PT_TABLE_NPK', 'PG', 'testuser', 'dbz_test_pt_table_npk_target', 2, '2022-11-14 10:12:41.527', '2022-11-14 10:22:34.709', 'DT', '{"ids":[]}', 'Oracle2Postgres', '', 'dbsync', NULL, NULL, true, true, true, false);
INSERT INTO tbl_sync_task_info
(synchronizer_name, source_database, source_schema, source_table, topic_name, target_database, target_schema, target_table, state, created_at, updated_at, sync_type, link_sync_request, division, primary_keys, consumer_group, is_partitioned, enable_truncate, is_batch, is_upsert, is_change_insert, is_all_condition_update)
VALUES('REV_PG.testuser.dbz_test_pt_table_npk', 'PG', 'testuser', 'dbz_test_pt_table_npk', 'REV_PG.testuser.dbz_test_pt_table_npk', 'ORA', 'TESTUSER', 'DBZ_TEST_PT_TABLE_NPK_TARGET', 2, '2022-11-14 10:12:41.527', '2022-11-14 10:22:34.724', 'DT', '{"ids":[]}', 'Postgres2Oracle', '', 'dbsync', NULL, NULL, true, true, true, false);
INSERT INTO tbl_sync_task_info
(synchronizer_name, source_database, source_schema, source_table, topic_name, target_database, target_schema, target_table, state, created_at, updated_at, sync_type, link_sync_request, division, primary_keys, consumer_group, is_partitioned, enable_truncate, is_batch, is_upsert, is_change_insert, is_all_condition_update)
VALUES('ORA.TESTUSER.DBZ_TEST_TABLE_DDL', 'ORA', 'TESTUSER', 'DBZ_TEST_TABLE_DDL', 'ORA.TESTUSER.DBZ_TEST_TABLE_DDL', 'PG', 'testuser', 'dbz_test_table_ddl_target', 2, '2022-11-14 10:12:41.725', '2022-11-14 10:22:34.741', 'DT', '{"ids":[]}', 'Oracle2Postgres', 'idx,initial_col', 'dbsync', NULL, NULL, true, true, true, false);
INSERT INTO tbl_sync_task_info
(synchronizer_name, source_database, source_schema, source_table, topic_name, target_database, target_schema, target_table, state, created_at, updated_at, sync_type, link_sync_request, division, primary_keys, consumer_group, is_partitioned, enable_truncate, is_batch, is_upsert, is_change_insert, is_all_condition_update)
VALUES('REV_PG.testuser.dbz_test_table_ddl', 'PG', 'testuser', 'dbz_test_table_ddl', 'REV_PG.testuser.dbz_test_table_ddl', 'ORA', 'TESTUSER', 'DBZ_TEST_TABLE_DDL_TARGET', 2, '2022-11-14 10:12:41.725', '2022-11-14 10:22:34.756', 'DT', '{"ids":[]}', 'Postgres2Oracle', 'IDX,INITIAL_COL', 'dbsync', NULL, NULL, true, true, true, false);
INSERT INTO tbl_sync_task_info
(synchronizer_name, source_database, source_schema, source_table, topic_name, target_database, target_schema, target_table, state, created_at, updated_at, sync_type, link_sync_request, division, primary_keys, consumer_group, is_partitioned, enable_truncate, is_batch, is_upsert, is_change_insert, is_all_condition_update)
VALUES('ORA.TESTUSER.DBZ_TEST_TABLE', 'ORA', 'TESTUSER', 'DBZ_TEST_TABLE', 'ORA.TESTUSER.DBZ_TEST_TABLE', 'PG', 'testuser', 'dbz_test_table_target', 2, '2022-11-14 10:12:41.425', '2022-11-14 10:22:34.767', 'DT', '{"ids":[]}', 'Oracle2Postgres', 'col_bpchar,col_varchar', 'dbsync', NULL, NULL, true, true, true, false);
INSERT INTO tbl_sync_task_info
(synchronizer_name, source_database, source_schema, source_table, topic_name, target_database, target_schema, target_table, state, created_at, updated_at, sync_type, link_sync_request, division, primary_keys, consumer_group, is_partitioned, enable_truncate, is_batch, is_upsert, is_change_insert, is_all_condition_update)
VALUES('ORA.TESTUSER.DBZ_TEST_TABLE_UK', 'ORA', 'TESTUSER', 'DBZ_TEST_TABLE_UK', 'ORA.TESTUSER.DBZ_TEST_TABLE_UK', 'PG', 'testuser', 'dbz_test_table_uk_target', 2, '2022-11-14 10:12:41.527', '2022-11-14 10:22:34.779', 'DT', '{"ids":[]}', 'Oracle2Postgres', 'col_bpchar,col_varchar', 'dbsync', NULL, NULL, true, true, true, false);
INSERT INTO tbl_sync_task_info
(synchronizer_name, source_database, source_schema, source_table, topic_name, target_database, target_schema, target_table, state, created_at, updated_at, sync_type, link_sync_request, division, primary_keys, consumer_group, is_partitioned, enable_truncate, is_batch, is_upsert, is_change_insert, is_all_condition_update)
VALUES('REV_PG.testuser.dbz_test_table', 'PG', 'testuser', 'dbz_test_table', 'REV_PG.testuser.dbz_test_table', 'ORA', 'TESTUSER', 'DBZ_TEST_TABLE_TARGET', 2, '2022-11-14 10:12:41.425', '2022-11-14 10:22:34.817', 'DT', '{"ids":[]}', 'Postgres2Oracle', 'COL_BPCHAR,COL_VARCHAR', 'dbsync', NULL, NULL, true, true, true, false);
INSERT INTO tbl_sync_task_info
(synchronizer_name, source_database, source_schema, source_table, topic_name, target_database, target_schema, target_table, state, created_at, updated_at, sync_type, link_sync_request, division, primary_keys, consumer_group, is_partitioned, enable_truncate, is_batch, is_upsert, is_change_insert, is_all_condition_update)
VALUES('REV_PG.testuser.dbz_test_table_uk', 'PG', 'testuser', 'dbz_test_table_uk', 'REV_PG.testuser.dbz_test_table_uk', 'ORA', 'TESTUSER', 'DBZ_TEST_TABLE_UK_TARGET', 2, '2022-11-14 10:12:41.527', '2022-11-14 10:22:34.831', 'DT', '{"ids":[]}', 'Postgres2Oracle', 'COL_BPCHAR,COL_VARCHAR', 'dbsync', NULL, NULL, true, true, true, false);
INSERT INTO tbl_sync_task_info
(synchronizer_name, source_database, source_schema, source_table, topic_name, target_database, target_schema, target_table, state, created_at, updated_at, sync_type, link_sync_request, division, primary_keys, consumer_group, is_partitioned, enable_truncate, is_batch, is_upsert, is_change_insert, is_all_condition_update)
VALUES('REV_PG.testuser.dbz_test_pt_table', 'PG', 'testuser', 'dbz_test_pt_table', 'REV_PG.testuser.dbz_test_pt_table', 'ORA', 'TESTUSER', 'DBZ_TEST_PT_TABLE_TARGET', 2, '2022-11-14 10:12:41.527', '2022-11-14 10:22:34.845', 'DT', '{"ids":[]}', 'Postgres2Oracle', 'COL_BPCHAR,COL_VARCHAR', 'dbsync', NULL, NULL, true, true, true, false);
INSERT INTO tbl_sync_task_info
(synchronizer_name, source_database, source_schema, source_table, topic_name, target_database, target_schema, target_table, state, created_at, updated_at, sync_type, link_sync_request, division, primary_keys, consumer_group, is_partitioned, enable_truncate, is_batch, is_upsert, is_change_insert, is_all_condition_update)
VALUES('REV_PG.testuser.dbz_test_pt_table_uk', 'PG', 'testuser', 'dbz_test_pt_table_uk', 'REV_PG.testuser.dbz_test_pt_table_uk', 'ORA', 'TESTUSER', 'DBZ_TEST_PT_TABLE_UK_TARGET', 2, '2022-11-14 10:12:41.527', '2022-11-14 10:22:34.856', 'DT', '{"ids":[]}', 'Postgres2Oracle', 'COL_BPCHAR,COL_VARCHAR', 'dbsync', NULL, NULL, true, true, true, false);
INSERT INTO tbl_sync_task_info
(synchronizer_name, source_database, source_schema, source_table, topic_name, target_database, target_schema, target_table, state, created_at, updated_at, sync_type, link_sync_request, division, primary_keys, consumer_group, is_partitioned, enable_truncate, is_batch, is_upsert, is_change_insert, is_all_condition_update)
VALUES('ORA.TESTUSER.DBZ_TEST_TABLE_NPK', 'ORA', 'TESTUSER', 'DBZ_TEST_TABLE_NPK', 'ORA.TESTUSER.DBZ_TEST_TABLE_NPK', 'PG', 'testuser', 'dbz_test_table_npk_target', 2, '2022-11-14 10:12:41.425', '2022-11-14 10:22:37.970', 'DT', '{"ids":[]}', 'Oracle2Postgres', '', 'dbsync', false, false, true, true, true, false);


--- tbl_db_comparison_info
delete from tbl_db_comparison_info;
INSERT INTO tbl_db_comparison_info
(sync_id,source_query,target_query,state,is_comparable,source_compare_database,target_compare_database,enable_column_comparison)
select
    id as sync_id,
    '' as source_query,
    '' as target_query,
    0 as state,
    'N' as is_comparable,
    '' as source_compare_database,
    '' as target_compare_database,
    false as enable_column_comparison
from tbl_sync_task_info;