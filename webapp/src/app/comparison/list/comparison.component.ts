import { DatePipe } from '@angular/common';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import {
  NgbCalendar,
  NgbDateStruct,
  NgbModal,
  NgbModalRef,
  NgbTimeStruct,
  NgbTypeahead
} from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';
import * as copy from 'copy-to-clipboard';
import { Workbook, Worksheet } from 'exceljs';
import * as fs from 'file-saver';
import { Observable, Subject } from 'rxjs';
import { RegistrationHelper } from 'src/app/db-scheduler/registration/registration-helper';
import {
  CURRENT_PAGE,
  CURRENT_PAGE_COMPARISON,
  CURRENT_SEARCHING_TABLE_NAME,
  PAGE_SIZE_COMPARISON,
  SortType
} from 'src/app/libs/constants';
import { AuthService } from 'src/app/libs/services/auth.service';
import { ComparisonService } from 'src/app/libs/services/comparison.service';
import {
  ComparisonResultDto,
  ComparisonSummary,
  DbComparisonResultDto,
  DbComparisonScheduleDto,
  GroupDateTime, QuickRunComparisonQuery, SynchronizationInfo,
  ViewComparisonResultDto, ViewKafkaConsumerGroup
} from 'src/app/libs/services/interface';
import StorageService from 'src/app/libs/services/storage.service';
import { NgbDateCustomParserFormatter } from 'src/app/libs/utils/helper';
import { SweetAlert } from 'src/app/libs/utils/sweetalert';
import { ViewSynchronizerComponent } from 'src/app/synchronizer/view-synchronizer/view-synchronizer.component';

export interface State {
  name: string;
  clazz: string;
  active: boolean;
}

@Component({
  selector: 'app-comparison',
  templateUrl: './comparison.component.html',
  styleUrls: ['./comparison.component.scss'],
})
export class ComparisonComponent implements OnInit {
  readonly DELIMITER = '-';
  mapState = new Map();
  viewLastMessageInfo: SynchronizationInfo;
  viewKafkaConsumerGroups: ViewKafkaConsumerGroup[] = [];

  selected: number[] = [];
  totalPage = 0;
  currentPage = 1;
  pageSize = 20;

  sortField = 'lastModified';
  dataSuggests: any[] = [];
  isSearching: boolean;
  fileToUpload: File | null = null;

  dateFrom: NgbDateStruct;
  timeSchedule = '';
  timeOnServer = '';

  selectedTableName = '';
  selectedSourceDB = '';
  selectedSourceSchema = '';
  selectedTargetDB = '';

  selectedTargetSchema = '';
  selectedDivision = '';

  sortErrorLogUp = 'bi bi-sort-up';
  sortErrorLogDown = 'bi bi-sort-down';
  sortIcon = this.sortErrorLogUp;
  sortType = SortType.DESC;

  summary: ComparisonSummary = {
    total: 0,
    different: 0,
    equal: 0,
    fail: 0,
    remaining: 0,
    sourceCount: 0,
    targetCount: 0,
  } as ComparisonSummary;

  isRemainingLoad = false;

  lastSelected: ViewComparisonResultDto = null;

  tableNames = [];
  sourceDBs = [];
  sourceSchemas = [];
  targetDBs = [];
  targetSchemas = [];
  divisions = [];
  states: State[] = [];

  @ViewChild('box', { static: true }) searchInput: ElementRef;
  @ViewChild('instance', { static: true }) instance: NgbTypeahead;
  @ViewChild('instanceSourceDB', { static: true })
  instanceSourceDB: NgbTypeahead;
  @ViewChild('instanceSourceSchema', { static: true })
  instanceSourceSchema: NgbTypeahead;
  @ViewChild('instanceTargetDB', { static: true })
  instanceTargetDB: NgbTypeahead;
  @ViewChild('instanceTargetSchema', { static: true })
  instanceTargetSchema: NgbTypeahead;

  schedules: DbComparisonScheduleDto[] = [];
  compareResult: DbComparisonResultDto[] = [];
  viewCompareResults: ViewComparisonResultDto[] = [];
  groupDateTime: GroupDateTime[] = [];

