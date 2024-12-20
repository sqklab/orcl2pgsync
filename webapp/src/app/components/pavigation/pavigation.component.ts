import { Component, Input, Output, EventEmitter, OnChanges } from '@angular/core';
import { generatePaginate } from 'src/app/libs/utils/helper';

interface IPaginate {
    page: number | string;
    isLink: boolean;
}

@Component({
    selector: 'app-pavigation',
    templateUrl: './pavigation.component.html',
    styleUrls: ['./pavigation.component.scss']
})
export class PavigationComponent implements OnChanges {
    @Input() hidePageSize = false;
    @Input() pageSize = 20;
    @Input() totalPage;
    @Output() onChangePage = new EventEmitter();
    @Output() onChangePageSize = new EventEmitter();
    @Input() currentPage = 1;
    pages = [];
    optionMinutes = [20, 50, 100, 200]

    constructor() { }

    ngOnChanges(): void {
        this.pages = generatePaginate(this.totalPage);
    }

    get paginates(): IPaginate[] {
        const _paginates: IPaginate[] = [];
        _paginates.push({ page: 1, isLink: true })
        if (this.currentPage > 3 && this.totalPage > 5) {
            _paginates.push({ page: '...', isLink: false })
        }
        let count = 0;
        const start = this.totalPage - this.currentPage > 2 ? this.currentPage - 1 : this.totalPage - 3;
        for (let item = start; item < this.totalPage; item++) {
            if (item > 1 && Math.abs(item - this.currentPage) <= 3
                && count < 3
            ) {
                count++;
                _paginates.push({ page: item, isLink: true })
            }
        }
        if (this.totalPage - this.currentPage >= 3) {
            _paginates.push({ page: '...', isLink: false })
        }
        _paginates.push({ page: this.totalPage, isLink: true })
        return _paginates;
    }

    handleChangePage(page: number): void {
        this.currentPage = page;
        this.onChangePage.emit(page);
    }

  changePageSize(e) {
    this.onChangePageSize.emit(e);
  }
}
