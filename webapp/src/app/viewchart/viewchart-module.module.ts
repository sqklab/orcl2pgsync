import { CommonModule, DatePipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbDateParserFormatter, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { NgApexchartsModule } from 'ng-apexcharts';
import { AutoCompleteModule } from '../components/auto-complete/auto-complete.module';
import { LinechartComponent } from '../components/linechart/linechart.component';
import { MultiselectDropdownModule } from '../components/multiselect-dropdown/multiselect-dropdown.module';
import { PavigationModule } from '../components/pavigation/pavigation.module';
import { NgbDateCustomParserFormatter } from '../libs/utils/helper';
import { ViewChartRoutingModule } from './viewchart-routing.module';
import { ViewchartminuteComponent } from './view-chart-minute/viewchartminute.component';
import {MatSelectModule} from '@angular/material/select';
import {NgSelectModule} from "@ng-select/ng-select";

@NgModule({
  declarations: [
    ViewchartminuteComponent,
    LinechartComponent
  ],
  imports: [
    CommonModule,
    CommonModule,
    AutoCompleteModule,
    PavigationModule,
    FormsModule,
    ViewChartRoutingModule,
    TranslateModule,
    NgbModule,
    NgApexchartsModule,
    MultiselectDropdownModule,
    MatSelectModule,
    NgSelectModule,
  ],
  providers: [
    DatePipe,
    {provide: NgbDateParserFormatter, useClass: NgbDateCustomParserFormatter}
  ]
})
export class ViewChartModule { }
