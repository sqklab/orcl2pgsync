import { Component, OnInit } from '@angular/core';
import { DbScheduleProcedureResultResponse } from 'src/app/libs/services/interface';

@Component({
  selector: 'app-scheduler-container',
  templateUrl: './scheduler-container.component.html',
  styleUrls: ['./scheduler-container.component.scss'],
})
export class SchedulerContainerComponent implements OnInit {
  active = '1';

  constructor() {}

  ngOnInit(): void {}
}
