(window.webpackJsonp=window.webpackJsonp||[]).push([[17],{"5A5r":function(e,t,n){"use strict";n.d(t,"a",function(){return p});var i=n("fXoL"),a=n("izjy"),s=n("ofXK"),c=n("3Pt+");function o(e,t){if(1&e&&(i.Ub(0,"option",7),i.Ic(1),i.Tb()),2&e){const e=t.$implicit;i.nc("value",e),i.Bb(1),i.Jc(e)}}function r(e,t){if(1&e){const e=i.Vb();i.Ub(0,"div",3),i.Ub(1,"label",4),i.Ic(2,"Page size"),i.Tb(),i.Ub(3,"select",5),i.ec("ngModelChange",function(t){return i.zc(e),i.gc(2).pageSize=t})("ngModelChange",function(t){return i.zc(e),i.gc(2).changePageSize(t)}),i.Gc(4,o,2,2,"option",6),i.Tb(),i.Tb()}if(2&e){const e=i.gc(2);i.Bb(3),i.nc("ngModel",e.pageSize),i.Bb(1),i.nc("ngForOf",e.optionMinutes)}}function b(e,t){if(1&e&&(i.Sb(0),i.Gc(1,r,5,2,"div",2),i.Rb()),2&e){const e=i.gc();i.Bb(1),i.nc("ngIf",e.totalPage>0)}}function d(e,t){if(1&e){const e=i.Vb();i.Ub(0,"a",16),i.ec("click",function(){i.zc(e);const t=i.gc().$implicit;return i.gc(2).handleChangePage(t.page)}),i.Ic(1),i.Tb()}if(2&e){const e=i.gc().$implicit;i.Bb(1),i.Jc(e.page)}}function l(e,t){if(1&e&&(i.Ub(0,"span",17),i.Ic(1),i.Tb()),2&e){const e=i.gc().$implicit;i.Bb(1),i.Jc(e.page)}}const g=function(e){return{active:e}};function u(e,t){if(1&e&&(i.Ub(0,"li",9),i.Gc(1,d,2,1,"a",14),i.Gc(2,l,2,1,"span",15),i.Tb()),2&e){const e=t.$implicit,n=i.gc(2);i.nc("ngClass",i.rc(3,g,n.currentPage===e.page)),i.Bb(1),i.nc("ngIf",e.isLink),i.Bb(1),i.nc("ngIf",!e.isLink)}}const h=function(e){return{disabled:e}};function f(e,t){if(1&e){const e=i.Vb();i.Ub(0,"nav"),i.Ub(1,"ul",8),i.Ub(2,"li",9),i.Ub(3,"a",10),i.ec("click",function(){i.zc(e);const t=i.gc();return t.handleChangePage(t.currentPage>1?t.currentPage-1:1)}),i.Ub(4,"span",11),i.Ic(5,"\xab"),i.Tb(),i.Tb(),i.Tb(),i.Gc(6,u,3,5,"li",12),i.Ub(7,"li",9),i.Ub(8,"a",13),i.ec("click",function(){i.zc(e);const t=i.gc();return t.handleChangePage(t.currentPage<t.totalPage?t.currentPage+1:t.totalPage)}),i.Ub(9,"span",11),i.Ic(10,"\xbb"),i.Tb(),i.Tb(),i.Tb(),i.Tb(),i.Tb()}if(2&e){const e=i.gc();i.Bb(2),i.nc("ngClass",i.rc(3,h,1===e.currentPage)),i.Bb(4),i.nc("ngForOf",e.paginates),i.Bb(1),i.nc("ngClass",i.rc(5,h,e.currentPage===e.totalPage))}}let p=(()=>{class e{constructor(){this.hidePageSize=!1,this.pageSize=20,this.onChangePage=new i.n,this.onChangePageSize=new i.n,this.currentPage=1,this.pages=[],this.optionMinutes=[20,50,100,200]}ngOnChanges(){this.pages=Object(a.b)(this.totalPage)}get paginates(){const e=[];e.push({page:1,isLink:!0}),this.currentPage>3&&this.totalPage>5&&e.push({page:"...",isLink:!1});let t=0;for(let n=this.totalPage-this.currentPage>2?this.currentPage-1:this.totalPage-3;n<this.totalPage;n++)n>1&&Math.abs(n-this.currentPage)<=3&&t<3&&(t++,e.push({page:n,isLink:!0}));return this.totalPage-this.currentPage>=3&&e.push({page:"...",isLink:!1}),e.push({page:this.totalPage,isLink:!0}),e}handleChangePage(e){this.currentPage=e,this.onChangePage.emit(e)}changePageSize(e){this.onChangePageSize.emit(e)}}return e.\u0275fac=function(t){return new(t||e)},e.\u0275cmp=i.Ib({type:e,selectors:[["app-pavigation"]],inputs:{hidePageSize:"hidePageSize",pageSize:"pageSize",totalPage:"totalPage",currentPage:"currentPage"},outputs:{onChangePage:"onChangePage",onChangePageSize:"onChangePageSize"},features:[i.zb],decls:4,vars:2,consts:[[1,"d-flex","justify-content-end","align-items-center","mt-3","mb-5"],[4,"ngIf"],["class","d-flex justify-content-end align-items-center",4,"ngIf"],[1,"d-flex","justify-content-end","align-items-center"],[1,"form-label","pe-2"],["name","selectedMinute",1,"form-control",2,"width","50px",3,"ngModel","ngModelChange"],[3,"value",4,"ngFor","ngForOf"],[3,"value"],[1,"pagination","justify-content-end","mb-0","ms-3"],[1,"page-item",3,"ngClass"],["aria-label","Previous",1,"page-link",3,"click"],["aria-hidden","true"],["class","page-item",3,"ngClass",4,"ngFor","ngForOf"],["aria-label","Next",1,"page-link",3,"click"],["class","page-link",3,"click",4,"ngIf"],["class","page-link text-dark",4,"ngIf"],[1,"page-link",3,"click"],[1,"page-link","text-dark"]],template:function(e,t){1&e&&(i.Ub(0,"div",0),i.Gc(1,b,2,1,"ng-container",1),i.Sb(2),i.Gc(3,f,11,7,"nav",1),i.Rb(),i.Tb()),2&e&&(i.Bb(1),i.nc("ngIf",!t.hidePageSize),i.Bb(2),i.nc("ngIf",t.totalPage>1))},directives:[s.m,c.s,c.k,c.n,s.l,c.o,c.u,s.k],styles:[""]}),e})()},"n0+S":function(e,t,n){"use strict";n.r(t),n.d(t,"DBConfigModule",function(){return S});var i=n("ofXK"),a=n("3Pt+"),s=n("tyNb"),c=n("CZ6/"),o=n("fXoL"),r=n("qlbE"),b=n("Px0i");let d=(()=>{class e{constructor(e,t,n,i){this.authService=e,this.dbConfigService=t,this.router=n,this.activeRouter=i,this.serverName="",this.url="",this.username="",this.password="",this.maxPoolSize=10,this.idleTimeout=3e4,this.status="ACTIVE",this.driverClassName="",this.title="Add new Data source"}ngOnInit(){this.isEdit=this.activeRouter.snapshot.queryParams.isEdit,this.isEdit&&(this.title="Edit Data source",this.getDbConfigById())}setPending(e){this.isPending=!!e.target.checked}testConnection(){const e={serverName:this.serverName,url:this.url,username:this.username,password:this.password,maxPoolSize:this.maxPoolSize,idleTimeout:this.idleTimeout,status:this.status,driverClassName:this.driverClassName,isPending:this.isPending};this.testing=!0,this.dbConfigService.testConnection(e).subscribe(e=>{e?c.a.notifyMessage("Datasource Connected Successfully!"):c.a.notifyMessage("Cannot connect to datasource!","error")},e=>{c.a.notifyMessage(507===e.status?"The datasource with server name already exist!":"Cannot connect to datasource!","error")},()=>{this.testing=!1})}upSert(){const e={serverName:this.serverName,url:this.url,username:this.username,password:this.password,maxPoolSize:this.maxPoolSize,idleTimeout:this.idleTimeout,status:this.status,driverClassName:this.driverClassName,isPending:this.isPending};this.authService.onLoading$.next(!0),this.isEdit?(e.id=this.id,this.dbConfigService.updateDatasource(e).subscribe(e=>{this.authService.onLoading$.next(!1),this.router.navigate(["/dbconfig"])},e=>{this.authService.onLoading$.next(!1),c.a.notifyMessage(404===e.status?"Cannot connect to the datasource. Please check again!":"Cannot update datasource. Please check again!","error")})):this.dbConfigService.addNewDBConfig(e).subscribe(e=>{this.authService.onLoading$.next(!1),e&&this.router.navigate(["/dbconfig"])},e=>{this.authService.onLoading$.next(!1),c.a.notifyMessage(507===e.status?"The datasource with server name already exist!":"Cannot edit datasource. Please check again!","error")})}getDbConfigById(){this.id=this.activeRouter.snapshot.queryParams.id,this.id&&(this.authService.onLoading$.next(!0),this.dbConfigService.getDatasourceById(this.id).subscribe(e=>{this.authService.onLoading$.next(!1),this.serverName=e.serverName,this.url=e.url,this.username=e.username,this.password=e.password,this.maxPoolSize=e.maxPoolSize,this.status=e.status,this.idleTimeout=e.idleTimeout,this.driverClassName=e.driverClassName,this.isPending=e.isPending}))}goBack(){window.history.back()}}return e.\u0275fac=function(t){return new(t||e)(o.Ob(r.a),o.Ob(b.a),o.Ob(s.c),o.Ob(s.a))},e.\u0275cmp=o.Ib({type:e,selectors:[["app-add-new-dbconfig"]],decls:56,vars:13,consts:[["aria-label","breadcrumb",1,"border-bottom","shadow-sm","bg-white","p-3"],[1,"breadcrumb","mb-0"],[1,"breadcrumb-item","cursor-pointer",3,"click"],[1,"fa","fa-arrow-left","mr-2"],[1,"breadcrumb-item","no-before"],["href","#"],["aria-current","page",1,"breadcrumb-item"],["routerLink","/dbconfig"],["aria-current","page",1,"breadcrumb-item","active"],[1,"m-3","card","card-body"],[1,"card-main-content"],[1,"row","col-md-12","col-xl-6","m-auto","py-5"],[1,"form-group"],["for","serverName",1,"control-label","required"],["type","text","id","serverName","placeholder","Server Name",1,"form-control",3,"ngModel","ngModelChange"],["for","url",1,"control-label","required"],["type","text","id","url","placeholder","url",1,"form-control",3,"ngModel","ngModelChange"],["for","username",1,"control-label","required"],["type","text","id","username","placeholder","Username",1,"form-control",3,"ngModel","ngModelChange"],["for","password",1,"control-label","required"],["type","text","id","password","placeholder","Password",1,"form-control",3,"ngModel","ngModelChange"],["for","maxPoolSize"],["type","text","id","maxPoolSize","placeholder","Max PoolSize",1,"form-control",3,"ngModel","ngModelChange"],["for","idleTimeout"],["type","text","id","idleTimeout","placeholder","Idle Timeout",1,"form-control",3,"ngModel","ngModelChange"],["for","driverClassName"],["type","text","id","driverClassName","placeholder","Driver ClassName",1,"form-control",3,"ngModel","ngModelChange"],["type","checkbox","id","isPending",1,"mb-2",3,"ngModel","checked","ngModelChange","click"],[1,"btns"],["type","button",1,"btn","btn-outline-primary","add-new",3,"click"],["type","button","routerLink","/dbconfig",1,"btn","btn-outline-primary"],["type","button",1,"btn","btn-outline-primary","test-connection",3,"click"],[1,"fa","fa-spin","mr-2"]],template:function(e,t){1&e&&(o.Ub(0,"nav",0),o.Ub(1,"ol",1),o.Ub(2,"li",2),o.ec("click",function(){return t.goBack()}),o.Ub(3,"a"),o.Pb(4,"span",3),o.Ic(5," Back |"),o.Tb(),o.Tb(),o.Ub(6,"li",4),o.Ub(7,"a",5),o.Ic(8,"Home"),o.Tb(),o.Tb(),o.Ub(9,"li",6),o.Ub(10,"a",7),o.Ic(11,"Datasource management"),o.Tb(),o.Tb(),o.Ub(12,"li",8),o.Ic(13),o.Tb(),o.Tb(),o.Tb(),o.Ub(14,"div",9),o.Ub(15,"div",10),o.Ub(16,"div",11),o.Ub(17,"div",12),o.Ub(18,"label",13),o.Ic(19,"Server Name"),o.Tb(),o.Ub(20,"input",14),o.ec("ngModelChange",function(e){return t.serverName=e}),o.Tb(),o.Tb(),o.Ub(21,"div",12),o.Ub(22,"label",15),o.Ic(23,"Url"),o.Tb(),o.Ub(24,"input",16),o.ec("ngModelChange",function(e){return t.url=e}),o.Tb(),o.Tb(),o.Ub(25,"div",12),o.Ub(26,"label",17),o.Ic(27,"Username"),o.Tb(),o.Ub(28,"input",18),o.ec("ngModelChange",function(e){return t.username=e}),o.Tb(),o.Tb(),o.Ub(29,"div",12),o.Ub(30,"label",19),o.Ic(31,"Password"),o.Tb(),o.Ub(32,"input",20),o.ec("ngModelChange",function(e){return t.password=e}),o.Tb(),o.Tb(),o.Ub(33,"div",12),o.Ub(34,"label",21),o.Ic(35,"Max PoolSize"),o.Tb(),o.Ub(36,"input",22),o.ec("ngModelChange",function(e){return t.maxPoolSize=e}),o.Tb(),o.Tb(),o.Ub(37,"div",12),o.Ub(38,"label",23),o.Ic(39,"Idle Timeout"),o.Tb(),o.Ub(40,"input",24),o.ec("ngModelChange",function(e){return t.idleTimeout=e}),o.Tb(),o.Tb(),o.Ub(41,"div",12),o.Ub(42,"label",25),o.Ic(43,"Driver ClassName"),o.Tb(),o.Ub(44,"input",26),o.ec("ngModelChange",function(e){return t.driverClassName=e}),o.Tb(),o.Tb(),o.Ub(45,"div",12),o.Ub(46,"input",27),o.ec("ngModelChange",function(e){return t.isPending=e})("click",function(e){return t.setPending(e)}),o.Tb(),o.Ic(47," \xa0\xa0Is Pending (If checked - Application will not initialize this datasource at startup) "),o.Tb(),o.Ub(48,"div",28),o.Ub(49,"button",29),o.ec("click",function(){return t.upSert()}),o.Ic(50),o.Tb(),o.Ub(51,"button",30),o.Ic(52,"Cancel"),o.Tb(),o.Ub(53,"button",31),o.ec("click",function(){return t.testConnection()}),o.Pb(54,"span",32),o.Ic(55," Test Connection "),o.Tb(),o.Tb(),o.Tb(),o.Tb(),o.Tb()),2&e&&(o.Bb(13),o.Jc(t.title),o.Bb(7),o.nc("ngModel",t.serverName),o.Bb(4),o.nc("ngModel",t.url),o.Bb(4),o.nc("ngModel",t.username),o.Bb(4),o.nc("ngModel",t.password),o.Bb(4),o.nc("ngModel",t.maxPoolSize),o.Bb(4),o.nc("ngModel",t.idleTimeout),o.Bb(4),o.nc("ngModel",t.driverClassName),o.Bb(2),o.nc("ngModel",t.isPending)("checked",t.isPending),o.Bb(4),o.Jc(t.isEdit?"Update":"Create"),o.Bb(4),o.Gb("fa-spinner",t.testing))},directives:[s.f,a.b,a.k,a.n,a.a,s.d],styles:[".btns[_ngcontent-%COMP%]{margin-top:20px}.btns[_ngcontent-%COMP%]   .add-new[_ngcontent-%COMP%]{margin-right:10px}.btns[_ngcontent-%COMP%]   .test-connection[_ngcontent-%COMP%]{float:right}.card-main-content[_ngcontent-%COMP%]{min-height:calc(100vh - 180px)}"]}),e})();var l=n("XNiG"),g=n("5A5r");function u(e,t){if(1&e&&(o.Ub(0,"span",32),o.Ic(1),o.Tb()),2&e){const e=o.gc().$implicit;o.Bb(1),o.Jc(e.status)}}function h(e,t){if(1&e&&(o.Ub(0,"span",33),o.Ic(1),o.Tb()),2&e){const e=o.gc().$implicit;o.Bb(1),o.Jc(e.status)}}function f(e,t){if(1&e&&(o.Ub(0,"span",34),o.Ic(1),o.Tb()),2&e){const e=o.gc().$implicit;o.Bb(1),o.Jc(e.status)}}function p(e,t){if(1&e&&(o.Ub(0,"span",35),o.Ic(1),o.Tb()),2&e){const e=o.gc().$implicit;o.Bb(1),o.Jc(e.status)}}function m(e,t){if(1&e&&(o.Ub(0,"span",36),o.Ic(1),o.Tb()),2&e){const e=o.gc().$implicit;o.Bb(1),o.Jc(e.status)}}function P(e,t){if(1&e){const e=o.Vb();o.Ub(0,"tr",19),o.ec("dblclick",function(){o.zc(e);const n=t.$implicit;return o.gc().dbclickToEdit(n)}),o.Ub(1,"td",20),o.ec("click",function(n){o.zc(e);const i=t.$implicit;return o.gc().selectItem(n,i.id)}),o.Pb(2,"input",21),o.Tb(),o.Ub(3,"td",22),o.Ic(4),o.Tb(),o.Ub(5,"td",23),o.Ic(6),o.Tb(),o.Ub(7,"td",24),o.Gc(8,u,2,1,"span",25),o.Gc(9,h,2,1,"span",26),o.Gc(10,f,2,1,"span",27),o.Gc(11,p,2,1,"span",28),o.Gc(12,m,2,1,"span",29),o.Tb(),o.Ub(13,"td",23),o.Ic(14),o.Tb(),o.Ub(15,"td",23),o.Ic(16),o.Tb(),o.Ub(17,"td",23),o.Ic(18),o.hc(19,"date"),o.Tb(),o.Ub(20,"td",23),o.Ic(21),o.hc(22,"date"),o.Tb(),o.Ub(23,"td",24),o.Ub(24,"button",30),o.ec("click",function(n){o.zc(e);const i=t.$implicit;return o.gc().testConnection(n,i)}),o.Pb(25,"span",31),o.Ic(26," Test Connection"),o.Tb(),o.Tb(),o.Tb()}if(2&e){const e=t.$implicit;o.Bb(2),o.nc("checked",e.checked),o.Bb(2),o.Jc(e.serverName),o.Bb(2),o.Jc(e.url),o.Bb(2),o.nc("ngIf","ACTIVE"===e.status),o.Bb(1),o.nc("ngIf","IN_USE"===e.status),o.Bb(1),o.nc("ngIf","INACTIVE"===e.status),o.Bb(1),o.nc("ngIf","DISCONNECTED"===e.status),o.Bb(1),o.nc("ngIf","PENDING"===e.status),o.Bb(2),o.Jc(e.createdBy),o.Bb(2),o.Jc(e.updatedBy),o.Bb(2),o.Jc(o.jc(19,15,e.createdAt,"yyyy.MM.dd / HH:mm")),o.Bb(3),o.Jc(o.jc(22,18,e.updatedAt,"yyyy.MM.dd / HH:mm")),o.Bb(3),o.nc("disabled",e.testing),o.Bb(1),o.Gb("fa-spinner",e.testing)}}const C=[{path:"",component:(()=>{class e{constructor(e,t,n){this.authService=e,this.dbConfigService=t,this.router=n,this.datasources=[],this.selected=[],this.currentPage=1,this.pageSize=20,this.keyValue="",this.dataSuggests=[],this.focus$=new l.a,this.click$=new l.a,this.topicNames=[]}ngOnInit(){this.loadDBConfigs()}addNew(){this.router.navigate(["/dbconfig/add"])}handleChangePageSize(e){this.pageSize=e,this.loadDBConfigs()}handleChangePage(e){this.currentPage=e,this.loadDBConfigs()}loadDBConfigs(){this.totalPage=0,this.authService.onLoading$.next(!0),this.dbConfigService.getDBConfigs(this.currentPage,this.pageSize).subscribe(e=>{this.authService.onLoading$.next(!1),e&&(this.datasources=e.dataSourceDescriptions,this.selected.length>0&&this.datasources.forEach(e=>{e.checked=this.selected.includes(e.id)}),this.totalPage=e.totalPage)},e=>{this.authService.onLoading$.next(!1)})}dbclickToEdit(e){"IN_USE"!==e.status?this.router.navigate(["/dbconfig/edit"],{queryParams:{id:e.id,isEdit:!0}}):this.authService.onShowModal$.next({isShow:!0,title:"Infomation",message:"Cannot edit IN-USE Data Source!",type:"info",confirm:()=>{}})}testConnection(e,t){t.testing=!0;const n={serverName:t.serverName,url:t.url,username:t.username,password:t.password,maxPoolSize:t.maxPoolSize,idleTimeout:t.idleTimeout,status:t.status,driverClassName:t.driverClassName,isPending:t.isPending};e.stopPropagation(),this.dbConfigService.testConnection(n).subscribe(e=>{e?c.a.notifyMessage("Datasource Connected Successfully!"):c.a.notifyMessage("Cannot connect to datasource!","error")},e=>{c.a.notifyMessage(507===e.status?"The datasource with server name already exist!":"Cannot connect to datasource!","error"),t.testing=!1},()=>{t.testing=!1})}edit(){if(1===this.selected.length){const e=this.datasources.findIndex(e=>e.id===this.selected[0]);this.authService.onShowModal$.next("IN_USE"!==this.datasources[e].status?{isShow:!0,title:"Infomation",message:"Are you sure to EDIT this Data Source?",confirm:()=>{this.router.navigate(["/dbconfig/edit"],{queryParams:{id:this.selected[0],isEdit:!0}})},type:"info",cancel:!0}:{isShow:!0,title:"Infomation",message:"Cannot edit IN-USE Data Source!",type:"info",confirm:()=>{}})}else this.authService.onShowModal$.next({isShow:!0,title:"Infomation",message:"You should select only one Data Source to EDIT",type:"info",confirm:()=>{}})}delete(){this.authService.onShowModal$.next(1===this.selected.length?{isShow:!0,title:"Infomation",message:"Are you sure to DELETE this Data Source?",confirm:()=>{this.dbConfigService.deleteDatasource(this.selected).subscribe(e=>{e?(this.loadDBConfigs(),this.selected=[],c.a.notifyMessage("Deleted Data Source Successfully!")):(this.selected=[],c.a.notifyMessage("Cannot delete IN-USE Datasource","error"))})},type:"info",cancel:!0}:{isShow:!0,title:"Infomation",message:"You should select one Data Source to DELETE",type:"info",confirm:()=>{}})}selectItem(e,t){e.target.checked?this.selected.push(t):this.selected=this.selected.filter(e=>e!==t)}}return e.\u0275fac=function(t){return new(t||e)(o.Ob(r.a),o.Ob(b.a),o.Ob(s.c))},e.\u0275cmp=o.Ib({type:e,selectors:[["app-dbconfig"]],decls:44,vars:2,consts:[["aria-label","breadcrumb",1,"border-bottom","shadow-sm","bg-white","p-3"],[1,"breadcrumb","mb-0"],[1,"breadcrumb-item"],["href","#"],["aria-current","page",1,"breadcrumb-item","active"],[1,"content","m-3"],[1,"card","card-body"],[1,"toolbar"],[1,"left"],["type","button",1,"btn","btn-outline-primary","mr-5",3,"click"],[1,"bi","bi-plus-lg","plus"],[1,"right"],[1,"card","card-body","mt-3","card-main-content"],[1,"table","table-hover","table-bordered"],["scope","col",1,"text-center","server-name-th"],["scope","col",1,"text-center"],["scope","col",1,"text-center",2,"width","190px"],[3,"dblclick",4,"ngFor","ngForOf"],[3,"totalPage","onChangePage","onChangePageSize"],[3,"dblclick"],[1,"text-center","checkbox-item",3,"click"],["type","checkbox",3,"checked"],[1,"server-name"],[1,"url"],[1,"text-center"],["class","badge bg-info",4,"ngIf"],["class","badge bg-success",4,"ngIf"],["class","badge bg-secondary",4,"ngIf"],["class","badge bg-warning",4,"ngIf"],["class","badge bg-dark",4,"ngIf"],["type","button",1,"btn","btn-outline-primary","test-connection",3,"disabled","click"],[1,"fa","fa-spin","mr-2"],[1,"badge","bg-info"],[1,"badge","bg-success"],[1,"badge","bg-secondary"],[1,"badge","bg-warning"],[1,"badge","bg-dark"]],template:function(e,t){1&e&&(o.Ub(0,"nav",0),o.Ub(1,"ol",1),o.Ub(2,"li",2),o.Ub(3,"a",3),o.Ic(4,"Home"),o.Tb(),o.Tb(),o.Ub(5,"li",4),o.Ic(6,"Datasource management"),o.Tb(),o.Tb(),o.Tb(),o.Ub(7,"div",5),o.Ub(8,"div",6),o.Ub(9,"div",7),o.Ub(10,"div",8),o.Ub(11,"button",9),o.ec("click",function(){return t.addNew()}),o.Pb(12,"i",10),o.Ic(13,"Add new"),o.Tb(),o.Tb(),o.Ub(14,"div",11),o.Ub(15,"button",9),o.ec("click",function(){return t.edit()}),o.Ic(16,"Edit"),o.Tb(),o.Ub(17,"button",9),o.ec("click",function(){return t.delete()}),o.Ic(18,"Delete"),o.Tb(),o.Tb(),o.Tb(),o.Tb(),o.Ub(19,"div",12),o.Ub(20,"div"),o.Ub(21,"table",13),o.Ub(22,"thead"),o.Ub(23,"tr"),o.Pb(24,"th"),o.Ub(25,"th",14),o.Ic(26,"Server Name"),o.Tb(),o.Ub(27,"th",15),o.Ic(28,"URL"),o.Tb(),o.Ub(29,"th",15),o.Ic(30,"Status"),o.Tb(),o.Ub(31,"th",15),o.Ic(32,"Created User"),o.Tb(),o.Ub(33,"th",15),o.Ic(34,"Updated User"),o.Tb(),o.Ub(35,"th",15),o.Ic(36,"Created At"),o.Tb(),o.Ub(37,"th",15),o.Ic(38,"Updated At"),o.Tb(),o.Ub(39,"th",16),o.Ic(40,"Test Connection"),o.Tb(),o.Tb(),o.Tb(),o.Ub(41,"tbody"),o.Gc(42,P,27,21,"tr",17),o.Tb(),o.Tb(),o.Ub(43,"app-pavigation",18),o.ec("onChangePage",function(e){return t.handleChangePage(e)})("onChangePageSize",function(e){return t.handleChangePageSize(e)}),o.Tb(),o.Tb(),o.Tb(),o.Tb()),2&e&&(o.Bb(42),o.nc("ngForOf",t.datasources),o.Bb(1),o.nc("totalPage",t.totalPage))},directives:[i.l,g.a,i.m],pipes:[i.e],styles:[".toolbar[_ngcontent-%COMP%]{display:flex;justify-content:space-between}.toolbar[_ngcontent-%COMP%]   .left[_ngcontent-%COMP%]   i[_ngcontent-%COMP%]{margin-right:5px}.toolbar[_ngcontent-%COMP%]   .left[_ngcontent-%COMP%]   .plus[_ngcontent-%COMP%]{font-size:13px}.card-main-content[_ngcontent-%COMP%]{min-height:calc(100vh - 225px)}.server-name[_ngcontent-%COMP%]{max-width:350px;word-wrap:break-word;cursor:default}.server-name-th[_ngcontent-%COMP%]{width:350px}.url[_ngcontent-%COMP%]{max-width:400px;word-wrap:break-word}.checkbox-item[_ngcontent-%COMP%]{width:50px}.toolbar[_ngcontent-%COMP%]   button[_ngcontent-%COMP%]{min-width:70px}"]}),e})()},{path:"edit",component:d},{path:"add",component:d}];let T=(()=>{class e{}return e.\u0275fac=function(t){return new(t||e)},e.\u0275mod=o.Mb({type:e}),e.\u0275inj=o.Lb({imports:[[s.g.forChild(C)],s.g]}),e})();var v=n("/zYI"),U=n("1kSV"),I=n("izjy");let S=(()=>{class e{}return e.\u0275fac=function(t){return new(t||e)},e.\u0275mod=o.Mb({type:e}),e.\u0275inj=o.Lb({providers:[{provide:U.c,useClass:I.a}],imports:[[i.c,U.f,v.a,a.f,T]]}),e})()}}]);