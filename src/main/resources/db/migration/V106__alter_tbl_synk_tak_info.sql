update public.tbl_sync_task_info set enable_truncate = false where enable_truncate is null;
ALTER TABLE public.tbl_sync_task_info alter column enable_truncate set default false;
ALTER TABLE public.tbl_sync_task_info alter column enable_truncate set not null;