alter table tbl_db_connectors add column created_user varchar(100);

alter table tbl_db_connectors add column updated_user varchar(100);

alter table tbl_datasource add column created_user varchar(100);

alter table tbl_datasource add column updated_user varchar(100);

alter table tbl_sync_task_info add column created_user varchar(100);

alter table tbl_sync_task_info add column updated_user varchar(100);

alter table tbl_db_schedule_procedure add column created_user varchar(100);

alter table tbl_db_schedule_procedure add column updated_user varchar(100);

ALTER TABLE public.tbl_datasource ADD created_at timestamptz NOT NULL DEFAULT now();
ALTER TABLE public.tbl_datasource ADD updated_at timestamptz NOT NULL DEFAULT now();

ALTER TABLE public.tbl_db_connectors ADD created_at timestamptz NOT NULL DEFAULT now();
ALTER TABLE public.tbl_db_connectors ADD updated_at timestamptz NOT NULL DEFAULT now();