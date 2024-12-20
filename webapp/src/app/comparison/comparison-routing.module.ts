import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ViewLogComponent } from '../view-log/view-log.component';
import { ViewchartminuteComponent } from '../viewchart/view-chart-minute/viewchartminute.component';
import { ComparisonComponent } from './list/comparison.component';

const routes: Routes = [
  { path: '', component: ComparisonComponent },
  { path: 'viewlog', component: ViewLogComponent },
  { path: 'viewchart', component: ViewchartminuteComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ComparisonRoutingModule { }
