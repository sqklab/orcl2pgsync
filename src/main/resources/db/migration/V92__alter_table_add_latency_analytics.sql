alter table tbl_received_message
add column msg_latency bigint default 0;


alter table tbl_analysis_message_each_topic
add column total_latency bigint default 0;

alter table tbl_analysis_message_per_minute
    add column total_latency bigint default 0;