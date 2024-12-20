import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbDateStruct, NgbModal, NgbTypeahead } from '@ng-bootstrap/ng-bootstrap';
import {merge, Observable, OperatorFunction, Subject, throwError} from 'rxjs';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  filter,
  map,
} from 'rxjs/operators';
import {
  CURRENT_ERROR_LOG,
  PAGE_SIZE_ERROR_LOG,
  SortType,
  SORT_FIELD_ERROR_LOG,
  SORT_TYPE_ERROR_TYPE,
} from 'src/app/libs/constants';
import { AuthService } from 'src/app/libs/services/auth.service';
import {
  CommonResponse,
  SyncErrorCountOperationsDto,
  SyncErrorDto,
  SyncErrorInfo,
} from 'src/app/libs/services/interface';
import StorageService from 'src/app/libs/services/storage.service';
import { SyncService } from 'src/app/libs/services/syncDocument.service';
import { NgbDateCustomParserFormatter } from 'src/app/libs/utils/helper';
import { SweetAlert } from 'src/app/libs/utils/sweetalert';
import {saveAs} from "file-saver";

@Component({
  selector: 'app-view-error-synchronizer',
  templateUrl: './view-error-synchronizer.component.html',
  styleUrls: ['./view-error-synchronizer.component.scss'],
  // host: {
  //   '(window:keydown)': 'findKey($event)',
  //   '(window:mousedown)': 'findKey($event)'
  // }
})
export class ViewErrorSynchronizerComponent implements OnInit {
  topicName = '';
  status = '';
  operationState = [];
  errors: SyncErrorInfo[] = [];
  selected: number[] = [];
  isSelectAll = false;

  totalPage = 0;
  currentPage = 1;
  pageSize = 25;
  sortField = 'errorTime';
  errorType = '';
  // isOrderErrorType = true;

  selectedErrorType = '';
  selectedErrorState = '';
  errorTypes = [];
  errorState = [];
  mapErrorType = new Map();

  dateFrom: NgbDateStruct;
  dateTo: NgbDateStruct;

  sortErrorLogUp = 'bi bi-sort-up';
  sortErrorLogDown = 'bi bi-sort-down';
  sortErrorTimeIcon = this.sortErrorLogUp;
  sortType = SortType.DESC;

  sortStateIcon = this.sortErrorLogUp;
  timeFrom: string;
  timeTo: string

  operationCount: SyncErrorCountOperationsDto;

  focus$ = new Subject<string>();
  click$ = new Subject<string>();

  focusErrorState$ = new Subject<string>();
  clickErrorState$ = new Subject<string>();

  selectedInsert = false;
  selectedUpdate = false;
  selectedDelete = false;

  @ViewChild('instance', { static: true }) instance: NgbTypeahead;
  @ViewChild('instanceErrorState', { static: true })
  instanceErrorState: NgbTypeahead;
  spinners = false;

  constructor(
    private syncService: SyncService,
    private activeRouter: ActivatedRoute,
    protected storage: StorageService,
    private modalService: NgbModal,
    private router: Router,
    private authService: AuthService,
    private formater: NgbDateCustomParserFormatter,
  ) {}

  ngOnInit(): void {
    this.topicName = this.activeRouter.snapshot.queryParams['topicName'];
    this.status = this.activeRouter.snapshot.queryParams['status'];
    this.sortType = this.storage.get(SORT_TYPE_ERROR_TYPE) || SortType.ASC;
    this.sortField = this.storage.get(SORT_FIELD_ERROR_LOG) || this.sortField;
    this.selectedErrorState = this.status ? 'RESOLVED' : 'ERROR';
    this.loadErrors();
    this.countOperation();
  }

  private countOperation() {
    this.syncService.getErrorOperations(this.topicName, this.selectedErrorState).subscribe(data => {
      this.operationCount = data;
    });
  }

  initTime(){
    if(this.timeFrom == null || this.timeTo == null ){
      this.timeFrom ='00:00:01';
      this.timeTo = '23:59:59';
    }
  }

