import {Pipe, PipeTransform} from '@angular/core';
import {prettyPrintJson} from "pretty-print-json";


@Pipe({
  name: 'jsonViewer',
})
export class JsonViewerPipe implements PipeTransform {
  transform(value: any): string {
    if (!value) {
      return '';
    }

    const data = JSON.parse(JSON.stringify(value));
    return prettyPrintJson.toHtml(
      data,
      {
        indent: 2,
        linkUrls: true,
        quoteKeys: true,
      }
    );
  }
}
