(window.webpackJsonp=window.webpackJsonp||[]).push([[19],{"0m0A":function(n,t,e){"use strict";e.r(t),e.d(t,"SigninModule",function(){return v});var i=e("ofXK"),o=e("3Pt+"),r=e("tyNb"),s=e("CnPz"),c=e("3FKL"),a=e("fXoL"),d=e("qlbE");function b(n,t){1&n&&(a.Ub(0,"div",16),a.Ub(1,"div",17),a.Ic(2," Invalid Username or Password "),a.Tb(),a.Tb())}function u(n,t){1&n&&(a.Ub(0,"div",16),a.Ub(1,"div",17),a.Ic(2," Username/Password is required! "),a.Tb(),a.Tb())}function h(n,t){1&n&&(a.Ub(0,"div",18),a.Ub(1,"div",19),a.Pb(2,"span",20),a.Tb(),a.Tb())}function l(n,t){1&n&&a.Pb(0,"div",21)}const g=[{path:"",component:(()=>{class n{constructor(n,t,e){this.authService=n,this.router=t,this.storageService=e,this.errorRequired=!1,this.errorInvalid=!1,this.isLoading=!1,this.account={username:"",password:""},this.authService.isAuthenticated()&&this.authService.router.navigate(["/"])}ngOnInit(){this.loadingSubs=this.authService.onLoading$.subscribe(n=>{this.isLoading=n})}get isInvalid(){return this.account.username=this.account.username.trim(),this.account.password=this.account.password.trim(),this.account.username.length&&this.account.password.length?(this.errorRequired=!1,!1):(this.errorRequired=!0,!0)}handleLogin(){this.isInvalid||(this.errorInvalid=!1,this.authService.onLoading$.next(!0),this.authService.login(this.account).subscribe(n=>{this.storageService.save(s.b,n.token),this.storageService.save(s.r,this.account.username),this.authService.onAuth$.next(!0),this.authService.onLoading$.next(!1),this.errorInvalid=!1,this.router.navigate(["/synchronize"])},()=>{this.errorInvalid=!0,this.authService.onLoading$.next(!1)}))}ngOnDestroy(){this.loadingSubs&&this.loadingSubs.unsubscribe()}}return n.\u0275fac=function(t){return new(t||n)(a.Ob(d.a),a.Ob(r.c),a.Ob(c.a))},n.\u0275cmp=a.Ib({type:n,selectors:[["app-signin"]],decls:24,vars:6,consts:[[1,"body-login"],[1,"d-flex","flex-column","justify-content-center","align-items-center","card-container"],[1,"card","p-3","login-card","shadow-lg"],[1,"card-body"],[1,"card-title","text-center"],[1,"mb-4"],[1,"form-label","required"],["type","text","placeholder","Username","maxlength","50",1,"form-control",3,"ngModel","keyup.enter","ngModelChange"],["type","password","placeholder","Password","maxlength","50",1,"form-control",3,"ngModel","keyup.enter","ngModelChange"],["class","row justify-content-center mt-2",4,"ngIf"],[1,"justify-content-center","actions"],[1,"text-center"],[1,"btn","btn-outline-primary","btn-signin",3,"click"],["class","spinner show",4,"ngIf"],["class","modal-backdrop fade show modal-backdrop-spinner show",4,"ngIf"],[1,"footer","bg-white","d-flex","justify-content-center"],[1,"row","justify-content-center","mt-2"],[1,"text-danger"],[1,"spinner","show"],["role","status",1,"spinner-border"],[1,"sr-only"],[1,"modal-backdrop","fade","show","modal-backdrop-spinner","show"]],template:function(n,t){1&n&&(a.Ub(0,"div",0),a.Ub(1,"div",1),a.Ub(2,"div",2),a.Ub(3,"div",3),a.Ub(4,"h4",4),a.Ic(5,"Data Synchronization"),a.Tb(),a.Ub(6,"div",5),a.Ub(7,"label",6),a.Ic(8,"Username"),a.Tb(),a.Ub(9,"input",7),a.ec("keyup.enter",function(){return t.handleLogin()})("ngModelChange",function(n){return t.account.username=n}),a.Tb(),a.Tb(),a.Ub(10,"div",5),a.Ub(11,"label",6),a.Ic(12,"Password"),a.Tb(),a.Ub(13,"input",8),a.ec("keyup.enter",function(){return t.handleLogin()})("ngModelChange",function(n){return t.account.password=n}),a.Tb(),a.Tb(),a.Gc(14,b,3,0,"div",9),a.Gc(15,u,3,0,"div",9),a.Ub(16,"div",10),a.Ub(17,"div",11),a.Ub(18,"button",12),a.ec("click",function(){return t.handleLogin()}),a.Ic(19,"Sign in"),a.Tb(),a.Tb(),a.Tb(),a.Tb(),a.Tb(),a.Tb(),a.Tb(),a.Gc(20,h,3,0,"div",13),a.Gc(21,l,1,0,"div",14),a.Ub(22,"div",15),a.Ic(23,"Copyright 2022 \xa9 MEGAZONE CLOUD Corp. All Right Reserved."),a.Tb()),2&n&&(a.Bb(9),a.nc("ngModel",t.account.username),a.Bb(4),a.nc("ngModel",t.account.password),a.Bb(1),a.nc("ngIf",t.errorInvalid),a.Bb(1),a.nc("ngIf",t.errorRequired),a.Bb(5),a.nc("ngIf",t.isLoading),a.Bb(1),a.nc("ngIf",t.isLoading))},directives:[o.b,o.g,o.k,o.n,i.m],styles:["body{background-color:#fff!important}.spinner[_ngcontent-%COMP%]{position:absolute;top:50%;left:50%;z-index:10;opacity:.8;transform:translate(-50%,-50%);display:none;color:#fff!important}.spinner.show[_ngcontent-%COMP%]{display:block}.modal-backdrop-spinner[_ngcontent-%COMP%]{position:absolute;z-index:1}.img-responsive[_ngcontent-%COMP%]{width:100px;height:100px}.body-login[_ngcontent-%COMP%]{height:100vh;width:100vw;background-color:#f9fafb}.login-card[_ngcontent-%COMP%]{width:450px}.card-container[_ngcontent-%COMP%]{height:calc(100vh - 40px)}.actions[_ngcontent-%COMP%]{margin-top:60px}.card-title[_ngcontent-%COMP%]{margin-bottom:60px}@media screen and (max-width:450px){.login-card[_ngcontent-%COMP%]{width:100vw;height:calc(100vh - 40px)}}.btn-signin[_ngcontent-%COMP%]{width:100px}"]}),n})()}];let p=(()=>{class n{}return n.\u0275fac=function(t){return new(t||n)},n.\u0275mod=a.Mb({type:n}),n.\u0275inj=a.Lb({imports:[[r.g.forChild(g)],r.g]}),n})(),v=(()=>{class n{}return n.\u0275fac=function(t){return new(t||n)},n.\u0275mod=a.Mb({type:n}),n.\u0275inj=a.Lb({providers:[],imports:[[i.c,o.f,p]]}),n})()}}]);