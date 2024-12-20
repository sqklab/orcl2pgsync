import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';

import { AuthGuardService as AuthGuard } from './libs/services/auth-guard.service';
import { NotFoundComponent } from './not-found/not-found.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: '/synchronize',
    pathMatch: 'full'
  },
  {
    path: 'home',
    loadChildren: () =>
      import('./synchronizer/synchronizer.module').then(
        m => m.SynchronizerModule
      ),
    canActivate: [AuthGuard]
  },
  {
    path: 'login',
    loadChildren: () =>
      import('./signin/signin.module').then(m => m.SigninModule)
  },
  {
    path: 'synchronize',
    loadChildren: () =>
      import('./synchronizer/synchronizer.module').then(
        m => m.SynchronizerModule
      ),
    canActivate: [AuthGuard]
  },
  {
    path: 'dbconfig',
    loadChildren: () => import('./dbconfig/dbconfig.module').then(m => m.DBConfigModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'comparison',
    loadChildren: () =>
      import('./comparison/comparison-module.module').then(m => m.ComparisoModule), canActivate: [AuthGuard]
  },
  {
    path: 'db-scheduler',
    loadChildren: () =>
      import('./db-scheduler/query.module').then(m => m.ExecuteQueryModule), canActivate: [AuthGuard]
  },
  {
    path: 'db-connector',
    loadChildren: () =>
      import('./db-connector/connector.module').then(m => m.ConnectorsModule), canActivate: [AuthGuard]
  },
  {
    path: 'analysis',
    loadChildren: () =>
      import('./viewchart/viewchart-module.module').then(m => m.ViewChartModule), canActivate: [AuthGuard]
  },
  {
    path: 'operation',
    loadChildren: () =>
      import('./operation/operation-module.module').then(m => m.OperationModule), canActivate: [AuthGuard]
  },
  {
    path: 'kafka-producer',
    loadChildren: () =>
      import('./kafka-send-message/kafka-send-msg-module.module').then(m => m.KafkaSendMessageModule), canActivate: [AuthGuard]
  },
  {
    path: '**',
    component: NotFoundComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true, preloadingStrategy: PreloadAllModules })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
