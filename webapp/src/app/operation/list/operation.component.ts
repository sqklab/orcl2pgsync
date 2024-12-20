import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Subject, timer } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from 'src/app/libs/services/auth.service';
import { ConnectorService } from 'src/app/libs/services/connectors.service';
import {
  CompareDiffItem, DiffDeleteReq, OperationDto, OperationSummary
} from 'src/app/libs/services/interface';
import StorageService from 'src/app/libs/services/storage.service';
import { SweetAlert } from 'src/app/libs/utils/sweetalert';
import { OperationService } from '../../libs/services/operation.service';
import { ColumnIdAutoCompleteComponent } from '../column-id-auto-complete/column-id-auto-complete.component';
import { ShowDetailRecordComponent } from '../show-detail-record/show-detail-record.component';

@Component({
  selector: 'app-operation',
  templateUrl: './operation.component.html',
  styleUrls: ['./operation.component.scss'],
})
export class OperationComponent implements OnInit, OnDestroy {
  mapState = new Map();

  totalPage = 0;
  currentPage = 1;
  pageSize = 20;
  active = '1'

  isSearching = false;
  isCorrecting = false;

  invalidOracleIdValue = false;

  tableNames = ['XCION_SBC_TBL_UNITED', 'PT_VO_BUY', 'PT_VO_WATCH_HISTORY'];
  sourceOracleDBs = [];
  oracleSchemas = [];
  postgresDBs = [];
  postgresSchemas = [];
  divisions = [];

  invalidOracleDB = false;
  invalidOracleSchema = false;
  invalidPostgresDB = false;
  invalidPostgresSchema = false;
  invalidTable = false;
  checkAll = false;
  correctionMsg = '';
  // loadFirstPage = true;
  username = '';
  showNoDiff = false;
  notiMsgRunning = 'System is finding different between 2 databases';
  primaryKeys = '';
  uniqueKeys = '';

  // sourceTable

  operationDto: OperationDto;
  // operationResponse: CompareDiffItem[];
  operationResponsePaged: CompareDiffItem[];
  headers = [];
  isSame = true;
  operationI = 0;
  operationU = 0;
  operationD = 0;
  totalOperation = 0;
  operationError: CompareDiffItem;
  operationSummary: OperationSummary;

  // loadSearchingDataSubject = new Subject();
  summarySubject = new Subject();

  @ViewChild('appColumnIdAutoComplete', {static:true}) appColumnIdAutoComplete: ColumnIdAutoCompleteComponent;
  @ViewChild('dialogDelete', {static:true}) dialogDelete;

  constructor(
    private authService: AuthService,
    private operationService: OperationService,
    private modalService: NgbModal,
    protected storage: StorageService,
    private dbConnectorService: ConnectorService,
  ) {
    this.operationDto = {
      sourceDatabase: '',
      sourceSchema: '',
      sourceSql: '',
      targetDatabase: '',
      targetSchema: '',
      targetSql: '',
      table: '',
      columnIdValue: '',
      whereStm: '',
      whereStmFillData: '',
      columnIdName: 'SA_ID',
      errorMessage: ''
    } as OperationDto;
  }

  ngOnInit(): void {
    this.username = this.authService.getUserLogged();
    this.loadViewInfoForSearching();
  }

  ngOnDestroy(): void {
      // this.loadSearchingDataSubject.next();
      this.summarySubject.next();
  }

  onCloseModal(): void {
    this.modalService.dismissAll();
  }

  buildQueryOracle(): void {
    const valid = this.validateInputs();
    if (!valid) {
      return;
    }
    // this.appColumnIdAutoComplete.bkSuggestValues = [];
    let query = 'SELECT * FROM ';
    // tslint:disable-next-line: max-line-length
    query +=
      this.operationDto.sourceSchema +
      '.' +
      this.operationDto.table +
      ' WHERE 1=1 ';
    if (this.operationDto.columnIdValue !== '') {
      query +=
        'AND ' + this.operationDto.columnIdName + ' =\'' + this.operationDto.columnIdValue + '\'';
    }
    query += ' ' + this.operationDto.whereStmFillData;

    this.operationDto.whereStm = query.substring(query.indexOf('WHERE'));
    this.operationDto.sourceSql = query.trim();
  }

