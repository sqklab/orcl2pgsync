alter table tbl_db_comparison_info
    add enable_column_comparison boolean default false not null;

----- drop view -----
drop view if exists view_comparison_info;
----- end drop view -----

create view view_comparison_info
            (synchronizer_name, source_database, source_schema, source_table, target_database, target_schema,
             target_table, rd_source_database, rd_source_schema,
             rd_target_database, rd_target_schema, id, is_comparable, enable_column_comparison, sync_id, sync_rd_id, source_compare_database, target_compare_database, source_query, target_query)
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
       compa_info.enable_column_comparison,
       compa_info.sync_id,
       compa_info.sync_rd_id,
       compa_info.source_compare_database,
       compa_info.target_compare_database,
       compa_info.source_query,
       compa_info.target_query
FROM tbl_db_comparison_info compa_info
         LEFT JOIN tbl_sync_task_info sync_info ON compa_info.sync_id = sync_info.id
         LEFT JOIN tbl_sync_read_model sync_rd_info ON compa_info.sync_rd_id = sync_rd_info.id;