  statusComparison = false;
  comparisonState = '';
  quickViewInfo: ViewComparisonResultDto;
  resultQuickRunSource = '';
  resultQuickRunTarget = '';
  openSchedule$: NgbModalRef;

  constructor(
    private authService: AuthService,
    private comparisonService: ComparisonService,
    private formater: NgbDateCustomParserFormatter,
    public translate: TranslateService,
    private modalService: NgbModal,
    protected storage: StorageService,
    private calendar: NgbCalendar,
    private datePipe: DatePipe,
    private router: Router
  ) {
    const pageSize = this.storage.get(PAGE_SIZE_COMPARISON);
    if (pageSize) {
      this.pageSize = Number(pageSize);
    }
    const currentPageFromCache = this.storage.get(CURRENT_PAGE);
    if (currentPageFromCache) {
      this.currentPage = Number(currentPageFromCache);
    }
    const currentSelectedTopicName: string = this.storage.get(
      CURRENT_SEARCHING_TABLE_NAME
    );
    if (currentSelectedTopicName) {
      this.selectedTableName = currentSelectedTopicName;
    }
    this.states.push({
      name: 'SAME',
      clazz: 'filter-status bg-success text-white',
      active: false,
    } as State);
    this.states.push({
      name: 'DIFFERENT',
      clazz: 'filter-status bg-warning text-white',
      active: false,
    } as State);
    this.states.push({
      name: 'FAILED',
      clazz: 'filter-status bg-danger text-white',
      active: false,
    } as State);
  }

  ngOnInit(): void {
    this.authService.onLoading$.next(true);
    this.dateFrom = this.calendar.getToday();
    this.loadResultByDate(this.dateFrom);
    this.loadSchedules();
    this.loadViewInfoForSearching();
    this.runningCount();
    this.getDivisions();
  }

  openLastMsgInfo(lastMsgInfo, doc): void {
    this.viewLastMessageInfo = doc;
    this.viewLastMessageInfo.msgTimestamp = doc.msgTimestamp === 0 ? null : doc.msgTimestamp;
    this.modalService.open(lastMsgInfo);
  }

  onCloseModal(): void {
    this.modalService.dismissAll();
  }

  viewLog(): void {
    const fileNameSystem = 'comparison';
    this.router.navigate(['/comparison/viewlog'], {
      queryParams: { topicName: fileNameSystem },
    });
  }

  private runningCount(): void {
    this.comparisonService.runningCount().subscribe((data) => {
      if (data && +data > 0) {
        this.statusComparison = true;
      }
    });
  }

  private getDivisions(): void {
    this.comparisonService.getDivisions().subscribe(data => this.divisions = data);
  }

  toLocalString(num: number): any {
    if (num) {
      return num.toLocaleString();
    } else {
      return num;
    }
  }

  openSyncInfo(id, syncId): void {
    const modalRef = this.modalService.open(ViewSynchronizerComponent, {
      size: 'xl',
    });
    modalRef.componentInstance.id = syncId;
    modalRef.componentInstance.comparisonId = id;
    modalRef.componentInstance.isSynRdId = true;
  }

  checkRemainingLoad(): boolean {
    return this.summary.remaining > 0;
  }

  viewConsumerGroup(viewKafkaConsumerGroup): void {
    this.viewKafkaConsumerGroups = [];
    this.authService.onLoading$.next(true);
    this.comparisonService.viewKafkaConsumerGroup(1, 1, 10).subscribe(data => {
      this.viewKafkaConsumerGroups = data;
      this.modalService.open(viewKafkaConsumerGroup, {
        size: 'lg',
      });
    }, err => {
      console.log('Error:' + err);
    }, () => {
      this.authService.onLoading$.next(false);
    });
  }

  loadResultByDate(date: NgbDateStruct): void {
    const sDate = this.formater.format(date);
    this.groupDateTime = [];
    this.totalPage = 0;
    this.authService.onLoading$.next(true);
    this.comparisonService.getResultByDate(sDate).subscribe((data) => {
      this.authService.onLoading$.next(false);
      if (data && data.length > 0) {
        data.forEach((dateTime, index) => {
          this.groupDateTime.push({
            compareDate: sDate,
            compareTime: dateTime,
            active: index === 0 ? true : false,
          } as GroupDateTime);
        });
        this.timeSchedule = this.groupDateTime[0].compareTime;
        this.onSearch();
        this.getSummaryByDateAndTime(sDate);
      } else {
        this.groupDateTime = [];
        this.viewCompareResults = [];
        this.summary = {
          total: 0,
          fail: 0,
          equal: 0,
          different: 0,
          remaining: 0,
          sourceCount: 0,
          targetCount: 0,
        } as ComparisonSummary;
      }
    });
  }

