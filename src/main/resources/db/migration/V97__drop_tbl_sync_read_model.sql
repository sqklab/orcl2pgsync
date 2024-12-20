ALTER TABLE tbl_db_comparison_info DROP CONSTRAINT tbl_db_comparison_info_tbl_sync_read_model_id_fk;


----- drop view -----
drop view if exists view_comparison_info;
----- end drop view -----

create view view_comparison_info
            (synchronizer_name, source_database, source_schema, source_table, target_database, target_schema,
             target_table, id, is_comparable, enable_column_comparison, sync_id, sync_rd_id, source_compare_database, target_compare_database, source_query, target_query)
as
SELECT sync_info.synchronizer_name,
       sync_info.source_database,
       sync_info.source_schema,
       sync_info.source_table,
       sync_info.target_database,
       sync_info.target_schema,
       sync_info.target_table,
       compa_info.id,
       compa_info.is_comparable,
       compa_info.enable_column_comparison,
       compa_info.sync_id,
       compa_info.sync_rd_id,
       compa_info.source_compare_database,
       compa_info.target_compare_database,
       compa_info.source_query,
       compa_info.target_query
FROM tbl_db_comparison_info compa_info
         LEFT JOIN tbl_sync_task_info sync_info ON compa_info.sync_id = sync_info.id;


----- drop view -----
drop view if exists view_comparison_result;
----- end drop view -----

create view view_comparison_result
            (synchronizer_name, topic_name, source_database, source_schema, source_table, target_database, target_schema,
             target_table, id, sync_id, sync_rd_id, source_compare_database, target_compare_database, source_query, target_query, compare_result_id, compare_date,
             compare_time, source_count, target_count, comparison_state, division, last_modified, error_msg_source, error_msg_target, number_diff)
as
SELECT sync_info.synchronizer_name,
       sync_info.topic_name,
       sync_info.source_database,
       sync_info.source_schema,
       sync_info.source_table,
       sync_info.target_database,
       sync_info.target_schema,
       sync_info.target_table,
       compa_info.id,
       compa_info.sync_id,
       compa_info.sync_rd_id,
       compa_info.source_compare_database,
       compa_info.target_compare_database,
       compa_info.source_query,
       compa_info.target_query,
       compare_result.id            AS compare_result_id,
       compare_result.compare_date,
       compare_result.compare_time,
       compare_result.source_count,
       compare_result.target_count,
       compare_result.comparison_state,
       sync_info.division,
       compare_result.last_modified,
       compare_result.error_msg_source,
       compare_result.error_msg_target,
       CASE
           WHEN compare_result.comparison_state = 1 THEN abs(compare_result.source_count - compare_result.target_count)
           ELSE 0
           END                      AS number_diff
FROM tbl_db_comparison_info compa_info
         LEFT JOIN tbl_sync_task_info sync_info ON compa_info.sync_id = sync_info.id
         LEFT JOIN tbl_db_comparison_result compare_result ON compa_info.id = compare_result.sync_compare_id;


----- drop tbl_sync_read_model -----
DROP TABLE tbl_sync_read_model;

UPDATE tbl_sync_task_error
SET error_type='SQL_ERROR'
WHERE error_type in ('FOREIGN_KEY_NOT_FOUND', 'SQL_SYNTAX', 'SQL_FUNCTION_NOT_SUPPORT', 'TABLE_NOT_FOUND', 'RD_QUERY_INVALID');

ALTER TABLE tbl_db_comparison_result_summary DROP COLUMN msg_rd_behind;

