I. enable wal_level
1 Set wal_level with logical

1.1 Execute query "ALTER SYSTEM SET wal_level = logical" in execute script
	Or modify postgresql.conf configuration file and set wal_level = logical
1.2 Restart postgresql database


2. Enable full log in the case Update(before) or Delete
	ALTER TABLE imcsuser.pt_ab_album_group REPLICA IDENTITY FULL
    (You should define a list of table need to enable full log and execute)

II. create a folder plugin
- mkdir  /kafka_2.12-3.2.1/plugin

III. Edit config: connect-distributed.properties
- edit file: /kafka_2.12-3.2.1/config/connect-distributed.properties
- add text: plugin.path=./plugin/

IV. start servers:
./bin/zookeeper-server-start.sh config/zookeeper.properties
./bin/kafka-server-start.sh config/server.properties
./bin/connect-distributed.sh config/connect-distributed.properties

V. create a connector
API: localhost:8083/connectors
body:
{
	"name": "rd_msa_imcsuser",
	"config": {
		"connector.class": "io.debezium.connector.postgresql.PostgresConnector",
		"database.hostname": "rds-prd-iptv-prd-iptvrs-01.cluster-ca6s2a679eb7.ap-northeast-2.rds.amazonaws.com",
		"database.port": "5432",
		"database.user": "postgres",
		"database.password": "postgres",
		"database.dbname": "msa_mylgdb",
		"database.server.name": "RD_msa_mylgdb",
		"table.include.list": "imcsuser.pt_ab_album_group,imcsuser.pt_ab_album_poster,imcsuser.pt_ab_album_img,imcsuser.pt_ab_album_sub,imcsuser.pt_ab_album_person_map,imcsuser.pt_cd_person_vod,imcsuser.pt_ab_album_platform,imcsuser.pt_ab_test_mst,imcsuser.pt_ab_test_map,imcsuser.pt_la_album_group,imcsuser.pt_la_album_group_sub,imcsuser.pt_la_album_poster,imcsuser.pt_la_album_img,imcsuser.pt_la_content_badge,imcsuser.pt_la_album_index,imcsuser.pt_la_album_info,imcsuser.pt_la_album_sub,imcsuser.pt_cd_imcs_cd,imcsuser.pt_cd_genre,imcsuser.pt_la_album_person_map,imcsuser.pt_cd_person_vod,imcsuser.pt_la_album_platform,imcsuser.pt_wc_watcha_rating,imcsuser.pt_wc_cine_rating,imcsuser.pt_la_album_sub,imcsuser.pt_la_album_pr,imcsuser.pt_la_album_smi,imcsuser.pt_la_album_relation,imcsuser.pt_la_asset_info,imcsuser.pt_cd_com_cd,imcsuser.pt_cd_cp_mst,imcsuser.pt_lv_dong_info,imcsuser.pt_lv_node_info,imcsuser.pt_pd_package_detail,imcsuser.pt_pd_package_united,imcsuser.pt_pd_package_sub,imcsuser.pt_pd_package_map,imcsuser.pt_pd_package_relation_united,imcsuser.pt_la_season_series_map",
		"plugin.name": "pgoutput",
		"snapshot.mode": "never",
		"decimal.handling.mode": "double",
		"time.precision.mode": "connect",
		"binary.handling.mode": "bytes",
		"slot.name": "rd_msa_imcsuser_1",
		"tombstones.on.delete":"false"
	}
}