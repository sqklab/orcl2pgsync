import { Component, OnInit } from '@angular/core';
import { AuthService } from 'src/app/libs/services/auth.service';
import { KafkaProducerService } from 'src/app/libs/services/kafkaproducer.service';
import StorageService from 'src/app/libs/services/storage.service';
import { SweetAlert } from 'src/app/libs/utils/sweetalert';
import { KafkaPostgres, KafkaOracle } from '../kafka.model';
import _ from 'lodash';
import { ConnectorService } from 'src/app/libs/services/connectors.service';

@Component({
  selector: 'app-kafka-send-msg',
  templateUrl: './kafka-send-msg.component.html',
  styleUrls: ['./kafka-send-msg.component.scss'],
})
export class KafkaSendMessageComponent implements OnInit {
  postgresInfo: KafkaPostgres;
  oracleInfo: KafkaOracle;
  responseMsg = '';
  fetchBy = 'connect';
  topics = [];
  active = '1';

  invalidConnectorPostgres = false;
  invalidConnectorOracle = false;

  connectorsForOracle: any[] = [];
  connectorsForPostgres: any[] = [];
  mapConnectorsForOracle = new Map();
  mapConnectorsForPostgres = new Map();

  constructor(
    private authService: AuthService,
    private kafkaProducerAPI: KafkaProducerService,
    protected storage: StorageService,
    private dbConnectorService: ConnectorService,
  ) {
    this.postgresInfo = {
      topic: 'POSTGRES',
      transaction_id: null,
      lsn_proc: null,
      lsn_commit: null,
      lsn: null,
      txId: null,
      ts_usec: null,
      message: null,
      connectorName: null,
      dbServerName: null
    } as KafkaPostgres;
    this.oracleInfo = {
      topic: 'ORACLE',
      commit_scn: null,
      transaction_id: null,
      snapshot_pending_tx: null,
      snapshot_scn: null,
      scn: null,
      message: null,
      connectorName: null,
      dbServerName: null
    } as KafkaOracle;
  }

  ngOnInit(): void {
    this.getConnectors();
  }

  private getConnectors(): void {
    this.authService.onLoading$.next(true);
    this.dbConnectorService.refresh().subscribe(res => {
        this.authService.onLoading$.next(false);
        if (res && res.status === 200) {
          if (res.body.length > 0) {
            res.body.forEach(x => {
              const item = new Map(Object.entries(x.info.config));
              if (item.get('connector.class').indexOf('oracle') >= 0) {
                const name = x.info.name;
                const dbServerName = x.info.config['database.server.name'];
                this.mapConnectorsForOracle.set(name, dbServerName);
                this.connectorsForOracle.push(name);
              } else if (item.get('connector.class').indexOf('postgresql') >= 0) {
                const name = x.info.name;
                const dbServerName = x.info.config['database.server.name'];
                this.mapConnectorsForPostgres.set(name, dbServerName);
                this.connectorsForPostgres.push(name);
              }
            })
          }
        } else if (res.status === 400) {
          console.error(res.message);
        }
      },
      (err) => {
        this.authService.onLoading$.next(false);
      }
    );
  }