  filterState(operation: string[]) {
    this.operationState = operation;
    this.loadErrors();
  }

  clearFilterState() {
   this.operationState = [];
   this.loadErrors();
   this.selectedInsert = false;
   this.selectedDelete = false;
   this.selectedUpdate = false;
  }

  loadErrors() {
    const sDateFrom = this.formater.format(this.dateFrom);
    const sDateTo = this.formater.format(this.dateTo);
    if (this.topicName) {
      this.authService.onLoading$.next(true);
      this.initTime();
      this.syncService
        .getErrors(
          this.topicName,
          this.sortField,
          this.pageSize,
          this.errorType,
          this.currentPage,
          this.sortType,
          this.selectedErrorState,
          this.selectedErrorType,
          sDateFrom,
          sDateTo,
          this.timeFrom,
          this.timeTo,
          this.operationState
        )
        .subscribe(
          (data: CommonResponse<SyncErrorDto>) => {
            this.authService.onLoading$.next(false);
            if (data) {
              this.errors = data.body.syncErrorEntities;
              this.errors.forEach((err, index) => {
                err.index = index;
                err.environment = data.body.environment;
                err.errorVersion = data.body.errorVersion;
                err.errorType = this.transformErrorType(err.errorType);
              });
              this.totalPage = data.body.totalPage;
              this.loadAllTypes(data.body.allTypes);
              this.loadAllState(data.body.errorStates);
            }
          },
          (err) => {
            console.log(err);
          }
        );
    }
  }

  open(content) {
    this.modalService.open(content);
  }

  onSaveSetting(PageSize) {
    let valid = true;
    if (PageSize) {
      this.pageSize = PageSize.value;
      if (!Number(this.pageSize)) {
        valid = false;
        PageSize.classList.add('is-invalid');
      } else {
        this.storage.save(PAGE_SIZE_ERROR_LOG, Number(this.pageSize));
      }
    }
    if (valid) {
      this.modalService.dismissAll();
      this.onSearch();
    }
  }

  sortErrorTime() {
    this.currentPage = 1;
    this.storage.save(CURRENT_ERROR_LOG, this.currentPage);
    this.sortField = 'errorTime';
    this.storage.save(SORT_FIELD_ERROR_LOG, this.sortField);

    this.sortErrorTimeIcon =
      this.sortErrorTimeIcon === this.sortErrorLogUp
        ? this.sortErrorLogDown
        : this.sortErrorLogUp;
    this.sortType =
      this.sortErrorTimeIcon === this.sortErrorLogUp
        ? SortType.ASC
        : SortType.DESC;
    this.storage.save(SORT_TYPE_ERROR_TYPE, this.sortType);
    // this.isOrderErrorType = false;
    this.loadErrors();
  }

  sortErrorState() {
    this.currentPage = 1;
    this.storage.save(CURRENT_ERROR_LOG, this.currentPage);
    this.sortField = 'state';
    this.storage.save(SORT_FIELD_ERROR_LOG, this.sortField);

    this.sortStateIcon =
      this.sortStateIcon === this.sortErrorLogUp
        ? this.sortErrorLogDown
        : this.sortErrorLogUp;
    this.sortType =
      this.sortStateIcon === this.sortErrorLogUp ? SortType.ASC : SortType.DESC;
    this.storage.save(SORT_TYPE_ERROR_TYPE, this.sortType);
    // this.isOrderErrorType = true;
    this.loadErrors();
  }

  lastSelected: SyncErrorInfo = null;

  onSelectionStart(doc: SyncErrorInfo) {
    doc.checked = !doc.checked;
    if (doc.checked) {
      this.selected.push(doc.id);
      this.lastSelected = doc;
    } else {
      this.lastSelected = null;
      this.selected = this.selected.filter((item) => item !== doc.id);
    }
    this.getCheckAllItem();
  }

