import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from 'src/app/libs/services/auth.service';
import { DBConfigService } from 'src/app/libs/services/dbconfig.service';
import { DataSourceInfoDTO } from 'src/app/libs/services/interface';
import { SyncService } from 'src/app/libs/services/syncDocument.service';
import { SweetAlert } from 'src/app/libs/utils/sweetalert';

@Component({
  selector: 'app-add-new-dbconfig',
  templateUrl: './add-new-dbconfig.component.html',
  styleUrls: ['./add-new-dbconfig.component.scss'],
})
export class AddNewDBConfigComponent implements OnInit {
  serverName = '';
  url = '';
  username = '';
  password = '';
  maxPoolSize = 10;
  idleTimeout = 30000;
  status = 'ACTIVE';
  driverClassName = '';
  isPending;
  id;
  isEdit;
  title = 'Add new Data source';

  testing;

  constructor(
    private authService: AuthService,
    private dbConfigService: DBConfigService,
    private router: Router,
    private activeRouter: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.isEdit = this.activeRouter.snapshot.queryParams['isEdit'];
    if (this.isEdit) {
      this.title = 'Edit Data source';
      this.getDbConfigById();
    }
  }

  setPending(event) {
    this.isPending = !!event.target.checked;
  }

  testConnection() {
    const body = {
      serverName: this.serverName,
      url: this.url,
      username: this.username,
      password: this.password,
      maxPoolSize: this.maxPoolSize,
      idleTimeout: this.idleTimeout,
      status: this.status,
      driverClassName: this.driverClassName,
      isPending: this.isPending
    } as DataSourceInfoDTO;
    this.testing = true;
    this.dbConfigService.testConnection(body).subscribe(
      (res) => {
        if (res) {
          SweetAlert.notifyMessage('Datasource Connected Successfully!');
        } else {
          SweetAlert.notifyMessage('Cannot connect to datasource!', 'error');
        }
      },
      (err) => {
        if (507 === err.status) {
          SweetAlert.notifyMessage(
            'The datasource with server name already exist!',
            'error'
          );
        } else {
          SweetAlert.notifyMessage('Cannot connect to datasource!', 'error');
        }
      },
      () => {
        this.testing = false;
      }
    );
  }

  upSert() {
    const body = {
      serverName: this.serverName,
      url: this.url,
      username: this.username,
      password: this.password,
      maxPoolSize: this.maxPoolSize,
      idleTimeout: this.idleTimeout,
      status: this.status,
      driverClassName: this.driverClassName,
      isPending: this.isPending
    } as DataSourceInfoDTO;
    this.authService.onLoading$.next(true);
    if (this.isEdit) {
      body.id = this.id;
      this.dbConfigService.updateDatasource(body).subscribe(
        (res) => {
          this.authService.onLoading$.next(false);
          this.router.navigate(['/dbconfig']);
        },
        (err) => {
          this.authService.onLoading$.next(false);
          if (404 === err.status) {
            SweetAlert.notifyMessage(
              'Cannot connect to the datasource. Please check again!',
              'error'
            );
          } else {
            SweetAlert.notifyMessage(
              'Cannot update datasource. Please check again!',
              'error'
            );
          }
          // SweetAlert.notifyMessage('Cannot edit IN_USE Data Source!', 'error');
        }
      );
    } else {
      this.dbConfigService.addNewDBConfig(body).subscribe(
        (res) => {
          this.authService.onLoading$.next(false);
          if (res) {
            this.router.navigate(['/dbconfig']);
          }
        },
        (err) => {
          this.authService.onLoading$.next(false);
          if (507 === err.status) {
            SweetAlert.notifyMessage(
              'The datasource with server name already exist!',
              'error'
            );
          } else {
            SweetAlert.notifyMessage(
              'Cannot edit datasource. Please check again!',
              'error'
            );
          }
        }
      );
    }
  }

  getDbConfigById(): void {
    this.id = this.activeRouter.snapshot.queryParams['id'];
    if (this.id) {
      this.authService.onLoading$.next(true);
      this.dbConfigService.getDatasourceById(this.id).subscribe((data) => {
        this.authService.onLoading$.next(false);
        this.serverName = data.serverName;
        this.url = data.url;
        this.username = data.username;
        this.password = data.password;
        this.maxPoolSize = data.maxPoolSize;
        this.status = data.status;
        this.idleTimeout = data.idleTimeout;
        this.driverClassName = data.driverClassName;
        this.isPending = data.isPending;
      });
    }
  }

  goBack() {
    window.history.back();
  }
}
