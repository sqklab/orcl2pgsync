import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { Subject } from 'rxjs';
import { AuthService } from '../libs/services/auth.service';
import { DBConfigService } from '../libs/services/dbconfig.service';
import { DataSourceInfoDTO } from '../libs/services/interface';
import { SweetAlert } from '../libs/utils/sweetalert';

@Component({
  selector: 'app-dbconfig',
  templateUrl: './dbconfig.component.html',
  styleUrls: ['./dbconfig.component.scss'],
})
export class DBConfigComponent implements OnInit {
  datasources: DataSourceInfoDTO[] = [];

  selected: number[] = [];
  totalPage;
  currentPage = 1;
  pageSize = 20;
  keyValue = '';
  dataSuggests: any[] = [];
  isSearching: boolean;

  dateFrom: NgbDateStruct;
  dateTo: NgbDateStruct;
  model: NgbDateStruct;

  focus$ = new Subject<string>();
  click$ = new Subject<string>();

  topicNames = [];

  constructor(
    private authService: AuthService,
    private dbConfigService: DBConfigService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDBConfigs();
  }

  addNew() {
    this.router.navigate(['/dbconfig/add']);
  }

  handleChangePageSize(e: number): void {
    this.pageSize = e;
    this.loadDBConfigs();
  }

  handleChangePage(page: number): void {
    this.currentPage = page;
    this.loadDBConfigs();
  }

  loadDBConfigs(): void {
    this.totalPage = 0;
    this.authService.onLoading$.next(true);
    this.dbConfigService
      .getDBConfigs(this.currentPage, this.pageSize)
      .subscribe(
        (res) => {
          this.authService.onLoading$.next(false);
          if (res) {
            this.datasources = res.dataSourceDescriptions;
            if (this.selected.length > 0) {
              this.datasources.forEach((item) => {
                item.checked = this.selected.includes(item.id);
              });
            }
            this.totalPage = res.totalPage;
          }
        },
        (err) => {
          this.authService.onLoading$.next(false);
        }
      );
  }

  dbclickToEdit(datasource: DataSourceInfoDTO) {
    if (datasource.status !== 'IN_USE') {
      this.router.navigate(['/dbconfig/edit'], {
        queryParams: { id: datasource.id, isEdit: true },
      });
    } else {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Infomation',
        message: 'Cannot edit IN-USE Data Source!',
        type: 'info',
        confirm: () => {},
      });
    }
  }

  testConnection(event, doc: DataSourceInfoDTO) {
    doc.testing = true;
    const body = {
      serverName: doc.serverName,
      url: doc.url,
      username: doc.username,
      password: doc.password,
      maxPoolSize: doc.maxPoolSize,
      idleTimeout: doc.idleTimeout,
      status: doc.status,
      driverClassName: doc.driverClassName,
      isPending: doc.isPending,
    } as DataSourceInfoDTO;
    event.stopPropagation();
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
        doc.testing = false;
      },
      () => {
        doc.testing = false;
      }
    );
  }

  edit() {
    if (this.selected.length === 1) {
      const pos = this.datasources.findIndex((x) => x.id === this.selected[0]);
      if (this.datasources[pos].status !== 'IN_USE') {
        this.authService.onShowModal$.next({
          isShow: true,
          title: 'Infomation',
          message: 'Are you sure to EDIT this Data Source?',
          confirm: () => {
            this.router.navigate(['/dbconfig/edit'], {
              queryParams: { id: this.selected[0], isEdit: true },
            });
          },
          type: 'info',
          cancel: true,
        });
      } else {
        this.authService.onShowModal$.next({
          isShow: true,
          title: 'Infomation',
          message: 'Cannot edit IN-USE Data Source!',
          type: 'info',
          confirm: () => {},
        });
      }
    } else {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Infomation',
        message: 'You should select only one Data Source to EDIT',
        type: 'info',
        confirm: () => {},
      });
    }
  }

  delete() {
    if (this.selected.length === 1) {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Infomation',
        message: 'Are you sure to DELETE this Data Source?',
        confirm: () => {
          this.dbConfigService
            .deleteDatasource(this.selected)
            .subscribe((res) => {
              if (res) {
                this.loadDBConfigs();
                this.selected = [];
                SweetAlert.notifyMessage('Deleted Data Source Successfully!');
              } else {
                this.selected = [];
                SweetAlert.notifyMessage(
                  'Cannot delete IN-USE Datasource',
                  'error'
                );
              }
            });
        },
        type: 'info',
        cancel: true,
      });
    } else {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Infomation',
        message: 'You should select one Data Source to DELETE',
        type: 'info',
        confirm: () => {},
      });
    }
  }

  selectItem(event, id: number): void {
    if (event.target.checked) {
      this.selected.push(id);
    } else {
      this.selected = this.selected.filter((item) => item !== id);
    }
  }
}
