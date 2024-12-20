import { NgbModal, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { AuthService } from '../libs/services/auth.service';
import { SweetAlert } from '../libs/utils/sweetalert';
import { Component, OnInit, ViewChild } from '@angular/core';
import { ComparisonService } from '../libs/services/comparison.service';
import { RedoLogService } from '../libs/services/redolog.service';
import { CurrentScnInfo, LogMnrContentResult, LogMnrContents, LogMnrContentsTables, WrapLogMnrContent } from '../libs/services/interface';
import * as copy from 'copy-to-clipboard';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { NgbDateCustomParserFormatter } from '../libs/utils/helper';

@Component({
  selector: 'app-redo-log',
  templateUrl: './redo-log.component.html',
  styleUrls: ['./redo-log.component.scss']
})
export class RedoLogComponent implements OnInit {

  @ViewChild('modalDetailReUndoLog', {static:true}) modalDetailReUndoLog;
  detailReUndoLog: LogMnrContentResult;
  totalPage: number = 0;
  currentPage = 1;
  pageSize = 20;

  sourceDBs = [];
  sourceDBsForLogMnrContents = [];
  sourceSchemas = [];

  selectedSourceDB = '';
  selectedSourceSchema = '';
  allTables: LogMnrContentsTables[] = [];

  timeToSNC = '';
  timeHHMMSS;
  // currentScn = '';
  // findTime = '';
  timeToSCN_SCN = '';

  scnToTime = '';
  scnToTime_Time = '';
  currentScnInfo: CurrentScnInfo;
  logmrnContent: LogMnrContents;
  logContentsResult: WrapLogMnrContent;

  constructor(private comparisonService: ComparisonService, private redoLogService: RedoLogService,
              private authService: AuthService, private modalService: NgbModal, private formater: NgbDateCustomParserFormatter) {
    this.logmrnContent = {
      startScn: '',
      endScn: '',
      operationTypes: [],
      db: '',
      schema: '',
      tables: []
    } as LogMnrContents;
   }

  ngOnInit(): void {
    this.loadViewInfoForSearching();
  }

  private loadViewInfoForSearching(): void {
    this.comparisonService.getViewInfoForSearching().subscribe((data) => {
      if (data) {
        const sourceDBSet = new Set();
        const sourceSchemasSet = new Set();
        const targetDBstSet = new Set();

        data.forEach((dt) => {

          sourceDBSet.add(
            dt.syncRdId == null ? dt.sourceDatabase : dt.rdSourceDatabase
          );
          sourceSchemasSet.add(
            dt.syncRdId == null ? dt.sourceSchema : dt.rdSourceSchema
          );

          targetDBstSet.add(
            dt.syncRdId == null ? dt.targetDatabase : dt.rdTargetDatabase
          );
        });
        sourceDBSet.forEach((sdb) => this.sourceDBs.push(sdb));
        this.sourceDBsForLogMnrContents = this.sourceDBs;
        sourceSchemasSet.forEach((sss) => this.sourceSchemas.push(sss));
      }
    });
  }

  getCurrentSCN() {
    if (this.selectedSourceDB.trim() === '') {
      SweetAlert.notifyMessage('Source DB is required!', 'error');
      return;
    }
    this.authService.onLoading$.next(true);
    this.redoLogService.getCurrentSCN(this.selectedSourceDB).subscribe(res => {
      this.authService.onLoading$.next(false);
      if (res && res.status === 200) {
        this.currentScnInfo = res.body;
      } else {
        SweetAlert.notifyMessage(res.message, 'error');
      }
    });
  }

  getTimestampByScn() {
    if (this.selectedSourceDB.trim() === '') {
      SweetAlert.notifyMessage('Source DB is required!', 'error');
      return;
    }
    if (this.scnToTime.trim() === '') {
      SweetAlert.notifyMessage('SCN is required!', 'error');
      return;
    }
    // if (!Number.isInteger(this.scnToTime)) {
    //   SweetAlert.notifyMessage('Invalid SCN!. It should be a number', 'error');
    //   return;
    // }
    this.authService.onLoading$.next(true);
    this.redoLogService.getTimestampByScn(this.selectedSourceDB, this.scnToTime)
      .pipe(catchError(err => {
          SweetAlert.notifyMessage(err, 'error');
          this.authService.onLoading$.next(false);
          return throwError(err);
        })
      )
      .subscribe(res => {
        this.authService.onLoading$.next(false);
        if (res && res.status === 200) {
          this.timeToSCN_SCN = res.body;
        } else {
          SweetAlert.notifyMessage(res.message, 'error');
        }
    })
  }

  loadResultByDate(date: NgbDateStruct): void {
    this.timeToSNC = this.formater.format(date);
  }

  getScnByTimestamp() {
    if (this.selectedSourceDB.trim() === '') {
      SweetAlert.notifyMessage('Source DB is required!', 'error');
      return;
    }
    if (!this.timeToSNC) {
      SweetAlert.notifyMessage('Date is required!', 'error');
      return;
    }
    console.log(this.timeToSNC);

    if (!this.timeHHMMSS) {
      SweetAlert.notifyMessage('HH:mm:ss is required!', 'error');
      return
    }
    // dateTime: 2022-05-31T10:29:03
    const dateTime = this.timeToSNC + 'T' + this.autoFill2Character(this.timeHHMMSS.hour) + ':' + this.autoFill2Character(this.timeHHMMSS.minute) + ':' + this.autoFill2Character(this.timeHHMMSS.second);

    this.authService.onLoading$.next(true);
    this.redoLogService.getScnByTimestamp(this.selectedSourceDB, dateTime)
    .pipe(catchError(err => {
        SweetAlert.notifyMessage(err, 'error');
        this.authService.onLoading$.next(false);
        return throwError(err);
      })
    )
    .subscribe(res => {
      this.authService.onLoading$.next(false);
      if (res && res.status === 200) {
        this.scnToTime_Time = res.body;
      } else {
        SweetAlert.notifyMessage(res.message, 'error');
      }
    })
  }

  private autoFill2Character(value) {
    return value.toString().length === 1 ? '0' + value : value;
  }

  onChangeOperation(event) {
    const checked = event.target.checked;
    if (checked) {
      this.logmrnContent.operationTypes.push(event.target.value);
    } else {
      this.logmrnContent.operationTypes = this.logmrnContent.operationTypes.filter(x => x !== event.target.value);
    }
  }

  onGetTablesByDbAndSchema() {
    if (this.logmrnContent.db.trim() !== '' && this.logmrnContent.schema !== '') {
      // call api
      this.authService.onLoading$.next(true);
      this.redoLogService.getTablesByDbAndSchema(this.logmrnContent.db, this.logmrnContent.schema)
      .pipe(catchError(err => {
        SweetAlert.notifyMessage(err, 'error');
        return throwError(err);
      }))
      .subscribe(res => {
        this.authService.onLoading$.next(false);
        if (res && res.status === 200) {
          this.allTables = res.body.map(x => {
            return {
              name: x,
              checked: false,
            } as LogMnrContentsTables
          });
        }
      });
    }
  }

  onCheckAllTables(event) {
    if (this.allTables.length === 0) {
      return;
    }
    this.allTables.forEach(x => x.checked = event.target.checked);
  }

  searchLogMnrContents() {
    // if (!Number.isInteger(this.logmrnContent.startScn)) {
    //   SweetAlert.notifyMessage('Invalid Start SCN!. It should be a number', 'error');
    //   return;
    // }
    // if (!Number.isInteger(this.logmrnContent.endScn)) {
    //   SweetAlert.notifyMessage('Invalid End SCN!. It should be a number', 'error');
    //   return;
    // }
    if (this.logmrnContent.db.toString().trim() === '') {
      SweetAlert.notifyMessage('Source DB is required!', 'error');
      return;
    }
    if (!this.logmrnContent.startScn) {
      SweetAlert.notifyMessage('Start SCN is required!', 'error');
      return;
    }
    if (!this.logmrnContent.endScn) {
      SweetAlert.notifyMessage('End SCN is required!', 'error');
      return;
    }

    if (this.allTables.length > 0) {
      this.logmrnContent.tables = this.allTables.filter(x => x.checked).map(x => x.name);
    }
    // reset data
    this.logContentsResult = null;
    this.detailReUndoLog = null;
    this.totalPage = 0;
    this.authService.onLoading$.next(true);
    this.redoLogService.searchLogMnrContents(this.logmrnContent.db, this.logmrnContent.schema, this.logmrnContent.startScn,
      this.logmrnContent.endScn, this.logmrnContent.operationTypes, this.logmrnContent.tables, this.totalPage, this.currentPage, this.pageSize)
      .pipe(catchError(err => {
          SweetAlert.notifyMessage(err, 'error');
          return throwError(err);
        })
      )
      .subscribe(res => {
        this.authService.onLoading$.next(false);
        if (res && res.status === 200) {
          this.logContentsResult = res.body;
          this.logContentsResult.logContents.forEach(x => x.selected=false);
          if (this.totalPage <= 0) {
            this.totalPage = Math.ceil(this.logContentsResult.count / this.pageSize);
          }
        } else {
          SweetAlert.notifyMessage(res.message, 'error');
        }
      }, err => {
        this.authService.onLoading$.next(false);
        SweetAlert.notifyMessage('Error!', 'error');
      })
  }

  onShowReUnDoLog(doc: LogMnrContentResult) {
    this.logContentsResult.logContents.forEach(x => x.selected=false);
    doc.selected = true;
    this.detailReUndoLog = doc;
    // this.modalService.open(this.modalDetailReUndoLog, { size: 'xl' });
  }

  onCloseModal(): void {
    this.modalService.dismissAll();
  }

  copy(text): void {
    copy(text);
  }

  handleChangePageSize(e: number): void {
    this.pageSize = e;
    this.searchLogMnrContents();
  }

  handleChangePage(page: number): void {
    this.currentPage = page;
    this.searchLogMnrContents();
  }

}
