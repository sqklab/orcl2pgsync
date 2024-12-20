import { CommonModule, DatePipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AutoCompleteModule } from '../components/auto-complete/auto-complete.module';
import { PavigationModule } from '../components/pavigation/pavigation.module';
import { ExecuteQueryComponent } from './registration/registration-query.component';
import { ExecuteQueryRoutingModule } from './query-routing.module';
import { ResultQueryComponent } from './result/result.component';
import { SchedulerContainerComponent } from './scheduler-container/scheduler-container.component';
import { HeaderSearchComponent } from './header-search/header-search.component';
import { NgbDateCustomParserFormatter } from '../libs/utils/helper';
import { NgbDateParserFormatter, NgbModule } from '@ng-bootstrap/ng-bootstrap';


@NgModule({
  declarations: [
    ExecuteQueryComponent,
    ResultQueryComponent,
    SchedulerContainerComponent,
    HeaderSearchComponent
  ],
  imports: [
    CommonModule,
    PavigationModule,
    FormsModule,
    ExecuteQueryRoutingModule,
    AutoCompleteModule,
    NgbModule
  ],
  providers: [
    DatePipe,
    {provide: NgbDateParserFormatter, useClass: NgbDateCustomParserFormatter}
  ]
})
export class ExecuteQueryModule { }
