import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ConnectorService} from "../../libs/services/connectors.service";
import {SourceRecordInfoDto} from "../../libs/services/interface";
import {ConnectorJsonComponent} from "../connector-json/connector-json.component";
import {SweetAlert} from "../../libs/utils/sweetalert";
import {AuthService} from "../../libs/services/auth.service";

@Component({
  selector: 'app-connector-history',
  templateUrl: './connector-history.component.html',
  styleUrls: ['./connector-history.component.scss']
})
export class ConnectorHistoryComponent implements OnInit {
  @Input() connector: SourceRecordInfoDto;
  @Output() revertDone = new EventEmitter<boolean>(false);

  list = [];
  selected = [];
  errorMsg;

  constructor(
    public activeModal: NgbActiveModal,
    private dbConnectorService: ConnectorService,
    private modalService: NgbModal,
    private authService: AuthService
  ) {
  }

  ngOnInit(): void {
    this.viewHistory(this.connector);
  }

  viewHistory(connector: SourceRecordInfoDto) {
    this.dbConnectorService.history(connector.id, connector.info.name, connector.info.config['connector.class'])
      .subscribe(
        res => {
          if (res.status == 200) {
            this.list = res.body;
          }
        },
        error => {

        })
  }

  getCheckAllItem() {
    return this.selected.length == this.list.length;
  }

  selectAllItems(event) {
    if (event.target.checked) {
      this.list.forEach((item) => {
        this.selected.push(Number(item.id));
        item.checked = true;
      });
    } else {
      this.list.forEach((item) => {
        item.checked = false;
      });
      this.selected = [];
    }
  }

  selectItem(event, syncInfo): void {
    syncInfo.checked = event.target.checked;
    if (event.target.checked) {
      this.selected.push(Number(syncInfo.id));
    } else {
      this.selected = this.selected.filter(
        (item) => item !== Number(syncInfo.id)
      );
    }
  }

  openConfg(connector): void {
    const modalRef = this.modalService.open(ConnectorJsonComponent, {
      size: 'lg',
      scrollable: true
    });
    modalRef.componentInstance.json = connector;
  }

  revert(revertId, currentId) {
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Confirmation',
      message: 'Are you sure to revert connector config ?',
      confirm: () => {
        this.authService.onLoading$.next(true);
        this.dbConnectorService.revert(revertId, currentId)
          .subscribe(
            (_) => {
              SweetAlert.notifyMessage('Revert connector config successfully');
              this.activeModal.dismiss({revert: true});
            },
            (err) => {
              SweetAlert.notifyMessage('Fail to revert connector config', 'error');
            }, () => {
              this.authService.onLoading$.next(false);
            }
          );
      },
      type: 'info',
      cancel: true,
    });
  }


  delete(): void {
    const ids = this.selected;
    if (ids.length == 0) {
      return;
    }
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Confirmation',
      message: 'Are you sure to DELETE?',
      confirm: () => {
        this.authService.onLoading$.next(true);
        this.dbConnectorService
          .deleteHistory(ids)
          .subscribe(
            (res) => {
              if (res && res.status === 200) {
                SweetAlert.notifyMessage('Delete success');
                this.viewHistory(this.connector);
              } else {
                SweetAlert.notifyMessage('Delete error', 'error');
              }
            },
            (err) => {

            },
            () => {
              console.log('complete')
              this.authService.onLoading$.next(false);
            }
          );
      },
      type: 'info',
      cancel: true,
    });
  }
}
