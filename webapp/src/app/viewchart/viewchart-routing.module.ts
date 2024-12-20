import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ViewchartminuteComponent } from './view-chart-minute/viewchartminute.component';

const routes: Routes = [
  { path: '', component: ViewchartminuteComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ViewChartRoutingModule { }
