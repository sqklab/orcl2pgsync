import { NgbTimeStruct } from '@ng-bootstrap/ng-bootstrap';
import { DayOfWeek, MonthQuarterYear } from 'src/app/libs/services/interface';

export class RegistrationHelper {
  static LAST_DAY = 'LASTDAY';

  public static nth(d) {
    if (d > 3 && d < 21) { return 'th'; }
    switch (d % 10) {
      case 1:  return 'st';
      case 2:  return 'nd';
      case 3:  return 'rd';
      default: return 'th';
    }
  }

  public static initDayOfWeek(): any[] {
    const dayOfWeeks = [];
    dayOfWeeks.push({
      actived: false,
      name: 'Mon',
      value: 2,
    } as DayOfWeek);
    dayOfWeeks.push({
      actived: false,
      name: 'Tue',
      value: 3,
    } as DayOfWeek);
    dayOfWeeks.push({
      actived: false,
      name: 'Wed',
      value: 4,
    } as DayOfWeek);
    dayOfWeeks.push({
      actived: false,
      name: 'Thu',
      value: 5,
    } as DayOfWeek);
    dayOfWeeks.push({
      actived: false,
      name: 'Fri',
      value: 6,
    } as DayOfWeek);
    dayOfWeeks.push({
      actived: false,
      name: 'Sat',
      value: 7,
    } as DayOfWeek);
    return dayOfWeeks;
  }

  public static validateMonthy(monthy: MonthQuarterYear[]): string {
    let errMsg = '';
    if (monthy.length === 1) {
      if (monthy[0].day !== this.LAST_DAY) {
        errMsg = 'You should select at leat day';
      }
    } else if (monthy[0].day !== this.LAST_DAY) {
      for (let i = 1; i < monthy.length; i++) {
        if (!monthy[i].day || monthy[i].day.trim() === '') {
          errMsg = 'You have enter a day';
        }
      }
    }
    return errMsg;
  }

  public static validateQuaterlyOYear(quaterly: MonthQuarterYear[]): string {
    let errMsg = '';
    if (quaterly.length === 0) {
      errMsg = 'You have select at least a month';
    }
    quaterly.forEach((x) => {
      if (!x.month || !x.day) {
        errMsg = 'You have enter number of month or number of day';
      } else if (x.month > 3) {
        errMsg = 'The month should between 1 and 3';
      }
    });
    return errMsg;
  }

  public static validateYearly(quaterly: MonthQuarterYear[]): string {
    let errMsg = '';
    if (quaterly.length === 0) {
      errMsg = 'You have select at least a year';
    }
    quaterly.forEach((x) => {
      if (!x.month || !x.day) {
        errMsg = 'You have enter number of year';
      } else if (x.month > 3) {
        errMsg = 'The month should between 1 and 12';
      }
    });
    return errMsg;
  }


  public static getTimeNgbStruct(time: string): any {
    const hourMinute = time.split(':');
    const timeDisplay = {
      hour: +hourMinute[0],
      minute: +hourMinute[1],
      second: 0,
    } as NgbTimeStruct;
    return timeDisplay;
  }

  public static getTimeValueForSave(time: NgbTimeStruct): any {
    const hour = this.padLeadingZeros(time.hour, 2);
    const minute = this.padLeadingZeros(time.minute, 2);
    return hour + ':' + minute + ':' + '00';
  }

  static padLeadingZeros(num, size): any {
    let s = num + '';
    while (s.length < size) {
        s = '0' + s;
    }
    return s;
  }
}
