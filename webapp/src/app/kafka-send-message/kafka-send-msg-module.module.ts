import { CommonModule, DatePipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbDateParserFormatter, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { AutoCompleteModule } from '../components/auto-complete/auto-complete.module';
import { PavigationModule } from '../components/pavigation/pavigation.module';
import { ShareModule } from '../libs/share.module';
import { NgbDateCustomParserFormatter } from '../libs/utils/helper';
import { KafkaSendMessageRoutingModule } from './kafka-send-msg-routing.module';
import { KafkaSendMessageComponent } from './list/kafka-send-msg.component';
@NgModule({
  declarations: [
    KafkaSendMessageComponent,
  ],
  imports: [
    CommonModule,
    KafkaSendMessageRoutingModule,
    AutoCompleteModule,
    PavigationModule,
    FormsModule,
    NgbModule,
    ShareModule
  ],
  providers: [
    DatePipe,
    {provide: NgbDateParserFormatter, useClass: NgbDateCustomParserFormatter}
  ]
})
export class KafkaSendMessageModule { }
