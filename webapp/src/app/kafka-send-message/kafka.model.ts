export interface KafkaMessage {
    topic: string;
    message: any;
    key: any[];
}

export interface KafkaPostgres extends KafkaMessage {
    transaction_id: number;
    lsn_proc: number;
    lsn_commit: number;
    lsn: number;
    txId: number;
    ts_usec: number;
    connectorName: string;
    dbServerName: string;
}

export interface KafkaOracle extends KafkaMessage {
    commit_scn: string;
    transaction_id: string;
    snapshot_pending_tx: string;
    snapshot_scn: string;
    scn: string;
    connectorName: string;
    dbServerName: string;
}
