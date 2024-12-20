--- set unique index as tbl_sync_task_info.topic_name
DROP INDEX index_topic;
CREATE UNIQUE INDEX index_topic ON public.tbl_sync_task_info USING btree (topic_name);
ALTER TABLE "tbl_sync_task_info" ADD is_all_condition_update boolean default false not null;