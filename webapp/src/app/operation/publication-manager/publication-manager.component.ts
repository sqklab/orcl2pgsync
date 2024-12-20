import {AuthService} from '../../libs/services/auth.service';
import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {fromEvent} from 'rxjs';
import {debounceTime, distinctUntilChanged, map} from 'rxjs/operators';
import {DBConfigService} from 'src/app/libs/services/dbconfig.service';
import {SweetAlert} from 'src/app/libs/utils/sweetalert';
import {PublicationInfo} from '../../libs/services/interface';
import {PublicationService} from '../../libs/services/publication.service';
import {PublicationPopupAddingComponent} from './publication-popup-adding/publication-popup-adding.component';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

export interface DataPupup {
  db: string;
  pubname: string;
  tables: string;
}

@Component({
  selector: 'app-publication-manager',
  templateUrl: './publication-manager.component.html',
  styleUrls: ['./publication-manager.component.scss']
})
export class PublicationManagerComponent implements OnInit {
  dbName = [];

  publications: string[] = [];

  selectedDB = '';

  selectedPublication = '';

  selected: string[] = [];

  pageSize = 20;
  pageNo = 1;
  total = 0;

  tables: PublicationInfo[] = []

  disableSelectPublication = true;

  tableSearch = '';

  @ViewChild('SearchInput', { static: true }) searchInput: ElementRef;

  constructor(private publicationService: PublicationService, private dbConfig: DBConfigService,
              private dialog: MatDialog,
              private authService: AuthService,
              private modalService: NgbModal) { }

  ngOnInit(): void {
    this.loadDBConfigs();
    this.registerSearchingEvent();
  }

  private loadDBConfigs(): void {
    this.dbConfig
      .getDBConfigs(1, 1000)
      .subscribe(
        (res) => {
          if (res) {
            this.dbName = res.dataSourceDescriptions.map(x => x.serverName);
          }
        }
      );
  }

  handleSelectDB(event) {
    this.selectedDB = event;
    if (this.selectedDB.length === 0) {
      return;
    }
    this.disableSelectPublication = true;
    this.publicationService.getPublications(this.selectedDB).subscribe(res => {
      if (res && res.status === 200) {
        this.disableSelectPublication = false;
        this.publications = res.body;
      } else {
        SweetAlert.notifyMessage(res.message, 'error');
      }
    });
  }

  handleSelectPublication(event) {
    this.selectedPublication = event;
    if (this.selectedPublication.length === 0 || !this.selectedDB) {
      return;
    }
    this.onSearch();
  }

  private onSearch() {
    this.publicationService.getPublicationTableByPublication(this.selectedDB, this.selectedPublication, this.tableSearch, this.pageNo, this.pageSize).subscribe(res => {
      if (res && res.status === 200) {
        this.tables = res.body.publicationInfoList;
        this.total = Math.ceil(res.body.total / this.pageSize);
      } else {
        SweetAlert.notifyMessage(res.message, 'error');
      }
    });
  }

  private registerSearchingEvent(): void {
    fromEvent(this.searchInput.nativeElement, 'keyup').pipe(
      map((event: any) => {
        return event.target.value;
      })
      , debounceTime(300)
      , distinctUntilChanged()
    ).subscribe((text: string) => {
      if (this.selectedDB && this.selectedPublication) {
        this.onSearch();
      }
    });
  }


  handleChangePage(page: number): void {
    this.pageNo = page;
    this.onSearch();
  }

  getCheckAllItem(): boolean {
    return this.selected.length > 0;
  }

  selectItemAllItems(event): void {
    this.tables.forEach(x => x.checked = event.target.checked)
    if (event.target.checked) {
      this.selected = this.tables.map(x => this.buildTable(x.schemaName,x.table));
    } else {
      this.selected = [];
    }
  }

  selectItem(event, doc: PublicationInfo): void {
    doc.checked = event.target.checked;
    if (event.target.checked) {
      const table = this.buildTable(doc.schemaName,doc.table);
      this.selected.push(table);
    } else {
      this.selected = this.selected.filter(
        (item) => item !== this.buildTable(doc.schemaName,doc.table)
      );
    }
  }

  private buildTable(schema, table) {
    return schema + '.' + table;
  }

  onDelete() {
    if (!this.selectedDB) {
      SweetAlert.notifyMessage('You have not select DB', 'error');
      return;
    }

    if (!this.selectedPublication) {
      SweetAlert.notifyMessage('You have not select Publication', 'error');
      return;
    }

    if (this.selected.length === 0) {
      SweetAlert.notifyMessage('You have not select any record to delete', 'error');
      return;
    }
    this.authService.onShowModal$.next({
      isShow: true,
      title: 'Information',
      message: 'Are you sure to Delete ?',
      confirm: () => {
        this.authService.onLoading$.next(true);
        const deleteType = 2;
        // append schema to table
        this.publicationService.alterPublication(this.selectedDB, this.selectedPublication, this.selected.join(','), deleteType).subscribe(res => {
          if (res && res.status === 200) {
            SweetAlert.notifyMessage('Alter publication success!');
            this.onSearch();
          } else {
            SweetAlert.notifyMessage(res.message, 'error');
          }
          this.authService.onLoading$.next(false);
        });
      },
      type: 'info',
      cancel: true,
    });
  }

  onAdding(result: DataPupup) {
    if (result.db.length === 0 || result.pubname.length === 0 || result.tables.length === 0) {
      return;
    }
    const addType = 1;
    this.publicationService.alterPublication(result.db, result.pubname, result.tables, addType).subscribe(res => {
      if (res && res.status === 200) {
        SweetAlert.notifyMessage('Alter publication success!');
        this.selectedDB = result.db;
        this.selectedPublication = result.pubname;
        this.onSearch();
      } else {
        SweetAlert.notifyMessage(res.message, 'error');
      }
    });
  }

  showPopupAdding(): void {
    const modalRef = this.modalService.open(PublicationPopupAddingComponent, {
      size: 'lg',
      scrollable: true
    });
    modalRef.componentInstance.popupDbName = this.dbName;
    modalRef.componentInstance.popupPublications = this.publications;

    modalRef.result.then(
      (result) => {
        console.log('result', result);
        this.onAdding(result);
      },
      (reason) => {
        console.log('reason', reason);
      })
  }
}
