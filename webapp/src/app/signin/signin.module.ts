import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { SignInRoutingModule } from './signin-routing.module';
import { SignInComponent } from './signin.component';


@NgModule({
  declarations: [
    SignInComponent,
  ],
  providers: [],
  imports: [
    CommonModule,
    FormsModule,
    SignInRoutingModule,

  ]
})
export class SigninModule { }
