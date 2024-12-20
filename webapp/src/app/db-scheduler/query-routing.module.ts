import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ViewLogComponent } from '../view-log/view-log.component';
import { SchedulerContainerComponent } from './scheduler-container/scheduler-container.component';

const routes: Routes = [
  { path: '', component: SchedulerContainerComponent },
  { path: 'viewlog', component: ViewLogComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ExecuteQueryRoutingModule { }
