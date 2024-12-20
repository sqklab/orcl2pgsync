import { DatePipe } from '@angular/common';
import { Component, Input, OnChanges, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NgbCalendar } from '@ng-bootstrap/ng-bootstrap';
import { SortType } from 'src/app/libs/constants';
import { AuthService } from 'src/app/libs/services/auth.service';
import { ExecuteQueryService } from 'src/app/libs/services/executeQuery.service';
import {
  ComparisonSummary,
  DBScheduleDto,
  DBScheduleResultDto,
  GroupDateTime,
  ViewExportDbSchedule,
} from 'src/app/libs/services/interface';
import { NgbDateCustomParserFormatter } from 'src/app/libs/utils/helper';
import { SweetAlert } from 'src/app/libs/utils/sweetalert';
import * as xlsx from 'xlsx';

@Component({
  selector: 'app-result',
  templateUrl: './result.component.html',
  styleUrls: ['./result.component.scss'],
})
export class ResultQueryComponent implements OnInit {
  @Input() filterObject: DBScheduleDto;

  selected: number[] = [];
  scheduledTimes: GroupDateTime[] = [];
  selectedScheduleTime: GroupDateTime = null;


  targetDB = [];
  targetSchema = [];
  tables = [];

  dateFrom = '';

  totalPage = 0;
  currentPage = 1;
  pageSize = 20;
  sortType = SortType.DESC;

  selectedTime = '';
  selectedDate = '';

  // for filters
  sql = '';
  db = '';
  schema = '';
  table = '';
  filterStatus = [true, false];

  totalComparison = 0;
  summary: ComparisonSummary = {
    total: 0,
    different: 0,
    equal: 0,
    fail: 0,
    remaining: 0,
  } as ComparisonSummary;

  registedSchedule: DBScheduleResultDto[] = [];

  constructor(
    private api: ExecuteQueryService,
    private formater: NgbDateCustomParserFormatter,
    private calendar: NgbCalendar,
    private router: Router,
    private authService: AuthService,
    private datePipe: DatePipe
  ) {}

  ngOnInit(): void {
    if (!this.dateFrom) {
      const dateFrom = this.calendar.getToday();
      const sDate = dateFrom ? this.formater.format(dateFrom) : '';
      this.dateFrom = sDate;
    }
    this.loadScheduleByDate(this.dateFrom);
  }

  handleFilter(event): void {
    this.filterObject = event;
    if (this.filterObject) {
      this.sql = this.filterObject ? this.filterObject.plSQL : '';
      this.db = this.filterObject ? this.filterObject.db : '';
      this.schema = this.filterObject ? this.filterObject.schema : '';
      this.dateFrom = this.filterObject ? this.filterObject.createdAt : '';
      this.table = this.filterObject ? this.filterObject.table : '';
      this.filterStatus = this.filterObject.filterStatus;
      if (!this.dateFrom) {
        const dateFrom = this.calendar.getToday();
        const sDate = dateFrom ? this.formater.format(dateFrom) : '';
        this.dateFrom = sDate;
      }
      this.loadScheduleByDate(this.dateFrom);
    }
  }

  reload(): void {
    this.loadScheduleByDate(this.dateFrom);
  }

