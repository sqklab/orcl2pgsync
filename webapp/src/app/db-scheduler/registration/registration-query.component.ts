import { Component, Input, OnInit } from '@angular/core';
import { NgbModal, NgbTimeStruct } from '@ng-bootstrap/ng-bootstrap';
import { SortType } from 'src/app/libs/constants';
import { AuthService } from 'src/app/libs/services/auth.service';
import { ComparisonService } from 'src/app/libs/services/comparison.service';
import { DBConfigService } from 'src/app/libs/services/dbconfig.service';
import { ExecuteQueryService } from 'src/app/libs/services/executeQuery.service';
import {
  DayOfWeek,
  DBScheduleDto,
  MonthQuarterYear, Task
} from 'src/app/libs/services/interface';
import StorageService from 'src/app/libs/services/storage.service';
import { SweetAlert } from 'src/app/libs/utils/sweetalert';
import { Utils } from 'src/app/libs/utils/utils';
import { RegistrationHelper } from './registration-helper';

@Component({
  selector: 'app-registrator-schedule',
  templateUrl: './registration-query.component.html',
  styleUrls: ['./registration-query.component.scss'],
})
export class ExecuteQueryComponent implements OnInit {

  serverNames = [];

  selected: number[] = [];
  dayOfWeeks: DayOfWeek[] = [];
  schedulesOfWeek: DBScheduleDto[] = [];

  mapDayOfWeek = new Map();

  registedSchedule: DBScheduleDto[] = [];

  newSchedule: DBScheduleDto;

  totalSchedules = 0;

  @Input() filterObject: DBScheduleDto;

  // for filters
  sql = '';
  db = '';
  schema = '';
  table = '';
  filterStatus = [true, false];
  sortType = SortType.DESC;

  targetDB = [];
  targetSchema = [];
  tables = [];

  invalidSQL = false;
  invalidDB = false;

  dateFrom = '';

  timeOnServer = '';

  schedulesDaily: DBScheduleDto[] = [];
  mapPositionsForWeek = new Map();

  totalPage = 0;
  currentPage = 1;
  pageSize = 20;

  errorMsgDaily = '';
  errorMsgWeek = '';
  errorMsgMonth = '';
  errorMsgQuaterly = '';
  errorMsgYearly = '';

  errorMsgInput = '';

  constructor(
    private api: ExecuteQueryService,
    protected storage: StorageService,
    private comparisonService: ComparisonService,
    private modalService: NgbModal,
    private authService: AuthService,
    private dbConfigService: DBConfigService
  ) {
    this.newSchedule = {
      id: null,
      plSQL: '',
      db: '',
      schema: '',
      table: '',
      type: 0,
      timeDaily: '',
      status: true,
      schedulesOfMonth: [],
      schedulesOfQuarterly: [],
      schedulesOfYear: [],
    } as DBScheduleDto;
    this.initDayOfWeek();
  }

  ngOnInit(): void {
    this.countAll();
    this.onSearch();
    this.onGetTimeOnServer();
    this.loadAllServerNames();
  }

  private loadAllServerNames(): void {
    this.serverNames = [];
    this.dbConfigService.getDBConfigs(1, 100).subscribe(res => {
      if (res) {
        res.dataSourceDescriptions.forEach(x => this.serverNames.push(x.serverName));
      }
    });
  }

  private countAll(): void {
    this.api.countAll().subscribe(data => {
      this.totalSchedules = data.body;
    });
  }

  handleFilter(event): void {
    this.filterObject = event;
    if (this.filterObject) {
      const sortType = SortType.DESC;
      this.sql = this.filterObject ? this.filterObject.plSQL : '';
      this.db = this.filterObject ? this.filterObject.db : '';
      this.schema = this.filterObject ? this.filterObject.schema : '';
      this.dateFrom = this.filterObject ? this.filterObject.createdAt : '';
      this.table = this.filterObject ? this.filterObject.table : '';
      this.filterStatus = this.filterObject.filterStatus;

      this.api
        .filter(
          this.sql,
          this.db,
          this.schema,
          this.dateFrom,
          this.table,
          this.filterStatus,
          this.currentPage,
          this.pageSize,
          'id',
          sortType
        )
        .subscribe((data) => {
          this.registedSchedule = data.entities;
          this.totalPage = data.totalPage;
        });
    }
  }

