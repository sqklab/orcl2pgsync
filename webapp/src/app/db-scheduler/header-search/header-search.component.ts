import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { NgbCalendar, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { State } from 'src/app/comparison/list/comparison.component';
import { DBScheduleDto } from 'src/app/libs/services/interface';
import { NgbDateCustomParserFormatter } from 'src/app/libs/utils/helper';

@Component({
  selector: 'app-header-search',
  templateUrl: './header-search.component.html',
  styleUrls: ['./header-search.component.scss']
})
export class HeaderSearchComponent implements OnInit, OnChanges {

  plSQL = [];

  @Input () targetDB = [];
  @Input () targetSchema = [];
  @Input () tables = [];
  @Input () modeActiveTab;

  states: State[] = [];

  selectedPLSQL = '';
  selectedTargetDB = '';
  selectedTargetSchema = '';
  selectedTable = '';
  selectedState = '';

  totalPage = 0;
  currentPage = 1;
  pageSize = 20;

  readonly RESULT_STATUS_SUCCESS = 'SUCCESS';
  readonly RESULT_STATUS_FAILED = 'FAILED';

  readonly REGISTRATION_STATUS_SCHEDLUE_LABEL = 'SCHEDULED';
  readonly REGISTRATION_STATUS_STOP_LABEL = 'STOPPED';

  filterStatus = [true, false];

  public dateFrom: NgbDateStruct;

  @Output() filter = new EventEmitter<DBScheduleDto>();

  constructor(private calendar: NgbCalendar, private formater: NgbDateCustomParserFormatter) { }

  ngOnChanges(changes: SimpleChanges): void {
    this.initState();
  }

  ngOnInit(): void {
    // this.loadAllServerNames();
  }

  private initState(): void {
    this.selectedState = '';
    if (this.modeActiveTab === '1') { // active Schedule Result
      // fill date
      this.dateFrom = this.calendar.getToday();
      this.states = [{
          name: this.RESULT_STATUS_SUCCESS,
          clazz: 'filter-status bg-success text-white',
          active: false
        } as State,
        {
          name: this.RESULT_STATUS_FAILED,
          clazz: 'filter-status bg-danger text-white',
          active: false
        } as State
      ];
    } else { // active Schedule Registration
      // clear date
      this.dateFrom = null;
      this.states = [{
        name: this.REGISTRATION_STATUS_SCHEDLUE_LABEL,
        clazz: 'filter-status bg-success text-white',
        active: false
      } as State,
      {
        name: this.REGISTRATION_STATUS_STOP_LABEL,
        clazz: 'filter-status bg-secondary text-white',
        active: false
      } as State
    ];
    }
  }

  loadResultByDate(date: NgbDateStruct): void {
    const sDate = date ? this.formater.format(date) : '';
    const filterObject = {
      plSQL: this.selectedPLSQL,
      db: this.selectedTargetDB,
      schema: this.selectedTargetSchema,
      table: this.selectedTable,
      createdAt: sDate,
      filterStatus: this.filterStatus
    } as DBScheduleDto;
    this.filter.emit(filterObject);
  }

  onSearchALL(): void {
    const sDate = this.dateFrom ? this.formater.format(this.dateFrom) : '';
    const filterObject = {
      plSQL: this.selectedPLSQL,
      db: this.selectedTargetDB,
      schema: this.selectedTargetSchema,
      table: this.selectedTable,
      createdAt: sDate,
      filterStatus: this.filterStatus
    } as DBScheduleDto;
    this.filter.emit(filterObject);
  }

  removeSelectedState(): void {
    this.states.forEach(st => st.active = false);
    this.selectedState = '';
    this.filterStatus = [true, false];
    this.loadResultByDate(this.dateFrom);
  }

  removeDate(): void {
    this.dateFrom = null;
    this.loadResultByDate(this.dateFrom);
  }

  selectedStatus(status: State): void {
    this.currentPage = 1;
    // this.storage.save(CURRENT_PAGE, this.currentPage);
    this.states.forEach(st => st.active = false);
    this.selectedState = status.name;
    status.active = true;
    // this.onSearch();
    if (this.REGISTRATION_STATUS_SCHEDLUE_LABEL === status.name || this.RESULT_STATUS_SUCCESS === status.name) {
      this.filterStatus = [true];
    } else {
      this.filterStatus = [false];
    }
    this.loadResultByDate(this.dateFrom);
  }


}