  private loadScheduleByDate(sDate): void {
    this.scheduledTimes = [];
    this.summary = {
      total: 0,
      different: 0,
      equal: 0,
      fail: 0,
      remaining: 0,
    } as ComparisonSummary;
    this.registedSchedule = [];
    this.totalComparison = 0;
    this.totalPage = 0;

    if (!sDate) {
      return;
    }
    this.authService.onLoading$.next(true);
    this.api.getScheduleByDate(sDate).subscribe((data) => {
      if (data.status === 200) {
        const arr = (data.body as string[]) || [];
        const scheduledTimesLocal = [];
        arr.forEach((x) => {
          const item = {
            compareDate: sDate,
            compareTime: x,
            active: false,
          } as GroupDateTime;
          scheduledTimesLocal.push(item);
        });
        this.scheduledTimes = scheduledTimesLocal;
        this.authService.onLoading$.next(false);
        // get first result
        if (this.scheduledTimes.length > 0) {
          this.scheduledTimes[0].active = true;
          this.selectedTime = this.scheduledTimes[0].compareTime;
          this.getViewResultByDateAndTime(this.scheduledTimes[0]);
          this.api
            .getResultSummary(
              this.scheduledTimes[0].compareDate,
              this.scheduledTimes[0].compareTime
            )
            .subscribe(data => {
              this.summary.equal = data.body.success;
              this.summary.fail = data.body.failure;
              this.totalComparison = data.body.total;
            });
        }
      } else {
        console.error('Invalid data');
      }
    });
  }

  getViewResultByDateAndTime(dateTime: GroupDateTime): void {
    this.registedSchedule = [];
    this.scheduledTimes.forEach((item) => (item.active = false));
    dateTime.active = true;
    this.selectedScheduleTime = dateTime;
    this.api.getResultSummary(
      dateTime.compareDate,
      dateTime.compareTime
    )
    .subscribe(data => {
      this.summary.equal = data.body.success;
      this.summary.fail = data.body.failure;
      this.totalComparison = data.body.total;
    });
    this.onSearch();
  }

  private onSearch(): void {
    this.api
      .filterResult(
        this.sql ? this.sql.trim() : '',
        this.db,
        this.schema,
        this.selectedScheduleTime.compareDate,
        this.selectedScheduleTime.compareTime,
        this.table,
        this.filterStatus,
        this.currentPage,
        this.pageSize,
        'id',
        this.sortType
      )
      .subscribe((data) => {
        this.registedSchedule = data.entities;
        this.totalPage = data.totalPage;
      });
  }

