import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CompareDiffItem } from 'src/app/libs/services/interface';

export interface CellOp {
  value: string;
  notMatch: boolean;
}
@Component({
  selector: 'app-show-detail-record',
  templateUrl: './show-detail-record.component.html',
  styleUrls: ['./show-detail-record.component.scss']
})
export class ShowDetailRecordComponent implements OnInit {
  @Input() doc: CompareDiffItem;
  @Input() headers;

  rowOracles: CellOp[] = [];
  rowPostgres: CellOp[] = [];

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {
    this.computedCell();
  }

  private computedCell() {
    // build oracles
    if (this.doc.source) {
      this.headers.forEach(h => {
        this.rowOracles.push({
          value: this.doc.source[h]
        } as CellOp);
      });
    } else {
      this.headers.forEach(_ => {
        this.rowOracles.push({
          value: 'N/A'
        } as CellOp);
      });
    }
    // build postgres
    if (this.doc.target) {
      this.headers.forEach(h => {
        this.rowPostgres.push({
          value: this.doc.target[h.toLocaleLowerCase()]
        } as CellOp);
      });
    } else {
      this.headers.forEach(_ => {
        this.rowPostgres.push({
          value: 'N/A'
        } as CellOp);
      });
    }
    // check diff and highlight the cell
    if (this.doc.operation === 'UPDATE') {
      for (let i = 0; i < this.rowOracles.length; i++) {
        if (this.rowOracles[i].value !== this.rowPostgres[i].value) {
          this.rowOracles[i].notMatch = true;
          this.rowPostgres[i].notMatch = true;
        }
      }
    }
  }

}
