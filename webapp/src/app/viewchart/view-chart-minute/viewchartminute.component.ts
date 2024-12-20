import {Component, OnInit, ViewChild} from '@angular/core';
import {ComparisonService} from 'src/app/libs/services/comparison.service';
import {DataAnalysisDtoResponse, GraphType,} from 'src/app/libs/services/interface';
import {NgbCalendar, NgbDateStruct} from '@ng-bootstrap/ng-bootstrap';
import {NgbDateCustomParserFormatter} from '../../libs/utils/helper';
import {SyncService} from 'src/app/libs/services/syncDocument.service';
import {MultipeSelectDropDown} from '../../components/multiselect-dropdown/interface';
import {ActivatedRoute} from '@angular/router';
import {LinechartComponent} from 'src/app/components/linechart/linechart.component';
import {AuthService} from "../../libs/services/auth.service";

export interface StateMinute {
  value: number;
  active: boolean;
}

@Component({
  selector: 'app-viewchartminute',
  templateUrl: './viewchartminute.component.html',
  styleUrls: ['./viewchartminute.component.scss'],
})
export class ViewchartminuteComponent implements OnInit {
  chartOptions;
  year;
  month;
  date;
  dateFrom: any;
  graphType: GraphType = GraphType.HOUR;
  graphTitle: string;
  fromHour = 0;
  toHour = 1;
  minuteType: number;
  topicNames = [];
  selectedTopicName = '';
  selectedDivision = '';
  dbName = [];
  schmName = [];
  selectedSourceDB: string[] = [];
  selectedSourceSchema: string[] = [];
  schmSelection = -1;
  dbSelection = -1;
  disabledDB = false;
  disabledDivision = false;
  disabledTopic = false;
  disabledSchema = false;
  isMinute = false;
  isDay = false;
  compareTopicNames;
  viewTypeList: string[] = ['Per Minute', 'Per Hour', 'Per Day'];
  selectedQuantity = 5;
  optionMinutes = [1, 5, 15, 30];
  selectedPerMs;
  divisions = [];


  chartTypes = [{
    key: 'MESSAGE_COUNT',
    label: 'Message count'
  }, {
    key: 'MESSAGE_LATENCY',
    label: 'Message latency'
  }];
  selectedChartType = this.chartTypes[0].key;
  monthFrom: any;

  kafkaName = [];
  processedName = [];

  @ViewChild('chartComp') chartComp: LinechartComponent;

  constructor(
    private comparisonService: ComparisonService,
    private formater: NgbDateCustomParserFormatter,
    private calendar: NgbCalendar,
    private syncService: SyncService,
    private activeRouter: ActivatedRoute,
    private authService: AuthService,
  ) {
    this.monthFrom =
      this.calendar.getToday().month < 10
        ? this.calendar.getToday().year +
          '-' +
          '0' +
          this.calendar.getToday().month
        : this.calendar.getToday().year + '-' + this.calendar.getToday().month;
    this.dateFrom = this.calendar.getToday();
    this.loadResultByDate(this.dateFrom);
  }

  ngOnInit(): void {
    if (this.compareTopicNames === undefined) {
      this.compareTopicNames =
        this.activeRouter.snapshot.queryParams['compareTopicNames'];
    }
    this.loadTopicNames();
    this.loadDbNameAndSchemas();
    this.viewAnalytics();
    this.getDivisions();
  }

  validateToHour() {
    if (this.toHour > 24) {
      this.toHour = 24;
    }
    if (this.toHour < 1) {
      this.toHour = 1;
    }
  }
  validateFromHour() {
    if (this.fromHour > 23) {
      this.fromHour = 23;
    }
    if (this.fromHour < 0) {
      this.fromHour = 0;
    }
  }

  selectedMinute(op) {
    console.log(op)
  }

  goBack() {
    window.history.back();
  }

  public get GraphType() {
    return GraphType;
  }

  loadTopicNames() {
    this.syncService.getTopicNames().subscribe((res) => {
      if (res) {
        this.topicNames = res.map((x) => x.topicName);
        this.syncService.synInfos = res;
      }
    });
  }

  loadDbNameAndSchemas() {
    this.comparisonService.getViewInfoForSearching().subscribe((res) => {
      if (res) {
        this.dbName = res.map((x) => x.sourceDatabase);
        this.dbName = [...new Set(this.dbName)];
        this.dbName = this.dbName.map(
          (x) =>
            ({
              item_id: x,
              item_text: x,
            } as MultipeSelectDropDown)
        );
        this.schmName = res.map((x) => x.sourceSchema);
        this.schmName = [...new Set(this.schmName)];

        this.schmName = this.schmName.map(
          (x) =>
            ({
              item_id: x.toUpperCase(),
              item_text: x.toUpperCase(),
            } as MultipeSelectDropDown)
        );
        const expected = new Set();
        this.schmName = this.schmName.filter(item => !expected.has(JSON.stringify(item)) ? expected.add(JSON.stringify(item)) : false);
      }
    });
  }