  private getSummaryByDateAndTime(sDate): void {
    this.authService.onLoading$.next(true);
    this.comparisonService
      .getSummaryByDateAndTime(sDate, this.timeSchedule)
      .subscribe((data) => {
        if (data) {
          this.summary = data;
          this.authService.onLoading$.next(false);
        }
      });
  }

  calcRunning(): number {
    return this.summary.different + this.summary.equal + this.summary.fail;
  }

  toArr(data: ComparisonResultDto): [string, ComparisonResultDto][] {
    if (!data) {
      return [];
    }
    const arr = [];
    const times = Object.keys(data.map);
    times.forEach((time) => arr.push([time, data.map[time]]));

    return arr;
  }

  toArrRaw(
    data: Map<string, ViewComparisonResultDto[]>
  ): [string, ViewComparisonResultDto[]][] {
    if (!data) {
      return [];
    }
    const arr = [];
    const times = Object.keys(data).sort().reverse();
    times.forEach((time) => arr.push([time, data[time]]));

    return arr;
  }

  sortNumberDiff(): void {
    this.currentPage = 1;
    this.storage.save(CURRENT_PAGE, this.currentPage);
    this.states.forEach((st) => (st.active = false));
    this.states[1].active = true;
    this.comparisonState = this.states[1].name;
    this.sortIcon =
      this.sortIcon === this.sortErrorLogUp
        ? this.sortErrorLogDown
        : this.sortErrorLogUp;
    this.sortField = 'numberDiff';
    this.sortType =
      this.sortIcon === this.sortErrorLogUp ? SortType.ASC : SortType.DESC;
    this.onSearch();
  }

