import { MultipeSelectDropDown } from '../../components/multiselect-dropdown/interface';
import { ComparisonService } from 'src/app/libs/services/comparison.service';
import {
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { Router } from '@angular/router';
import {
  NgbDateStruct,
  NgbModal,
  NgbTypeahead,
} from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';
import * as copy from 'copy-to-clipboard';
import {
  CURRENT_PAGE,
  CURRENT_SEARCHING_TOPIC_NAME,
  PAGE_SIZE,
  BATCH_SIZE,
  SortType,
  SORT_FIELD_SYNCHRONIZER,
  SORT_FIELD_TOTAL_RESOLVED_SYNCHRONIZER,
  SORT_TYPE,
} from 'src/app/libs/constants';
import { AuthService } from 'src/app/libs/services/auth.service';
import {
  HistoryInfoDto,
  SyncErrorInfo,
  SynchronizationInfo,
  SyncState,
} from 'src/app/libs/services/interface';
import StorageService from 'src/app/libs/services/storage.service';
import { SyncService } from 'src/app/libs/services/syncDocument.service';
import { NgbDateCustomParserFormatter } from 'src/app/libs/utils/helper';
import { SweetAlert } from 'src/app/libs/utils/sweetalert';
import { ViewSynchronizerComponent } from '../view-synchronizer/view-synchronizer.component';
import { catchError } from 'rxjs/operators';
import { Observable, throwError} from 'rxjs';
import { saveAs } from 'file-saver';

const SYNCHRONIZER_LIST_CURRENT_DATE_FROM = '_synchronizer_list_current_date_from';
const SYNCHRONIZER_LIST_CURRENT_DATE_TO = '_synchronizer_list_current_date_to';
const SYNCHRONIZER_LIST_SELECTED_DB = 'SYNCHRONIZER_LIST_SELECTED_DB';
const SYNCHRONIZER_LIST_SELECTED_SCHEMA = 'SYNCHRONIZER_LIST_SELECTED_SCHEMA';
const SYNCHRONIZER_LIST_SELECTED_STATE = 'SYNCHRONIZER_LIST_SELECTED_STATE';
const SYNCHRONIZER_LIST_SELECTED_DIVISION = 'SYNCHRONIZER_LIST_SELECTED_DIVISION';
const SYNCHRONIZER_LIST_SELECTED_SELECTED_TOPIC = 'SYNCHRONIZER_LIST_SELECTED_SELECTED_TOPIC';


@Component({
  selector: 'app-synchronizer',
  templateUrl: './synchronizer.component.html',
  styleUrls: ['./synchronizer.component.scss'],
})
export class SynchronizerComponent implements OnInit, OnDestroy {
  topics: SynchronizationInfo[] = [];
  isToSlow = false;
  viewLastMessageInfo: SynchronizationInfo;
  mapState = new Map();

  selected: number[] = [];
  totalPage;
  currentPage = 1;
  pageSize = 20;

  batchSize = 500;

  sortField = 'updated_at';
  dataSuggests: any[] = [];
  isSearching: boolean;
  fileToUpload: File | null = null;

  isSortLastReceivedTime = false;

  dateFrom: NgbDateStruct;
  dateTo: NgbDateStruct;

  selectedTopicName = [];
  selectedState = '';
  selectedDivision = '';

  sortErrorLogUp = 'bi bi-sort-up';
  sortErrorLogDown = 'bi bi-sort-down';

  sortLastReceivedUp = 'bi bi-sort-up';
  sortLastReceivedDown = 'bi bi-sort-down';

  sortIcon = this.sortErrorLogUp;
  sortLastReceivedIcon = this.sortErrorLogUp;
  sortResolvedIcon = this.sortErrorLogUp;
  sortState = this.sortErrorLogUp;
  sortType = SortType.DESC;
  sortTypeLastReceived = SortType.DESC;

  pending = 0;
  running = 0;
  stopped = 0;
  linked = 0;

  selectedPeding = false;
  selectedRunning = false;
  selectedStopped = false;
  selectedLinked = false;

  allDBs = [];
  allSchemas = [];
  selectedDB = '';
  selectedSchema = '';

  // timeToReload = 6000; //60s
  topicNames = [];
  states = [];
  division = [];
  // for reset offset
  dateForUpdateOffset;
  idTopicForUpdateOffset;
  topicNameForUpdateOffset;
  synchronizerNameForUpdateOffset;
  isRunningSynchronizer = true;
  loadingStop = false;
  loadingBtnRun = false;
  selectSynchronizerToResetOffset: SyncErrorInfo;
  currentOffset;
  offsetByDateTime;
  offsetByDate = '';
  errorMsgOffset = '';
  invalidDateInput = '';
  history: Observable<HistoryInfoDto>;
  synchronizerId: number;

  @ViewChild('box', { static: true }) searchInput: ElementRef;
  @ViewChild('instance', { static: true }) instance: NgbTypeahead;
  @ViewChild('instanceState', { static: true }) instanceState: NgbTypeahead;

  constructor(
    private comparisonService: ComparisonService,
    private authService: AuthService,
    private syncService: SyncService,
    private formater: NgbDateCustomParserFormatter,
    private router: Router,
    public translate: TranslateService,
    private modalService: NgbModal,
    protected storage: StorageService
  ) {
    const pageSize = this.storage.get(PAGE_SIZE);
    if (pageSize) {
      this.pageSize = Number(pageSize);
    }
    const size = this.storage.get(BATCH_SIZE);
    if (size) {
      this.batchSize = Number(size);
    }
    const currentPageFromCache = this.storage.get(CURRENT_PAGE);
    if (currentPageFromCache) {
      this.currentPage = Number(currentPageFromCache);
    }
    const currentSelectedTopicName = this.storage.get(
      CURRENT_SEARCHING_TOPIC_NAME
    ) as any[];
    if (currentSelectedTopicName) {
      this.selectedTopicName = currentSelectedTopicName;
    }
  }

  ngOnInit(): void {
    this.sortType = this.storage.get(SORT_TYPE) || SortType.ASC;
    this.sortField = this.storage.get(SORT_FIELD_SYNCHRONIZER) || this.sortField;

    this.dateFrom = this.storage.get(SYNCHRONIZER_LIST_CURRENT_DATE_FROM);
    this.dateTo = this.storage.get(SYNCHRONIZER_LIST_CURRENT_DATE_TO);
    this.selectedDB = this.storage.get(SYNCHRONIZER_LIST_SELECTED_DB) || '';
    this.selectedSchema = this.storage.get(SYNCHRONIZER_LIST_SELECTED_SCHEMA) || '';
    this.selectedState = this.storage.get(SYNCHRONIZER_LIST_SELECTED_STATE) || '';

    this.selectedDivision = this.storage.get(SYNCHRONIZER_LIST_SELECTED_DIVISION) || '';
    this.selectedTopicName = this.storage.get(SYNCHRONIZER_LIST_SELECTED_SELECTED_TOPIC) || [];

    this.loadViewInfoForSearching();
    this.loadSynchronizers();
    this.loadTopicNames();
    this.loadStates();
    // this.dateForUpdateOffset = new Date();
  }

  ngOnDestroy() {
    // if (this.updateSubscription && !this.updateSubscription.closed) {
    //   this.updateSubscription.unsubscribe();
    // }
  }

  onClearSourceDB(): void {
    this.selectedDB = '';
    this.onSearch();
  }
  onClearSourceSchema(): void {
    this.selectedSchema = '';
    this.onSearch();
  }

  handleSelectTopics(event) {
    const arr = event as Array<MultipeSelectDropDown>;
    this.selectedTopicName = arr;
    // this.selectedTopicName = '';
    // this.selectedDivision = '';
    // this.schmSelection = this.selectedSourceDB.length > 1 ? 1 : -1;
  }

  clearSelectedTopicName() {
    this.selectedTopicName = [];
    this.storage.save(CURRENT_SEARCHING_TOPIC_NAME, this.selectedTopicName);
  }

  sortErrorLog() {
    this.currentPage = 1;
    this.storage.save(CURRENT_PAGE, this.currentPage);
    this.sortIcon =
      this.sortIcon === this.sortErrorLogUp
        ? this.sortErrorLogDown
        : this.sortErrorLogUp;
    this.sortField = 'total_error';
    this.storage.save(SORT_FIELD_SYNCHRONIZER, this.sortField);
    this.sortType =
      this.sortIcon === this.sortErrorLogUp ? SortType.ASC : SortType.DESC;
    this.storage.save(SORT_TYPE, this.sortType);
    this.loadSynchronizers();
  }

  sortResolvedErrorLog() {
    this.currentPage = 1;
    this.storage.save(CURRENT_PAGE, this.currentPage);
    this.sortResolvedIcon =
      this.sortResolvedIcon === this.sortErrorLogUp
        ? this.sortErrorLogDown
        : this.sortErrorLogUp;
    this.sortField = 'total_resolve';
    this.storage.save(SORT_FIELD_TOTAL_RESOLVED_SYNCHRONIZER, this.sortField);
    this.sortType =
      this.sortResolvedIcon === this.sortErrorLogUp
        ? SortType.ASC
        : SortType.DESC;
    this.storage.save(SORT_TYPE, this.sortType);
    this.loadSynchronizers();
  }

  sortLastReceivedTime(isFromHandlePage = false): void {
    this.authService.onLoading$.next(true);
    this.isSortLastReceivedTime = true;
    const sDateFrom = this.formater.format(this.dateFrom);
    const sDateTo = this.formater.format(this.dateTo);
    if (!isFromHandlePage) {
      this.sortLastReceivedIcon = (this.sortLastReceivedIcon === this.sortLastReceivedUp)
        ? this.sortLastReceivedDown
        : this.sortLastReceivedUp;
      this.sortTypeLastReceived = (this.sortLastReceivedIcon === this.sortLastReceivedUp)
        ? SortType.ASC
        : SortType.DESC;
    }
    this.syncService
      .sortLastReceivedTime(
        this.pageSize,
        this.currentPage,
        this.sortTypeLastReceived,
        sDateFrom,
        sDateTo,
        this.selectedTopicName,
        this.selectedState,
        this.selectedDivision, this.selectedDB, this.selectedSchema)
      .subscribe(
        (res) => {
          if (res) {
            this.topics = res.synchronizationParams;
            this.isToSlow = res.isToSlow;
            this.totalPage = res.totalPage;
            this.topics.forEach((tp, index) => {
              tp.index = index;
              tp.logFileMinimized = this.minimizeLogName(tp.logFile);
              if (this.selected.length > 0) {
                tp.checked = this.selected.includes(+tp.id);
              }
            });
          }
          this.authService.onLoading$.next(false);
        },
        (err) => {
          this.authService.onLoading$.next(false);
        }
      );
  }

  onSortState() {
    this.currentPage = 1;
    this.storage.save(CURRENT_PAGE, this.currentPage);
    this.sortState =
      this.sortState === this.sortErrorLogUp
        ? this.sortErrorLogDown
        : this.sortErrorLogUp;
    this.sortField = 'state';
    this.storage.save(SORT_FIELD_TOTAL_RESOLVED_SYNCHRONIZER, this.sortField);
    this.sortType =
      this.sortState === this.sortErrorLogUp ? SortType.ASC : SortType.DESC;
    this.storage.save(SORT_TYPE, this.sortType);
    this.loadSynchronizers();
  }

  edit() {
    if (this.selected.length === 1) {
      this.router.navigate(['/synchronize/add'], {
        queryParams: { id: this.selected[0], isFromEdit: true },
      });
    } else {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Infomation',
        message: 'You should select one Synchronizer to EDIT',
        type: 'info',
        confirm: () => {},
      });
    }
  }

  dbclickToEdit(id) {
    this.router.navigate(['/synchronize/add'], {
      queryParams: { id: id, isFromEdit: true },
    });
  }

  viewLog(topicName) {
    this.router.navigate(['/synchronize/viewlog'], {
      queryParams: { topicName: topicName },
    });
  }

  showLogSystem() {
    const fileNameSystem = 'dbsync';
    this.router.navigate(['/synchronize/viewlog'], {
      queryParams: { topicName: fileNameSystem },
    });
  }

  showErrorLogSystem() {
    const fileNameSystem = 'dbsync-error';
    this.router.navigate(['/synchronize/viewlog'], {
      queryParams: { topicName: fileNameSystem },
    });
  }

  viewError(topicName) {
    this.router.navigate(['/synchronize/view-errors'], {
      queryParams: { topicName: topicName },
    });
  }

  viewResolveds(topicName) {
    this.router.navigate(['/synchronize/view-errors'], {
      queryParams: { topicName: topicName, status: 'RESOLVED' },
    });
  }

  handleFileInput(event) {
    this.router.navigate(['/synchronize/import']); // synchronize
  }

  copy(text) {
    copy(text);
  }

  onSearch() {
    this.currentPage = 1;
    this.storage.save(CURRENT_PAGE, this.currentPage);
    this.storage.save(CURRENT_SEARCHING_TOPIC_NAME, this.selectedTopicName);
    this.storage.save(CURRENT_PAGE, this.currentPage);

    this.storage.save(SYNCHRONIZER_LIST_CURRENT_DATE_FROM, this.dateFrom);
    this.storage.save(SYNCHRONIZER_LIST_CURRENT_DATE_TO, this.dateTo);
    this.storage.save(SYNCHRONIZER_LIST_SELECTED_DB, this.selectedDB);
    this.storage.save(SYNCHRONIZER_LIST_SELECTED_SCHEMA, this.selectedSchema);
    this.storage.save(SYNCHRONIZER_LIST_SELECTED_STATE, this.selectedState);
    this.storage.save(SYNCHRONIZER_LIST_SELECTED_DIVISION, this.selectedDivision);
    this.storage.save(SYNCHRONIZER_LIST_SELECTED_SELECTED_TOPIC, this.selectedTopicName);

    if (this.isSortLastReceivedTime) {
      this.sortLastReceivedTime();
    } else {
      this.loadSynchronizers();
    }
  }

  addNew() {
    this.router.navigate(['/synchronize/add']);
  }

  exportWithParams() {
    this.authService.onLoading$.next(true);
    const sDateFrom = this.formater.format(this.dateFrom);
    const sDateTo = this.formater.format(this.dateTo);
    this.syncService.exportWithConditions(
      sDateFrom,
      sDateTo,
      this.selectedTopicName,
      this.selectedState,
      this.selectedDivision,
      this.selectedDB,
      this.selectedSchema
    )
    .pipe(catchError(err => {
        SweetAlert.notifyMessage(err, 'error');
        this.authService.onLoading$.next(false);
        return throwError(err);
      })
    )
    .subscribe((res: any) => {
      this.authService.onLoading$.next(false);
      var contentDisposition = res.headers.get('content-disposition');
      var filename = contentDisposition.split(';')[1].split('filename')[1].split('=')[1].trim();
      saveAs(res.body, filename);
    });
  }

  loadTopicNames() {
    this.syncService.getTopicNames().subscribe((res) => {
      if (res) {
        this.topicNames = res.map((x) => x.topicName);
        this.syncService.synInfos = res;
      }
    });
  }

  handleChangePageSize(e: number): void {
    this.pageSize = e;
    if (this.isSortLastReceivedTime) {
      this.sortLastReceivedTime(true);
    } else {
      this.loadSynchronizers();
    }
  }

  handleChangePage(page: number): void {
    this.currentPage = page;
    this.storage.save(CURRENT_PAGE, this.currentPage);
    if (this.isSortLastReceivedTime) {
      this.sortLastReceivedTime(true);
    } else {
      this.loadSynchronizers();
    }
  }

  loadSynchronizers(): void {
    this.authService.onLoading$.next(true);
    this.isSortLastReceivedTime = false;
    const sDateFrom = this.formater.format(this.dateFrom);
    const sDateTo = this.formater.format(this.dateTo);
    this.isToSlow = false;
    this.syncService
      .filterState(
        this.sortField,
        this.pageSize,
        this.currentPage,
        sDateFrom,
        sDateTo,
        this.selectedTopicName,
        this.sortType,
        this.selectedState,
        this.selectedDivision,
        this.selectedDB,
        this.selectedSchema
      )
      .subscribe(
        (res) => {
          if (res) {
            this.topics = res.synchronizationParams;
            this.isToSlow = res.isToSlow;
            this.totalPage = res.totalPage;
            this.topics.forEach((tp, index) => {
              tp.index = index;
              tp.logFileMinimized = this.minimizeLogName(tp.logFile);
              if (this.selected.length > 0) {
                tp.checked = this.selected.includes(+tp.id);
              }
            });
          }
          this.authService.onLoading$.next(false);
        },
        (err) => {
          this.authService.onLoading$.next(false);
        }
      );
  }

  private minimizeLogName(logName: string) {
    if (!logName) {
      return;
    }
    if (logName.indexOf('.log') < 0) {
      return;
    }
    const firstDot = logName.indexOf('.');
    if (!firstDot) {
      return;
    }
    const first = logName.substring(0, firstDot + 1);
    const end = logName.substring(logName.length - 8); // 8='.log'.length + 4
    const patternMinimize = '...';
    return first + patternMinimize + end;
  }

  private loadViewInfoForSearching(): void {
    this.comparisonService.getViewInfoForSearching().subscribe((data) => {
      if (data) {
        const sourceDBSet = new Set();
        const sourceSchemasSet = new Set();

        const targetSchemasSet = new Set();
        const targetDBstSet = new Set();

        const tables = new Set();

        data.forEach((dt) => {
          tables.add(dt.sourceTable);
          tables.add(dt.targetTable);

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
        sourceDBSet.forEach((sdb) => this.allDBs.push(sdb));
        targetDBstSet.forEach((sdb) => this.allDBs.push(sdb));

        sourceSchemasSet.forEach((sss) => this.allSchemas.push(sss));
        targetSchemasSet.forEach((sss) => this.allSchemas.push(sss));
      }
    });
  }

  loadStates() {
    this.syncService.getStateCount().subscribe((data) => {
      if (data) {
        this.running = data['running'];
        this.pending = data['pending'];
        this.stopped = data['stopped'];
        this.linked = data['linked'];
      }
    });
    this.mapState.set(SyncState.Running, SyncState.Running);
    this.mapState.set(SyncState.Stopped, SyncState.Stopped);
    this.mapState.set(SyncState.Pending, SyncState.Pending);
    this.mapState.set(SyncState.Linked, SyncState.Linked);
    this.states = Array.from(this.mapState.keys());
    this.syncService.getDivisions().subscribe((data) => {
      this.division = data;
    });
  }

  filterState(state: string) {
    if ('RUNNING' === state) {
      this.selectedRunning = true;
      this.selectedStopped = false;
      this.selectedPeding = false;
      this.selectedLinked = false;
    } else if ('STOPPED' === state) {
      this.selectedRunning = false;
      this.selectedStopped = true;
      this.selectedPeding = false;
      this.selectedLinked = false;
    } else if ('PENDING' === state) {
      this.selectedRunning = false;
      this.selectedStopped = false;
      this.selectedPeding = true;
      this.selectedLinked = false;
    } else if ('LINKED' === state) {
      this.selectedRunning = false;
      this.selectedStopped = false;
      this.selectedPeding = false;
      this.selectedLinked = true;
    }
    this.selectedState = state;
    this.resetCurrentPage();
    if (this.isSortLastReceivedTime) {
      this.sortLastReceivedTime();
    } else {
      this.loadSynchronizers();
    }
  }

  clearFilterState() {
    this.selectedRunning = false;
    this.selectedStopped = false;
    this.selectedPeding = false;
    this.selectedLinked = false;
    this.selectedState = '';
    this.resetCurrentPage();
    if (this.isSortLastReceivedTime) {
      this.sortLastReceivedTime();
    } else {
      this.loadSynchronizers();
    }
  }

  // onSelectedTags(event) {
  //   this.selectedTopicName = event;
  //   this.addTag();
  // }


  private resetCurrentPage() {
    this.currentPage = 1;
    this.storage.save(CURRENT_PAGE, this.currentPage);
  }

  start() {
    if (this.selected.length > 0) {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Information',
        message: 'Are you sure to START this Synchronizer?',
        confirm: () => {
          this.authService.onLoading$.next(true);
          this.syncService.start(this.selected).subscribe((res) => {
            this.loadSynchronizers();
            this.selected = [];
            this.loadStates();
            this.authService.onLoading$.next(false);
            SweetAlert.notifyMessage('Start Success');
          });
        },
        type: 'info',
        cancel: true,
      });
    } else {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Information',
        message: 'You should select at least one Synchronizer to START',
        type: 'info',
        confirm: () => {
        },
      });
    }
  }

  stop() {
    if (this.selected.length > 0) {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Information',
        message: 'Are you sure to STOP this Synchronizer?',
        confirm: () => {
          this.authService.onLoading$.next(true);
          this.syncService.stop(this.selected).subscribe((res) => {
            this.loadSynchronizers();
            this.selected = [];
            this.loadStates();
            this.authService.onLoading$.next(false);
            SweetAlert.notifyMessage('Stop Success');
          });
        },
        type: 'info',
        cancel: true,
      });
    } else {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Information',
        message: 'You should select one Synchronizer to STOP',
        type: 'info',
        confirm: () => {},
      });
    }
  }

  startAll() {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Information',
      message: 'Are you sure to START ALL Synchronizer?',
      confirm: () => {
        this.authService.onLoading$.next(true);
        this.syncService.startAll().subscribe((_) => {
          this.loadSynchronizers();
          this.selected = [];
          this.loadStates();
          this.authService.onLoading$.next(false);
          SweetAlert.notifyMessage('Start All Success');
        });
      },
      type: 'warning',
      cancel: true,
    });
  }

  deleteAll() {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'WARNING',
      message: 'Are you sure to DELETE ALL Synchronizer?',
      confirm: () => {
        this.authService.onLoading$.next(true);
        this.syncService.deleteAll().subscribe((res) => {
          if (res['status'] === 400) {
            this.authService.onLoading$.next(false);
            SweetAlert.notifyMessage(res['body'], 'error');
          } else {
            this.loadSynchronizers();
            this.selected = [];
            this.topicNames = [];
            this.loadStates();
            this.clearSelectedTopicName();
            this.authService.onLoading$.next(false);
            SweetAlert.notifyMessage('DELETE All Success');
          }
        });
      },
      type: 'warning',
      cancel: true,
    });
  }

  stopAll() {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Information',
      message: 'Are you sure to STOP ALL Synchronizer?',
      confirm: () => {
        this.authService.onLoading$.next(true);
        this.syncService.stopAll().subscribe((_) => {
          this.loadSynchronizers();
          this.selected = [];
          this.loadStates();
          SweetAlert.notifyMessage('Stop Success');
        });
      },
      type: 'warning',
      cancel: true,
    });
  }

  delete() {
    if (this.selected.length > 0) {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Information',
        message: 'Are you sure to DELETE this Synchronizer?',
        confirm: () => {
          this.syncService.delete(this.selected).subscribe(
            (res) => {
              if (res.status == 400) {
                SweetAlert.notifyMessage(res.message, 'error');
              } else {
                this.loadSynchronizers();
                this.loadStates();
                this.getCheckAllItem();
                SweetAlert.notifyMessage('Delete Synchronizer Success');
              }
            },
            (err) => {
              SweetAlert.notifyMessage(
                'Delete Synchronizer has an error',
                'error'
              );
            }
          );
        },
        type: 'info',
        cancel: true,
      });
    } else {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Information',
        message: 'You should select one Synchronizer to DELETE',
        type: 'info',
        confirm: () => {},
      });
    }
  }

  lastSelected: SynchronizationInfo = null;

  selectItem(event, syncInfo: SynchronizationInfo): void {
    syncInfo.checked = event.target.checked;
    if (event.target.checked) {
      this.lastSelected = syncInfo;
      this.selected.push(Number(syncInfo.id));
    } else {
      this.lastSelected = null;
      this.selected = this.selected.filter(
        (item) => item !== Number(syncInfo.id)
      );
    }
  }

  onSelectionEnd(event, doc: SynchronizationInfo) {
    if (event.shiftKey && event.which === 1) {
      if (this.lastSelected && doc.index > this.lastSelected.index) {
        this.topics.forEach((item) => {
          if (item.index > this.lastSelected.index && item.index < doc.index) {
            item.checked = true;
            this.selected.push(+item.id);
          }
        });
      }
    }
  }

  selectItemAllItems(event) {
    if (event.target.checked) {
      this.topics.forEach((item) => {
        this.selected.push(Number(item.id));
        item.checked = true;
      });
    } else {
      this.topics.forEach((item) => {
        item.checked = false;
      });
      this.selected = [];
    }
  }

  getCheckAllItem() {
    return this.selected.length > 0;
  }

  open(content) {
    this.modalService.open(content);
  }

  openLastMsgInfo(lastMsgInfo, doc) {
    this.viewLastMessageInfo = doc;
    this.viewLastMessageInfo.msgTimestamp =
      doc.msgTimestamp === 0 ? null : doc.msgTimestamp;
    this.modalService.open(lastMsgInfo);
  }
  openWatchHistory(viewHistory, doc){
    console.log(doc);
    this.synchronizerId = doc.id;
    this.history = this.syncService.watchHistory(this.synchronizerId);
    this.history.subscribe(data => {
      console.log(data);
      this.modalService.open(viewHistory, { size: 'xl' });
    });
  }

  openSyncInfo(id) {
    const modalRef = this.modalService.open(ViewSynchronizerComponent, {
      size: 'xl',
    });
    modalRef.componentInstance.id = id;
  }

  onCloseModal(): void {
    this.modalService.dismissAll();
  }

  openResetOffset(viewResetOffset, doc): void {
    this.dateForUpdateOffset = undefined;
    this.selectSynchronizerToResetOffset = doc;
    this.idTopicForUpdateOffset = doc.id;
    this.topicNameForUpdateOffset = doc.topicName;
    this.synchronizerNameForUpdateOffset = doc.synchronizerName;
    // this.isRunningSynchronizer = doc.state === 'RUNNING';
    this.offsetByDate = '';
    this.errorMsgOffset = '';
    this.invalidDateInput = '';
    this.offsetByDateTime = -1;
    this.syncService.hasAtLeastOneRunning(doc.id).subscribe(rs => {
      this.isRunningSynchronizer = rs;
      this.modalService.open(viewResetOffset, { backdrop: 'static' }).result.then(
        (result) => {},
        (reason) => {
          this.onCloseModal();
          this.authService.onLoading$.next(false);
        }
      );
    });
  }

  onUpdateOffset(): void {
    this.errorMsgOffset = '';
    this.invalidDateInput = '';
    if (!this.dateForUpdateOffset || !this.idTopicForUpdateOffset || !this.topicNameForUpdateOffset) {
      this.invalidDateInput = 'You should select date time!';
      return;
    }
    this.loadingBtnRun = true;
    this.updateOffset();
  }

  private updateOffset(): void {
    this.syncService.updateOffset(this.topicNameForUpdateOffset, this.synchronizerNameForUpdateOffset, this.dateForUpdateOffset).subscribe(data => {
      if (data.status === 200) {
        this.offsetByDate = data.body;
        this.loadingBtnRun = false;
        SweetAlert.notifyMessage('Update offset done!');
      } else {
        this.loadingBtnRun = false;
        this.errorMsgOffset = data.message;
        SweetAlert.notifyMessage('Update offset error!', 'error');
      }
    }, err => {
      this.loadingBtnRun = false;
      this.errorMsgOffset = err;
      SweetAlert.notifyMessage('Update offset error!', 'error');
    }
    , () => {
      this.loadingBtnRun = false;
      this.onCloseModal();
      this.loadStates();
      this.onSearch();
    });
  }

  quickStopAll(): void {
    this.loadingStop = true;
    this.onCloseModal();

    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Information',
      message: 'Are you sure to STOP ALL Synchronizer?',
      confirm: () => {
        this.authService.onLoading$.next(true);
        this.syncService.stopAll().subscribe((_) => {
          setTimeout(() => {
            this.selected = [];
            this.loadStates();
            this.loadSynchronizers();
            this.loadingStop = false;
            this.isRunningSynchronizer = false;
            this.authService.onLoading$.next(false);
            SweetAlert.notifyMessage('Stop all Success');
          }, 3000);
        });
      },
      closeModal: () => {
        this.loadingStop = false;
        this.isRunningSynchronizer = false;
      },
      type: 'warning',
      cancel: true,
    });
  }

  onSaveSetting(PageSize): void {
    let valid = true;
    if (PageSize) {
      this.pageSize = PageSize.value;
      if (!Number(this.pageSize)) {
        valid = false;
        PageSize.classList.add('is-invalid');
      } else {
        this.storage.save(PAGE_SIZE, Number(this.pageSize));
      }
    }
    if (valid) {
      this.modalService.dismissAll();
      this.onSearch();
    }
  }
}
