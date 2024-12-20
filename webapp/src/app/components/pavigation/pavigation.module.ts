import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {PavigationComponent} from './pavigation.component';
import {FormsModule} from "@angular/forms";
import {NgbPaginationModule} from "@ng-bootstrap/ng-bootstrap";

@NgModule({
  declarations: [PavigationComponent],
  imports: [
    CommonModule,
    FormsModule,
    NgbPaginationModule
  ],
  exports: [PavigationComponent]
})
export class PavigationModule {
}
