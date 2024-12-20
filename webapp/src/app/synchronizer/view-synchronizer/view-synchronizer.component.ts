import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Division, GroupTargetSync, SynchronizationInfo, SyncType, SynInfo} from 'src/app/libs/services/interface';
import {SyncService} from 'src/app/libs/services/syncDocument.service';
import {SweetAlert} from '../../libs/utils/sweetalert';

@Component({
  selector: 'app-view-synchronizer',
  templateUrl: './view-synchronizer.component.html',
  styleUrls: ['./view-synchronizer.component.scss'],
})
export class ViewSynchronizerComponent implements OnInit {
  @Input() topic;
  @Input() id;
  @Input() comparisonId;
  @Input() isSynRdId = false;

  invalidTopicName = false;
  isFromEdit = false;
  readonly PREFIX_READ_MODEL = 'RD_';
  readonly PREFIX_REVERSE = 'REV_';
  syncInfo: SynchronizationInfo;
  syncType: SyncType = SyncType.DT;
  loaded = false;
  division: string;
  synInfos: SynInfo[] = [];
  groupLinkSyn = [];

  constructor(
    private syncService: SyncService,
    public activeModal: NgbActiveModal
  ) {}

  ngOnInit(): void {
    this.getSync();
  }

  private getSync() {
    if (!this.topic && !this.id) {
      return;
    }

    const ob = this.id
      ? this.syncService.getTopicById(this.id)
      : this.syncService.getSynchronizerByTopic(this.topic);
    // this.authService.onLoading$.next(true);
    this.loaded = true;
    ob.subscribe(
      (data) => {
        // this.authService.onLoading$.next(false);
        this.syncInfo = data;
        this.syncType = data.syncType;
        this.division = data.division;
        if (data.syncType === SyncType.DT) {
          if (data.topicName.indexOf(this.PREFIX_REVERSE) === 0) {
            this.syncInfo.topicName = this.syncInfo.topicName.substr(
              this.PREFIX_REVERSE.length
            );
          }
          this.syncInfo.syncRdRequestParams.push({
            sourceDatabase: this.syncInfo.sourceDatabase,
            sourceSchema: this.syncInfo.sourceSchema,
            sourceTable: this.syncInfo.sourceTable,

            targetDatabase: this.syncInfo.targetDatabase,
            targetSchema: this.syncInfo.targetSchema,
            targetTable: this.syncInfo.targetTable,

            sourceQuery: this.syncInfo.sourceQuery,
            sourceCompareDatabase: this.syncInfo.sourceCompareDatabase,
            targetCompareDatabase: this.syncInfo.targetCompareDatabase,
            targetQuery: this.syncInfo.targetQuery,
            isComparable: this.syncInfo.isComparable,
            enableColumnComparison: this.syncInfo.enableColumnComparison,
          } as GroupTargetSync);
        } else if (data.syncType === SyncType.RD) {
          if (data.topicName.indexOf(this.PREFIX_READ_MODEL) === 0) {
            this.syncInfo.topicName = this.syncInfo.topicName.substr(
              this.PREFIX_READ_MODEL.length
            );
          }
          if (this.isSynRdId) {
            this.syncInfo.syncRdRequestParams = this.syncInfo.syncRdRequestParams.filter(x => x.comparisonInfoId === this.comparisonId);
          }
        }

        this.synInfos = this.syncService.synInfos;

        this.groupLinkSyn = [];
        if (data.linkSyncRequest && data.linkSyncRequest.ids.length > 0) {
          data.linkSyncRequest.ids.forEach((x) => {
            const item = this.synInfos.filter((info) => info.id === x);
            if (item && item.length > 0) {
              this.groupLinkSyn.push(item[0].topicName);
            }
          });
        }
      },
      (error) => {
        SweetAlert.notifyMessage('Error occurred', 'error');
      },
      () => {
        this.loaded = false;
      }
    );
  }

  detechPrimaryKeys(event, doc: SynchronizationInfo) {
    doc.detectKey = true;
    const topicName = this.syncInfo.topicName;
    const division = this.syncInfo.division;
    const sourceDatabase = this.syncInfo.sourceDatabase;
    const sourceSchema = this.syncInfo.sourceSchema;
    const sourceTable = this.syncInfo.sourceTable;
    const targetDatabase = this.syncInfo.targetDatabase;
    const targetSchema = this.syncInfo.targetSchema;
    const targetTable = this.syncInfo.targetTable;
    this.syncService.detectPrimaryKey(topicName, division, sourceDatabase, sourceSchema, sourceTable, targetDatabase, targetSchema, targetTable).subscribe(res => {
      if (res.status === 400) {
        SweetAlert.notifyMessage(res.message, 'error');
      } else if (res.status === 200) {
        if (res.body) {
          this.syncInfo.primaryKeysPartitionInfo = res.body;
          this.syncInfo.primaryKeys = res.body.primaryKeys;
          this.syncInfo.uniqueKeys = res.body.uniqueKeys;
          this.syncInfo.isPartitioned = res.body.isPartitioned;
          const message = [];
          if (this.syncInfo.isPartitioned) {
            message.push('partition');
          }
          if (this.syncInfo.primaryKeys !== '') {
            message.push('primary keys');
          }
          if (this.syncInfo.uniqueKeys !== '') {
            message.push('unique keys');
          }
          SweetAlert.notifyMessage(message.length === 0 ? 'detect nothing' : ('detected( ' +message.join(', ')+ ' )'));
        } else {
          SweetAlert.notifyMessage('Could not execute detect function!', 'error');
        }
      }
    }, ()=>{},() => {
      doc.detectKey = false;
    });
  }

  public get SyncType() {
    return SyncType;
  }
  public get Division() {
    return Division;
  }
}
