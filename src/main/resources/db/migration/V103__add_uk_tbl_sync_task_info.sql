alter table tbl_sync_task_info add column unique_keys varchar(1000);

update tbl_sync_task_info set unique_keys = '' where 1=1;

update tbl_sync_task_info
set primary_keys = '',
    unique_keys  = 'col_bpchar,col_varchar'
where synchronizer_name in (
                            'ORA.TESTUSER.DBZ_TEST_TABLE_UK',
                            'ORA.TESTUSER.DBZ_TEST_PT_TABLE_UK'
    );

update tbl_sync_task_info
set primary_keys = '',
    unique_keys  = 'COL_BPCHAR,COL_VARCHAR'
where synchronizer_name in (
                            'REV_PG.testuser.dbz_test_table_uk',
                            'REV_PG.testuser.dbz_test_pt_table_uk'
    );