  onSendPostgres(): void {
    // this.postgresInfo.lsn = this.postgresInfo.lsn.trim();
    this.postgresInfo.topic = this.postgresInfo.topic.trim();
    // this.postgresInfo.lsn_commit = this.postgresInfo.lsn_commit.trim();
    // this.postgresInfo.lsn_proc = this.postgresInfo.lsn_proc.trim();
    // this.postgresInfo.transaction_id = this.postgresInfo.transaction_id.trim();
    // this.postgresInfo.ts_usec = this.postgresInfo.ts_usec.trim();
    // this.postgresInfo.txId = this.postgresInfo.txId.trim();

    this.invalidConnectorPostgres = false;
    if (!this.postgresInfo.lsn && !this.postgresInfo.lsn_commit && !this.postgresInfo.lsn_proc &&
      !this.postgresInfo.transaction_id && !this.postgresInfo.ts_usec && !this.postgresInfo.txId) {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Infomation',
        message: 'At least one field valid which filled data!',
        type: 'warning',
        confirm: () => {},
      });
      return;
    }
    if (!this.postgresInfo.connectorName || this.postgresInfo.connectorName === '') {
      this.invalidConnectorPostgres = true;
      return;
    }

    this.responseMsg = '';
    this.authService.onLoading$.next(true);

    this.kafkaProducerAPI.send(this.postgresInfo).subscribe(res => {
      this.authService.onLoading$.next(false);
      if (res && res.status === 200) {
        SweetAlert.notifyMessageAlwaysShow('Send Content done!');
      } else {
        this.responseMsg = res.message;
      }
    },
    err => this.authService.onLoading$.next(false),
    () => this.authService.onLoading$.next(false)
    );
  }

  onSendOracle(): void {
    // this.oracleInfo.topic = this.oracleInfo.topic.trim();
    // this.oracleInfo.commit_scn = this.oracleInfo.commit_scn.trim();
    // this.oracleInfo.transaction_id = this.oracleInfo.transaction_id.trim();
    // this.oracleInfo.snapshot_pending_tx = this.oracleInfo.snapshot_pending_tx.trim();
    // this.oracleInfo.snapshot_scn = this.oracleInfo.snapshot_scn.trim();
    // this.oracleInfo.scn = this.oracleInfo.scn.trim();

    this.invalidConnectorOracle = false;
    if (!this.oracleInfo.commit_scn && !this.oracleInfo.transaction_id && !this.oracleInfo.snapshot_pending_tx && !this.oracleInfo.snapshot_scn && !this.oracleInfo.scn) {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Infomation',
        message: 'At least one field valid which filled data!',
        type: 'warning',
        confirm: () => {},
      });
      return;
    }
    if (!this.oracleInfo.connectorName || this.oracleInfo.connectorName === '') {
      this.invalidConnectorOracle = true;
      return;
    }

    this.responseMsg = '';
    this.authService.onLoading$.next(true);

    this.kafkaProducerAPI.send(this.oracleInfo).subscribe(res => {
      this.authService.onLoading$.next(false);
      if (res && res.status === 200) {
        SweetAlert.notifyMessageAlwaysShow('Send Content done!');
      } else {
        this.responseMsg = res.message;
      }
    },
    err => this.authService.onLoading$.next(false),
    () => this.authService.onLoading$.next(false)
    );
  }

  buildMessageOracle() {
    // this.oracleInfo.topic = this.oracleInfo.topic.trim();
    // this.oracleInfo.commit_scn = this.oracleInfo.commit_scn.trim();
    // this.oracleInfo.transaction_id = this.oracleInfo.transaction_id.trim();
    // this.oracleInfo.snapshot_pending_tx = this.oracleInfo.snapshot_pending_tx.trim();
    // this.oracleInfo.snapshot_scn = this.oracleInfo.snapshot_scn.trim();
    // this.oracleInfo.scn = this.oracleInfo.scn.trim();

    const msg = {
      commit_scn: this.oracleInfo.commit_scn,
      transaction_id: this.oracleInfo.transaction_id,
      snapshot_pending_tx: this.oracleInfo.snapshot_pending_tx,
      snapshot_scn: this.oracleInfo.snapshot_scn,
      scn: this.oracleInfo.scn,
    } as KafkaOracle;
    this.oracleInfo.message = msg;
  }

  buildMessagePostgres() {
    this.postgresInfo.topic = this.postgresInfo.topic.trim();
    // this.postgresInfo.lsn_proc = this.postgresInfo.lsn_proc.trim();
    // this.postgresInfo.transaction_id = this.postgresInfo.transaction_id.trim();
    // this.postgresInfo.lsn_commit = this.postgresInfo.lsn_commit.trim();
    // this.postgresInfo.lsn = this.postgresInfo.lsn.trim();
    // this.postgresInfo.txId = this.postgresInfo.txId.trim();
    // this.postgresInfo.ts_usec = this.postgresInfo.ts_usec.trim();

    const msg = {
      transaction_id: this.postgresInfo.transaction_id,
      lsn_proc: this.postgresInfo.lsn_proc,
      lsn_commit: this.postgresInfo.lsn_commit,
      lsn: this.postgresInfo.lsn,
      txId: this.postgresInfo.txId,
      ts_usec: this.postgresInfo.ts_usec,
    } as KafkaPostgres;
    this.postgresInfo.message = msg;
  }

  buildKeyForPostGres(event) {
    // this.postgresInfo.connectorName = this.postgresInfo.connectorName.trim();
    // this.postgresInfo.dbServerName = this.postgresInfo.dbServerName.trim();
    const arr = [];
    this.postgresInfo.connectorName = event;
    arr.push(this.postgresInfo.connectorName);
    arr.push({
      server: this.mapConnectorsForPostgres.get(event)//this.postgresInfo.dbServerName
    });
    this.postgresInfo.key = arr;
  }

  buildKeyForOracle(event) {
    // this.oracleInfo.connectorName = this.oracleInfo.connectorName.trim();
    // this.oracleInfo.dbServerName = this.oracleInfo.dbServerName.trim();
    const arr = [];
    this.oracleInfo.connectorName = event;
    arr.push(this.oracleInfo.connectorName);
    arr.push({
      server: this.mapConnectorsForOracle.get(event) // this.oracleInfo.dbServerName
    });
    this.oracleInfo.key = arr;
  }
}
