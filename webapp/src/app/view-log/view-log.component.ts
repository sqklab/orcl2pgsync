import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { saveAs } from 'file-saver';
import _ from 'lodash';
import { AuthService } from 'src/app/libs/services/auth.service';
import { LogFileInfo } from 'src/app/libs/services/interface';
import { SyncService } from 'src/app/libs/services/syncDocument.service';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-view-log',
  templateUrl: './view-log.component.html',
  styleUrls: ['./view-log.component.scss'],
})
export class ViewLogComponent implements OnInit, OnDestroy {
  topicName = '';
  logs: LogFileInfo[] = [];
  websocket: WebSocket;
  logFileName = '';
  messages = [];
  currentPage = 0;
  totalPage = 0;
  pageSize = 20;
  title = 'View log';

  paging: LogFileInfo[][] = [];

  constructor(
    private router: Router,
    private syncService: SyncService,
    private activeRouter: ActivatedRoute,
    private authService: AuthService,
    private modalService: NgbModal
  ) {}

  ngOnInit(): void {
    this.topicName = this.activeRouter.snapshot.queryParams['topicName'];
    if (this.topicName) {
      this.authService.onLoading$.next(true);
      this.syncService.getFileLogs(this.topicName).subscribe((data) => {
        if (data) {
          data.forEach((item, index) => {
            item.size = this.convertSize(item.length);
            item.index = index + 1;
            if (item.name.lastIndexOf('.log') > 0) {
              item.isLogFile = true;
            } else {
              item.isLogFile = false;
            }
          });

          this.paging = _.chunk(data, this.pageSize);
          this.totalPage = this.paging.length;
          this.logs = this.paging[this.currentPage];
          this.authService.onLoading$.next(false);
        }
      });
    }
  }

  ngOnDestroy(): void {
    if (this.websocket) {
      this.websocket.close();
    }
  }

  goBack(): void {
    window.history.back();
  }

  handleChangePage(page: number): void {
    this.currentPage = page;
    this.logs = this.paging[this.currentPage];
  }

  private convertSize(len: number): any {
    if (len < 1024) {
      return len + ' (bytes)';
    } else if (len < 1024 * 1024) {
      return Number(len / 1024).toFixed(2) + ' (kb)';
    } else {
      return Number(len / (1024 * 1024)).toFixed(2) + ' (mb)';
    }
  }

  downloadFile(path, fileName): void {
    this.authService.onLoading$.next(true);
    this.syncService.downloadFile(path).subscribe((response) => {
      this.authService.onLoading$.next(false);
      saveAs(response.body, fileName);
    });
  }

  tailLog(logFileName: string): void {
    const isProduction = `${environment.production}` === 'true';
    let url;
    if (isProduction) {
      url = new URL(`${environment.socketUrl}${logFileName}`, location.href);
      url.protocol = 'ws';
    } else {
      url = `${environment.socketUrl}${logFileName}`;
    }
    this.websocket = new WebSocket(url);
    this.websocket.onmessage = (event) => {
      this.messages.push(event.data);
    };
  }

  open(content, logFileName: string): void {
    // this.logFileName = logFileName;
    // this.tailLog(logFileName);
    // const config: NgbModalOptions = {};
    // config.size = 'xl';
    // config.backdrop = false;

    // this.modalService.open(content, config).result.then(
    //   (result) => {},
    //   (reason) => {
    //     this.onCloseSetting();
    //     this.authService.onLoading$.next(false);
    //   }
    // );

    // this.viewInternalLog();
    this.viewExternalLog(logFileName);
  }

  private viewExternalLog(logFileName: string): void {
    const baseUrl = environment.baseUrl + `/logs/log?path=${logFileName}`;
    window.open(baseUrl, '_blank');
  }

  private viewInternalLog(logFileName: string): void {
    const newRelativeUrl = this.router.createUrlTree([`/synchronize/log`]);
    const baseUrl = window.location.href.replace(this.router.url, '');
    window.open(baseUrl + newRelativeUrl, '_blank');
  }

  onCloseSetting(): void {
    if (this.websocket) {
      this.websocket.close();
    }
    this.messages = [];
    this.modalService.dismissAll();
    this.authService.onLoading$.next(false);
  }
}
