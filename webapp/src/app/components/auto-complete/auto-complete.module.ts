import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { AutoCompleteComponent } from './auto-complete.component';


@NgModule({
  declarations: [AutoCompleteComponent],
  imports: [CommonModule, FormsModule, NgbModule],
  exports: [AutoCompleteComponent]
})
export class AutoCompleteModule { }