  initMonthy(): void {
    this.newSchedule.schedulesOfMonth = [];
    this.comparisonService.getTimeOnServer().subscribe((data) => {
      this.timeOnServer = data.body.dateTime;
      const timeServer = Utils.convertDateServer(this.timeOnServer);
      this.newSchedule.schedulesOfMonth.push({
        day: '',
        time: timeServer.time,
        timeDisplay: timeServer.timeDisplay,
      } as MonthQuarterYear);
    });
  }

  initQuaterly(): void {
    this.comparisonService.getTimeOnServer().subscribe((data) => {
      this.timeOnServer = data.body.dateTime;
      if (!this.newSchedule.id) {
        // create new
        const time = new Date(this.timeOnServer);
        const timeServer = Utils.convertDateServer(this.timeOnServer);
        const newQuarterly = {
          month: 1,
          day: time.getDate().toString(),
          time: timeServer.time,
          timeDisplay: timeServer.timeDisplay,
        } as MonthQuarterYear;
        this.newSchedule.schedulesOfQuarterly = [newQuarterly];
      }
    });
  }

  initYearly(): void {
    this.comparisonService.getTimeOnServer().subscribe((data) => {
      this.timeOnServer = data.body.dateTime;
      if (!this.newSchedule.id) {
        // create new
        const time = new Date(this.timeOnServer);
        const timeServer = Utils.convertDateServer(this.timeOnServer);
        const newYearly = {
          month: data.body.month,
          day: time.getDate().toString(),
          time: timeServer.time,
          timeDisplay: timeServer.timeDisplay,
        } as MonthQuarterYear;
        this.newSchedule.schedulesOfYear = [newYearly];
      }
    });
  }

  addNewScheduleMonth(): void {
    const timeServer = Utils.convertDateServer(this.timeOnServer);
    this.newSchedule.schedulesOfMonth.push({
      day: '1',
      time: timeServer.time,
      timeDisplay: timeServer.timeDisplay,
    } as MonthQuarterYear);
  }

  addNewScheduleQuarterly(): void {
    if (this.newSchedule.schedulesOfQuarterly.length >= 3) {
      return;
    }
    const timeServer = Utils.convertDateServer(this.timeOnServer);
    this.newSchedule.schedulesOfQuarterly.push({
      day: '1',
      time: timeServer.time,
      timeDisplay: timeServer.timeDisplay,
      month: this.newSchedule.schedulesOfQuarterly.length + 1,
    } as MonthQuarterYear);
  }

  addNewScheduleYearly(): void {
    const timeServer = Utils.convertDateServer(this.timeOnServer);
    this.newSchedule.schedulesOfYear.push({
      day: '1',
      time: timeServer.time,
      timeDisplay: timeServer.timeDisplay,
      month: this.newSchedule.schedulesOfYear.length + 1,
    } as MonthQuarterYear);
  }

  changeLastDay(event, item: MonthQuarterYear): void {
    if (event.target.checked) {
      item.day = RegistrationHelper.LAST_DAY;
    } else {
      item.day = '';
    }
  }

  getLastDay(): any {
    return RegistrationHelper.LAST_DAY;
  }

  getSTT(index): any {
    return RegistrationHelper.nth(index);
  }

  getTypeName(i): string {
    if (i === 0) {
      return 'Day';
    }
    if (i === 1) {
      return 'Week';
    }
    if (i === 2) {
      return 'Month:';
    }
    if (i === 3) {
      return 'Quarterly:';
    }
    if (i === 4) {
      return 'Yearly:';
    }
  }

  private onSearch(): void {
    this.api
      .filter(
        this.sql,
        this.db,
        this.schema,
        this.dateFrom,
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

  initDayOfWeek(): void {
    this.dayOfWeeks = RegistrationHelper.initDayOfWeek();
  }

  private onGetTimeOnServer(): void {
    this.comparisonService.getTimeOnServer().subscribe((data) => {
      this.timeOnServer = data.body.dateTime;
    });
  }

  openViewInfo(content): void {
    this.modalService
      .open(content, { size: 'xl', backdrop: 'static' })
      .result.then(
        (result) => {},
        (reason) => {
          this.onCloseSetting();
        }
      );
  }

  scheduleAll(): void {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Information',
      message: 'Are you sure to Run All?',
      confirm: () => {
        this.authService.onLoading$.next(true);
        this.api.scheduleAll().subscribe(data => {
          if (data.status === 500) {
            SweetAlert.notifyMessage('Error: ' + data.body, 'error');
          } else {
            const result = data.body;
            SweetAlert.notifyMessage(result.numSuccess + ' Success. ' + result.numFailure + ' Failure');
            this.authService.onLoading$.next(false);
          }
          this.authService.onLoading$.next(false);
        }, err => {
          SweetAlert.notifyMessage('Run all has error', 'error');
          this.authService.onLoading$.next(false);
        });
      },
      type: 'info',
      cancel: true,
    });


  }

