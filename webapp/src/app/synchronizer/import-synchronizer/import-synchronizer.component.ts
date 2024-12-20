import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { SyncType } from 'src/app/libs/services/interface';
import { SyncService } from 'src/app/libs/services/syncDocument.service';
import { SweetAlert } from 'src/app/libs/utils/sweetalert';
import {catchError} from "rxjs/operators";
import {throwError} from "rxjs";
import {saveAs} from "file-saver";
import {AuthService} from "../../libs/services/auth.service";
import {NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";
import {NgbDateCustomParserFormatter} from "../../libs/utils/helper";

@Component({
  selector: 'app-import-synchronizer',
  templateUrl: './import-synchronizer.component.html',
  styleUrls: ['./import-synchronizer.component.scss'],
})
export class ImportSynchronizerComponent implements OnInit {
  selectedFiles: FileList;
  currentFileUpload: File[] = [];
  syncType: SyncType = SyncType.DT;

  dateFrom: NgbDateStruct;
  dateTo: NgbDateStruct;
  selectedTopicName = [];
  selectedState = '';
  selectedDivision = '';
  selectedDB = '';
  selectedSchema = '';
  fileName = 'No file selected';

  disableCancel = false;
  uploaded = false;

  constructor(private syncService: SyncService,
              private authService: AuthService,
              private router: Router,
              private formater: NgbDateCustomParserFormatter) {}

  ngOnInit(): void {}

  selectFile(files: FileList) {
    this.selectedFiles = files;
    this.fileName = this.selectedFiles[0].name;
  }

  upload() {
    if (!this.selectedFiles) {
      return;
    }
    for (let i = 0; i < this.selectedFiles.length; i++) {
      this.currentFileUpload.push(this.selectedFiles.item(i));
    }
    this.disableCancel = true;
    this.uploaded = true;
    const sDateFrom = this.formater.format(this.dateFrom);
    const sDateTo = this.formater.format(this.dateTo);
    this.syncService.exportWithConditions(
      sDateFrom,
      sDateTo,
      this.selectedTopicName,
      this.selectedState,
      this.selectedDivision,
      this.selectedDB,
      this.selectedSchema
    ).pipe(catchError(err => {
        SweetAlert.notifyMessage(err, 'error');
        this.authService.onLoading$.next(false);
        return throwError(err);
      })
    ).subscribe((res: any) => {
      this.authService.onLoading$.next(false);
      var contentDisposition = res.headers.get('content-disposition');
      var filename = contentDisposition.split(';')[1].split('filename')[1].split('=')[1].trim();
      saveAs(res.body, filename);
    });
    this.syncService
      .postFile(this.currentFileUpload, this.syncType)
      .subscribe(
        (response) => {
          this.selectedFiles = null;
          if (response.body && 200 === response.body.status) {
            this.selectedFiles = null;
            this.disableCancel = false;
            SweetAlert.notifyMessage('Import success!');
            this.router.navigate(['/synchronize']);
          } else if (response.body && 400 === response.body.status) {
            this.selectedFiles = null;
            this.disableCancel = false;
            this.fileName = undefined;
            SweetAlert.notifyMessage(response.body.message, 'error');
          }
        },
        (err) => {
          this.selectedFiles = null;
          this.disableCancel = false;
          this.fileName = undefined;
          this.uploaded = false;
          SweetAlert.notifyMessage('Import has an error!', 'error');
        },
        () => {
          this.selectedFiles = null;
          this.disableCancel = false;
          this.fileName = undefined;
          this.uploaded = false;
        }
      );
  }

  selectDomainTable() {
    this.syncType = SyncType.DT;
  }

  selectReadModel() {
    this.syncType = SyncType.RD;
  }

  public get SyncType() {
    return SyncType;
  }
}
