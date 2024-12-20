import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DBConfigRoutingModule } from './dbconfig-routing.module';
import { PavigationModule } from '../components/pavigation/pavigation.module';
import { NgbDateParserFormatter, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { NgbDateCustomParserFormatter } from '../libs/utils/helper';
import { DBConfigComponent } from './dbconfig.component';
import { AddNewDBConfigComponent } from './add-new/add-new-dbconfig.component';


@NgModule({
  declarations: [
    DBConfigComponent,
    AddNewDBConfigComponent
  ],
  imports: [
    CommonModule,
    NgbModule,
    PavigationModule,
    FormsModule,
    DBConfigRoutingModule,
  ],
  providers: [
    {provide: NgbDateParserFormatter, useClass: NgbDateCustomParserFormatter}
  ]
})
export class DBConfigModule { }