  private getDivisions(): void {
    this.comparisonService.getDivisions().subscribe(data => this.divisions = data);
  }

  handleSelectDB(event) {
    const arr = event as Array<MultipeSelectDropDown>;
    this.selectedSourceDB = arr.map((x) => x.item_id);
    this.disabledTopic = true;
    this.disabledDivision = true;
    this.selectedTopicName = '';
    this.selectedDivision = '';
    this.schmSelection = this.selectedSourceDB.length > 1 ? 1 : -1;
  }

  handleSelectSchema(event) {
    const arr = event as Array<MultipeSelectDropDown>;
    this.selectedSourceSchema = arr.map((x) => x.item_id);
    this.disabledTopic = true;
    this.disabledDivision = true;
    this.selectedTopicName = '';
    this.selectedDivision = '';
    this.dbSelection = this.selectedSourceSchema.length > 1 ? 1 : -1;
  }

  isLowerCase(str) {
    return str !== str.toUpperCase();
  }

  handleSelectTopic(event) {
    this.selectedTopicName = event;
    if(this.selectedTopicName !== '') {
      this.disabledDB = true;
      this.disabledSchema = true;
      this.disabledDivision = true;
      this.selectedSourceDB = [];
      this.selectedSourceSchema = [];
      this.selectedDivision = '';
    }
  }

  clearSelectedDatabase(event) {
    this.selectedSourceDB = [];
    if (event) {
      if (this.selectedSourceSchema.length == 0) {
        this.disabledTopic = false;
        this.disabledDivision = false;
      }
    }
  }

  clearSelectedSchema(event) {
    this.selectedSourceSchema = [];
    if (event) {
      if (this.selectedSourceDB.length == 0) {
        this.disabledTopic = false;
        this.disabledDivision = false;
      }
    }
  }

  clearSelectedTopicName(event) {
    this.selectedTopicName = '';
    if (event) {
      this.disabledDB = false;
      this.disabledSchema = false;
      this.disabledDivision = false;
    }
  }

  clearSelectedDivision(event) {
    this.selectedDivision = '';
    if (event) {
      this.disabledDB = false;
      this.disabledSchema = false;
      this.disabledTopic = false;
    }
  }

  handleDivision(event) {
    this.selectedDivision = event;
    if(this.selectedDivision !== '') {
      this.disabledDB = true;
      this.disabledSchema = true;
      this.disabledTopic = true;
      this.selectedSourceDB = [];
      this.selectedSourceSchema = [];
      this.selectedTopicName = '';
    }
  }

  selectionChangeChartType(event) {
    this.comparisonService
    .viewHourlyGraph(
      this.selectedSourceDB ? this.selectedSourceDB : [],
      this.selectedSourceSchema ? this.selectedSourceSchema : [],
      this.selectedDivision ? this.selectedDivision : '',
      this.selectedTopicName ? this.selectedTopicName.trim() : '',
      this.year,
      this.month,
      this.date,
      this.selectedChartType
    )
    .subscribe((data) => {
      this.buildGroupLineChart(
        data
      );
    });
  }

  loadResultByDate(dateFrom: NgbDateStruct) {
    const sDate = this.formater.format(dateFrom);
    const splitDate = sDate.split('-');
    this.year = Number(splitDate[0]);
    this.month = Number(splitDate[1]);
    this.date = Number(splitDate[2]);
  }

  viewByMinute() {
    this.isMinute = true;
    this.isDay = false;
  }

  viewByHour() {
    this.isMinute = false;
    this.isDay = false;
  }

