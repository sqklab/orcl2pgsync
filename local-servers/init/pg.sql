create schema testuser;

CREATE PUBLICATION dbz_publication FOR ALL TABLES WITH (publish_via_partition_root = true);

CREATE TABLE testuser.dbz_test_table (
	col_bpchar bpchar NOT NULL,
	col_varchar varchar NOT NULL,
	col_float8 float8 NULL,
    col_int4 int4 NULL,
	col_numeric numeric NULL,
	col_int2 int2 NULL,
	col_text text NULL,
	col_timestamp timestamp NULL,
	CONSTRAINT pk_dbz_test_table PRIMARY KEY (col_bpchar, col_varchar)
);
alter table testuser.dbz_test_table replica identity full;

CREATE TABLE testuser.dbz_test_table_target (
     col_bpchar bpchar NOT NULL,
     col_varchar varchar NOT NULL,
     col_float8 float8 NULL,
     col_int4 int4 NULL,
     col_numeric numeric NULL,
     col_int2 int2 NULL,
     col_text text NULL,
     col_timestamp timestamp NULL,
     CONSTRAINT pk_dbz_test_table_target PRIMARY KEY (col_bpchar, col_varchar)
);
alter table testuser.dbz_test_table_target replica identity full;

create table testuser.dbz_test_pt_table (
    col_bpchar bpchar,
    col_varchar varchar,
    col_float8 float8 null,
    col_int4 int4 null,
    col_numeric numeric null,
    col_int2 int2 null,
    col_text text null,
    col_timestamp timestamp null,
    CONSTRAINT pk_dbz_test_pt_table PRIMARY KEY (col_bpchar, col_varchar)
) partition by list (col_bpchar);
create table p_test_1   partition of testuser.dbz_test_pt_table for values in ('test_1', 'TEST_1');
create table p_test_2   partition of testuser.dbz_test_pt_table for values in ('test_2', 'TEST_2');
create table p_test_3   partition of testuser.dbz_test_pt_table for values in ('test_3', 'TEST_3');
create table p_units  partition of testuser.dbz_test_pt_table for values in('1', '2', '3', '4', '5', '6', '7', '8', '9', '0');
create table p_else   partition of testuser.dbz_test_pt_table default;
alter table testuser.dbz_test_pt_table replica identity full;
alter table p_test_1 replica identity full;
alter table p_test_2 replica identity full;
alter table p_test_3 replica identity full;
alter table p_units replica identity full;
alter table p_else replica identity full;

create table testuser.dbz_test_pt_table_target (
    col_bpchar bpchar,
    col_varchar varchar,
    col_float8 float8 null,
    col_int4 int4 null,
    col_numeric numeric null,
    col_int2 int2 null,
    col_text text null,
    col_timestamp timestamp null,
    CONSTRAINT pk_dbz_test_pt_table_target PRIMARY KEY (col_bpchar, col_varchar)
) partition by list (col_bpchar);
create table p_target_test_1   partition of testuser.dbz_test_pt_table_target for values in ('test_1', 'TEST_1');
create table p_target_test_2   partition of testuser.dbz_test_pt_table_target for values in ('test_2', 'TEST_2');
create table p_target_test_3   partition of testuser.dbz_test_pt_table_target for values in ('test_3', 'TEST_3');
create table p_target_units  partition of testuser.dbz_test_pt_table_target for values in('1', '2', '3', '4', '5', '6', '7', '8', '9', '0');
create table p_target_else   partition of testuser.dbz_test_pt_table_target default;
alter table testuser.dbz_test_pt_table_target replica identity full;
alter table p_target_test_1 replica identity full;
alter table p_target_test_2 replica identity full;
alter table p_target_test_3 replica identity full;
alter table p_target_units replica identity full;
alter table p_target_else replica identity full;

--------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------

CREATE TABLE testuser.dbz_test_table_npk (
     col_bpchar bpchar NULL,
     col_varchar varchar NULL,
     col_float8 float8 NULL,
     col_int4 int4 NULL,
     col_numeric numeric NULL,
     col_int2 int2 NULL,
     col_text text NULL,
     col_timestamp timestamp NULL
);
alter table testuser.dbz_test_table_npk replica identity full;

