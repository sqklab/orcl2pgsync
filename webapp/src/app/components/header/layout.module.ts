import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { HeaderComponent } from './header.component';
import { FooterComponent } from '../footer/footer.component';
import { ModalModule } from '../modal/modal.module';
import { TranslateModule } from '@ngx-translate/core';
import {KafkaStatusPipe} from "../../libs/pipe/kafka-status.pipe";

@NgModule({
    declarations: [
        HeaderComponent,
        FooterComponent,
        KafkaStatusPipe
    ],
    imports: [
        CommonModule,
        ModalModule,
        TranslateModule,
        RouterModule.forChild([]),
    ],
    exports: [
        HeaderComponent,
        FooterComponent,
        KafkaStatusPipe
    ]
})
export class LayoutModule { }