  buildQueryPostgres(): void {
    const valid = this.validateInputs();
    if (!valid) {
      return;
    }
    this.operationDto.whereStm = this.operationDto.whereStmFillData;
    let query = 'SELECT * FROM ';
    // tslint:disable-next-line: max-line-length
    query +=
      this.operationDto.targetSchema +
      '.' +
      this.operationDto.table.toLowerCase() +
      ' WHERE 1=1 ';
    if (this.operationDto.columnIdValue !== '') {
      query +=
        'AND ' +
        this.operationDto.columnIdName.toLocaleLowerCase() +
        ' =\'' +
        this.operationDto.columnIdValue +
        '\'';
    }
    query += ' ' + this.operationDto.whereStmFillData;
    this.operationDto.whereStm = query.substring(query.indexOf('WHERE'));

    this.operationDto.targetSql = query.trim();
  }

  selectTable(event) {
    this.operationDto.table = event;
    // tslint:disable-next-line: max-line-length
    this.operationDto.table === 'XCION_SBC_TBL_UNITED' ? this.operationDto.columnIdName = 'PVS_SBC_CONT_NO' : this.operationDto.columnIdName = 'SA_ID';
    this.buildQueryOracle();
    this.buildQueryPostgres();
  }

  toArrayValueOfColumn(row: CompareDiffItem): any {
    const arr = [];
    const data = row.source ? row.source : row.target;
    if (row.operation === 'DELETE') { // if action is DELETE, should display postgres data
      this.headers.forEach((h) => {
        arr.push(data[h.toLowerCase()]);
      });
    } else {
      this.headers.forEach((h) => {
        arr.push(data[h]);
      });
    }
    return arr;
  }

  onClearOracleDB(): void {
    this.operationDto.sourceDatabase = '';
  }

  onClearOracleSchema(): void {
    this.operationDto.sourceSchema = '';
  }

  onClearPostgresDB(): void {
    this.operationDto.targetDatabase = '';
  }

  onClearPostgresSchema(): void {
    this.operationDto.targetSchema = '';
  }

