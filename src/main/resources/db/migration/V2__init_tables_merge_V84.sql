-- public.tbl_analysis_message_each_topic definition

-- Drop table

-- DROP TABLE public.tbl_analysis_message_each_topic;

CREATE TABLE public.tbl_analysis_message_each_topic (
                                                        id varchar(50) NOT NULL,
                                                        topic varchar(100) NOT NULL,
                                                        received_message int8 NOT NULL DEFAULT 0,
                                                        total_message int8 NOT NULL DEFAULT 0,
                                                        at_year int4 NULL DEFAULT 0,
                                                        at_month int4 NULL DEFAULT 0,
                                                        at_date int4 NULL DEFAULT 0,
                                                        at_hour int4 NULL DEFAULT 0,
                                                        db_name varchar(50) NULL,
                                                        schm_name varchar(50) NULL,
                                                        received_message_hourly int8 NULL DEFAULT 0,
                                                        kafka_message_hourly int8 NULL DEFAULT 0,
                                                        at_minute int4 NOT NULL DEFAULT 0,
                                                        kafka_message_per_five int8 NULL DEFAULT 0,
                                                        end_offset_per_five int8 NULL DEFAULT 0,
                                                        per_five bool NULL DEFAULT false,
                                                        CONSTRAINT tbl_analysis_message_each_topic_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX tbl_analysis_message_each_topic_pk ON public.tbl_analysis_message_each_topic USING btree (topic, at_year, at_month, at_date, at_hour, at_minute, per_five);


-- public.tbl_analysis_message_per_minute definition

-- Drop table

-- DROP TABLE public.tbl_analysis_message_per_minute;

CREATE TABLE public.tbl_analysis_message_per_minute (
                                                        id varchar NOT NULL,
                                                        db_name varchar(100) NULL,
                                                        schm_name varchar(100) NULL,
                                                        topic varchar(255) NOT NULL,
                                                        received_message int4 NOT NULL DEFAULT 0,
                                                        at_year int4 NOT NULL DEFAULT 0,
                                                        at_month int4 NOT NULL DEFAULT 0,
                                                        at_date int4 NOT NULL DEFAULT 0,
                                                        at_hour int4 NOT NULL DEFAULT 0,
                                                        at_minute int4 NOT NULL DEFAULT 0,
                                                        CONSTRAINT tbl_analysis_message_per_minute_pk UNIQUE (topic, at_year, at_month, at_date, at_hour, at_minute),
                                                        CONSTRAINT tbl_analysis_message_per_minute_pkey PRIMARY KEY (id)
);


-- public.tbl_datasource definition

-- Drop table

-- DROP TABLE public.tbl_datasource;

CREATE TABLE public.tbl_datasource (
                                       id int2 NOT NULL GENERATED ALWAYS AS IDENTITY,
                                       server_name varchar(100) NOT NULL,
                                       url varchar(300) NOT NULL,
                                       username varchar(100) NULL,
                                       "password" varchar(100) NULL,
                                       max_pool_size int2 NULL,
                                       idle_timeout int4 NULL,
                                       status int2 NOT NULL,
                                       driver_class_name varchar(100) NOT NULL,
                                       CONSTRAINT datasource_info_pkey PRIMARY KEY (id),
                                       CONSTRAINT key_name UNIQUE (server_name),
                                       CONSTRAINT key_url UNIQUE (url)
);


-- public.tbl_db_comparison_result_summary definition

-- Drop table

-- DROP TABLE public.tbl_db_comparison_result_summary;

CREATE TABLE public.tbl_db_comparison_result_summary (
                                                         id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
                                                         compare_date date NOT NULL,
                                                         compare_time time NOT NULL,
                                                         source_count int8 NOT NULL,
                                                         target_count int8 NOT NULL,
                                                         msg_dt_behind int8 NULL,
                                                         msg_rd_behind int8 NULL,
                                                         total int4 NOT NULL,
                                                         fail int4 NOT NULL,
                                                         equal int4 NOT NULL,
                                                         different int4 NOT NULL,
                                                         CONSTRAINT tbl_db_comparison_result_summary_pkey PRIMARY KEY (id)
);


-- public.tbl_db_comparison_schedule definition

-- Drop table

-- DROP TABLE public.tbl_db_comparison_schedule;

CREATE TABLE public.tbl_db_comparison_schedule (
                                                   id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
                                                   "time" time NOT NULL,
                                                   state int4 NULL DEFAULT 0,
                                                   CONSTRAINT tbl_db_comparison_schedule_pk PRIMARY KEY (id)
);
CREATE UNIQUE INDEX tbl_db_comparison_schedule_time_uindex ON public.tbl_db_comparison_schedule USING btree ("time");


-- public.tbl_db_connectors definition

-- Drop table

-- DROP TABLE public.tbl_db_connectors;

CREATE TABLE public.tbl_db_connectors (
                                          id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
                                          name varchar(200) NULL,
                                          status varchar(50) NULL,
                                          worker_id varchar(300) NULL,
                                          "type" varchar(100) NULL,
                                          config text NULL,
                                          create_at timestamptz NOT NULL DEFAULT now(),
                                          update_at timestamptz NOT NULL DEFAULT now(),
                                          CONSTRAINT tbl_db_connectors_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX tbl_db_connectors_name_type_uindex ON public.tbl_db_connectors USING btree (name, type);


-- public.tbl_db_schedule_procedure definition

-- Drop table

-- DROP TABLE public.tbl_db_schedule_procedure;

CREATE TABLE public.tbl_db_schedule_procedure (
                                                  id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
                                                  pl_sql varchar(500) NOT NULL,
                                                  db varchar(50) NOT NULL,
                                                  schedule_schema varchar(100) NULL,
                                                  schedule_table varchar(100) NULL,
                                                  "type" int2 NOT NULL,
                                                  day_of_week varchar(50) NULL,
                                                  times_of_week varchar(100) NULL,
                                                  time_daily varchar(4000) NULL,
                                                  "enable" bool NOT NULL,
                                                  process_status bool NOT NULL,
                                                  created_at timestamptz NOT NULL DEFAULT now(),
                                                  updated_at timestamptz NOT NULL DEFAULT now(),
                                                  last_run timestamptz NULL,
                                                  monthly varchar(500) NULL,
                                                  quarterly varchar(500) NULL,
                                                  yearly varchar(500) NULL,
                                                  name varchar(500) NULL,
                                                  CONSTRAINT tbl_db_schedule_procedure_pkey PRIMARY KEY (id)
);


-- public.tbl_db_schedule_procedure_result definition

-- Drop table

-- DROP TABLE public.tbl_db_schedule_procedure_result;

CREATE TABLE public.tbl_db_schedule_procedure_result (
                                                         id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
                                                         status bool NULL,
                                                         error_msg varchar(1000) NULL,
                                                         schedule_time time NOT NULL,
                                                         schedule_date date NOT NULL,
                                                         start_at timestamptz NOT NULL DEFAULT now(),
                                                         end_at timestamptz NOT NULL DEFAULT now(),
                                                         fk_id_schedule_procedure int4 NULL,
                                                         CONSTRAINT tbl_db_schedule_procedure_result_pkey PRIMARY KEY (id)
);


-- public.tbl_last_received_message_info definition

-- Drop table

-- DROP TABLE public.tbl_last_received_message_info;

CREATE TABLE public.tbl_last_received_message_info (
                                                       id serial4 NOT NULL,
                                                       topic varchar(255) NULL,
                                                       scn int8 NULL,
                                                       commit_scn int8 NULL,
                                                       msg_timestamp int8 NULL,
                                                       received_date date NULL,
                                                       received_time time NULL,
                                                       CONSTRAINT tbl_last_received_message_info_pk PRIMARY KEY (id)
);
CREATE UNIQUE INDEX tbl_last_received_message_info_id_uindex ON public.tbl_last_received_message_info USING btree (id);
CREATE UNIQUE INDEX tbl_last_received_message_info_topic_uindex ON public.tbl_last_received_message_info USING btree (topic);


-- public.tbl_op_pt_vo_buy definition

-- Drop table

-- DROP TABLE public.tbl_op_pt_vo_buy;

CREATE TABLE public.tbl_op_pt_vo_buy (
                                         uuid varchar NOT NULL,
                                         primary_keys varchar NULL,
                                         "session" varchar(50) NULL,
                                         op_date_time timestamptz NOT NULL DEFAULT now(),
                                         correction_type varchar(10) NULL,
                                         where_condition varchar(1000) NULL,
                                         CONSTRAINT tbl_op_pt_vo_buy_pkey PRIMARY KEY (uuid)
);


-- public.tbl_op_pt_vo_watch_history definition

-- Drop table

-- DROP TABLE public.tbl_op_pt_vo_watch_history;

CREATE TABLE public.tbl_op_pt_vo_watch_history (
                                                   uuid varchar NOT NULL,
                                                   primary_keys varchar NULL,
                                                   "session" varchar(50) NULL,
                                                   op_date_time timestamptz NOT NULL DEFAULT now(),
                                                   correction_type varchar(10) NULL,
                                                   where_condition varchar(1000) NULL,
                                                   CONSTRAINT tbl_op_pt_vo_watch_history_pkey PRIMARY KEY (uuid)
);


-- public.tbl_op_xcion_sbc_tbl_united definition

-- Drop table

-- DROP TABLE public.tbl_op_xcion_sbc_tbl_united;

CREATE TABLE public.tbl_op_xcion_sbc_tbl_united (
                                                    uuid varchar NOT NULL,
                                                    primary_keys varchar NULL,
                                                    "session" varchar(100) NULL,
                                                    op_date_time timestamptz NOT NULL DEFAULT now(),
                                                    correction_type varchar(10) NULL,
                                                    where_condition varchar(1000) NULL,
                                                    CONSTRAINT tbl_op_xcion_sbc_tbl_united_pkey PRIMARY KEY (uuid)
);


-- public.tbl_operation_process definition

-- Drop table

-- DROP TABLE public.tbl_operation_process;

CREATE TABLE public.tbl_operation_process (
                                              id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
                                              "session" varchar(50) NULL,
                                              op_date_time timestamptz NOT NULL DEFAULT now(),
                                              op_end_date_time timestamptz NULL,
                                              op_table varchar(100) NULL,
                                              where_condition varchar(1000) NULL,
                                              state bool NOT NULL,
                                              search_type varchar(10) NULL,
                                              CONSTRAINT tbl_operation_process_pkey PRIMARY KEY (id)
);


-- public.tbl_received_message definition

-- Drop table

-- DROP TABLE public.tbl_received_message;

CREATE TABLE public.tbl_received_message (
                                             id varchar NOT NULL,
                                             topic varchar(255) NOT NULL,
                                             received_date date NOT NULL,
                                             received_time time NOT NULL,
                                             commit_scn int8 NULL DEFAULT 0,
                                             scn int8 NULL DEFAULT 0,
                                             msg_timestamp int8 NULL DEFAULT 0
);
CREATE INDEX idx_tbl_received_message_01 ON public.tbl_received_message USING btree (topic);


-- public.tbl_sync_task_error definition

-- Drop table

-- DROP TABLE public.tbl_sync_task_error;

CREATE TABLE public.tbl_sync_task_error (
                                            id int8 NOT NULL GENERATED ALWAYS AS IDENTITY,
                                            topic_name varchar(255) NOT NULL,
                                            sync_message text NOT NULL,
                                            error_message text NULL,
                                            error_time timestamptz NOT NULL DEFAULT now(),
                                            state int2 NOT NULL,
                                            updated_at timestamptz NOT NULL DEFAULT now(),
                                            error_type varchar(50) NULL,
                                            last_update timestamptz NULL,
                                            operation varchar(20) NULL,
                                            CONSTRAINT tbl_sync_task_error_pkey PRIMARY KEY (id)
);
CREATE INDEX tbl_sync_task_error_topic_name_index ON public.tbl_sync_task_error USING btree (topic_name);


-- public.tbl_sync_task_info definition

-- Drop table

-- DROP TABLE public.tbl_sync_task_info;

CREATE TABLE public.tbl_sync_task_info (
                                           id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
                                           synchronizer_name varchar(255) NOT NULL,
                                           source_database varchar(255) NOT NULL,
                                           source_schema varchar(255) NOT NULL,
                                           source_table varchar(255) NOT NULL,
                                           topic_name varchar(255) NOT NULL,
                                           target_database varchar(255) NULL,
                                           target_schema varchar(255) NULL,
                                           target_table varchar(255) NULL,
                                           state int2 NOT NULL,
                                           created_at timestamptz NOT NULL DEFAULT now(),
                                           updated_at timestamptz NOT NULL DEFAULT now(),
                                           sync_type varchar(10) NULL DEFAULT 'DT'::character varying,
                                           link_sync_request varchar(300) NULL DEFAULT '{}'::character varying,
                                           division varchar(50) NULL,
                                           primary_keys varchar(1000) NULL,
                                           consumer_group varchar(50) NULL,
                                           CONSTRAINT tbl_sync_task_info_pkey PRIMARY KEY (id)
);
CREATE INDEX index_topic ON public.tbl_sync_task_info USING btree (topic_name);


-- public.tbl_user definition

-- Drop table

-- DROP TABLE public.tbl_user;

CREATE TABLE public.tbl_user (
                                 id serial4 NOT NULL,
                                 username varchar(100) NOT NULL,
                                 "password" varchar(100) NOT NULL,
                                 status int4 NULL,
                                 email varchar(100) NULL,
                                 created_at timestamp NULL,
                                 updated_at timestamp NULL,
                                 CONSTRAINT user_pk PRIMARY KEY (id)
);
CREATE UNIQUE INDEX users_username_uindex ON public.tbl_user USING btree (username);


-- public.tbl_sync_read_model definition

-- Drop table

-- DROP TABLE public.tbl_sync_read_model;

CREATE TABLE public.tbl_sync_read_model (
                                            id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
                                            synchronizer_id int4 NOT NULL,
                                            source_database varchar(255) NOT NULL,
                                            source_schema varchar(255) NOT NULL,
                                            source_table varchar(255) NOT NULL,
                                            target_database varchar(255) NOT NULL,
                                            target_schema varchar(255) NOT NULL,
                                            target_table varchar(255) NOT NULL,
                                            operator_insert text NULL,
                                            operator_update text NULL,
                                            operator_delete text NULL,
                                            CONSTRAINT tbl_sync_read_model_pkey PRIMARY KEY (id),
                                            CONSTRAINT fk_synchronizer FOREIGN KEY (synchronizer_id) REFERENCES public.tbl_sync_task_info(id)
);


-- public.tbl_db_comparison_info definition

-- Drop table

-- DROP TABLE public.tbl_db_comparison_info;

CREATE TABLE public.tbl_db_comparison_info (
                                               id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
                                               sync_id int4 NOT NULL,
                                               sync_rd_id int4 NULL,
                                               source_query text NULL,
                                               target_query text NULL,
                                               state int4 NOT NULL DEFAULT 0,
                                               is_comparable varchar(4) NULL,
                                               last_run timestamp NULL,
                                               CONSTRAINT tbl_db_comparison_info_pkey PRIMARY KEY (id),
                                               CONSTRAINT fk_comparison_synchronizer FOREIGN KEY (sync_id) REFERENCES public.tbl_sync_task_info(id) ON DELETE CASCADE,
                                               CONSTRAINT tbl_db_comparison_info_tbl_sync_read_model_id_fk FOREIGN KEY (sync_rd_id) REFERENCES public.tbl_sync_read_model(id) ON DELETE CASCADE
);


-- public.tbl_db_comparison_result definition

-- Drop table

-- DROP TABLE public.tbl_db_comparison_result;

CREATE TABLE public.tbl_db_comparison_result (
                                                 id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
                                                 compare_date date NULL,
                                                 compare_time time NULL,
                                                 sync_compare_id int4 NOT NULL,
                                                 source_count int4 NOT NULL,
                                                 target_count int4 NOT NULL,
                                                 comparison_state int2 NOT NULL,
                                                 last_modified timestamptz NOT NULL DEFAULT now(),
                                                 notified bool NULL DEFAULT false,
                                                 error_msg_source text NULL,
                                                 error_msg_target text NULL,
                                                 CONSTRAINT tbl_db_comparison_result_pkey PRIMARY KEY (id),
                                                 CONSTRAINT fk_comparison_result FOREIGN KEY (sync_compare_id) REFERENCES public.tbl_db_comparison_info(id) ON DELETE CASCADE
);
CREATE INDEX tbl_db_comparison_result_compare_date_compare_time_index ON public.tbl_db_comparison_result USING btree (compare_date, compare_time);
CREATE INDEX tbl_db_comparison_result_sync_compare_id_index ON public.tbl_db_comparison_result USING btree (sync_compare_id);