  registerNewSchedule(contentViewInfo): void {
    this.newSchedule = {
      id: null,
      plSQL: '',
      db: '',
      schema: '',
      table: '',
      type: 0,
      timeDaily: '',
      status: true,
      schedulesOfMonth: [],
      schedulesOfQuarterly: [],
      schedulesOfYear: [],
    } as DBScheduleDto;
    this.schedulesDaily = [];
    this.addNewSchedule();
    this.schedulesOfWeek = [];
    this.onGetTimeOnServer();
    this.openViewInfo(contentViewInfo);
  }

  onCloseSetting(): void {
    this.invalidDB = false;
    this.invalidSQL = false;

    this.errorMsgDaily = '';
    this.errorMsgInput = '';
    this.errorMsgWeek = '';
    this.errorMsgMonth = '';
    this.errorMsgQuaterly = '';
    this.errorMsgYearly = '';

    this.modalService.dismissAll();
  }

  addNewSchedule(): void {
    const datetime = new Date(this.timeOnServer);
    const hour = datetime.getHours();
    const minute = datetime.getMinutes();
    const timeDisplay = {
      hour: +hour,
      minute: +minute,
      second: 0,
    } as NgbTimeStruct;
    const newItem = {
      timeDaily: hour + ':' + minute,
      timeDisplay,
    } as DBScheduleDto;
    this.schedulesDaily.push(newItem);
  }

  onSaveSchedule(): void {
    this.errorMsgWeek = '';
    this.invalidSQL = false;
    this.invalidDB = false;
    this.errorMsgInput = '';
    this.errorMsgDaily = '';

    if (!this.newSchedule.plSQL || this.newSchedule.plSQL.trim() === '') {
      this.errorMsgInput = 'PL SQL is required!';
      this.invalidSQL = true;
      return;
    }

    if (!this.newSchedule.db || this.newSchedule.db.trim() === '') {
      this.errorMsgInput = 'DB is required!';
      this.invalidDB = true;
      return;
    }

    // validate daily
    if (this.newSchedule.type === 0 && this.schedulesDaily.length === 0) {
      this.errorMsgDaily = 'You should add new at least one schedule';
      return;
    }

    // validate dayofweek
    if (this.newSchedule.type === 1) {
      if (
        this.schedulesOfWeek.length > 0 &&
        this.dayOfWeeks.filter((x) => x.actived).length === 0
      ) {
        // error
        this.errorMsgWeek = 'You should select a day of Week';
        // this.activeScheduleType =1;
        this.newSchedule.type = 1;
        return;
      }
      if (
        this.schedulesOfWeek.length === 0 &&
        this.dayOfWeeks.filter((x) => x.actived).length > 0
      ) {
        // error
        this.errorMsgWeek = 'You should add new Schedule';
        this.newSchedule.type = 1;
        return;
      }
      if (
        this.newSchedule.type === 1 &&
        this.schedulesOfWeek.length === 0 &&
        this.dayOfWeeks.filter((x) => x.actived).length === 0
      ) {
        // error
        this.errorMsgWeek = 'You should select a day of Week';
        this.newSchedule.type = 1;
        return;
      }
    }

    // validate monthy
    if (this.newSchedule.type === 2) {
      this.errorMsgMonth = RegistrationHelper.validateMonthy(
        this.newSchedule.schedulesOfMonth
      );
      if (this.errorMsgMonth) {
        return;
      }
    }

    // validate quaterly
    if (this.newSchedule.type === 3) {
      this.errorMsgQuaterly = RegistrationHelper.validateQuaterlyOYear(
        this.newSchedule.schedulesOfQuarterly
      );
      if (this.errorMsgQuaterly) {
        return;
      }
    }

    // validate yearly
    if (this.newSchedule.type === 4) {
      this.errorMsgYearly = RegistrationHelper.validateYearly(
        this.newSchedule.schedulesOfYear
      );
      if (this.errorMsgYearly) {
        return;
      }
    }

    const selectedDay = this.dayOfWeeks.filter((x) => x.actived);
    if (selectedDay.length > 0) {
      this.newSchedule.dayOfWeek = selectedDay.map((x) => x.value).join(',');
    }
    if (this.schedulesDaily.length > 0) {
      const arr = this.schedulesDaily.map((item) => {
        return RegistrationHelper.getTimeValueForSave(item.timeDisplay);
      });
      this.newSchedule.timeDaily = arr.join(',');
    }
    if (this.schedulesOfWeek.length > 0) {
      const arr = this.schedulesOfWeek.map((item) => {
        return RegistrationHelper.getTimeValueForSave(item.timeDisplay);
      });
      this.newSchedule.timesOfWeek = arr.join(',');
    }

    if (
      this.newSchedule.type === 2 &&
      this.newSchedule.schedulesOfMonth.length > 0
    ) {
      const arrMonth = this.newSchedule.schedulesOfMonth
        .filter((x) => x)
        .map(
          (value) =>
            value.day +
            ',' +
            RegistrationHelper.getTimeValueForSave(value.timeDisplay)
        );
      this.newSchedule.monthly = arrMonth.join(';');
    }
    if (
      this.newSchedule.type === 3 &&
      this.newSchedule.schedulesOfQuarterly.length > 0
    ) {
      const arrQuarterly = this.newSchedule.schedulesOfQuarterly.map(
        (value) =>
          value.month +
          ',' +
          value.day +
          ',' +
          RegistrationHelper.getTimeValueForSave(value.timeDisplay)
      );
      this.newSchedule.quarterly = arrQuarterly.join(';');
    }
    if (
      this.newSchedule.type === 4 &&
      this.newSchedule.schedulesOfYear.length > 0
    ) {
      const arrYearly = this.newSchedule.schedulesOfYear.map(
        (value) =>
          value.month +
          ',' +
          value.day +
          ',' +
          RegistrationHelper.getTimeValueForSave(value.timeDisplay)
      );
      this.newSchedule.yearly = arrYearly.join(';');
    }

    // call api
    this.api.add(this.newSchedule).subscribe(
      (res) => {
        if (res.status === 200) {
          this.dayOfWeeks.forEach((d) => (d.actived = false));
          SweetAlert.notifyMessage('Add new Schedule Success!');
          this.onSearch();
          this.countAll();
          this.onCloseSetting();
        } else if (res.status === 500) {
          SweetAlert.notifyMessage('Error!', 'error');
        }
      },
      (err) => {
        SweetAlert.notifyMessage('Error!', 'error');
      },
      () => this.onCloseSetting()
    );
  }