  viewByDay() {
    this.isMinute = false;
    this.isDay = true;
  }
  viewAnalytics() {
    this.authService.onLoading$.next(true);
    this.loadResultByDate(this.dateFrom);
    if (this.isMinute) {
      this.comparisonService
        .viewMinutelyGraph(
          this.selectedSourceDB ? this.selectedSourceDB : [],
          this.selectedSourceSchema ? this.selectedSourceSchema : [],
          this.selectedDivision ? this.selectedDivision : '',
          this.selectedTopicName ? this.selectedTopicName.trim() : '',
          this.year,
          this.month,
          this.date,
          this.fromHour ? this.fromHour : 0,
          this.toHour ? this.toHour : 1,
          this.selectedQuantity,
          this.selectedChartType ? this.selectedChartType : 'MESSAGE_COUNT'
        )
        .subscribe((data) => {
          this.buildGroupLineChart(
            data,
          );
          this.authService.onLoading$.next(false);
        }, error => {
          this.authService.onLoading$.next(false);
        });
    } else {
      if (this.isDay) {
        const splitDate = this.monthFrom.split('-');
        this.year = Number(splitDate[0]);
        this.month = Number(splitDate[1]);
        this.comparisonService
          .viewDailyGraph(
            this.selectedSourceDB ? this.selectedSourceDB : [],
            this.selectedSourceSchema ? this.selectedSourceSchema : [],
            this.selectedDivision ? this.selectedDivision : '',
            this.selectedTopicName ? this.selectedTopicName.trim() : '',
            this.year,
            this.month,
            this.date,
            this.selectedChartType
          )
          .subscribe((data) => {
            this.buildGroupLineChart(
              data,
            );
            this.authService.onLoading$.next(false);
          }, error => {
            this.authService.onLoading$.next(false);
          });
      } else {
        if (
          this.compareTopicNames !== undefined &&
          this.compareTopicNames.length > 0
        ) {
          this.comparisonService
            .viewTopicsHourly(
              this.compareTopicNames,
              this.year,
              this.month,
              this.date
            )
            .subscribe((data) => {
              this.buildGroupLineChart(
                data,
              );
            });
        } else {
          this.comparisonService
            .viewHourlyGraph(
              this.selectedSourceDB ? this.selectedSourceDB : [],
              this.selectedSourceSchema ? this.selectedSourceSchema : [],
              this.selectedDivision ? this.selectedDivision : '',
              this.selectedTopicName ? this.selectedTopicName.trim() : '',
              this.year,
              this.month,
              this.date,
              this.selectedChartType ? this.selectedChartType : 'MESSAGE_COUNT'
            )
            .subscribe((data) => {
              this.buildGroupLineChart(
                data,
              );
              this.authService.onLoading$.next(false);
            }, error => {
              this.authService.onLoading$.next(false);
            });
        }
      }
    }
  }

  toggleLineKafka() {
    this.kafkaName.forEach(x => this.chartComp.toggleSeries(x));
  }

  toggleLineProcessed() {
    this.processedName.forEach(x => this.chartComp.toggleSeries(x));
  }

