import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { IDropdownSettings } from 'ng-multiselect-dropdown';
import { MultipeSelectDropDown } from './interface';

@Component({
  selector: 'app-multiselect-dropdown',
  templateUrl: './multiselect-dropdown.component.html',
  styleUrls: ['./multiselect-dropdown.component.scss']
})
export class MultiselectDropdownComponent implements OnInit, OnChanges {
  @Input() dropdownList: MultipeSelectDropDown[] = [];
  @Input() width: number = 200;
  @Input() limitSelection = -1;
  @Input() itemsShowLimit = 3;
  @Input() disabled = false;
  @Output() selectedItemsOut = new EventEmitter();
  @Output() clearSelected = new EventEmitter();
  @Input() selectedItems: MultipeSelectDropDown[] = [];

  dropdownSettings = {};

  constructor() {
  }

  ngOnInit(): void {
    this.buildSetting();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.buildSetting();
  }

  private buildSetting(): void {
    this.dropdownSettings = {
      singleSelection: false,
      idField: 'item_id',
      textField: 'item_text',
      enableCheckAll: false,
      allowSearchFilter: true,
      limitSelection: this.limitSelection,
      clearSearchFilter: true,
      maxHeight: 197,
      itemsShowLimit: this.itemsShowLimit,
      closeDropDownOnSelection: true,
      showSelectedItemsAtTop: false,
      defaultOpen: false,
    }  as IDropdownSettings;
  }

  onItemSelect(item: any): void {
    this.selectedItemsOut.emit(this.selectedItems);

  }

  clearSelection(): void {
    this.selectedItems = [];
    this.selectedItemsOut.emit(this.selectedItems);
    this.clearSelected.emit(true);
  }

  removeSelected(event): void {
    this.selectedItemsOut.emit(this.selectedItems);
    if (this.selectedItems.length === 0) {
      this.clearSelected.emit(true);
    }
  }
}
