import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {FormControl} from '@angular/forms';
import {MatAutocompleteSelectedEvent, MatAutocomplete} from '@angular/material/autocomplete';
import {MatChipInputEvent} from '@angular/material/chips';
import {Observable} from 'rxjs';
import {map, startWith} from 'rxjs/operators';
@Component({
  selector: 'app-chips',
  templateUrl: './chips.component.html',
  styleUrls: ['./chips.component.scss']
})
export class ChipsComponent implements OnInit {


  ngOnInit(): void {
  }
  visible = true;
  selectable = true;
  removable = true;
  separatorKeysCodes: number[] = [ENTER, COMMA];
  topicNameCtrl = new FormControl();
  filteredChips: Observable<string[]>;
  
  @Input('selectedChips') selectedChips: string[] = [];
  @Input('allChips') allChips: string[] = [];
  @Output('selectedTags') selectedTags = new EventEmitter();
  @Output('deletedTags') deletedTags = new EventEmitter();

  @ViewChild('topicNameInput') topicNameInput: ElementRef<HTMLInputElement>;
  @ViewChild('auto') matAutocomplete: MatAutocomplete;

  constructor() {
    this.filteredChips = this.topicNameCtrl.valueChanges.pipe(
        map((fruit: string | null) => fruit ? this._filter(fruit) : this.allChips.slice()));
  }

  add(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;

    // Add our fruit
    if ((value || '').trim()) {
      this.selectedChips.push(value.trim());
    }

    // Reset the input value
    if (input) {
      input.value = '';
    }

    this.topicNameCtrl.setValue(null);
  }

  remove(fruit: string): void {
    const index = this.selectedChips.indexOf(fruit);

    if (index >= 0) {
      this.selectedChips.splice(index, 1);
      this.deletedTags.emit(this.selectedChips);
      this.topicNameCtrl.updateValueAndValidity();
    }
  }

  selected(event: MatAutocompleteSelectedEvent): void {
    if (this.selectedChips.indexOf(event.option.viewValue) < 0) {
      this.selectedChips.push(event.option.viewValue);
      this.selectedTags.emit(this.selectedChips);
    }
    this.topicNameInput.nativeElement.value = '';
    this.topicNameCtrl.setValue(null);
  }

  private _filter(value: string): string[] {
    const filterValue = value.toLowerCase();

    return this.allChips.filter(fruit => fruit.toLowerCase().indexOf(filterValue) === 0);
  }

}
