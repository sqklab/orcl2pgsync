import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import * as copy from 'copy-to-clipboard';
import { prettyPrintJson } from 'pretty-print-json';
import { SyncErrorInfo } from 'src/app/libs/services/interface';
import { SyncService } from 'src/app/libs/services/syncDocument.service';
import { SweetAlert } from 'src/app/libs/utils/sweetalert';
import { ViewSynchronizerComponent } from '../../view-synchronizer/view-synchronizer.component';

@Component({
  selector: 'app-view-detail-error-log',
  templateUrl: './view-detail-error-log.component.html',
  styleUrls: ['./view-detail-error-log.component.scss'],
})
export class ViewDetailErrorLogComponent implements OnInit {
  syncInfo: SyncErrorInfo;

  constructor(
    private syncService: SyncService,
    private activeRouter: ActivatedRoute,
    private router: Router,
    private modalService: NgbModal
  ) {}

  ngOnInit(): void {
    const info = this.activeRouter.snapshot.queryParams['info'];
    if (!info) {
      // goback synchronize view
      this.router.navigate(['/synchronize']);
    }
    this.syncInfo = JSON.parse(info) as SyncErrorInfo;
    const data = JSON.parse(this.syncInfo.syncMessage);
    document.getElementById('message-kafka').innerHTML = prettyPrintJson.toHtml(
      data,
      {
        indent: 2,
        linkUrls: true,
        quoteKeys: true,
      }
    );
  }

  goBack() {
    this.router.navigate(['/synchronize/view-errors'], {
      queryParams: { topicName: this.syncInfo.topicName },
    });
  }

  resolve(): void {
    this.syncService.resolvedError([this.syncInfo.id]).subscribe((_) => {
      this.goBack();
      SweetAlert.notifyMessage('Resolved Success');
    });
  }

  copy(text): void {
    copy(text);
  }

  copyMsgKafka(): void {
    const data = JSON.parse(JSON.stringify(this.syncInfo.syncMessage));
    copy(data);
  }

  onClickTopic(topic) {
    const modalRef = this.modalService.open(ViewSynchronizerComponent, {
      size: 'xl',
    });
    modalRef.componentInstance.topic = topic;
  }


  goBackHistory() {
    window.history.back();
  }
}
