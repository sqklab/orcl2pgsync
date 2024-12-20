import {Pipe, PipeTransform} from '@angular/core';
import {prettyPrintJson} from "pretty-print-json";


@Pipe({
  name: 'jsonViewerV2',
})
export class JsonViewerV2Pipe implements PipeTransform {
  transform(value: any): string {
    if (!value) {
      return '';
    }

    return prettyPrintJson.toHtml(
      value,
      {
        indent: 2,
        linkUrls: true,
        quoteKeys: true,
      }
    );
  }
}
