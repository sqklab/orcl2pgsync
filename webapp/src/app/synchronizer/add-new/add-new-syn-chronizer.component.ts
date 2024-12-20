import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, of, OperatorFunction, Subject } from 'rxjs';
import {
  debounceTime,
  distinctUntilChanged,
  filter,
  map,
} from 'rxjs/operators';
import { AuthService } from 'src/app/libs/services/auth.service';
import {
  Division,
  GroupLinkSync,
  GroupTargetSync,
  LinkSyncRequest,
  SynchronizationInfo,
  SyncType,
  SynInfo,
} from 'src/app/libs/services/interface';
import { SyncService } from 'src/app/libs/services/syncDocument.service';
import { SweetAlert } from 'src/app/libs/utils/sweetalert';

@Component({
  selector: 'app-add-new-syn-chronizer',
  templateUrl: './add-new-syn-chronizer.component.html',
  styleUrls: ['./add-new-syn-chronizer.component.scss'],
})
export class AddNewSynChronizerComponent implements OnInit {
  invalidTopicName = false;
  id: number;

  isFromEdit = false;
  title = 'Add new Synchronizer';

  readonly PREFIX_READ_MODEL = 'RD_';
  readonly PREFIX_REVERSE = 'REV_';

  syncInfo: SynchronizationInfo;

  syncType: SyncType = SyncType.DT;
  division: string = Division.Oracle2Postgres;
  synInfos: SynInfo[] = [];
  groupLinkSyn: GroupLinkSync[] = [];

  constructor(
    private syncService: SyncService,
    private authService: AuthService,
    private router: Router,
    private activeRouter: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.isFromEdit = this.activeRouter.snapshot.queryParams['isFromEdit'];
    if (this.isFromEdit) {
      this.title = 'Edit Synchronizer';
      this.id = this.activeRouter.snapshot.queryParams['id'];
      this.getSyncById();
    } else {
      this.loadTopicNames();
      this.initGroupSync();
    }
  }

  removeNewLinkSync(index) {
    this.groupLinkSyn.splice(index, 1);
  }

  addNewLinkSyn() {
    this.groupLinkSyn.push({
      model: {},
    } as GroupLinkSync);
  }

  search: OperatorFunction<string, readonly { id; topicName }[]> = (
    text$: Observable<string>
  ) =>
    text$.pipe(
      debounceTime(200),
      distinctUntilChanged(),
      filter((term) => term.length >= 2),
      map((term) =>
        this.synInfos
          .filter((state) => new RegExp(term, 'mi').test(state.topicName))
          .slice(0, 10)
      )
    );

  formatter = (result: SynInfo) => result.topicName;

  loadTopicNames(): Observable<SynInfo[]> {
    this.synInfos = this.syncService.synInfos;
    if (this.synInfos && this.synInfos.length > 0) {
      this.synInfos = this.synInfos.filter((x) => x.id !== +this.id);
      return of(this.synInfos);
    }
    const subject = new Subject<SynInfo[]>();
    this.syncService.getTopicNames().subscribe((res) => {
      if (res) {
        this.synInfos = res;
        this.synInfos = this.synInfos.filter((x) => x.id !== +this.id);
        subject.next(this.synInfos);
        subject.complete();
      }
    });
    return subject;
  }

