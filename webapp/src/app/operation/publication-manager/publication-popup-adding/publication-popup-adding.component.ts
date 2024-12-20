import {Component, OnInit} from '@angular/core';
import {PublicationService} from 'src/app/libs/services/publication.service';
import {SweetAlert} from 'src/app/libs/utils/sweetalert';
import {DataPupup} from '../publication-manager.component';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-publication-popup-adding',
  templateUrl: './publication-popup-adding.component.html',
  styleUrls: ['./publication-popup-adding.component.scss']
})
export class PublicationPopupAddingComponent implements OnInit {
  popupDbName = [];
  popupPublications: string[] = [];
  popupSelectedDB = '';

  dataFilled: DataPupup = {
    db: '',
    pubname: '',
    tables: '',
  };

  error;

  constructor(private publicationService: PublicationService,
              public activeModal: NgbActiveModal) {
  }

  ngOnInit(): void {
  }

  handleSelectDBPopup(event) {
    this.popupSelectedDB = event;
    if (this.popupSelectedDB.length === 0) {
      return;
    }
    this.popupPublications = [];
    this.publicationService.getPublications(this.popupSelectedDB).subscribe(res => {
      if (res && res.status === 200) {
        this.popupPublications = res.body;
      } else {
        SweetAlert.notifyMessage(res.message, 'error');
      }
    });
  }

  save() {
    this.error = null;
    console.log(this.dataFilled);
    if (this.isEmpty(this.dataFilled.db) || this.isEmpty(this.dataFilled.pubname) || this.isEmpty(this.dataFilled.tables)) {
      this.error = 'All fields are required';
      return;
    }

    let pattern = /^[a-zA-Z0-9#)\(,._-s]+$/i;
    let tableValid = this.dataFilled.tables.match(pattern);
    if(!tableValid){
      this.error = 'Split table by comma (,). Pattern: <Schema>.<Table>';
      return;
    }
    this.activeModal.close(this.dataFilled);
  }

  isEmpty(s: string) {
    return !s || s.trim().length === 0;
  }
}