  deleteViewResultByDateAndTime(date, time): void {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Information',
      message: 'Are you sure to DELETE this result?',
      confirm: () => {
        this.api.deleteResultByDateAndTime(date, time).subscribe((data) => {
          if (data) {
            const num = data['body'] || 0;
            SweetAlert.notifyMessage(num + ' Deleted success!');
            this.loadScheduleByDate(this.dateFrom);
          }
        });
      },
      type: 'info',
      cancel: true,
    });
  }

  deleteSelectedItems(): void {
    if (this.selected.length > 0) {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Information',
        message: 'Are you sure to DELETE this result?',
        confirm: () => {
          this.authService.onLoading$.next(true);
          this.api.deleteResultByIds(this.selected).subscribe(
            (data) => {
              this.authService.onLoading$.next(false);
              if (data) {
                this.selected = [];
                SweetAlert.notifyMessage('Delete result success!');
                this.loadScheduleByDate(this.dateFrom);
              }
            },
            (err) => {
              this.selected = [];
              this.authService.onLoading$.next(false);
              SweetAlert.notifyMessage(err, 'error');
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
        message: 'You should select at least one Result to DELETE',
        type: 'info',
        confirm: () => {},
      });
    }
  }

  onRetry(doc: DBScheduleResultDto): void {
    this.api.retry(doc).subscribe((data) => {
      if (data && data.status === 200) {
        SweetAlert.notifyMessage('Retry success');
      } else {
        SweetAlert.notifyMessage(data.body, 'error');
      }
    });
  }

  handleChangePageSize(e: number): void {
    this.pageSize = e;
    this.onSearch();
  }
  
  handleChangePage(page: number): void {
    this.currentPage = page;
    // this.storage.save(CURRENT_PAGE_DB_SCHEDULE, this.currentPage);
    this.onSearch();
  }

  getCheckAllItem(): any {
    return this.selected.length > 0;
  }

  selectItemAllItems(event): void {
    if (event.target.checked) {
      this.registedSchedule.forEach((item) => {
        this.selected.push(Number(item.id));
        item.checked = true;
      });
    } else {
      this.registedSchedule.forEach((item) => {
        item.checked = false;
      });
      this.selected = [];
    }
  }

  selectItem(event, doc: DBScheduleResultDto): void {
    doc.checked = event.target.checked;
    if (event.target.checked) {
      this.selected.push(Number(doc.id));
    } else {
      this.selected = this.selected.filter((item) => item !== Number(doc.id));
    }
  }

  export(): void {
    const sDate = this.dateFrom;
    const time = this.scheduledTimes.filter((x) => x.active);
    if (!time) {
      SweetAlert.notifyMessage('You have select a time', 'error');
      return;
    }
    const selectedTime = time[0].compareTime + ':' + '00';
    const Heading = [
      ['PL SQL', 'DB', 'Schema', 'Table', 'Status', 'Start time', 'End time'],
    ];
    this.authService.onLoading$.next(true);
    this.api.exportByDateAndTime(sDate, selectedTime).subscribe((response) => {
      this.authService.onLoading$.next(false);
      if (response) {
        const data = response as Map<string, DBScheduleResultDto[]>;
        const wb: xlsx.WorkBook = xlsx.utils.book_new();
        Object.keys(data).forEach((k) => {
          const arr = data[k] as DBScheduleResultDto[];
          const rows: ViewExportDbSchedule[] = [];
          arr.forEach((row) => {
            rows.push({
              plSQL: row.plSQL,
              db: row.db,
              schema: row.schema,
              table: row.table,
              status: row.status ? 'Success' : 'Failed',
              startTime: this.parseDate(row.startAt),
              endTime: this.parseDate(row.endAt),
            } as ViewExportDbSchedule);
          });
          const ws: xlsx.WorkSheet = xlsx.utils.json_to_sheet(rows);
          xlsx.utils.sheet_add_aoa(ws, Heading);
          xlsx.utils.book_append_sheet(wb, ws, k.replace(/:/gi, '-'));
        });
        const fileName = 'DBScheduler_' + sDate + '.xlsx';
        xlsx.writeFile(wb, fileName);
      }
    });
  }

  private parseDate(date): any {
    return this.datePipe.transform(date, 'yyyy.MM.dd / HH:mm');
  }

  // TODO
  exportAll(): void {}

  private exportAllToExcel(): void {
    const sDate = this.dateFrom;
    const Heading = [
      ['PL SQL', 'DB', 'Schema', 'Table', 'Status', 'Start time', 'End time'],
    ];
    this.authService.onLoading$.next(true);
    this.api.exportAllByDate(sDate).subscribe((response) => {
      this.authService.onLoading$.next(false);
      if (response) {
        const data = response as Map<string, DBScheduleResultDto[]>;
        const wb: xlsx.WorkBook = xlsx.utils.book_new();
        Object.keys(data).forEach((k) => {
          const arr = data[k] as DBScheduleResultDto[];
          const rows: ViewExportDbSchedule[] = [];
          arr.forEach((row) => {
            rows.push({
              plSQL: row.plSQL,
              db: row.db,
              schema: row.schema,
              table: row.table,
              status: row.status ? 'Success' : 'Failed',
              startTime: row.startAt,
              endTime: row.endAt,
            } as ViewExportDbSchedule);
          });
          const ws: xlsx.WorkSheet = xlsx.utils.json_to_sheet(rows);
          xlsx.utils.sheet_add_aoa(ws, Heading);
          xlsx.utils.book_append_sheet(wb, ws, k.replace(/:/gi, '-'));
        });
        const fileName = 'DBScheduler_' + sDate + '.xlsx';
        xlsx.writeFile(wb, fileName);
      }
    });
  }

  viewLog(): void {
    const fileNameSystem = 'dbschedule-procs';
    this.router.navigate(['/db-scheduler/viewlog'], {
      queryParams: { topicName: fileNameSystem },
    });
  }
}