  onSelectionEnd(event, doc: SyncErrorInfo) {
    if (event.shiftKey && event.which === 1) {
      if (this.lastSelected && doc.index > this.lastSelected.index) {
        this.errors.forEach((item) => {
          if (item.index > this.lastSelected.index && item.index < doc.index) {
            item.checked = true;
            this.selected.push(item.id);
          }
        });
      }
    }
  }

  getKeyByValue(value) {
    let key = '';
    this.mapErrorType.forEach((v, k) => {
      if (v === value) {
        key = k;
      }
    });
    return key;
  }

  private loadAllState(allStates) {
    if (this.errorState.length > 0) {
      return;
    }
    allStates.forEach((x) => {
      this.errorState.push(x);
    });
  }

  private loadAllTypes(allTypes: string[]) {
    allTypes.forEach((x) => {
      this.mapErrorType.set(x, x);
    });

    this.errorTypes = Array.from(this.mapErrorType.values());
  }

  private transformErrorType(type: string) {
    const value = this.mapErrorType.get(type);
    return value ? value : type;
  }

  search: OperatorFunction<string, readonly string[]> = (
    text$: Observable<string>
  ) => {
    const debouncedText$ = text$.pipe(
      debounceTime(200),
      distinctUntilChanged()
    );
    const clicksWithClosedPopup$ = this.click$.pipe(
      filter(() => !this.instance.isPopupOpen())
    );
    const inputFocus$ = this.focus$;

    return merge(debouncedText$, inputFocus$, clicksWithClosedPopup$).pipe(
      map((term) =>
        (term === ''
          ? this.errorTypes
          : this.errorTypes.filter(
              (v) => v.toLowerCase().indexOf(term.toLowerCase()) > -1
            )
        ).slice(0, 20)
      )
    );
  };

  searchErrorState: OperatorFunction<string, readonly string[]> = (
    text$: Observable<string>
  ) => {
    const debouncedText$ = text$.pipe(
      debounceTime(200),
      distinctUntilChanged()
    );
    const clicksWithClosedPopup$ = this.clickErrorState$.pipe(
      filter(() => !this.instanceErrorState.isPopupOpen())
    );
    const inputFocus$ = this.focusErrorState$;

    return merge(debouncedText$, inputFocus$, clicksWithClosedPopup$).pipe(
      map((term) =>
        (term === ''
          ? this.errorState
          : this.errorState.filter(
              (v) => v.toLowerCase().indexOf(term.toLowerCase()) > -1
            )
        ).slice(0, 5)
      )
    );
  };

  onSearch() {
    this.errorType = this.getKeyByValue(this.selectedErrorType);
    this.loadErrors();
    this.countOperation();
  }

  viewDetailError(info: SyncErrorInfo) {
    this.router.navigate(['/synchronize/view-detail-error'], {
      queryParams: { info: JSON.stringify(info) },
    });
  }

  handleChangePageSize(e: number): void {
    this.pageSize = e;
    this.loadErrors();
  }

  handleChangePage(page: number): void {
    this.currentPage = page;
    this.loadErrors();
  }