  cancelSearching() {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Information',
      message: 'Are you sure to Cancel Searching ?',
      type: 'info',
      cancel: true,
      confirm: () => {
        this.operationService.cancelSearching(this.operationDto.table, this.username, this.operationDto.whereStm.trim()).subscribe(res => {
          this.isSearching = false;
          // this.loadSearchingDataSubject.next();
          this.summarySubject.next();
        })
      },
    });
  }

  handleCheckAll(): void {
    this.checkAll = !this.checkAll;
    this.operationResponsePaged.forEach(x => x.selected = this.checkAll);
  }

  onSearch(): void {
    const valid = this.validateInputs();
    if (!valid) {
      return;
    }
    // call api
    if (this.operationDto.columnIdValue === '' && this.operationDto.whereStm === '') {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Information',
        message: 'There is no condition in the query, it takes a long time to compare all record of tables. Are you sure?',
        type: 'info',
        cancel: true,
        confirm: () => {
          this.handleSearching();
        },
      });
    } else {
      this.handleSearching();
    }
  }

  private handleSearching() {

    this.operationService.checkOperation(this.operationDto.table, this.username, this.operationDto.whereStm.trim()).subscribe(res => {
      if (res.body) {
        this.operationSummary = res.body;
        if (res.body.state) {
          this.totalPage = 0;
          this.currentPage = 1;
          this.totalOperation = 0;
          this.loadSearchData();
        } else {
          if (res.body.isOutDate) { // outdate => research again
            this.totalPage = 0;
            this.currentPage = 1;
            this.totalOperation = 0;
            this.reSearch();
          } else {
            this.modalService.open(this.dialogDelete);
          }
        }
      } else { // not run before
        this.search();
      }
    })
  }

  search(): void {
    this.isSame = true;
    this.operationI = 0;
    this.operationU = 0;
    this.operationD = 0;
    this.totalOperation = 0;
    this.operationResponsePaged = [];
    this.currentPage = 1;
    this.authService.onLoading$.next(true);
    this.correctionMsg = '';
    this.isSearching = true;
    this.operationDto.sessionId = this.username;
    this.operationService.search(this.operationDto).subscribe(
      (res) => {
        this.authService.onLoading$.next(false);
        if (res && res.status === 200) {
          this.loadSearchData();
          SweetAlert.notifyMessageAlwaysShow(res.message);
        } else {
          this.correctionMsg = res.message;
          this.isSearching = false;
        }
      },
      (err) => {
        this.authService.onLoading$.next(false);
        SweetAlert.notifyMessageAlwaysShow(err, 'error');
        this.isSearching = false;
        this.correctionMsg = err;
      },
      () => {
        this.authService.onLoading$.next(false);
      }
    );
  }

  loadSearchData() {
    // this.loadFirstPage = true;
    this.correctionMsg = null;
    this.isSearching = true;
    this.showNoDiff = false;
    this.operationResponsePaged = [];
    this.totalPage = 0;
    this.totalOperation = 0;
    // timer(2000, 10000).pipe(
    //   takeUntil(this.loadSearchingDataSubject),
    // ).subscribe(t => {
    //   this.fetchData();
    // });

    // load summary
    timer(2000, 5000).pipe(
      takeUntil(this.summarySubject),
    ).subscribe(t => {
      this.operationService.getOperationSummary(this.operationDto.table, this.username, this.operationDto.whereStm).subscribe(res => {
        if (res && res.status === 200) {
          if (res.body) {
            if (!res.body.state) {
              // waiting for fetch data
              // timer(1000, 1000).subscribe(t => {
              //   this.fetchData();
              //   // this.loadSearchingDataSubject.next(); // stop load searching data
              //   this.summarySubject.next();
              //   this.isSearching = false;
              // });
              this.fetchData();
              this.summarySubject.next();
              this.isSearching = false;
            }
            this.operationD = res.body.delete ? res.body.delete : 0;
            this.operationI = res.body.insert ? res.body.insert : 0;
            this.operationU = res.body.update ? res.body.update : 0;
            // if (this.totalOperation < (this.operationD + this.operationI + this.operationU)) {
            if (this.totalOperation < this.pageSize && this.totalOperation < (this.operationD + this.operationI + this.operationU)) {
              // this.isSearching = true;
              this.fetchData();
            }
            this.totalOperation = this.operationD + this.operationI + this.operationU;
            this.totalPage = Math.ceil(this.totalOperation / this.pageSize);
          }
        } else {
          this.correctionMsg = res.message;
          this.isSearching = false;
        }
      })
    });
  }

  fetchData() {
    this.operationService.loadSearchingData(this.operationDto.sourceDatabase, this.operationDto.sourceSchema, this.operationDto.targetDatabase, this.operationDto.targetSchema
      ,this.operationDto.table, this.username, this.operationDto.whereStm.trim(), this.currentPage, this.pageSize, this.primaryKeys, this.uniqueKeys).subscribe(res => {
      if (res && res.status === 200) {
        this.operationResponsePaged = res.body.entities;
        this.primaryKeys = res.body.primaryKeys;
        this.uniqueKeys = res.body.uniqueKeys;
        if (this.operationResponsePaged.length === 0) {
          this.showNoDiff = true;
        } else if (res.body.entities.length > 0) {
          try {
              const firstRecord = res.body.entities[0];
              if (firstRecord.source) {
                this.headers = Object.keys(firstRecord.source);
              } else {
                this.headers = Object.keys(firstRecord.target);
                this.headers = this.headers.map(x => x.toUpperCase());
              }
          } catch (e) {
            console.error(e)
          }
          // this.loadSearchingDataSubject.next(); // stop load data, just fetch total page
          this.isSame = false;
          this.showNoDiff = false;
        }
      } else {
        this.correctionMsg = res.message;
        this.isSearching = false;
        this.showNoDiff = false;
        // this.loadSearchingDataSubject.next();
      }
    })
  }

  reSearch() {
    const body = {
      table: this.operationDto.table,
      session: this.username,
      where: this.operationDto.whereStm.trim()
    } as DiffDeleteReq;
    this.operationService.deleteProcess(body).subscribe(res => {
    });
    this.onCloseModal();
    this.operationService.deleteOperationTable(body).subscribe(res => {
      if (res.status === 200) {
        this.search();
      }
    });
  }

  onCorrection(batch = false): void {
    const selected = this.operationResponsePaged.filter(
      (x) => x.selected
      ) as CompareDiffItem[];
    if (selected.length === 0) {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Information',
        message: 'You should select one item!',
        type: 'info',
        confirm: () => {},
      });
    } else {
      const hasDeleteOp = selected.filter(x => x.operation === 'DELETE').length > 0;
      if (hasDeleteOp) {
        this.authService.onShowModal$.next({
          isShow: true,
          title: 'Information',
          message: 'Are you sure to delete different record?',
          type: 'info',
          cancel: true,
          confirm: () => {
            this.correction(selected);
          },
        });
      } else {
        this.correction(selected);
      }
    }
  }

  private correction(selected: CompareDiffItem[]): void {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Information',
      message: 'Correct data by using latest data in the source database(Oracle) to update the target database (Postgres)',
      type: 'info',
      cancel: true,
      confirm: () => {
        this.handleCorrectionResponseBatch(selected);
      },
    });
  }

  private handleCorrectionResponseBatch(selected: CompareDiffItem[]): void {
    this.authService.onLoading$.next(true);
    this.isCorrecting = true;
    this.correctionMsg = '';
    this.operationService.correctionBatch(selected, this.operationDto.sourceDatabase,
      this.operationDto.sourceSchema, this.operationDto.targetDatabase, this.operationDto.targetSchema, this.operationDto.table, this.username, this.operationDto.whereStm.trim())
      .subscribe(res => {
        this.authService.onLoading$.next(false);
        this.isCorrecting = false;
        if (res.body.exceptions.length > 0) {

        }
        this.operationService.getOperationSummary(this.operationDto.table, this.username, this.operationDto.whereStm).subscribe(res => {
          if (res && res.status === 200 && res.body) {
            this.operationD = res.body.delete ? res.body.delete : 0;
            this.operationI = res.body.insert ? res.body.insert : 0;
            this.operationU = res.body.update ? res.body.update : 0;
            this.totalOperation = this.operationD + this.operationI + this.operationU;
          }
        });
        this.handleChangePage(1);
        this.checkAll = false;
        SweetAlert.notifyMessageAlwaysShow('Correction done!');
      }, err => {
      this.correctionMsg = err;
      SweetAlert.notifyMessageAlwaysShow(err, 'error');
      this.authService.onLoading$.next(false);
      this.isCorrecting = false;
    },
    () => {
      this.authService.onLoading$.next(false);
      this.isCorrecting = false;
    });
  }

  showDetailRecord(doc: CompareDiffItem) {
    const modalRef = this.modalService.open(ShowDetailRecordComponent, {
      size: 'xl',
    });
    modalRef.componentInstance.doc = doc;
    modalRef.componentInstance.headers = this.headers;
  }

  checkStatusRow(doc: CompareDiffItem) {
    return doc.errorMessage && doc.errorMessage !== '';
  }

  onShowDetailError(doc: CompareDiffItem, viewDetaiError) {
    if (doc.errorMessage) {
      this.operationError = doc;
      this.modalService.open(viewDetaiError);
    }
  }

  validateInputs() {
    this.invalidOracleDB = false;
    this.invalidOracleSchema = false;
    this.invalidPostgresDB = false;
    this.invalidPostgresSchema = false;
    this.invalidTable = false;
    this.operationDto.sourceDatabase = this.operationDto.sourceDatabase ? this.operationDto.sourceDatabase.trim() : '';
    this.operationDto.sourceSchema = this.operationDto.sourceSchema ? this.operationDto.sourceSchema.trim() : '';
    this.operationDto.targetDatabase = this.operationDto.targetDatabase ? this.operationDto.targetDatabase.trim() : '';
    this.operationDto.targetSchema = this.operationDto.targetSchema ? this.operationDto.targetSchema.trim() : '';
    this.operationDto.table = this.operationDto.table ? this.operationDto.table.trim() : '';

    if (this.operationDto.sourceDatabase === '') {
      this.invalidOracleDB = true;
    }
    if (this.operationDto.sourceSchema === '') {
      this.invalidOracleSchema = true;
    }
    if (this.operationDto.targetDatabase === '') {
      this.invalidPostgresDB = true;
    }
    if (this.operationDto.targetSchema === '') {
      this.invalidPostgresSchema = true;
    }
    if (this.operationDto.table === '') {
      this.invalidTable = true;
    }
    return !(
      this.invalidOracleDB ||
      this.invalidOracleSchema ||
      this.invalidPostgresDB ||
      this.invalidPostgresSchema ||
      this.invalidTable
    );
  }

  loadSearchingData(){
    this.authService.onLoading$.next(true);
    this.operationService.loadSearchingData(this.operationDto.sourceDatabase, this.operationDto.sourceSchema, this.operationDto.targetDatabase, this.operationDto.targetSchema
      ,this.operationDto.table, this.username, this.operationDto.whereStm.trim(), this.currentPage, this.pageSize, this.primaryKeys, this.uniqueKeys).subscribe(res => {
      this.authService.onLoading$.next(false);
      if (res && res.status === 200) {
        this.operationResponsePaged = res.body.entities;
        this.primaryKeys = res.body.primaryKeys;
        this.uniqueKeys = res.body.uniqueKeys;
        if (res.body.entities && res.body.entities.length > 0) {
          const firstRecord = res.body.entities[0];
          if (firstRecord.source) {
            this.headers = Object.keys(firstRecord.source);
          } else {
            this.headers = Object.keys(firstRecord.target);
            this.headers = this.headers.map(x => x.toUpperCase());
          }
        }
      }
    })
  }

  handleChangePageSize(e: number): void {
    this.pageSize = e;
    this.loadSearchingData();
  }

  handleChangePage(page: number): void {
    this.currentPage = page;
    this.loadSearchingData();
  }

  private loadViewInfoForSearching(): void {
    this.authService.onLoading$.next(true);
    this.operationService.getViewInfoForSearching().subscribe((data) => {
      if (data) {
        const sourceDBSet = new Set();
        const sourceSchemasSet = new Set();

        const targetSchemasSet = new Set();
        const targetDBstSet = new Set();

        data.forEach((dt) => {
          sourceDBSet.add(
            dt.syncRdId == null ? dt.sourceDatabase : dt.rdSourceDatabase
          );
          sourceSchemasSet.add(
            dt.syncRdId == null ? dt.sourceSchema : dt.rdSourceSchema
          );

          targetSchemasSet.add(
            dt.syncRdId == null ? dt.targetSchema : dt.rdTargetSchema
          );
          targetDBstSet.add(
            dt.syncRdId == null ? dt.targetDatabase : dt.rdTargetDatabase
          );
        });
        sourceDBSet.forEach((sdb: string) => {
          if (sdb === sdb.toUpperCase()) {
            this.sourceOracleDBs.push(sdb);
          }
        });
        sourceSchemasSet.forEach((sss: string) => {
          if (sss === sss.toUpperCase()) {
            this.oracleSchemas.push(sss);
          }
        });
        targetSchemasSet.forEach((tss) => this.postgresSchemas.push(tss));
        targetDBstSet.forEach((tdbs) => this.postgresDBs.push(tdbs));
        this.authService.onLoading$.next(false);
      }
    });
  }

  onCloseSetting(): void {
    this.modalService.dismissAll();
  }
}
