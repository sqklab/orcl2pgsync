import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {environment} from 'src/environments/environment';
import * as copy from 'copy-to-clipboard';

@Component({
  selector: 'app-full-page-log',
  templateUrl: './full-page-log.component.html',
  styleUrls: ['./full-page-log.component.scss']
})
export class FullPageLogComponent implements OnInit, OnDestroy {
  logFileName;
  messages = [];
  websocket: WebSocket;

  constructor(private activeRouter: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.logFileName = this.activeRouter.snapshot.queryParams['path'];
    this.tailLog(this.logFileName);
  }

  ngOnDestroy(): void {
    if (this.websocket) {
      this.websocket.close();
    }
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

  copy(text): void {
    copy(text);
  }

}
