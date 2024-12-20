import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { NgbTypeahead } from '@ng-bootstrap/ng-bootstrap';
import { merge, Observable, OperatorFunction, Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter, map } from 'rxjs/operators';

@Component({
  selector: 'app-auto-complete',
  templateUrl: './auto-complete.component.html',
  styleUrls: ['./auto-complete.component.scss']
})
export class AutoCompleteComponent implements OnInit {

  @Input() items = [];
  @Input() width = 200;
  @Input() chooseItem = '';
  @Input() invalidInput = false;
  @Input() disabled = false;

  @Output() selectedItem = new EventEmitter();
  @Output() onBlueSelectedItem = new EventEmitter();
  @Output() clearSelected = new EventEmitter(false);

  focus$ = new Subject<string>();
  click$ = new Subject<string>();
  @ViewChild('instance', {static: true}) instance: NgbTypeahead;

  constructor() { }

  ngOnInit(): void {
  }

  search: OperatorFunction<string, readonly string[]> = (text$: Observable<string>) => {
    const debouncedText$ = text$.pipe(debounceTime(200), distinctUntilChanged());
    const clicksWithClosedPopup$ = this.click$.pipe(filter(() => !this.instance.isPopupOpen()));
    const inputFocus$ = this.focus$;

    return merge(debouncedText$, inputFocus$, clicksWithClosedPopup$).pipe(
      map(term => (term === '' ? this.items
        : this.items.filter(v => v.toLowerCase().indexOf(term.toLowerCase()) > -1)).slice(0, 20))
    );
  }

  onSelectedItem(event) {
    this.chooseItem = event['item'];
    this.selectedItem.emit(this.chooseItem);
  }

  onBlurSelectedItem(event) {
    this.selectedItem.emit(this.chooseItem);
    this.onBlueSelectedItem.emit(this.chooseItem);
  }

  clearSelection() {
    this.chooseItem = '';
    this.selectedItem.emit(this.chooseItem);
    this.clearSelected.emit(true);
  }
}
