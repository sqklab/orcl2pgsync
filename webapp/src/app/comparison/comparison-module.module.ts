import { CommonModule, DatePipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbDateParserFormatter, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { NgApexchartsModule } from 'ng-apexcharts';
import { AutoCompleteModule } from '../components/auto-complete/auto-complete.module';
import { PavigationModule } from '../components/pavigation/pavigation.module';
import { NgbDateCustomParserFormatter } from '../libs/utils/helper';
import { SynchronizerRoutingModule } from '../synchronizer/synchronizer-routing.module';
import { ViewChartModule } from '../viewchart/viewchart-module.module';
import { ComparisonRoutingModule } from './comparison-routing.module';
import { ComparisonComponent } from './list/comparison.component';
@NgModule({
  declarations: [
    ComparisonComponent
  ],
  imports: [
    CommonModule,
    ComparisonRoutingModule,
    CommonModule,
    AutoCompleteModule,
    PavigationModule,
    FormsModule,
    SynchronizerRoutingModule,
    TranslateModule,
    NgbModule,
    NgApexchartsModule,
    ViewChartModule
  ],
  providers: [
    DatePipe,
    {provide: NgbDateParserFormatter, useClass: NgbDateCustomParserFormatter}
  ]
})
export class ComparisoModule { }
