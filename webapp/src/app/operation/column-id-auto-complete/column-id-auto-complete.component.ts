import { Component, ElementRef, EventEmitter, HostListener, Input, OnInit, Output, ViewChild } from '@angular/core';
import { fromEvent } from 'rxjs';
import {
  debounceTime,
  distinctUntilChanged
} from 'rxjs/operators';
import { AuthService } from 'src/app/libs/services/auth.service';
import { OperationDto } from 'src/app/libs/services/interface';
import { OperationService } from 'src/app/libs/services/operation.service';

@Component({
  selector: 'app-column-id-auto-complete',
  templateUrl: './column-id-auto-complete.component.html',
  styleUrls: ['./column-id-auto-complete.component.scss'],
})
export class ColumnIdAutoCompleteComponent implements OnInit {
  searching = false;
  searchFailed = false;
  showDropMenu = true;
  errorMsg = null;
  suggestValues = [];
  // bkSuggestValues = [];
  toggleMenu = false;
  isFetchHint = false;

  @Input() disabled = false;
  @Input('operationInfo') operationInfo: OperationDto;
  @Input('width') width = 400;
  @Output() outSelectedItem = new EventEmitter<boolean>(false);
  @Output() oracleValueIdNotFound = new EventEmitter<boolean>(false);

  @ViewChild('SearchInput', { static: true }) searchInput: ElementRef;

  constructor(private _service: OperationService, private authService: AuthService) {}

  ngOnInit() {
    // this.registerSearchingEvent();
  }

  onSelectedItem(event) {
    this.operationInfo.columnIdValue = event.item;
    // this.outSelectedItem.emit(true);
  }

  onChangeValue(event) {
    this.operationInfo.columnIdValue = event;
    this.outSelectedItem.emit(true);
  }

  onBlurValue(event) {
    // this.outSelectedItem.emit(true);
  }

  @HostListener('document:click', ['$event'])
  handleKeyDown(event) {
    const menuEl = document.getElementById('dropdown-menu-id');
    const columnIdEl = document.getElementById('typeahead-column-id');
    if (!menuEl) {
      return;
    }
    const isClickInsidemenuEl = menuEl.contains(event.target);
    const isClickInsidecolumnIdEl = columnIdEl.contains(event.target);
    if (!isClickInsidemenuEl && !isClickInsidecolumnIdEl) {
      this.toggleMenu = false;
    }
  }

  private registerSearchingEvent(): void {
    fromEvent(this.searchInput.nativeElement, 'keyup').pipe(
      debounceTime(500)
      ,distinctUntilChanged()
    ).subscribe(_ => {
      this.fetch(false);
    });
  }

  onShowSuggestion(event) {
    if (this.disabled) {
      return;
    }
    this.toggleMenu = true;
    if (this.operationInfo.columnIdValue) {
      this.fetch(false);
    }
  }

  onSelectedValue(item) {
    this.operationInfo.columnIdValue=item;
    this.toggleMenu = false;
    this.outSelectedItem.emit(true);
    // this.fetch(true);
  }

  private fetch(isClick) {
    if (this.operationInfo.sourceDatabase === '' || this.operationInfo.sourceSchema === '' || this.operationInfo.table === '') {
      return;
    }
    this.errorMsg = null;
    this.searching = true;
    this.searchFailed = false;
    this.authService.onLoading$.next(true);
    this._service.getColumnIdValue(this.operationInfo).subscribe(res => {
      this.operationInfo.columnIdValue === '' ? this.isFetchHint = true : this.isFetchHint = false;

      this.searching = false;
      this.authService.onLoading$.next(false);
      if (res && res.status === 200) {
        this.suggestValues = res.body;
        if (isClick) {
         this.toggleMenu = this.suggestValues.length > 1;
        } else {
         this.toggleMenu = this.suggestValues.length > 0;
        }
        if (this.suggestValues.length === 0 || this.suggestValues.indexOf(this.operationInfo.columnIdValue) == -1) {
          this.oracleValueIdNotFound.emit(true);
        } else {
          this.oracleValueIdNotFound.emit(false);
        }
      } else {
        this.searchFailed = true;
        this.errorMsg = res.message;
      }
    },
    err => {
      this.searching = false;
      this.authService.onLoading$.next(false);
      this.searchFailed = true;
    },
    () => {
      this.searching = false;
      this.authService.onLoading$.next(false);
    });
  }

  private getFirstCharacter() {
    if (this.operationInfo.sourceDatabase === '' || this.operationInfo.sourceSchema === '' || this.operationInfo.table === '') {
      return;
    }
    this.isFetchHint = false;
    this.errorMsg = null;
    this.searching = true;
    this.searchFailed = false;
    this.authService.onLoading$.next(true);
    this._service.getColumnIdValue(this.operationInfo).subscribe(res => {
      this.searching = false;
      this.authService.onLoading$.next(false);
      if (res && res.status === 200) {
        this.suggestValues = res.body;
        // this.bkSuggestValues = res.body;
      } else {
        this.searchFailed = true;
        this.errorMsg = res.message;
      }
      if (this.suggestValues.length > 0 && this.suggestValues[0]) {
        this.isFetchHint =  this.suggestValues[0].length == 1;
      }
    },
    err => {
      this.searching = false;
      this.authService.onLoading$.next(false);
      this.searchFailed = true;
    },
    () => {
      this.searching = false;
      this.authService.onLoading$.next(false);
    });
  }
}