  deleteSelectedItems(): void {
    if (this.selected.length > 0) {
      const isSchedule = this.registedSchedule.some(item => this.selected.includes(item.id) && item.status);
      if (isSchedule) {
        this.authService.onShowModal$.next({
          isShow: true,
          title: 'Warning',
          message: 'Can not delete Scheduling item',
          type: 'warning',
          confirm: () => {},
        });
      } else {
        this.authService.onShowModal$.next({
          isShow: true,
          title: 'Information',
          message: 'Are you sure to DELETE?',
          confirm: () => {
            this.authService.onLoading$.next(true);
            this.api.deleteRegistrationByIds(this.selected).subscribe(
              (data) => {
                this.authService.onLoading$.next(false);
                this.selected = [];
                if (data) {
                  SweetAlert.notifyMessage('Delete result success!');
                  this.onSearch();
                  this.countAll();
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
      }
    } else {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Information',
        message: 'You should select at least ONE to DELETE',
        type: 'info',
        confirm: () => {},
      });
    }
  }

  onEdit(info: DBScheduleDto, content): void {
    this.api.view(info.id).subscribe((doc) => {
      this.newSchedule = doc.body;
      this.newSchedule.schedulesOfMonth = [];
      this.newSchedule.schedulesOfQuarterly = [];
      this.newSchedule.schedulesOfYear = [];

      if (this.newSchedule.type === 1) {
        // for weekly
        this.schedulesOfWeek = [];

        const arr = this.newSchedule.timesOfWeek
          ? this.newSchedule.timesOfWeek.split(',')
          : [];
        arr.forEach((ar: string) => {
          const timeSplit = ar.split(':');
          const timeDisplay = {
            hour: +timeSplit[0],
            minute: +timeSplit[1],
            second: 0,
          } as NgbTimeStruct;
          const newItem = {
            timeDisplay,
          } as DBScheduleDto;
          this.schedulesOfWeek.push(newItem);
        });
        const days = this.newSchedule.dayOfWeek
          ? this.newSchedule.dayOfWeek.split(',')
          : [];
        this.dayOfWeeks.forEach((d) => {
          if (days.includes(d.value.toString())) {
            d.actived = true;
          }
        });
      }
      if (this.newSchedule.type === 0) {
        // daily
        this.schedulesDaily = [];
        const arr = this.newSchedule.timeDaily.split(',');
        arr.forEach((ar: string) => {
          const timeSplit = ar.split(':');
          const timeDisplay = {
            hour: +timeSplit[0],
            minute: +timeSplit[1],
            second: 0,
          } as NgbTimeStruct;
          const newItem = {
            timeDisplay,
          } as DBScheduleDto;
          this.schedulesDaily.push(newItem);
        });
      }
      if (this.newSchedule.type === 2) {
        // monthy
        if (this.newSchedule.monthly && this.newSchedule.monthly.length > 0) {
          this.newSchedule.monthly.split(';').forEach((ar) => {
            const dayTime = ar.split(',');
            const isLastDay = RegistrationHelper.LAST_DAY === dayTime[0];
            this.newSchedule.schedulesOfMonth.push({
              day: dayTime[0],
              isLastDay,
              timeDisplay: RegistrationHelper.getTimeNgbStruct(dayTime[1]),
            } as MonthQuarterYear);
          });
        }
      }
      if (this.newSchedule.type === 3) {
        // quarterly
        if (
          this.newSchedule.quarterly &&
          this.newSchedule.quarterly.length > 0
        ) {
          this.newSchedule.quarterly.split(';').forEach((ar) => {
            const dayTime = ar.split(',');
            const isLastDay = RegistrationHelper.LAST_DAY === dayTime[1];
            this.newSchedule.schedulesOfQuarterly.push({
              month: +dayTime[0],
              day: dayTime[1],
              isLastDay,
              timeDisplay: RegistrationHelper.getTimeNgbStruct(dayTime[2]),
            } as MonthQuarterYear);
          });
        }
      }
      if (this.newSchedule.type === 4) {
        // quarterly
        if (this.newSchedule.yearly && this.newSchedule.yearly.length > 0) {
          this.newSchedule.yearly.split(';').forEach((ar) => {
            const dayTime = ar.split(',');
            const isLastDay = RegistrationHelper.LAST_DAY === dayTime[1];
            this.newSchedule.schedulesOfYear.push({
              month: +dayTime[0],
              day: dayTime[1],
              isLastDay,
              timeDisplay: RegistrationHelper.getTimeNgbStruct(dayTime[2]),
            } as MonthQuarterYear);
          });
        }
      }
      this.openViewInfo(content);
    });
  }

  testScript(): void {
    this.errorMsgWeek = '';
    this.invalidSQL = false;
    this.invalidDB = false;

    if (!this.newSchedule.plSQL || this.newSchedule.plSQL.trim() === '') {
      this.errorMsgInput = 'PL SQL is required!';
      this.invalidSQL = true;
      return;
    }

    if (!this.newSchedule.db || this.newSchedule.db.trim() === '') {
      this.errorMsgInput = 'DB is required!';
      this.invalidSQL = true;
      this.invalidDB = true;
      return;
    }

    this.api.test(this.newSchedule).subscribe((data) => {
      if (data.status === 500) {
        SweetAlert.notifyMessage('Error: ' + data.body, 'error');
      } else {
        SweetAlert.notifyMessage(data.body);
      }
    });
  }

  addNewScheduleDayOfWeek(): void {
    // check selected day's week yet?
    const selected = this.dayOfWeeks.filter((x) => x.actived).length > 0;
    this.errorMsgWeek = '';
    if (!selected) {
      this.errorMsgWeek = 'You should select a day of Week';
      return;
    }

    // const timeNow = this.formater.format(this.calendar.getToday());
    const datetime = new Date(this.timeOnServer);
    const hour = datetime.getHours();
    const minute = datetime.getMinutes();

    const timeDisplay = {
      hour,
      minute,
      second: 0,
    } as NgbTimeStruct;
    const newItem = {
      timeDaily: hour + ':' + minute,
      timeDisplay,
    } as DBScheduleDto;
    this.schedulesOfWeek.push(newItem);
  }

  selectDayOfWeek(day: DayOfWeek): void {
    day.actived = !day.actived;
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

  changeStatus(status): void {
    this.newSchedule.status = status;
  }

  parseScheduleDisplay(label: string): string {
    if (label && label.indexOf(';') > 0) {
      return label.replace(/;/g, '|');
    } else {
      return label;
    }
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

  selectItem(event, doc: DBScheduleDto): void {
    doc.checked = event.target.checked;
    if (event.target.checked) {
      this.selected.push(Number(doc.id));
    } else {
      this.selected = this.selected.filter((item) => item !== Number(doc.id));
    }
  }
}
