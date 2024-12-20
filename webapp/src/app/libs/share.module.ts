import {NgModule} from '@angular/core';
import { JsonViewerV2Pipe } from './pipe/json-viewer-v2.pipe';
import {JsonViewerPipe} from "./pipe/json-viewer.pipe";
import {ConnectorTaskStatusPipe} from "./pipe/connector-task-status.pipe";
import {ConnectorStatusPipe} from "./pipe/connector-status.pipe";

@NgModule({
  declarations: [
    JsonViewerPipe,
    JsonViewerV2Pipe,
    ConnectorTaskStatusPipe,
    ConnectorStatusPipe
  ],
  imports: [],
  exports: [
    JsonViewerPipe,
    JsonViewerV2Pipe,
    ConnectorTaskStatusPipe,
    ConnectorStatusPipe
  ]
})
export class ShareModule {
}
