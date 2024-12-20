import { CommonModule, DatePipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbDateParserFormatter, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { AutoCompleteModule } from '../components/auto-complete/auto-complete.module';
import { PavigationModule } from '../components/pavigation/pavigation.module';
import { NgbDateCustomParserFormatter } from '../libs/utils/helper';
import { RedoLogComponent } from '../redo-log/redo-log.component';
import { ColumnIdAutoCompleteComponent } from './column-id-auto-complete/column-id-auto-complete.component';
import { OperationComponent } from './list/operation.component';
import { OperationRoutingModule } from './operation-routing.module';
import { PublicationManagerComponent } from './publication-manager/publication-manager.component';
import { ShowDetailRecordComponent } from './show-detail-record/show-detail-record.component';
import { MatSelectModule } from '@angular/material/select';
import { PublicationPopupAddingComponent } from './publication-manager/publication-popup-adding/publication-popup-adding.component';
import {MatDialogModule} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';


@NgModule({
  declarations: [
    OperationComponent,
    ShowDetailRecordComponent,
    ColumnIdAutoCompleteComponent,
    RedoLogComponent,
    PublicationManagerComponent,
    PublicationPopupAddingComponent
  ],
  imports: [
    CommonModule,
    OperationRoutingModule,
    CommonModule,
    AutoCompleteModule,
    PavigationModule,
    FormsModule,
    NgbModule,
    MatSelectModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule
  ],
  providers: [
    DatePipe,
    {provide: NgbDateParserFormatter, useClass: NgbDateCustomParserFormatter}
  ]
})
export class OperationModule { }
