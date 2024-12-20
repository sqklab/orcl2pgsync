ALTER TABLE "tbl_sync_task_info" ADD is_upsert boolean default true not null;
ALTER TABLE "tbl_sync_task_info" ADD is_change_insert boolean default true not null;