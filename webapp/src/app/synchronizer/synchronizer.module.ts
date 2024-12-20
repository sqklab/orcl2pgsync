import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbDateParserFormatter, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { AutoCompleteModule } from '../components/auto-complete/auto-complete.module';
import { PavigationModule } from '../components/pavigation/pavigation.module';
import { NgbDateCustomParserFormatter } from '../libs/utils/helper';
import { FullPageLogComponent } from '../view-log/full-page-log/full-page-log.component';
import { ViewLogComponent } from '../view-log/view-log.component';
import { ChipsModule } from './../chips/chips.module';
import { AddNewSynChronizerComponent } from './add-new/add-new-syn-chronizer.component';
import { ImportSynchronizerComponent } from './import-synchronizer/import-synchronizer.component';
import { SynchronizerComponent } from './list/synchronizer.component';
import { SynchronizerRoutingModule } from './synchronizer-routing.module';
import { ViewDetailErrorLogComponent } from './view-error-synchronizer/view-detail-error-log/view-detail-error-log.component';
import { ViewErrorSynchronizerComponent } from './view-error-synchronizer/view-error-synchronizer.component';
import { ViewSynchronizerComponent } from './view-synchronizer/view-synchronizer.component';
import {MatInputModule} from "@angular/material/input";

@NgModule({
  declarations: [
    SynchronizerComponent,
    ImportSynchronizerComponent,
    ViewLogComponent,
    ViewErrorSynchronizerComponent,
    ViewDetailErrorLogComponent,
    AddNewSynChronizerComponent,
    ViewSynchronizerComponent,
    FullPageLogComponent
  ],
  imports: [
    CommonModule,
    NgbModule,
    AutoCompleteModule,
    PavigationModule,
    FormsModule,
    SynchronizerRoutingModule,
    TranslateModule,
    ChipsModule,
    MatInputModule
  ],
  providers: [
    { provide: NgbDateParserFormatter, useClass: NgbDateCustomParserFormatter },
  ],
})
export class SynchronizerModule {}
