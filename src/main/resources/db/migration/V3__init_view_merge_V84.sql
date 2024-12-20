create view view_comparison_result
            (synchronizer_name, topic_name, source_database, source_schema, source_table, target_database, target_schema,
             target_table, rd_source_database, rd_source_schema, rd_source_table, rd_target_database, rd_target_schema,
             rd_target_table, id, sync_id, sync_rd_id, source_query, target_query, compare_result_id, compare_date,
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
       sync_rd_info.source_database AS rd_source_database,
       sync_rd_info.source_schema   AS rd_source_schema,
       sync_rd_info.source_table    AS rd_source_table,
       sync_rd_info.target_database AS rd_target_database,
       sync_rd_info.target_schema   AS rd_target_schema,
       sync_rd_info.target_table    AS rd_target_table,
       compa_info.id,
       compa_info.sync_id,
       compa_info.sync_rd_id,
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
         LEFT JOIN tbl_sync_read_model sync_rd_info ON compa_info.sync_rd_id = sync_rd_info.id
         LEFT JOIN tbl_db_comparison_result compare_result ON compa_info.id = compare_result.sync_compare_id;


create view view_comparison_info
            (synchronizer_name, source_database, source_schema, source_table, target_database, target_schema,
             target_table, rd_source_database, rd_source_schema,
             rd_target_database, rd_target_schema, id, is_comparable, sync_id, sync_rd_id, source_query, target_query)
as
SELECT sync_info.synchronizer_name,
       sync_info.source_database,
       sync_info.source_schema,
       sync_info.source_table,
       sync_info.target_database,
       sync_info.target_schema,
       sync_info.target_table,
       sync_rd_info.source_database AS rd_source_database,
       sync_rd_info.source_schema   AS rd_source_schema,
       sync_rd_info.target_database AS rd_target_database,
       sync_rd_info.target_schema   AS rd_target_schema,
       compa_info.id,
       compa_info.is_comparable,
       compa_info.sync_id,
       compa_info.sync_rd_id,
       compa_info.source_query,
       compa_info.target_query
FROM tbl_db_comparison_info compa_info
         LEFT JOIN tbl_sync_task_info sync_info ON compa_info.sync_id = sync_info.id
         LEFT JOIN tbl_sync_read_model sync_rd_info ON compa_info.sync_rd_id = sync_rd_info.id;