  exportToExcel(): void {
    if (!this.timeSchedule) {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Information',
        message: 'You have select one Schedule time to Export',
        type: 'info',
        confirm: () => {},
      });
      return;
    }
    const sDate = this.formater.format(this.dateFrom);
    const Heading = [
        'Source Database',
        'Source Schema',
        'Source Table',
        'Source Count',
        'Status',
        'Target Count',
        'Target Database',
        'Target Schema',
        'Target Table',
        'Division',
        'Last Update',
    ];
    this.authService.onLoading$.next(true);
    this.comparisonService
      .exportByDateAndTime(sDate, this.timeSchedule)
      .subscribe((response) => {
        this.authService.onLoading$.next(false);
        if (response) {
          const data = response as Map<string, ViewComparisonResultDto[]>;
          const newArray = Object.entries(data);
          const map = new Map(newArray);
          map.forEach((v: ViewComparisonResultDto[], k) => {
            const dataCell: any[][] = this.toArrayCell(v);
            // Create workbook and worksheet
            const workbook = new Workbook();
            const worksheet = workbook.addWorksheet(k.replace(/:/gi, '-'));

            // Add Header Row
            const headerRow = worksheet.addRow(Heading);
            // Cell Style : Fill and Border
            headerRow.eachCell((cell, i) => {
              cell.font = {
                name: 'Arial',
                family: 2,
                bold: true,
                size: 14,
              };
              cell.fill = {
                type: 'pattern',
                pattern: 'solid',
                fgColor: { argb: 'FFFFFF' }
              };
              cell.border = { top: { style: 'thin' }, left: { style: 'thin' }, bottom: { style: 'thin' }, right: { style: 'thin' } };
              if (i !== 5 && i !== 10) {
                worksheet.getColumn(i).width = 30;
              } else {
                worksheet.getColumn(i).width = 12;
              }
            });

            this.styleStatusColumn(dataCell, worksheet);

            const fileName = 'Comparison_' + sDate + '.xlsx';
            workbook.xlsx.writeBuffer().then(rs => {
              const blob = new Blob([rs], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
              fs.saveAs(blob, fileName);
              this.authService.onLoading$.next(false);
            });
          });
        }
      });
  }

  private toArrayCell(v: ViewComparisonResultDto[]): any[][] {
    const dataCell: any[][] = [];
    v.forEach((item: ViewComparisonResultDto) => {
      const dataRaw: any[] = [];
      dataRaw.push(item.sourceDatabase);
      dataRaw.push(item.sourceSchema);
      dataRaw.push(item.sourceTable);
      dataRaw.push(item.sourceCount);
      dataRaw.push(item.comparisonState);
      dataRaw.push(item.targetCount);
      dataRaw.push(item.syncRdId ? item.rdTargetDatabase : item.targetDatabase);
      dataRaw.push(item.syncRdId ? item.rdTargetSchema : item.targetSchema);
      dataRaw.push(item.syncRdId ? item.rdTargetTable : item.targetTable);
      dataRaw.push(item.division);
      dataRaw.push(this.parseDate(item.lastModified));
      dataCell.push(dataRaw);
    });
    return dataCell;
  }

  private styleStatusColumn(excelModel: any[][], worksheetRaw: Worksheet) {
    excelModel.forEach(model => {
      const row = worksheetRaw.addRow(model);
      const cellStatus = row.getCell(5); // Status
      let color = 'FFFFFF';
      if (cellStatus.value === 'SAME') {
        color = '2eb85c';
      } else if (cellStatus.value === 'DIFFERENT') {
        color = 'ffc107';
      } else if (cellStatus.value === 'FAILED') {
        color = 'dc3545';
      }
      cellStatus.fill = {
        type: 'pattern',
        pattern: 'solid',
        fgColor: { argb: color }
      };
      cellStatus.alignment = {
        horizontal: 'center'
      };
    });
  }

  exportAllToExcel(): void {
    const sDate = this.formater.format(this.dateFrom);
    this.authService.onLoading$.next(true);
    this.comparisonService.exportAllByDate(sDate).subscribe((response) => {
      if (response) {
        const map = new Map(this.toArr(response));
        const data: any[][] = [];

        map.forEach((v: any, k) => {
          const excelModel: any[] = [];
          const diffs = Array.from(v.diffs);
          const mapTimeAndNumberDiff = new Map();
          diffs.map(x => {
            const keys = Object.keys(x);
            mapTimeAndNumberDiff.set(keys[0], x[keys[0]]);
          });
          const splitK = k.split('.');
          excelModel.push(splitK[0]);
          excelModel.push(splitK[1]);
          excelModel.push(splitK[2]);
          excelModel.push(splitK[3]);
          excelModel.push(splitK[4]);
          excelModel.push(splitK[5]);
          excelModel.push(v.diffCount);
          response.headers.forEach(h => {
            const diff = mapTimeAndNumberDiff.get(h);
            if (diff !== undefined) {
              excelModel.push(diff);
            } else {
              excelModel.push(0);
            }
          });
          data.push(excelModel);
        });
        data.sort((a, b) => {
          return b[6] - a[6];
        });

        const Heading = [
          'Source Database',
          'Source Schema',
          'Source Table',
          'Target Database',
          'Target Schema',
          'Target Table',
          'Diff Count',
        ];
        response.headers.forEach(h => {
          Heading.push(h);
        });

        // Create workbook and worksheet
        const workbook = new Workbook();
        const worksheet = workbook.addWorksheet('DIFF_RESULT');

        // Add Header Row
        const headerRow = worksheet.addRow(Heading);
        // Cell Style : Fill and Border
        headerRow.eachCell((cell, i) => {
          cell.font = {
            name: 'Arial',
            family: 2,
            bold: true,
            size: 14,
          };
          cell.fill = {
            type: 'pattern',
            pattern: 'solid',
            fgColor: { argb: 'FFFFFF' }
          };
          cell.border = { top: { style: 'thin' }, left: { style: 'thin' }, bottom: { style: 'thin' }, right: { style: 'thin' } };
          if (i <= 7) {
            worksheet.getColumn(i).width = 30;
          } else {
            worksheet.getColumn(i).width = 20;
          }
        });

        // Add Data and Conditional Formatting
        data.forEach((model, i) => {
          worksheet.addRow(model);
        });

        // append more raw sheet
        const dataRaw = response.rawValues as Map<string, ViewComparisonResultDto[]>;
        const mapRaw = new Map(this.toArrRaw(dataRaw));
        const headerRaw = ['Source Database', 'Source Schema',	'Source Table',	'Source Count',	'Status',
        'Target Count',	'Target Database',	'Target Schema',
        'Target Table',	'Division',	'Last Update'];
        mapRaw.forEach((v: ViewComparisonResultDto[], k) => {
          const worksheetRaw = workbook.addWorksheet(k.replace(/:/gi, '-'));
          const headerRow = worksheetRaw.addRow(headerRaw);
          headerRow.eachCell((cell, i) => {
            cell.font = {
              name: 'Arial',
              family: 2,
              bold: true,
              size: 14,
            };
            cell.fill = {
              type: 'pattern',
              pattern: 'solid',
              fgColor: { argb: 'FFFFFF' }
            };
            cell.border = { top: { style: 'thin' }, left: { style: 'thin' }, bottom: { style: 'thin' }, right: { style: 'thin' } };
            if (i <= 4) {
              worksheetRaw.getColumn(i).width = 30;
            } else if (i > 5) {
              worksheetRaw.getColumn(i).width = 20;
            }
          });

          const excelModel: any[][] = this.toArrayCell(v);

          this.styleStatusColumn(excelModel, worksheetRaw);
        });

        const fileName = 'Comparison_' + sDate + '.xlsx';
        workbook.xlsx.writeBuffer().then(rs => {
          const blob = new Blob([rs], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
          fs.saveAs(blob, fileName);
          this.authService.onLoading$.next(false);
        });
      }
    });
  }

  private parseDate(date): any {
    return this.datePipe.transform(date, 'yyyy.MM.dd / HH:mm');
  }

  start(): void {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Infomation',
      message: 'Are you sure to Start ?',
      confirm: () => {
        this.authService.onLoading$.next(true);
        this.comparisonService.startSchedule().subscribe(
          (_) => {
            this.statusComparison = true;
            this.authService.onLoading$.next(false);
            SweetAlert.notifyMessage('Start Success!');
          },
          (err) => {
            this.authService.onLoading$.next(false);
            SweetAlert.notifyMessage('Start Error!', 'error');
          }
        );
      },
      type: 'info',
      cancel: true,
    });
  }

  stop(): void {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Infomation',
      message: 'Are you sure to Stop ?',
      confirm: () => {
        this.authService.onLoading$.next(true);
        this.comparisonService.stopSchedule().subscribe(
          (_) => {
            this.authService.onLoading$.next(false);
            this.statusComparison = false;
            SweetAlert.notifyMessage('Stop Success!');
          },
          (err) => {
            this.authService.onLoading$.next(false);
            SweetAlert.notifyMessage('Stop Error!', 'error');
          }
        );
      },
      type: 'info',
      cancel: true,
    });
  }

  // source db
  dbclickToSelectSourceDB(doc: ViewComparisonResultDto): void {
    this.selectedSourceDB = doc.syncRdId ? doc.rdSourceDatabase : doc.sourceDatabase;
    this.onSearch();
  }

  onClearSourceDB(): void {
    this.selectedSourceDB = '';
    this.onSearch();
  }

  // source schema
  dbclickToSelectSourceSchema(doc: ViewComparisonResultDto): void {
    this.selectedSourceSchema = doc.syncRdId ? doc.rdSourceSchema : doc.sourceSchema;
    this.onSearch();
  }

  onClearSourceSchema(): void {
    this.selectedSourceSchema = '';
    this.onSearch();
  }

  // sourceTable
  dbclickToSelectSourceTable(doc: ViewComparisonResultDto): void {
    this.selectedTableName = doc.syncRdId ? doc.rdSourceTable : doc.sourceTable;
    this.onSearch();
  }

  onClearSourceTable(): void {
    this.selectedTableName = '';
    this.onSearch();
  }

  // target DB
  dbclickToSelectTargetDB(doc: ViewComparisonResultDto): void {
    this.selectedTargetDB = doc.syncRdId ? doc.rdTargetDatabase : doc.targetDatabase;
    this.onSearch();
  }
  onClearTargetDB(): void {
    this.selectedTargetDB = '';
    this.onSearch();
  }

  // target schema
  dbclickToSelectTargetSchema(doc: ViewComparisonResultDto): void {
    this.selectedTargetSchema = doc.syncRdId ? doc.rdTargetSchema : doc.targetSchema;
    this.onSearch();
  }

  onClearTargetSchema(): void {
    this.selectedTargetSchema = '';
    this.onSearch();
  }

  // sourceTable
  dbclickToSelectTargetTable(doc: ViewComparisonResultDto): void {
    this.selectedTableName = doc.syncRdId ? doc.rdTargetTable : doc.targetTable;
    this.onSearch();
  }
  onClearSourceTargetTable(): void {
    this.selectedTableName = '';
    this.onSearch();
  }

  // division
  dbclickToSelectDivision(doc: ViewComparisonResultDto): void {
    this.selectedDivision = doc.division;
    this.onSearch();
  }
  onClearDivision(): void {
    this.selectedDivision = '';
    this.onSearch();
  }

  public onSearch(): void {
    this.storage.save(CURRENT_PAGE, this.currentPage);
    const sDate = this.formater.format(this.dateFrom);
    // const sortType = SortType.DESC;
    this.authService.onLoading$.next(true);
    const lowerCaseTableName = this.selectedTableName
      ? this.selectedTableName.toLowerCase()
      : '';

    this.comparisonService
      .filter(
        sDate,
        this.timeSchedule,
        this.selectedSourceDB.trim(),
        this.selectedSourceSchema.trim(),
        this.selectedTargetDB.trim(),
        this.selectedTargetSchema.trim(),
        lowerCaseTableName,
        lowerCaseTableName,
        this.comparisonState,
        this.currentPage,
        this.pageSize,
        this.sortField,
        this.sortType,
        this.selectedDivision
      )
      .subscribe((data) => {
        this.viewCompareResults = data.entities;
        this.viewCompareResults.forEach((x, index) => (x.index = index));
        this.totalPage = data.totalPage;
        this.authService.onLoading$.next(false);
      },
      err => this.authService.onLoading$.next(false)
      );
  }

  onSearchALL(): void {
    this.currentPage = 1;
    this.onSearch();
  }

  selectedStatus(status: State): void {
    this.currentPage = 1;
    this.storage.save(CURRENT_PAGE, this.currentPage);
    this.states.forEach((st) => (st.active = false));
    this.comparisonState = status.name;
    status.active = true;
    this.onSearch();
  }

  removeSelectedState(): void {
    this.states.forEach((st) => (st.active = false));
    this.comparisonState = '';
    this.sortField = 'lastModified';
    this.onSearch();
  }

  addNewSchedule(): void {
    // const timeNow = this.formater.format(this.calendar.getToday());
    const hour = new Date(this.timeOnServer).getHours();
    const minute = new Date(this.timeOnServer).getMinutes();
    const timeDisplay = {
      hour,
      minute,
      second: 0,
    } as NgbTimeStruct;
    const newItem = {
      state: '0',
      time: hour + ':' + minute,
      timeDisplay,
    } as DbComparisonScheduleDto;
    this.schedules.push(newItem);
  }

  removeSchedule(index): void {
    this.schedules.splice(index, 1);
  }

  getSTT(index): any {
    return RegistrationHelper.nth(index);
  }

  handleChangePageSize(e: number): void {
    this.pageSize = e;
    this.onSearch();
  }
  handleChangePage(page: number): void {
    this.currentPage = page;
    this.storage.save(CURRENT_PAGE_COMPARISON, this.currentPage);
    this.onSearch();
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
        sourceDBSet.forEach((sdb) => this.sourceDBs.push(sdb));
        sourceSchemasSet.forEach((sss) => this.sourceSchemas.push(sss));
        targetSchemasSet.forEach((tss) => this.targetSchemas.push(tss));
        targetDBstSet.forEach((tdbs) => this.targetDBs.push(tdbs));
        tables.forEach((tb) => this.tableNames.push(tb));
      }
    });
  }

  getViewResultByDateAndTime(compareTime: GroupDateTime): void {
    this.timeSchedule = compareTime.compareTime;
    this.groupDateTime.forEach((item) => (item.active = false));
    compareTime.active = true;
    this.currentPage = 1; // reset page
    this.onSearch();
    const sDate = this.formater.format(this.dateFrom);
    this.getSummaryByDateAndTime(sDate);
  }

  deleteViewResultByDateAndTime(event, compareTime: GroupDateTime): void {
    event.stopPropagation();
    const sDate = this.formater.format(this.dateFrom);
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'WARNING',
      message: 'Are you sure to DELETE this Summary?',
      confirm: () => {
        this.comparisonService
          .deleteSummaryByDateAndTime(sDate, this.timeSchedule)
          .subscribe((res) => {
            if (res) {
              SweetAlert.notifyMessage(
                'Deleted Comparison Summary Successfully!'
              );
              this.timeSchedule = compareTime.compareTime;
              this.groupDateTime.forEach((item) => (item.active = false));
              compareTime.active = true;
              this.currentPage = 1; // reset page
              this.loadResultByDate(this.dateFrom);
            } else {
              SweetAlert.notifyMessage('Cannot Comparison Summary', 'error');
              this.authService.onLoading$.next(false);
            }
          });
      },
      type: 'warning',
      cancel: true,
    });
  }

  openSchedule(contentSchedule): void {
    this.loadSchedules().subscribe((data) => {
      if (data) {
        this.openSchedule$ = this.modalService.open(contentSchedule);
      }
      this.onGetTimeOnServer();
    });
  }

  onGetTimeOnServer(): void {
    this.comparisonService.getTimeOnServer().subscribe((data) => {
      this.timeOnServer = data.body.dateTime;
    });
  }

  openViewInfo(content, doc: ViewComparisonResultDto): void {
    this.quickViewInfo = doc;
    this.resultQuickRunSource = '';
    this.resultQuickRunTarget = '';
    this.modalService.open(content, { size: 'xl' }).result.then(
      (result) => {},
      (reason) => {
        this.onCloseSetting();
      }
    );
  }

  quickRunSourceQuery(): void {
    if (this.quickViewInfo) {
      const sourceQuery = this.quickViewInfo.sourceQuery;
      const sourceDB = (this.quickViewInfo.sourceCompareDatabase == '' || this.quickViewInfo.sourceCompareDatabase == null)
        ? this.quickViewInfo.syncRdId ? this.quickViewInfo.rdSourceDatabase : this.quickViewInfo.sourceDatabase
        : this.quickViewInfo.sourceCompareDatabase;
      // call api to test
      const body = {
        query: sourceQuery,
        database: sourceDB
      } as QuickRunComparisonQuery;
      this.comparisonService.quickRunQuery(body).subscribe(data => {
        if (data.status === 200) {
          // success
          this.resultQuickRunSource = data.body;
        } else {
          this.resultQuickRunSource = data.message;
        }
      });
    }
  }
  quickRunTargetQuery(): void {
    if (this.quickViewInfo) {
      const targetQuery = this.quickViewInfo.targetQuery;
      const targetDB = (this.quickViewInfo.targetCompareDatabase == '' || this.quickViewInfo.targetCompareDatabase == null)
        ? this.quickViewInfo.syncRdId ? this.quickViewInfo.rdTargetDatabase : this.quickViewInfo.targetDatabase
        : this.quickViewInfo.targetCompareDatabase;
      // call api to test
      const body = {
        query: targetQuery,
        database: targetDB
      } as QuickRunComparisonQuery;
      this.comparisonService.quickRunQuery(body).subscribe(data => {
        if (data.status === 200) {
          // success
          this.resultQuickRunTarget = data.body;
        } else {
          this.resultQuickRunTarget = data.message;
        }
      });
    }
  }

  onSaveSetting(): void {
    this.schedules.forEach((item) => {
      const hour = this.padLeadingZeros(item.timeDisplay.hour, 2);
      const minute = this.padLeadingZeros(item.timeDisplay.minute, 2);
      const second = this.padLeadingZeros(item.timeDisplay.second, 2);
      item.time = hour + ':' + minute + ':' + second;
    });
    this.comparisonService.saveSchedules(this.schedules).subscribe(
      (_) => {
        this.modalService.dismissAll();
        SweetAlert.notifyMessage('Update Schedule success!');
      },
      (err) => {
        console.log(err);
      }
    );
  }

  private loadSchedules(): Observable<DbComparisonScheduleDto[]> {
    const subject = new Subject<DbComparisonScheduleDto[]>();
    this.comparisonService.getSchedules().subscribe((data) => {
      if (data) {
        if (data.length === 0) {
          return subject.next(null);
        }
        data.forEach((dt, index) => {
          const times = dt.time.split(':');
          dt.timeDisplay = {
            hour: +times[0],
            minute: +times[1],
            second: +times[2],
          } as NgbTimeStruct;
        });
        this.schedules = data;
        subject.next(data);
      }
    });
    return subject.asObservable();
  }

  compareAll(): void {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Infomation',
      message: 'Are you sure to Compare All ?',
      confirm: () => {
        this.authService.onLoading$.next(true);
        this.comparisonService.compareAll().subscribe(
          (data) => {
            this.authService.onLoading$.next(false);
            if (data) {
              SweetAlert.notifyMessage(data.message);
            } else {
              SweetAlert.notifyMessage('Error');
            }
            this.selected = [];
            this.onSearch();
          },
          (error) => {
            console.log(error);
            SweetAlert.notifyMessage(error || 'Error', 'error');
            this.authService.onLoading$.next(false);
          }
        );
      },
      type: 'info',
      cancel: true,
    });
  }

  compareSelectedItems(): void {
    if (this.selected.length === 0) {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Information',
        message: 'You should select at least one record to Compare',
        type: 'info',
        confirm: () => {},
      });
      return;
    }
    const sDate = this.formater.format(this.dateFrom);
    this.authService.onLoading$.next(true);
    this.comparisonService
      .compareSelectedItems(this.selected)
      .subscribe((data) => {
        this.authService.onLoading$.next(false);
        if (data) {
          SweetAlert.notifyMessage(data.message);
        } else {
          SweetAlert.notifyMessage('Error');
        }
        this.selected = [];
        this.onSearch();
        this.getSummaryByDateAndTime(sDate);
      });
  }

  selectItem(event, compareResult: ViewComparisonResultDto): void {
    compareResult.checked = event.target.checked;
    if (event.target.checked) {
      this.lastSelected = compareResult;
      this.selected.push(Number(compareResult.compareResultId));
    } else {
      this.lastSelected = null;
      this.selected = this.selected.filter(
        (item) => item !== Number(compareResult.compareResultId)
      );
    }
  }

  onSelectionEnd(event, doc: ViewComparisonResultDto): void {
    if (event.shiftKey && event.which === 1) {
      if (this.lastSelected && doc.index > this.lastSelected.index) {
        this.viewCompareResults.forEach((item) => {
          if (item.index > this.lastSelected.index && item.index < doc.index) {
            item.checked = true;
            this.selected.push(item.id);
          }
        });
      }
    }
  }

  selectItemAllItems(event): void {
    if (event.target.checked) {
      this.viewCompareResults.forEach((item) => {
        this.selected.push(Number(item.compareResultId));
        item.checked = true;
      });
    } else {
      this.viewCompareResults.forEach((item) => {
        item.checked = false;
      });
      this.selected = [];
    }
  }

  getCheckAllItem(): boolean {
    return this.selected.length > 0;
  }

  padLeadingZeros(num, size): string {
    let s = num + '';
    while (s.length < size) {
      s = '0' + s;
    }
    return s;
  }

  clearSelectedTopicName(): void {
    this.selectedTableName = '';
    this.storage.save(CURRENT_SEARCHING_TABLE_NAME, this.selectedTableName);
    this.onSearch();
  }

  copy(text): void {
    copy(text);
  }

  viewAnalytics(): void {
    const selectedTopicNames = [];
    this.viewCompareResults.forEach((item) => {
      if (this.selected.includes(item.compareResultId)) {
        selectedTopicNames.push(item.synchronizerName);
      }
    });
    this.router.navigate(['/comparison/viewchart'], {
      queryParams: { compareTopicNames: selectedTopicNames },
    });
  }

  onCloseSetting(): void {
    this.modalService.dismissAll();
  }
}
