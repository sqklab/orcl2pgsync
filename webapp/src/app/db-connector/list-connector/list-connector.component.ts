import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {NgbDateStruct, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import * as copy from 'copy-to-clipboard';
import {prettyPrintJson} from 'pretty-print-json';
import {AuthService} from 'src/app/libs/services/auth.service';
import {ConnectorService} from 'src/app/libs/services/connectors.service';
import {SourceRecordInfoDto, Task} from 'src/app/libs/services/interface';
import {SweetAlert} from 'src/app/libs/utils/sweetalert';
import {saveAs} from 'file-saver';
import * as JSZip from "jszip";
import {ConnectorHistoryComponent} from "../connector-history/connector-history.component";

@Component({
  selector: 'app-list-connector',
  templateUrl: './list-connector.component.html',
  styleUrls: ['./list-connector.component.scss']
})
export class ListConnectorComponent implements OnInit {
  connectors: SourceRecordInfoDto[] = [];
  info: any;
  connectorConfigDetail = {} as any;
  connectorTasks: Task[] = [];
  keysConfig = [];
  keysTask = [];

  selected: number[] = [];
  totalPage: number;
  currentPage = 1;
  pageSize = 20;
  keyValue = '';
  dataSuggests: any[] = [];
  isSearching: boolean;

  dateFrom: NgbDateStruct;
  dateTo: NgbDateStruct;
  model: NgbDateStruct;

  active = '1';

  topicNames = [];
  isDeleteKafkaConnectorAlso = false;
  isForceRestartKafkaConnectorAlso = false;

  errorMsg = '';

  private readonly VALUE_HIDE_PWD = '********';

  constructor(
    private authService: AuthService,
    private dbConnectorService: ConnectorService,
    private router: Router,
    private modalService: NgbModal
  ) {
  }

  ngOnInit(): void {
    this.errorMsg = '';
    this.isDeleteKafkaConnectorAlso = false;
    this.loadConnectorConfigs();
  }

  download() {
    const zip = new JSZip();
    this.connectors.forEach(connector => {
      zip.file(connector.info.name + ".json", JSON.stringify(connector));
    })
    zip.generateAsync({type: "blob"})
      .then(function (content) {
        saveAs(content, "connectors-configuration.zip");
      });
  }

  addNew(): void {
    this.router.navigate(['/db-connector/addOrUpdate']);
  }

  stopAll(): void {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Infomation',
      message: 'Are you sure to Stop All ?',
      confirm: () => {
        this.totalPage = 0;
        this.authService.onLoading$.next(true);
        this.dbConnectorService
          .stopAll()
          .subscribe(
            (res) => {
              this.authService.onLoading$.next(false);
              if (res && res.status === 200) {
                this.loadConnectorConfigs();
                SweetAlert.notifyMessage('Stop all success');
              } else {
                SweetAlert.notifyMessage('Stop all error', 'error');
              }
            },
            (err) => {
              this.authService.onLoading$.next(false);
            }
          );
      },
      type: 'info',
      cancel: true,
    });
  }

  restartAll(): void {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Infomation',
      message: 'Are you sure to Restart All ?',
      confirm: () => {
        this.totalPage = 0;
        this.authService.onLoading$.next(true);
        this.dbConnectorService
          .restartAll()
          .subscribe(
            (res) => {
              this.authService.onLoading$.next(false);
              if (res && res.status === 200) {
                this.loadConnectorConfigs();
                SweetAlert.notifyMessage('Restart all success');
              } else {
                SweetAlert.notifyMessage('Restart all error', 'error');
              }
            },
            (err) => {
              this.authService.onLoading$.next(false);
            }
          );
      },
      type: 'info',
      cancel: true,
    });
  }

  viewHistory(connector: SourceRecordInfoDto): void {
    const modalRef = this.modalService.open(ConnectorHistoryComponent, {
      size: 'lg',
      windowClass: 'connector-history',
      scrollable: true
    });
    modalRef.componentInstance.connector = connector;

    modalRef.result.then(
      (result) => {},
      (reason) => {
        if(reason && reason.revert){
          this.loadConnectorConfigs();
        }
      })
  }

  restart(id, state, tasks: any[]): void {
    this.isForceRestartKafkaConnectorAlso = false;
    if ('PAUSED' === state) {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Infomation',
        message: 'You should change to status RUNNING before!',
        type: 'warning',
        confirm: () => {
        },
      });
      return;
    }
    const firstTask = tasks[0];
    if (!firstTask) {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Infomation',
        message: 'Cant not restart TASK!',
        type: 'warning',
        confirm: () => {
        },
      });
      return;
    }

    let taskId = firstTask['id'];
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Infomation',
      isShowCheckBox: true,
      titleOfCheckBox: 'Force restart connector ?',
      message: 'Are you sure to Restart?',
      confirmDeleteKafkaConnector: (event) => {
        this.isForceRestartKafkaConnectorAlso = event;
      },
      confirm: () => {
        this.totalPage = 0;
        this.authService.onLoading$.next(true);
        if (this.isForceRestartKafkaConnectorAlso) {
          taskId = -1;
        }
        this.dbConnectorService
          .restart(id, taskId)
          .subscribe(
            (res) => {
              this.authService.onLoading$.next(false);
              if (res && (res.status === 202 || res.status === 204)) {
                this.loadConnectorConfigs();
                SweetAlert.notifyMessage('Restart success');
              } else {
                SweetAlert.notifyMessage('Restart error', 'error');
              }
            },
            (err) => {
              this.authService.onLoading$.next(false);
            }
          );
      },
      type: 'info',
      cancel: true,
    });
  }

  startAll(): void {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Infomation',
      message: 'Are you sure to Start All ?',
      confirm: () => {
        this.totalPage = 0;
        this.authService.onLoading$.next(true);
        this.dbConnectorService
          .startAll()
          .subscribe(
            (res) => {
              this.authService.onLoading$.next(false);
              if (res && res.status === 200) {
                this.loadConnectorConfigs();
                SweetAlert.notifyMessage('Start all success');
              } else {
                SweetAlert.notifyMessage('Start all error', 'error');
              }
            },
            (err) => {
              this.authService.onLoading$.next(false);
            }
          );
      },
      type: 'info',
      cancel: true,
    });
  }

  transfromData(key): string {
    if (key === 'database.password' || key === 'db.user.password') {
      return this.VALUE_HIDE_PWD;
    }
    return this.connectorConfigDetail[key];
  }

  open(info: any, tasks: any, content): void {
    this.keysConfig = Object.keys(info.config);
    this.connectorConfigDetail = info.config;
    if (tasks && tasks.tasks) {
      this.connectorTasks = [];
      try {
        const taskArr = Array.from(tasks.tasks);
        taskArr.forEach(x => {
          const taskObj = {
            id: x['id'],
            state: x['state'],
            worker_id: x['worker_id']
          } as Task;
          this.connectorTasks.push(taskObj);
        });
      } catch (error) {
        console.log('err:' + error);
      }
    }

    this.info = JSON.stringify(info);
    this.modalService.open(content, {size: 'xl'}).shown.subscribe(
      (result) => {
        setTimeout(() => {
          this.mountConfigToView();
        }, 100);
      },
      (reason) => {
        this.onCloseViewInfo();
      }
    );
  }

  mountConfigToView() {
    const data = JSON.parse(this.info);
    const cloneData = Object.assign({}, data);
    const classConfig = cloneData['config'];
    if (classConfig) {
      let pwdKey = '';
      Object.keys(classConfig).forEach(k => {
        if (k.indexOf('password') >= 0) {
          pwdKey = k;
        }
      });
      classConfig[pwdKey] = this.VALUE_HIDE_PWD;
    }
    document.getElementById('connector-info-id').innerHTML = prettyPrintJson.toHtml(
      cloneData,
      {
        indent: 2,
        linkUrls: true,
        quoteKeys: true,
      }
    );
  }

  onNavChange(event) {
    this.active = '1';
    setTimeout(() => {
      this.mountConfigToView();
    }, 200);
  }

  onCloseViewInfo(): void {
    this.modalService.dismissAll();
  }

  handleChangePageSize(e: number): void {
    this.pageSize = e;
    this.loadConnectorConfigs();
  }

  handleChangePage(page: number): void {
    this.currentPage = page;
    this.loadConnectorConfigs();
  }

  loadConnectorConfigs(): void {
    this.refresh();
  }

  stopOrResume(id, state): void {
    if (state === 'PAUSED') { // -> resume
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Infomation',
        message: 'Are you sure to Resume?',
        confirm: () => {
          this.authService.onLoading$.next(true);
          this.dbConnectorService
            .resume(id)
            .subscribe(
              (res) => {
                this.authService.onLoading$.next(false);
                if (res && res.status === 202) { // acepted!
                  setTimeout(() => {
                    this.loadConnectorConfigs();
                    SweetAlert.notifyMessage('Resume success');
                  }, 1000);
                } else {
                  SweetAlert.notifyMessage('Resume error', 'error');
                }
              },
              (err) => {
                this.authService.onLoading$.next(false);
              }
            );
        },
        type: 'info',
        cancel: true,
      });
    } else { // running -> stop
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Infomation',
        message: 'Are you sure to Stop?',
        confirm: () => {
          this.authService.onLoading$.next(true);
          this.dbConnectorService
            .pause(id)
            .subscribe(
              (res) => {
                this.authService.onLoading$.next(false);
                if (res && res.status === 202) { // acepted!
                  setTimeout(() => {
                    this.loadConnectorConfigs();
                    SweetAlert.notifyMessage('Stop success');
                  }, 1000);
                } else {
                  SweetAlert.notifyMessage('Stop error', 'error');
                }
              },
              (err) => {
                this.authService.onLoading$.next(false);
              }
            );
        },
        type: 'info',
        cancel: true,
      });
    }
  }

  parseJSon(jsons, divID): void {
    if (divID) {
      const element = divID as Element;
      const data = JSON.parse(JSON.stringify(jsons));
      element.innerHTML = prettyPrintJson.toHtml(
        data,
        {
          indent: 2,
          linkUrls: true,
          quoteKeys: true,
        }
      );
    }
  }

  refresh(): void {
    this.errorMsg = '';
    this.authService.onLoading$.next(true);
    this.dbConnectorService.refresh().subscribe(res => {
        this.authService.onLoading$.next(false);
        if (res && res.status === 200) {
          this.connectors = res.body;
          this.connectors.forEach(x => {
            const item = new Map(Object.entries(x.info.config));
            if (item.get('connector.class').indexOf('oracle') >= 0) {
              x.connectorClass = 'ORACLE';
            } else if (item.get('connector.class').indexOf('postgresql') >= 0) {
              x.connectorClass = 'POSTGRES';
            } else {
              x.connectorClass = item.get('connector.class');
            }
          });
          this.connectors.sort((a, b) => a.connectorClass.length - b.connectorClass.length);
        } else if (res.status === 400) {
          this.errorMsg = res.message;
          SweetAlert.notifyMessage('Fetch error!', 'error');
        } else if (res && res.status === 500) {
          SweetAlert.notifyMessage(res.message, 'error');
        }
      },
      (err) => {
        this.authService.onLoading$.next(false);
      }
    );
  }

  synchronize(): void {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Information',
      message: 'Are you sure to Synchronize Connector?',
      confirm: () => {
        this.totalPage = 0;
        this.authService.onLoading$.next(true);
        this.dbConnectorService
          .synchronize()
          .subscribe(
            (res) => {
              this.authService.onLoading$.next(false);
              if (res && res.status === 200) {
                // this.connectors = res.body;
                this.refresh();
              }
            },
            (err) => {
              this.authService.onLoading$.next(false);
            }
          );
      },
      type: 'info',
      cancel: true,
    });
  }

  dbclickToEdit(doc: SourceRecordInfoDto): void {
    this.router.navigate(['/db-connector/addOrUpdate'], {
      queryParams: {id: doc.id, isFromEdit: true}
    });
  }

  edit(): void {
    if (this.selected.length === 1) {
      this.router.navigate(['/db-connector/addOrUpdate'], {
        queryParams: {id: this.selected[0], isFromEdit: true},
      });
    } else {
      this.authService.onShowModal$.next({
        isShow: true,
        title: 'Infomation',
        message: 'You should select one Connector to EDIT',
        type: 'info',
        confirm: () => {
        },
      });
    }
  }

  delete(id): void {
    this.isDeleteKafkaConnectorAlso = false;
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Infomation',
      isShowCheckBox: true,
      titleOfCheckBox: 'Delete Kafka connector config also ?',
      message: 'Are you sure to DELETE?',
      confirmDeleteKafkaConnector: (event) => {
        this.isDeleteKafkaConnectorAlso = event;
      },
      confirm: () => {
        this.authService.onLoading$.next(true);
        this.dbConnectorService
          .delete(id, this.isDeleteKafkaConnectorAlso)
          .subscribe(
            (res) => {
              this.authService.onLoading$.next(false);
              if (res && res.status === 204) {
                this.loadConnectorConfigs();
                SweetAlert.notifyMessage('Delete success');
              } else {
                SweetAlert.notifyMessage('Delete error', 'error');
              }
            },
            (err) => {
              this.authService.onLoading$.next(false);
            }
          );
      },
      type: 'info',
      cancel: true,
    });
  }

  onResetSchema(doc: SourceRecordInfoDto) {
    const schema = doc.info.config['database.history.kafka.topic'];
    if (!schema) {
      SweetAlert.notifyMessage('No schema found!', 'error');
      return;
    }
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Warning',
      message: `This action gives an error (The DB history topic is missing...) when restarting the Connector. Are you sure to Reset Schema ?`,
      confirm: () => {
        this.authService.onLoading$.next(true);
        this.dbConnectorService
          .deleteTopicName(schema)
          .subscribe(
            (res) => {
              this.authService.onLoading$.next(false);
              SweetAlert.notifyMessage('Resete schema success!');
            },
            (err) => {
              this.authService.onLoading$.next(false);
            }
          );
      },
      type: 'info',
      cancel: true,
    });
  }

  hasResetSchema(doc: SourceRecordInfoDto) {
    return doc.info.config['database.history.kafka.topic'];
  }

  copy(text): void {
    copy(text);
  }

  selectItem(event, id: number): void {
    if (event.target.checked) {
      this.selected.push(id);
    } else {
      this.selected = this.selected.filter((item) => item !== id);
    }
  }

}
