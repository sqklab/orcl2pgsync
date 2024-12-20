import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { ChipsComponent } from './chips.component';
import {MatIconModule} from '@angular/material/icon';


@NgModule({
  declarations: [
    ChipsComponent
  ],
  imports: [
    CommonModule,
    MatChipsModule,
    MatFormFieldModule,
    MatAutocompleteModule,
    ReactiveFormsModule,
    MatIconModule
  ],
  exports: [ChipsComponent],
  providers: [],
})
export class ChipsModule {}