CREATE TABLE testuser.dbz_test_table_npk_target (
     col_bpchar bpchar NULL,
     col_varchar varchar NULL,
     col_float8 float8 NULL,
     col_int4 int4 NULL,
     col_numeric numeric NULL,
     col_int2 int2 NULL,
     col_text text NULL,
     col_timestamp timestamp NULL
);
alter table testuser.dbz_test_table_npk_target replica identity full;

create table testuser.dbz_test_pt_table_npk (
    col_bpchar bpchar default null,
    col_varchar varchar default null,
    col_float8 float8 null,
    col_int4 int4 null,
    col_numeric numeric null,
    col_int2 int2 null,
    col_text text null,
    col_timestamp timestamp null
) partition by list (col_bpchar);
create table p_npk_test_1   partition of testuser.dbz_test_pt_table_npk for values in ('test_1', 'TEST_1');
create table p_npk_test_2   partition of testuser.dbz_test_pt_table_npk for values in ('test_2', 'TEST_2');
create table p_npk_test_3   partition of testuser.dbz_test_pt_table_npk for values in ('test_3', 'TEST_3');
create table p_npk_units  partition of testuser.dbz_test_pt_table_npk for values in ('1', '2', '3', '4', '5', '6', '7', '8', '9', '0');
create table p_npk_null   partition of testuser.dbz_test_pt_table_npk for values in (null);
create table p_npk_else   partition of testuser.dbz_test_pt_table_npk default;
alter table testuser.dbz_test_pt_table_npk replica identity full;
alter table p_npk_test_1 replica identity full;
alter table p_npk_test_2 replica identity full;
alter table p_npk_test_3 replica identity full;
alter table p_npk_units replica identity full;
alter table p_npk_null replica identity full;
alter table p_npk_else replica identity full;

create table testuser.dbz_test_pt_table_npk_target (
    col_bpchar bpchar default null,
    col_varchar varchar default null,
    col_float8 float8 null,
    col_int4 int4 null,
    col_numeric numeric null,
    col_int2 int2 null,
    col_text text null,
    col_timestamp timestamp null
) partition by list (col_bpchar);
create table p_npk_target_test_1   partition of testuser.dbz_test_pt_table_npk_target for values in ('test_1', 'TEST_1');
create table p_npk_target_test_2   partition of testuser.dbz_test_pt_table_npk_target for values in ('test_2', 'TEST_2');
create table p_npk_target_test_3   partition of testuser.dbz_test_pt_table_npk_target for values in ('test_3', 'TEST_3');
create table p_npk_target_units  partition of testuser.dbz_test_pt_table_npk_target for values in('1', '2', '3', '4', '5', '6', '7', '8', '9', '0');
create table p_npk_target_null   partition of testuser.dbz_test_pt_table_npk_target for values in (null);
create table p_npk_target_else   partition of testuser.dbz_test_pt_table_npk_target default;
alter table testuser.dbz_test_pt_table_npk_target replica identity full;
alter table p_npk_target_test_1 replica identity full;
alter table p_npk_target_test_2 replica identity full;
alter table p_npk_target_test_3 replica identity full;
alter table p_npk_target_units replica identity full;
alter table p_npk_target_null replica identity full;
alter table p_npk_target_else replica identity full;

--------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------

CREATE TABLE testuser.dbz_test_table_uk (
    col_bpchar bpchar default NULL,
    col_varchar varchar default NULL,
    col_float8 float8 NOT NULL,
    col_int4 int4 NULL,
    col_numeric numeric NULL,
    col_int2 int2 NULL,
    col_text text NULL,
    col_timestamp timestamp NULL,
    CONSTRAINT pk_dbz_test_table_uk UNIQUE (col_bpchar, col_varchar)
);
alter table testuser.dbz_test_table_uk replica identity full;

CREATE TABLE testuser.dbz_test_table_uk_target (
    col_bpchar bpchar default NULL,
    col_varchar varchar default NULL,
    col_float8 float8 NULL,
    col_int4 int4 NULL,
    col_numeric numeric NULL,
    col_int2 int2 NULL,
    col_text text NULL,
    col_timestamp timestamp NULL,
    CONSTRAINT pk_dbz_test_table_uk_target UNIQUE (col_bpchar, col_varchar)
);
alter table testuser.dbz_test_table_uk_target replica identity full;

