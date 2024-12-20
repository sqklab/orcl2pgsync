import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseService } from './base.service';
import {
  CommonResponse, CurrentScnInfo, LogMnrContentResult, WrapLogMnrContent
} from './interface';
import StorageService from './storage.service';

@Injectable({
  providedIn: 'root',
})
export class RedoLogService extends BaseService {
  constructor(public storage: StorageService, httpClient: HttpClient) {
    super(httpClient);
  }

  getCurrentSCN(db): Observable<CommonResponse<CurrentScnInfo>> {
    return this.httpClient.get<CommonResponse<CurrentScnInfo>>(
      `/redolog/getCurrentSCN?db=${db}`
    );
  }

  getTimestampByScn(db, scn): Observable<CommonResponse<string>> {
    return this.httpClient.get<CommonResponse<string>>(
      `/redolog/getTimestampByScn?db=${db}&scn=${scn}`
    );
  }

  getScnByTimestamp(db, dateTime): Observable<CommonResponse<string>> {
    return this.httpClient.get<CommonResponse<string>>(
      `/redolog/getScnByTimestamp?db=${db}&dateTime=${dateTime}`
    );
  }

  getTablesByDbAndSchema(db, schema): Observable<CommonResponse<string[]>> {
    return this.httpClient.get<CommonResponse<string[]>>(
      `/redolog/getTablesByDbAndSchema?db=${db}&schema=${schema}`
    );
  }

  searchLogMnrContents(db, schema, startScn, endScn, operationTypes, tables, totalPage: number, currentPage, pageSize): Observable<CommonResponse<WrapLogMnrContent>> {
    return this.httpClient.get<CommonResponse<WrapLogMnrContent>>(
      `/redolog/searchLogMnrContents?totalPage=${totalPage}&currentPage=${currentPage}&pageSize=${pageSize}&db=${db}&schema=${schema}&startScn=${startScn}&endScn=${endScn}&operationTypes=${operationTypes}&tables=${tables}`
    );
  }

}
