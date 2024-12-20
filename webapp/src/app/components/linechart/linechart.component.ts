import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild} from '@angular/core';
import {
  ApexChart,
  ApexAxisChartSeries,
  ApexTitleSubtitle,
  ApexDataLabels,
  ApexFill,
  ApexYAxis,
  ApexXAxis,
  ApexTooltip,
  ApexMarkers,
  ApexAnnotations,
  ApexStroke,
  ChartComponent
} from 'ng-apexcharts';
import StorageService from 'src/app/libs/services/storage.service';
import _ from 'lodash';

export type ChartOptions = {
  series: ApexAxisChartSeries;
  chart: ApexChart;
  dataLabels: ApexDataLabels;
  markers: ApexMarkers;
  title: ApexTitleSubtitle;
  fill: ApexFill;
  yaxis: ApexYAxis;
  xaxis: ApexXAxis;
  tooltip: ApexTooltip;
  stroke: ApexStroke;
  annotations: ApexAnnotations;
  colors: any;
  toolbar: any;
};

@Component({
  selector: 'app-linechart',
  templateUrl: './linechart.component.html',
  styleUrls: ['./linechart.component.scss']
})
export class LinechartComponent implements OnInit, OnChanges {

  @Output() toggleLineKafka = new EventEmitter();
  @Output() toggleLineProcessed = new EventEmitter();
  @Input() chartOptions: ChartOptions;

  @ViewChild('chartObj') chartComp: ChartComponent;

  constructor(private storage: StorageService) { }

  ngOnChanges(changes: SimpleChanges): void {
      this.chartOptions = changes.chartOptions.currentValue;
  }

  ngOnInit(): void {
    this.storage.toggleLeftBar.subscribe(state => {
      setTimeout(() => {
        const clone = _.cloneDeep(this.chartOptions);
        clone.chart.width = '100%';
        this.chartOptions = clone;
      }, 100);
    })
  }

  public toggleSeries(seriesName: any) {
    this.chartComp.toggleSeries(seriesName);
  }


  _toggleLineKafka() {
    this.toggleLineKafka.emit();
  }

  _toggleLineProcessed() {
    this.toggleLineProcessed.emit();
  }
}
