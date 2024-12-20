import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { MultiselectDropdownComponent } from './multiselect-dropdown.component';
import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown';

@NgModule({
  declarations: [MultiselectDropdownComponent],
  imports: [CommonModule, FormsModule, NgbModule, NgMultiSelectDropDownModule.forRoot()],
  exports: [MultiselectDropdownComponent]
})
export class MultiselectDropdownModule { }
