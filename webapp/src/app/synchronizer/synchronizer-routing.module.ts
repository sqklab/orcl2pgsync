import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AddNewSynChronizerComponent } from './add-new/add-new-syn-chronizer.component';
import { ImportSynchronizerComponent } from './import-synchronizer/import-synchronizer.component';
import { SynchronizerComponent } from './list/synchronizer.component';
import { ViewDetailErrorLogComponent } from './view-error-synchronizer/view-detail-error-log/view-detail-error-log.component';
import { ViewErrorSynchronizerComponent } from './view-error-synchronizer/view-error-synchronizer.component';
import { ViewLogComponent } from '../view-log/view-log.component';
import { FullPageLogComponent } from '../view-log/full-page-log/full-page-log.component';

const routes: Routes = [
  { path: '', component: SynchronizerComponent },
  { path: 'add', component: AddNewSynChronizerComponent },
  { path: 'import', component: ImportSynchronizerComponent },
  { path: 'viewlog', component: ViewLogComponent },
  { path: 'view-errors', component: ViewErrorSynchronizerComponent },
  { path: 'view-detail-error', component: ViewDetailErrorLogComponent },
  { path: 'log', component: FullPageLogComponent },
  ];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SynchronizerRoutingModule { }
