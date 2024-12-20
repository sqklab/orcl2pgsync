import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AddNewDBConfigComponent } from './add-new/add-new-dbconfig.component';
import {DBConfigComponent} from './dbconfig.component';

const routes: Routes = [{ path: '', component: DBConfigComponent },
  { path: 'edit', component: AddNewDBConfigComponent },
  { path: 'add', component: AddNewDBConfigComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DBConfigRoutingModule { }