  resolve() {
    if (this.selected.length > 0) {
      this.syncService.resolvedError(this.selected).subscribe((data) => {
        this.selected = [];
        this.loadErrors();
        this.getCheckAllItem();
        SweetAlert.notifyMessage(data + ' Resolved Success!');
      });
    } else {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Infomation',
        message: 'You should select at least Error Message to RESOLVE',
        type: 'info',
        confirm: () => {},
      });
    }
  }

  retry() {
    if (this.selected.length > 0) {
      this.syncService.retryToResolveError(this.selected, this.topicName).subscribe((data) => {
        let numberRetried = 0;
        if (data.status === 200 && +data.body > 0) {
          this.selected = [];
          this.getCheckAllItem();
          this.loadErrors();
          numberRetried = +data.body;
        }
        this.authService.onLoading$.next(false);
        SweetAlert.notifyMessage(numberRetried + ' Retry Success!');
      });
    } else {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Infomation',
        message: 'You should select at least Error Message to RETRY',
        type: 'info',
        confirm: () => {},
      });
    }
  }

  retryAll() {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Warning',
      message: 'Are you sure to RETRY ALL ?',
      confirm: () => {
        this.authService.onLoading$.next(true);
        this.syncService.retryAllError(this.topicName).subscribe((data) => {
          this.authService.onLoading$.next(false);
          if (data.status === 400) {
            SweetAlert.notifyMessage(data.body + ' Retried');
          } else {
            this.selected = [];
            this.loadErrors();
            SweetAlert.notifyMessage(data.body + ' Retrying!');
          }
          this.authService.onLoading$.next(false);
        });
      },
      type: 'warning',
      cancel: true,
    });
  }

  resolveAll() {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Warning',
      message: 'Are you sure to RESOLVE ALL ?',
      confirm: () => {
        this.authService.onLoading$.next(true);
        this.syncService.resolvedAllError(this.topicName).subscribe((data) => {
          this.authService.onLoading$.next(false);
          if (data.status === 200) {
            this.selected = [];
            this.getCheckAllItem();
            this.loadErrors();
          }
          SweetAlert.notifyMessage(data.body);
        });
      },
      type: 'warning',
      cancel: true,
    });
  }

  delete() {
    if (this.selected.length > 0) {
      this.syncService.deleteError(this.selected).subscribe((data) => {
        if (data) {
          this.selected = [];
          this.loadErrors();
          this.getCheckAllItem();
          SweetAlert.notifyMessage('Deleted Success!');
        }
      });
    } else {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Infomation',
        message: 'You should select at least Error Message to DELETE',
        type: 'info',
        confirm: () => {},
      });
    }
  }

  deleteAll() {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Warning',
      message: 'Are you sure to DELETE ALL ?',
      confirm: () => {
        this.authService.onLoading$.next(true);
        this.syncService.deleteAllError(this.topicName).subscribe((data) => {
          this.authService.onLoading$.next(false);
          if (data) {
            this.selected = [];
            this.loadErrors();
            this.getCheckAllItem();
            SweetAlert.notifyMessage('DELETE Success!');
          }
        });
      },
      type: 'warning',
      cancel: true,
    });
  }

  exportPK(){
    const exportDateFrom = this.formater.format(this.dateFrom);
    const exportDateTo = this.formater.format(this.dateTo);
    this.errorType = this.getKeyByValue(this.selectedErrorType);
    this.authService.onLoading$.next(true);
    this.syncService.exportAllErrorPK(this.topicName,exportDateFrom,this.timeFrom,exportDateTo,this.timeTo,this.selectedErrorState,this.errorType,this.operationState)
      .pipe(catchError(err => {
        SweetAlert.notifyMessage(err, 'error');
        this.authService.onLoading$.next(false);
        return throwError(err);
      })
    )
      .subscribe((res: any) => {
        this.authService.onLoading$.next(false);
        SweetAlert.notifyMessage('export success', 'success',);
        const now = new Date();
        const filename = this.topicName+now.toLocaleString()+'.xlsx';
        saveAs(res.body,filename);
      })
  }

  markResolve() {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Warning',
      message: 'Are you sure to mark Resolve PROCESSING ?',
      confirm: () => {
        this.authService.onLoading$.next(true);
        this.syncService.markProcessing().subscribe((data) => {
          this.authService.onLoading$.next(false);
          if (data) {
            this.selected = [];
            this.loadErrors();
            this.getCheckAllItem();
            SweetAlert.notifyMessage('Resolved Success');
          }
        });
      },
      type: 'warning',
      cancel: true,
    });
  }

  selectItemAllItems(event) {
    if (event.target.checked) {
      this.errors.forEach((item) => {
        this.selected.push(Number(item.id));
        item.checked = true;
      });
    } else {
      this.errors.forEach((item) => {
        item.checked = false;
      });
      this.selected = [];
    }
    this.getCheckAllItem();
  }

  getCheckAllItem() {
    this.isSelectAll = this.selected.length > 0;
  }
  goBack() {
    window.history.back();
  }
}