create table testuser.dbz_test_pt_table_uk (
    col_bpchar bpchar default null,
    col_varchar varchar default null,
    col_float8 float8 null,
    col_int4 int4 null,
    col_numeric numeric null,
    col_int2 int2 null,
    col_text text null,
    col_timestamp timestamp null,
    constraint pk_dbz_test_pt_table_uk_target unique (col_bpchar, col_varchar)
) partition by list (col_bpchar);
create table p_uk_test_1   partition of testuser.dbz_test_pt_table_uk for values in ('test_1', 'TEST_1');
create table p_uk_test_2   partition of testuser.dbz_test_pt_table_uk for values in ('test_2', 'TEST_2');
create table p_uk_test_3   partition of testuser.dbz_test_pt_table_uk for values in ('test_3', 'TEST_3');
create table p_uk_units  partition of testuser.dbz_test_pt_table_uk for values in('1', '2', '3', '4', '5', '6', '7', '8', '9', '0');
create table p_uk_null   partition of testuser.dbz_test_pt_table_uk for values in (null);
create table p_uk_else   partition of testuser.dbz_test_pt_table_uk default;
alter table testuser.dbz_test_pt_table_uk replica identity full;
alter table p_uk_test_1 replica identity full;
alter table p_uk_test_2 replica identity full;
alter table p_uk_test_3 replica identity full;
alter table p_uk_units replica identity full;
alter table p_uk_null replica identity full;
alter table p_uk_else replica identity full;

create table testuser.dbz_test_pt_table_uk_target (
    col_bpchar bpchar default null,
    col_varchar varchar default null,
    col_float8 float8 null,
    col_int4 int4 null,
    col_numeric numeric null,
    col_int2 int2 null,
    col_text text null,
    col_timestamp timestamp null,
    constraint uk_dbz_test_pt_table_uk_target unique (col_bpchar, col_varchar)
) partition by list (col_bpchar);
create table p_uk_target_test_1   partition of testuser.dbz_test_pt_table_uk_target for values in ('test_1', 'TEST_1');
create table p_uk_target_test_2   partition of testuser.dbz_test_pt_table_uk_target for values in ('test_2', 'TEST_2');
create table p_uk_target_test_3   partition of testuser.dbz_test_pt_table_uk_target for values in ('test_3', 'TEST_3');
create table p_uk_target_units  partition of testuser.dbz_test_pt_table_uk_target for values in ('1', '2', '3', '4', '5', '6', '7', '8', '9', '0');
create table p_uk_target_null   partition of testuser.dbz_test_pt_table_uk_target for values in (null);
create table p_uk_target_else   partition of testuser.dbz_test_pt_table_uk_target default;
alter table testuser.dbz_test_pt_table_uk_target replica identity full;
alter table p_uk_target_test_1 replica identity full;
alter table p_uk_target_test_2 replica identity full;
alter table p_uk_target_test_3 replica identity full;
alter table p_uk_target_units replica identity full;
alter table p_uk_target_null replica identity full;
alter table p_uk_target_else replica identity full;


CREATE PUBLICATION pub_test_user
    WITH (publish = 'insert,update,delete,truncate', publish_via_partition_root = 'true');
ALTER PUBLICATION pub_test_user ADD TABLE testuser.dbz_test_table;
ALTER PUBLICATION pub_test_user ADD TABLE testuser.dbz_test_table_npk;
ALTER PUBLICATION pub_test_user ADD TABLE testuser.dbz_test_table_uk;
ALTER PUBLICATION pub_test_user ADD TABLE testuser.dbz_test_pt_table;
ALTER PUBLICATION pub_test_user ADD TABLE testuser.dbz_test_pt_table_npk;
ALTER PUBLICATION pub_test_user ADD TABLE testuser.dbz_test_pt_table_uk;

--------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------

create table testuser.dbz_test_table_ddl (
    idx         varchar(6),
    initial_col numeric,
    constraint pk_dbz_test_table_ddl primary key (idx, initial_col)
);
alter table testuser.dbz_test_table_ddl replica identity full;
drop table testuser.dbz_test_table_ddl;

create table testuser.dbz_test_table_ddl_target (
    idx         varchar(6),
    initial_col numeric,
    added_col numeric,
    constraint pk_dbz_test_table_ddl_target primary key (idx, initial_col)
);
alter table testuser.dbz_test_table_ddl_target replica identity full;

--------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------

CREATE PROCEDURE testuser.demoPLSQL()
    LANGUAGE SQL
    AS $$
SELECT * from testuser.dbz_test_table;
$$;

------
create database dbsync;
