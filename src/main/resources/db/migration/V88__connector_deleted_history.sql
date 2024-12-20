alter table tbl_db_connectors
    add deleted boolean;

drop index tbl_db_connectors_name_type_uindex;
