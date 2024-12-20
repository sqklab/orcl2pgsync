import { NgbDateParserFormatter, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { Injectable } from '@angular/core';

export function generatePaginate(totalPage: number): number[] {
    return Array.from(Array(totalPage), (_, i) => i + 1);
}

@Injectable({
    providedIn: 'root'
  })
export class NgbDateCustomParserFormatter extends NgbDateParserFormatter {
  readonly DELIMITER = '-';

  parse(value: string): NgbDateStruct {
      console.log('Method not implemented.');
      return null;
  }

  format(date: NgbDateStruct | null): string {
    if (date) {
      const day = (date.day && date.day.toString().length === 1) ? '0' + date.day : date.day;
      const month =( date.month && date.month.toString().length === 1) ? '0' + date.month : date.month;
      return date ? date.year + this.DELIMITER + month + this.DELIMITER + day : '';
    } else {
      return '';
    }
  }
}
