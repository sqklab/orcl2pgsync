import {CommonModule, DatePipe} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {NgbDateParserFormatter, NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {AutoCompleteModule} from '../components/auto-complete/auto-complete.module';
import {PavigationModule} from '../components/pavigation/pavigation.module';
import {NgbDateCustomParserFormatter} from '../libs/utils/helper';
import {ConnectorRoutingModule} from './connector-routing.module';
import {ListConnectorComponent} from './list-connector/list-connector.component';
import {InsertOrUpdateConnectorComponent} from './insert-or-update-connector/insert-or-update-connector.component';
import {ShareModule} from "../libs/share.module";
import {NgJsonEditorModule} from "@maaxgr/ang-jsoneditor";
import { ConnectorHistoryComponent } from './connector-history/connector-history.component';
import { ConnectorJsonComponent } from './connector-json/connector-json.component';


@NgModule({
  declarations: [
    ListConnectorComponent,
    InsertOrUpdateConnectorComponent,
    ConnectorHistoryComponent,
    ConnectorJsonComponent
  ],
  imports: [
    CommonModule,
    PavigationModule,
    FormsModule,
    ConnectorRoutingModule,
    AutoCompleteModule,
    NgbModule,
    ShareModule,
    NgJsonEditorModule
  ],
  providers: [
    DatePipe,
    {provide: NgbDateParserFormatter, useClass: NgbDateCustomParserFormatter}
  ]
})
export class ConnectorsModule {
}
