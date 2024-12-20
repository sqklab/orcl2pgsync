import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {prettyPrintJson} from "pretty-print-json";

@Component({
  selector: 'app-connector-json',
  templateUrl: './connector-json.component.html',
  styleUrls: ['./connector-json.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class ConnectorJsonComponent implements OnInit {
  json;

  constructor(public activeModal: NgbActiveModal) {
  }

  ngOnInit(): void {
    document.getElementById('connector-json-id').innerHTML = prettyPrintJson.toHtml(
      this.json.config,
      {
        indent: 2,
        linkUrls: true,
        quoteKeys: true,
      }
    );
  }

}
