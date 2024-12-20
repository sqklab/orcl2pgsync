import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'connectorTaskStatus',
})
export class ConnectorTaskStatusPipe implements PipeTransform {
  transform(value: any[]): string {
    if (!value || value.length === 0) {
      return '';
    }
    const item = value[0];

    switch (item.state) {
      case 'RUNNING':
        return '<span class="badge bg-success">' + item.state + '</span>';
      case 'UNASSIGNED':
        return '<span class="badge bg-warning">' + item.state + '</span>';
      case 'PAUSED':
        return '<span class="badge bg-warning">' + item.state + '</span>';
      case 'FAILED':
        return '<span class="badge bg-danger">' + item.state + '</span>';
      default:
        return '<span class="badge bg-secondary">' + item.state + '</span>';
    }
  }
}
