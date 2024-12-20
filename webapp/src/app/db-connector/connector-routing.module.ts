import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { InsertOrUpdateConnectorComponent } from './insert-or-update-connector/insert-or-update-connector.component';
import { ListConnectorComponent } from './list-connector/list-connector.component';

const routes: Routes = [
  { path: '', component: ListConnectorComponent },
  { path: 'addOrUpdate', component: InsertOrUpdateConnectorComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ConnectorRoutingModule { }