  private getSyncById() {
    if (this.id) {
      this.authService.onLoading$.next(true);
      this.syncService.getTopicById(this.id).subscribe((data) => {
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
        }
        this.groupLinkSyn = [];
        this.loadTopicNames().subscribe((topics) => {
          if (data.linkSyncRequest && data.linkSyncRequest.ids.length > 0) {
            data.linkSyncRequest.ids.forEach((x) => {
              const name = this.synInfos.filter((info) => info.id === x) || [];
              const model = {
                id: x,
                topicName: name.length > 0 ? name[0].topicName : '',
              };
              this.groupLinkSyn.push({
                model,
              } as GroupLinkSync);
            });
          }
        });
        this.authService.onLoading$.next(false);
      });
    }
  }

  initGroupSync() {
    const groupTargetInfo = [
      {
        targetDatabase: '',
        targetSchema: '',
        targetTable: '',

        sqlInsert: '',
        sqlUpdate: '',
        sqlDelete: '',
        sourceQuery: '',
        targetQuery: '',
        sourceCompareDatabase: '',
        targetCompareDatabase: '',
        division: '',
        numberRows: 2,
        invalidTargetDB: false,
        invalidTargetSchema: false,
        invalidTargetTable: false,
        invalidSQLCreate: false,
        invalidSQLUpdate: false,
        invalidSQLDelete: false,
      } as GroupTargetSync,
    ];
    this.syncInfo = {
      sourceDatabase: '',
      sourceSchema: '',
      sourceTable: '',

      targetDatabase: '',
      targetSchema: '',
      targetTable: '',
      sourceQuery: '',
      targetQuery: '',
      sourceCompareDatabase: '',
      targetCompareDatabase: '',
      topicName: '',
      state: 0,
      synchronizerName: '',
      logFile: '',
      numberOfError: 0,
      checked: false,
      syncType: SyncType.DT,
      division: Division.Oracle2Postgres,
      syncRdRequestParams: groupTargetInfo,
      consumerGroup: '',
    } as SynchronizationInfo;
  }

  addNewGroup() {
    const newGroupTargetInfo = {
      targetDatabase: '',
      targetSchema: '',
      targetTable: '',

      sqlInsert: '',
      sqlUpdate: '',
      sqlDelete: '',

      division: '',
      numberRows: 2,
      invalidTargetDB: false,
      invalidTargetSchema: false,
      invalidTargetTable: false,
      invalidSQLCreate: false,
      invalidSQLUpdate: false,
      invalidSQLDelete: false,
    } as GroupTargetSync;

    this.syncInfo.syncRdRequestParams.push(newGroupTargetInfo);
  }

  selectReadModel() {
    this.syncInfo.syncType = this.syncType = SyncType.RD;
    this.syncInfo.division = this.division = Division.Custom;
    this.syncInfo.syncRdRequestParams = [
      {
        sourceDatabase: this.syncInfo.sourceDatabase,
        sourceSchema: this.syncInfo.sourceSchema,
        sourceTable: this.syncInfo.sourceTable,

        targetDatabase: this.syncInfo.targetDatabase,
        targetSchema: this.syncInfo.targetSchema,
        targetTable: this.syncInfo.targetTable,

        sqlInsert: '',
        sqlUpdate: '',
        sqlDelete: '',

        numberRows: 2,
      } as GroupTargetSync,
    ];
    this.syncInfo.syncType = this.syncType;
    this.syncInfo.division = this.division;
  }

  selectDomainTable() {
    this.syncInfo.syncType = this.syncType = SyncType.DT;
    this.syncInfo.division = this.division = Division.Oracle2Postgres;
    // if (this.hasPrefixRD(this.syncInfo.topicName)) {
    //   this.syncInfo.topicName = this.syncInfo.topicName.substr(this.PREFIX_READ_MODEL.length);
    // }
    this.syncInfo.syncRdRequestParams = [this.syncInfo.syncRdRequestParams[0]];
    this.syncInfo.sourceDatabase =
      this.syncInfo.syncRdRequestParams[0].sourceDatabase;
    this.syncInfo.sourceSchema =
      this.syncInfo.syncRdRequestParams[0].sourceSchema;
    this.syncInfo.sourceTable =
      this.syncInfo.syncRdRequestParams[0].sourceTable;
    this.syncInfo.isComparable =
      this.syncInfo.syncRdRequestParams[0].isComparable;
    this.syncInfo.enableColumnComparison =
      this.syncInfo.syncRdRequestParams[0].enableColumnComparison;

    this.syncInfo.syncType = this.syncType;
    this.syncInfo.division = this.division;
  }

  selectOracle2Postgres() {
    this.syncInfo.division = this.division = Division.Oracle2Postgres;
  }

  selectPostgres2Postgres() {
    this.syncInfo.division = this.division = Division.Postgres2Postgres;
  }

  selectPostgres2Oracle() {
    this.syncInfo.division = this.division = Division.Postgres2Oracle;
  }

  selectCustom() {
    this.syncInfo.division = this.division = Division.Custom;
  }

  private hasPrefixRD(syncName: string): boolean {
    if (!syncName) return false;
    if (syncName.length < this.PREFIX_READ_MODEL.length) return false;
    return (
      syncName.substring(0, this.PREFIX_READ_MODEL.length) ===
      this.PREFIX_READ_MODEL
    );
  }

  private hasPrefixREV(syncName: string): boolean {
    if (!syncName) return false;
    if (syncName.length < this.PREFIX_REVERSE.length) return false;
    return (
      syncName.substring(0, this.PREFIX_REVERSE.length) ===
      this.PREFIX_REVERSE
    );
  }

  public get SyncType() {
    return SyncType;
  }

  public get Division() {
    return Division;
  }

  upSert() {
    console.log('haha')
    this.invalidTopicName = false;
    // this.validateTopicName();
    if (!this.validateWordFormat(this.syncInfo.topicName)) {
      this.invalidTopicName = true;
    }
    if (this.invalidTopicName) {
      return;
    }
    this.validateSourceData();
    this.validateTargetData();
    // this.validateTargetSchema();
    // this.validateTargetTable();
    // this.validateSQL();
    let valid = true;
    this.syncInfo.syncRdRequestParams.forEach((item) => {
      const invalidTargetData =
        item.invalidTargetDB ||
        item.invalidTargetSchema ||
        item.invalidTargetTable;
      const invalidSourceData =
        item.invalidSourceDB ||
        item.invalidSourceSchema ||
        item.invalidSourceTable;
      if (invalidTargetData || invalidSourceData) {
        valid = false;
      }
    });
    if (!valid) {
      return;
    }

    if (this.syncType === SyncType.DT) {
      this.syncInfo.targetDatabase =
        this.syncInfo.syncRdRequestParams[0].targetDatabase;
      this.syncInfo.targetSchema =
        this.syncInfo.syncRdRequestParams[0].targetSchema;
      this.syncInfo.targetTable =
        this.syncInfo.syncRdRequestParams[0].targetTable;
      this.syncInfo.sourceDatabase =
        this.syncInfo.syncRdRequestParams[0].sourceDatabase;
      this.syncInfo.sourceSchema =
        this.syncInfo.syncRdRequestParams[0].sourceSchema;
      this.syncInfo.sourceTable =
        this.syncInfo.syncRdRequestParams[0].sourceTable;
      this.syncInfo.sourceQuery =
        this.syncInfo.syncRdRequestParams[0].sourceQuery;
      this.syncInfo.sourceCompareDatabase = this.syncInfo.syncRdRequestParams[0].sourceCompareDatabase;
      this.syncInfo.targetCompareDatabase = this.syncInfo.syncRdRequestParams[0].targetCompareDatabase;
      this.syncInfo.targetQuery =
        this.syncInfo.syncRdRequestParams[0].targetQuery;
      this.syncInfo.isComparable =
        this.syncInfo.syncRdRequestParams[0].isComparable;
      this.syncInfo.enableColumnComparison =
        this.syncInfo.syncRdRequestParams[0].enableColumnComparison;
      this.syncInfo.division = this.division;
    }
    if (
      this.syncType === SyncType.RD &&
      !this.hasPrefixRD(this.syncInfo.topicName)
    ) {
      this.syncInfo.topicName =
        this.PREFIX_READ_MODEL + this.syncInfo.topicName;
    }

    if (
      this.syncType === SyncType.DT && this.division === Division.Postgres2Oracle &&
      !this.hasPrefixREV(this.syncInfo.topicName)
    ) {
      this.syncInfo.topicName =
        this.PREFIX_REVERSE + this.syncInfo.topicName;
    }

    this.authService.onLoading$.next(true);
    // set linked synchronizer
    this.syncInfo.linkSyncRequest = {
      ids: this.groupLinkSyn.map((x) => x.model.id).filter((x) => x),
    } as LinkSyncRequest;

    console.log(this.syncInfo);
    if (this.isFromEdit) {
      this.update();
    } else {
      this.syncService.addNew(this.syncInfo).subscribe(
        (res) => {
          this.authService.onLoading$.next(false);
          if (res.status === 400) {
            SweetAlert.notifyMessage(res.message, 'error');
            return;
          } else {
            this.router.navigate(['/synchronize']);
            SweetAlert.notifyMessage('Create Synchronizer success');
          }
        },
        (err) => {
          this.authService.onLoading$.next(false);
          SweetAlert.notifyMessage('Create Synchronizer error', 'error');
        }
      );
    }
  }

  detechPrimaryKeys(event, doc: SynchronizationInfo) {
    doc.detectKey = true;
    this.invalidTopicName = false;
    // this.validateTopicName();
    if (!this.validateWordFormat(this.syncInfo.topicName)) {
      this.invalidTopicName = true;
    }
    if (this.invalidTopicName) {
      SweetAlert.notifyMessage('Invalid Parameters!', 'error');
      doc.detectKey = false;
      return;
    }
    this.validateSourceData();
    this.validateTargetData();
    // this.validateTargetSchema();
    // this.validateTargetTable();
    // this.validateSQL();
    let valid = true;
    this.syncInfo.syncRdRequestParams.forEach((item) => {
      const invalidTargetData =
        item.invalidTargetDB ||
        item.invalidTargetSchema ||
        item.invalidTargetTable;
      const invalidSourceData =
        item.invalidSourceDB ||
        item.invalidSourceSchema ||
        item.invalidSourceTable;
      if (invalidTargetData || invalidSourceData) {
        valid = false;
      }
    });
    if (!valid) {
      SweetAlert.notifyMessage('Invalid Parameters!', 'error');
      doc.detectKey = false;
      return;
    }
    if (this.syncType === SyncType.DT) {
      this.syncInfo.targetDatabase =
        this.syncInfo.syncRdRequestParams[0].targetDatabase;
      this.syncInfo.targetSchema =
        this.syncInfo.syncRdRequestParams[0].targetSchema;
      this.syncInfo.targetTable =
        this.syncInfo.syncRdRequestParams[0].targetTable;
      this.syncInfo.sourceDatabase =
        this.syncInfo.syncRdRequestParams[0].sourceDatabase;
      this.syncInfo.sourceSchema =
        this.syncInfo.syncRdRequestParams[0].sourceSchema;
      this.syncInfo.sourceTable =
        this.syncInfo.syncRdRequestParams[0].sourceTable;
      this.syncInfo.division = this.division;
    }

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
          if (this.syncInfo.primaryKeys!='') {
            message.push('primary keys');
          }
          if (this.syncInfo.uniqueKeys!='') {
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

  update() {
    this.syncService.updateTopic(this.syncInfo).subscribe(
      (res) => {
        this.authService.onLoading$.next(false);
        if (res.status === 400) {
          SweetAlert.notifyMessage(res.message, 'error');
        } else {
          this.router.navigate(['/synchronize']);
          SweetAlert.notifyMessage('Update Success!');
        }
      },
      (err) => {
        this.authService.onLoading$.next(false);
        SweetAlert.notifyMessage('Can not Update Synchronizer!', 'error');
      }
    );
  }

  removeRDItem(index) {
    if (this.syncInfo.syncRdRequestParams.length > 1) {
      this.syncInfo.syncRdRequestParams.splice(index, 1);
    }
  }

  onKeyUpTopicName() {
    this.invalidTopicName = false;
    if (!this.validateWordFormat(this.syncInfo.topicName)) {
      this.invalidTopicName = true;
    }
    if (this.syncInfo.topicName) {
      const arr = this.syncInfo.topicName.split('.');
      if (arr.length === 1) {
        this.syncInfo.sourceDatabase = this.removePrefixRDForSourceDatabase(
          arr[0]
        );
      }
      if (arr.length === 2) {
        this.syncInfo.sourceDatabase = this.removePrefixRDForSourceDatabase(
          arr[0]
        );
        this.syncInfo.sourceSchema = arr[1];
      }
      if (arr.length === 3) {
        this.syncInfo.sourceDatabase = this.removePrefixRDForSourceDatabase(
          arr[0]
        );
        this.syncInfo.sourceSchema = arr[1];
        this.syncInfo.sourceTable = arr[2];
      }
    }
    // this.syncInfo.synchronizerName = this.syncInfo.topicName;
  }

  // private validateTopicName() {
  //   if (this.syncType === SyncType.RD && this.syncInfo.topicName.indexOf(this.PREFIX_READ_MODEL) < 0) {
  //     this.invalidTopicName = true;
  //   } else {
  //     this.invalidTopicName = false;
  //   }
  // }

  private removePrefixRDForSourceDatabase(sourceDatabase: string) {
    if (this.syncType === SyncType.RD && sourceDatabase) {
      if (sourceDatabase.indexOf(this.PREFIX_READ_MODEL) >= 0) {
        sourceDatabase = sourceDatabase.substring(
          this.PREFIX_READ_MODEL.length
        );
      }
    }
    return sourceDatabase;
  }

  private removePrefixREVForSourceDatabase(sourceDatabase: string) {
    if (this.syncType === SyncType.DT && sourceDatabase) {
      if (sourceDatabase.indexOf(this.PREFIX_REVERSE) >= 0) {
        sourceDatabase = sourceDatabase.substring(
          this.PREFIX_REVERSE.length
        );
      }
    }
    return sourceDatabase;
  }

  validateSourceData() {
    this.syncInfo.syncRdRequestParams.forEach((item) => {
      if (!item.sourceDatabase || item.sourceDatabase.trim() === '') {
        item.invalidSourceDB = true;
      } else {
        item.invalidSourceDB = false;
      }
      if (!item.sourceSchema || item.sourceSchema.trim() === '') {
        item.invalidSourceSchema = true;
      } else {
        item.invalidSourceSchema = false;
      }
      if (!item.sourceTable || item.sourceTable.trim() === '') {
        item.invalidSourceTable = true;
      } else {
        item.invalidSourceTable = false;
      }
    });
  }

  validateTargetData() {
    this.syncInfo.syncRdRequestParams.forEach((item) => {
      if (!item.targetDatabase || item.targetDatabase.trim() === '') {
        item.invalidTargetDB = true;
      } else {
        item.invalidTargetDB = false;
      }
      if (!item.targetSchema || item.targetSchema.trim() === '') {
        item.invalidTargetSchema = true;
      } else {
        item.invalidTargetSchema = false;
      }
      if (!item.targetTable || item.targetTable.trim() === '') {
        item.invalidTargetTable = true;
      } else {
        item.invalidTargetTable = false;
      }
    });
  }

  validateTargetSchema() {
    this.syncInfo.syncRdRequestParams.forEach((item) => {
      if (!item.targetSchema || item.targetSchema.trim() === '') {
        item.invalidTargetSchema = true;
      } else {
        item.invalidTargetSchema = false;
      }
    });
  }

  validateTargetTable() {
    this.syncInfo.syncRdRequestParams.forEach((item) => {
      if (!item.targetTable || item.targetTable.trim() === '') {
        item.invalidTargetTable = true;
      } else {
        item.invalidTargetTable = false;
      }
    });
  }

  validateTargetDatabase() {
    // if (!this.syncInfo.targetDatabase || this.syncInfo.targetDatabase.trim() === '') {
    //   this.syncInfo. = true;
    // } else {
    //   this.syncInfo.invalidTargetDB = false;
    // }
    this.syncInfo.syncRdRequestParams.forEach((item) => {
      if (!item.targetDatabase || item.targetDatabase.trim() === '') {
        item.invalidTargetDB = true;
      } else {
        item.invalidTargetDB = false;
      }
    });
  }

  validateSQL() {
    if (this.syncInfo.syncType === SyncType.RD) {
      this.syncInfo.syncRdRequestParams.forEach((item) => {
        if (!item.sqlInsert || item.sqlInsert.trim() === '') {
          item.invalidSQLCreate = true;
        } else {
          item.invalidSQLCreate = false;
        }
        if (!item.sqlUpdate || item.sqlUpdate.trim() === '') {
          item.invalidSQLUpdate = true;
        } else {
          item.invalidSQLUpdate = false;
        }
        if (!item.sqlDelete || item.sqlDelete.trim() === '') {
          item.invalidSQLDelete = true;
        } else {
          item.invalidSQLDelete = false;
        }
      });
    }
  }

  validateWordFormat(word) {
    const regex = /^[a-zA-Z0-9#)\(._-\s]+$/g;
    return regex.test(word);
  }

  goBack() {
    window.history.back();
  }
}
