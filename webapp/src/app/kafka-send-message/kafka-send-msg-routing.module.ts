import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { KafkaSendMessageComponent } from './list/kafka-send-msg.component';

const routes: Routes = [
  { path: '', component: KafkaSendMessageComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class KafkaSendMessageRoutingModule { }
