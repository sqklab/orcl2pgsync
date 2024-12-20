import {Component, OnInit, ViewChild} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ConnectorType } from 'src/app/libs/constants';
import { AuthService } from 'src/app/libs/services/auth.service';
import { ConnectorService } from 'src/app/libs/services/connectors.service';
import { prettyPrintJson } from 'pretty-print-json';
import { SweetAlert } from 'src/app/libs/utils/sweetalert';
import {JsonEditorComponent, JsonEditorOptions} from "@maaxgr/ang-jsoneditor";

const init = {
  name: "connector_name",
  config: {

  }
}

@Component({
  selector: 'app-insert-or-update-connector',
  templateUrl: './insert-or-update-connector.component.html',
  styleUrls: ['./insert-or-update-connector.component.scss']
})
export class InsertOrUpdateConnectorComponent implements OnInit {

  serverName = '';
  url = '';
  username = '';
  password = '';
  maxPoolSize = 10;
  idleTimeout = 30000;
  status = 'ACTIVE';
  driverClassName = '';

  id;
  isEdit;
  title = 'Add new Connector';

  jsonConfig: any;
  configType = 'JSON';

  errorMsg = '';

  jsonPostgresExample = '{"name":"connector_name","config":{"connector.class":"io.debezium.connector.postgresql.PostgresConnector","database.user":"","database.dbname":"","datatype.propagate.source.type":".+\\\\.BYTEA","slot.name":"slot_name","time.precision.mode":"connect","database.server.name":"","schema.include.list":"","heartbeat.interval.ms":"3000","database.port":"5432","plugin.name":"pgoutput","tombstones.on.delete":"false","decimal.handling.mode":"double","binary.handling.mode":"hex","database.hostname":"","database.password":"","name":"connector_name","table.include.list":"","snapshot.mode":"never"}}';
  jsonOracleExample = '{"name":"oracle_connector_1","config":{"connector.class":"io.debezium.connector.oracle.OracleConnector","database.hostname":"","database.port":1541,"database.user":"","database.password":"","database.dbname":"","database.server.name":"","table.include.list":"","tasks.max":1,"database.history.kafka.bootstrap.servers":"localhost:9092","database.history.kafka.topic":"schema-changes.oracle_connector_1","log.mining.strategy":"online_catalog","snapshot.mode":"schema_only","decimal.handling.mode":"double","time.precision.mode":"connect","tombstones.on.delete":"false","lob.enabled":false}}';

  testing;

  public editorOptions: JsonEditorOptions;
  public originJsonConfig: any;
  @ViewChild(JsonEditorComponent, { static: false }) editor!: JsonEditorComponent;

  constructor(
    private authService: AuthService,
    private connectorAPI: ConnectorService,
    private router: Router,
    private modalService: NgbModal,
    private activeRouter: ActivatedRoute
  ) {
    this.editorOptions = new JsonEditorOptions()
    this.editorOptions.modes = ['text', 'tree', 'code'];
    this.editorOptions.mode = 'code';
    this.editorOptions.history = false;
    this.editorOptions.enableSort = false;
    this.editorOptions.search = false;
    this.editorOptions.sortObjectKeys = false;
    this.editorOptions.enableTransform = false;

    this.jsonConfig = {};
    this.originJsonConfig = {};
  }

  ngOnInit(): void {
    this.isEdit = this.activeRouter.snapshot.queryParams.isFromEdit;
    if (this.isEdit) {
      this.title = 'Edit Connector';
      this.getDbConfigById();
    } else {
      this.originJsonConfig = init;
      this.jsonConfig = init;
    }
  }

  public get ConnectorType(): any {
    return ConnectorType;
  }

  changeType(type: string): void {
    this.configType = type;
  }

  onChangeJson(d: Event) {
    // Fix wrong event
    if(d.type && d.currentTarget){
      return
    }

    if (this.editor.isValidJson()) {
      this.errorMsg = null;
    }
    this.jsonConfig = d;
  }

  private isValidJson(json: string): boolean {
    const arr = Object.keys(json);
    return arr.length > 0 && this.editor.isValidJson();
  }

  upSert(): void {
    this.errorMsg = '';
    if (!this.isValidJson(this.jsonConfig)) {
      this.errorMsg = 'Invalid connector configuration';
      return;
    }
    this.authService.onLoading$.next(true);
    if (this.isEdit) {
      this.connectorAPI.update(JSON.stringify(this.jsonConfig), this.id).subscribe(data => {
        this.authService.onLoading$.next(false);
        if (data.status === 200) {
          SweetAlert.notifyMessage('Update success!');
          this.router.navigate(['/db-connector']);
        } else {
          SweetAlert.notifyMessage('Update error!', 'error');
        }
      }, (err) => {
        this.authService.onLoading$.next(false);
      });
    } else {
      if (this.configType === 'JSON') {
        if (!this.jsonConfig.name || !this.jsonConfig.config) {
          this.errorMsg = 'Connector configuration is invalid. name and config field are required.';
          this.authService.onLoading$.next(false);
          return;
        }
        this.connectorAPI.create(JSON.stringify(this.jsonConfig)).subscribe(data => {
          this.authService.onLoading$.next(false);
          if (data && data.status === 201) {
            this.router.navigate(['/db-connector']);
            SweetAlert.notifyMessage('Create success!');
          } else if (data.status === 400) {
            this.errorMsg = data.message;
            SweetAlert.notifyMessage('Create error!', 'error');
          } else {
            const item = JSON.parse(data.message);
            this.errorMsg = item['message'];
            SweetAlert.notifyMessage('Create error!', 'error');
          }
        }, err => {
          this.authService.onLoading$.next(false);
          SweetAlert.notifyMessage('Create error!', 'error');
        });
      } else {
        this.connectorAPI.create(JSON.stringify(this.jsonConfig)).subscribe(data => {
          this.authService.onLoading$.next(false);
          if (data && data.status === 201) {
            this.router.navigate(['/db-connector']);
            SweetAlert.notifyMessage('Create success!');
          } else {
            this.errorMsg = data.message;
            SweetAlert.notifyMessage('Create error!', 'error');
          }
        });
      }
    }
  }

  getDbConfigById(): void {
    this.id = this.activeRouter.snapshot.queryParams.id;
    if (this.id) {
      this.authService.onLoading$.next(true);
      this.connectorAPI.getById(this.id).subscribe((data) => {
        this.authService.onLoading$.next(false);
        if (data && data.status === 200) {
          this.originJsonConfig = data.body;
          this.jsonConfig = data.body;
        }
      });
    }
  }

  openJsonOracleExample(content): void {
    this.modalService.open(content, { size: 'xl' }).shown.subscribe(
      (result) => {
        const data = JSON.parse(this.jsonOracleExample);
        document.getElementById('connector-jsonOracleExample-id').innerHTML = prettyPrintJson.toHtml(
          data,
          {
            indent: 2,
            linkUrls: true,
            quoteKeys: true,
          }
        );
      },
      (reason) => {
        this.onCloseModal();
      }
    );
  }

  openJsonPostgresExample(content): void {
    this.modalService.open(content, { size: 'xl' }).shown.subscribe(
      (result) => {
        const data = JSON.parse(this.jsonPostgresExample);
        document.getElementById('connector-jsonPostgresExample-id').innerHTML = prettyPrintJson.toHtml(
          data,
          {
            indent: 2,
            linkUrls: true,
            quoteKeys: true,
          }
        );
      },
      (reason) => {
        this.onCloseModal();
      }
    );
  }

  onCloseModal(): void {
    this.modalService.dismissAll();
  }
  goBack() {
    window.history.back();
  }
}