  private getLineName(name) {
    if(name.includes("_KAFKA"))
    {
      this.kafkaName.push(name);
    } else if (name.includes("_PROCESSED")) {
      this.processedName.push(name);
    }
  }
  private getTitle() {
      this.graphTitle = 'Date: ' + this.year + '/' + this.month + '/' + this.date;

    if (this.isMinute)
      this.graphTitle = 'Date: ' + this.year + '/' + this.month + '/' + this.date + ' - '
        + 'Hour Range: (' + this.fromHour + ':00 -  ' + this.toHour + ':00) Per ' + this.selectedQuantity + 'th minute';
    if (this.isDay)
      this.graphTitle = 'Month: ' + this.month + ' Of ' + this.year;

    return this.graphTitle;
  }
  private buildGroupLineChart(
    data: DataAnalysisDtoResponse
  ): any {
    const titleOfChart = this.selectedChartType==='MESSAGE_LATENCY'? 'Message latency(ms)' : 'Message count'
    const series = [];
    this.kafkaName = [];
    this.processedName = [];

    if (this.isDay) {
      data.messageDataForYAxisGraphDtoList.forEach((item) => {
        item.data.shift();
        series.push({ name: item.name, data: item.data });
        this.getLineName(item.name);
      });
    } else {
      data.messageDataForYAxisGraphDtoList.forEach((item) => {
        series.push({ name: item.name, data: item.data });
        this.getLineName(item.name);
      });
    }
    const indexes = [];
    if (this.isDay) {
      for (const val of data.messageDataForXAxisGraphDtoList.data) {
        if (data.messageDataForXAxisGraphDtoList.data.indexOf(val) !== 0)
          indexes.push(val.toString());
      }
    } else {
      for (const val of data.messageDataForXAxisGraphDtoList.data) {
        indexes.push(val.toString());
      }
    };
    this.chartOptions = {
      series,
      chart: {
        height: 600,
        width: '100%',
        type: 'line',
        toolbar: {
          show: true,
          offsetX: -10,
          offsetY: -50,
        },
      },
      colors: [
        '#32d20c', '#e555b8', '#0196e3', '#e5d06f', '#664c82', '#29b701', '#f7e4ea', '#b885f6', '#21e5f0', '#579e27', '#c2d914', '#1c3316', '#239f9b', '#0deef4', '#5f5e5e', '#fb42ba', '#993f1d', '#097a6a', '#644005',
        '#6859af', '#931616', '#22ba44', '#ec48e8', '#5344b8', '#a725f1', '#35d8c5', '#61d21f', '#4998b7', '#a4b276', '#df16db', '#75a309', '#d02554', '#8686a9', '#b76cc1', '#00b88e', '#881dfd', '#694f47', '#39a2fb',
        '#340848', '#618aba', '#515ead', '#ae3d8a', '#7b03b0', '#1afba7', '#d4f354', '#f1fbc9', '#b36332', '#843c93', '#ee6fd6', '#b76567', '#64bb62', '#2b4e25', '#e70b8c', '#4d4df4', '#77a5dd', '#bd04f0', '#431e10',
        '#49e101', '#e0fbf6', '#b1b61a', '#574b29', '#a583ab', '#40d741', '#6aa730', '#0fe606', '#a6afb4', '#d5dc92', '#806bf5', '#3b3f14', '#5984cb', '#a269cc', '#eecd6f', '#dd20ec', '#355055', '#7f48a6', '#4e8a75',
        '#0fb638', '#78443f', '#38acae', '#d4867c', '#537791', '#abb0f3', '#64cab2', '#d315f5', '#9ddb69', '#db807e', '#7f3f8f', '#2fa72a', '#17f90e', '#9a5c3e', '#9623e3', '#29ad6e', '#a34e73', '#62a389', '#8a7f9a',
        '#10df47', '#221853', '#629e9e', '#29884c', '#007870', '#f90b2e', '#f83969', '#fd5ce6', '#9d51a4', '#313774', '#94cc2d', '#fe5adf', '#57690a', '#831115', '#2edc01', '#7eaf4d', '#941fea', '#1e2779', '#48ad03',
        '#b10e81', '#48dac7', '#8fab1e', '#327c01', '#cbc526', '#164559', '#eec361', '#d00fe9', '#92936c', '#b241a8', '#bc9d3a', '#32c404', '#646b51', '#1c85d4', '#4fc68c', '#ac5bed', '#4d4359', '#326ff5', '#fc9c8c',
        '#f79f57', '#279a90', '#5cd27c', '#7ff23d', '#8c2780', '#e6bdeb', '#19285c', '#bbea29', '#2340ca', '#d97547', '#1c5af5', '#9fbd18', '#e33827', '#aee60a', '#fc0d6c', '#4e836b', '#320f7c', '#20045a', '#319298',
        '#3143d4', '#7d1146', '#93c4ab', '#d71417', '#8a22eb', '#85feed', '#3150ee', '#daaf7e', '#d0b157', '#5b65b6', '#7f98bf', '#d6f447', '#7b11cf', '#6be633', '#36b1ae', '#47eda7', '#f9e83a', '#512b93', '#90b3b9',
        '#951db6', '#9d65ca', '#7f649a', '#0b8e25', '#e358b4', '#ad006b', '#8916a3', '#8d2c16', '#f6c8da', '#ce5692', '#badef4', '#7b397f', '#19dea5', '#27505a', '#107588', '#221dce', '#1edfd4', '#053ce5', '#50bb52',
        '#22c5fe', '#3c9af4', '#ebbf4d', '#1254a5', '#10ba9b', '#4f5d1e', '#a31f4d', '#9e3110', '#3d3257', '#c4d326',
      ],
      dataLabels: {
        enabled: true,
        formatter: function (val) {
          return val;
        },
      },
      stroke: {
        curve: 'smooth',
        width: 1,
      },
      title: {
        text: titleOfChart,
        align: 'left',
      },
      grid: {
        borderColor: '#e7e7e7',
        row: {
          colors: ['#333', 'transparent'], // takes an array which will be repeated on columns
          opacity: 0.5,
        },
      },
      markers: {
        size: 1,
      },
      xaxis: {
        categories: indexes,
        labels: {
          show: true,
          minHeight: undefined,
          maxHeight: 120,
        },
        title: {
          text: this.getTitle(),
        },
      },
      yaxis: {
        title: {
          text: titleOfChart,
        },
        min: 0,
        max: data.max,
        labels: {
          offsetX: -20,
          offsetY: 0,
        },
      },
      legend: {
        position: 'bottom',
        horizontalAlign: 'left',
        offsetX: 0,
        // offsetY: -100
        itemMargin: {
          // horizontal: 100,
          vertical: 0,
        },
      },
    };
  }
}
