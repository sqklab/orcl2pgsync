CREATE TABLE public.tbl_synchronizer_history (
                                                 id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
                                                 synchronizer_id int8 NULL,
                                                 topic varchar NULL,
                                                 sync_json varchar NULL,
                                                 operation varchar NULL,
                                                 sync_state int4 NULL,
                                                 CONSTRAINT tbl_synchronizer_history_pk PRIMARY KEY (id)
);