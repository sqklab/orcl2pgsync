import { HttpClient, HttpHeaders, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SortType } from '../constants';
import { BaseService } from './base.service';
import {
  CommonResponse,  HistoryInfoDto,
  LogFileInfo, PrimaryKeysPartitionInfo,
  SyncErrorCountOperationsDto,
  SyncErrorDto,
  SyncErrorInfo,
  SynchronizationInfo,
  SynchronizationParamsResponse,
  SynInfo,
} from './interface';
import StorageService from './storage.service';
import {retry} from "rxjs/operators";


@Injectable({
  providedIn: 'root',
})
export class SyncService extends BaseService {
  constructor(public storage: StorageService, httpClient: HttpClient) {
    super(httpClient);
  }

  synInfos: SynInfo[] = [];


  updateTopic(body): Observable<CommonResponse<string>> {
    return this.httpClient.post<CommonResponse<string>>(
      '/dbsync/synchronizers/update',
      body
    );
  }

  detectPrimaryKey(topicName, division, sourceDatabase, sourceSchema, sourceTable, targetDatabase, targetSchema, targetTable): Observable<CommonResponse<PrimaryKeysPartitionInfo>> {
    return this.httpClient.post<CommonResponse<PrimaryKeysPartitionInfo>>(
      `/dbsync/synchronizers/detectPrimaryKeys?topicName=${topicName}&division=${division}&sourceDatabase=${sourceDatabase}&sourceSchema=${sourceSchema}&sourceTable=${sourceTable}&targetDatabase=${targetDatabase}&targetSchema=${targetSchema}&targetTable=${targetTable}`,
      {}
    );
  }

  getTopicById(id): Observable<SynchronizationInfo> {
    return this.httpClient.get<SynchronizationInfo>(
      `/dbsync/synchronizers/view/${id}`
    );
  }

  getSynchronizerByTopic(topic): Observable<SynchronizationInfo> {
    return this.httpClient.get<SynchronizationInfo>(
      `/dbsync/synchronizers/viewByTopic/${topic}`
    );
  }

  downloadFile(path): Observable<any> {
    const headers = new HttpHeaders({ '*': '*' });

    return this.httpClient.get<any>(
      `/dbsync/synchronizers/download?filePath=${path}`,
      {
        headers: headers,
        responseType: 'blob' as 'json',
        observe: 'response' as 'response',
      }
    );
  }

  export() {
    // const headers = new HttpHeaders({ '*': '*' });
    const headers = {
      'Content-Length': 'application/json',
      'Accept': '*/*',
      'Cache-Control': 'no-cache',
    };
    return this.httpClient.get<any>(
      `/dbsync/synchronizers/export`,
      {
        headers,
        responseType: 'blob' as 'json',
        observe: 'response' as 'response',
      }
    );
  }

    /**
   * Method is use to download file.
   * @param data - Array Buffer data
   * @param type - type of the document.
   */
   downLoadFile(data: any, type: string) {
      let blob = new Blob([data], { type: type});
      let url = window.URL.createObjectURL(blob);
      let pwa = window.open(url);
      if (!pwa || pwa.closed || typeof pwa.closed == 'undefined') {
          alert( 'Please disable your Pop-up blocker and try again.');
      }
    }

  postFile(files: File[], syncType): Observable<any> {
    const formData: FormData = new FormData();
    for (let file of files) {
      formData.append('files', file);
    }
    // const req = new HttpRequest('POST', `/dbsync/synchronizers/upload?isOverwrite=${isOverwrite}&syncType=${syncType}`, formData, {
    //     reportProgress: true,
    //     responseType: 'text'
    //   });
    const req = new HttpRequest(
      'POST',
      `/dbsync/synchronizers/upload?&syncType=${syncType}`,
      formData
    );
    return this.httpClient.request(req);
  }

  addNew(
    body: SynchronizationInfo
  ): Observable<CommonResponse<SynchronizationInfo>> {
    return this.httpClient.post<CommonResponse<SynchronizationInfo>>(
      '/dbsync/synchronizers/add',
      body
    );
  }

  getTopicNames(): Observable<SynInfo[]> {
    return this.httpClient.get<SynInfo[]>(`/dbsync/synchronizers/topicNames`);
  }

  getFileLogs(topicName): Observable<LogFileInfo[]> {
    return this.httpClient.get<LogFileInfo[]>(
      `/dbsync/synchronizers/file-logs?topicName=${topicName}`
    );
  }

  getErrors(
    topicName: string,
    sortField: string,
    pageSize: number,
    errorType: string,
    pageNumber = 1,
    sortType: SortType,
    errorState: string,
    kidOfErrorType: string,
    dateFrom: string,
    dateTo: string,
    timeFrom: string,
    timeTo: string,
    operationState: string[],
  ): Observable<CommonResponse<SyncErrorDto>> {
    return this.httpClient.get<CommonResponse<SyncErrorDto>>(
      `/dbsync/synchronizers/error?pageNo=${pageNumber}&pageSize=${pageSize}&sortField=${sortField}&topicName=${topicName}&errorType=${errorType}&sortType=${sortType}&errorState=${errorState}&kidOfErrorType=${kidOfErrorType}&dateFrom=${dateFrom}&dateTo=${dateTo}&timeFrom=${timeFrom}&timeTo=${timeTo}&operationState=${operationState}`
    );
  }

  getStateCount(): Observable<SyncErrorInfo[]> {
    return this.httpClient.get<SyncErrorInfo[]>(
      `/dbsync/synchronizers/stateCount`
    );
  }

  getErrorOperations(topicName: string, selectedErrorState: string): Observable<SyncErrorCountOperationsDto> {
    return this.httpClient.get<SyncErrorCountOperationsDto>(
      `/dbsync/synchronizers/error/operationCount?topicName=${topicName}&stateParam=${selectedErrorState}`
    );
  }

  getDivisions(): Observable<string[]> {
    return this.httpClient.get<string[]>(
      `/dbsync/synchronizers/division`
    );
  }
  //
  // exportAllErrorPK(topicName: string, dateFrom: string,timeFrom: string,dateTo: string,timeTo: string ){
  //   return this.httpClient.get<any>(
  //     `/dbsync/synchronizers/error/export?topicName=${topicName}&dateFrom=${dateFrom}&timeFrom=${timeFrom}&dateTo=${dateTo}&timeTo=${timeTo}`
  //   );
  // }

  exportAllErrorPK(topicName: string,
                   dateFrom: string,
                   timeFrom: string,
                   dateTo: string,
                   timeTo: string,
                   errorState: string,
                   errorType: string,
                   operationState: string[] ){
    const headers = {
      'Content-Length': 'application/json',
      'Accept': '*/*',
      'Cache-Control': 'no-cache',
    };
    return this.httpClient.get<any>(
      `/dbsync/synchronizers/error/export?topicName=${topicName}&dateFrom=${dateFrom}&timeFrom=${timeFrom}&dateTo=${dateTo}&timeTo=${timeTo}&errorState=${errorState}&errorType=${errorType}&operationState=${operationState}`,
      {
        headers,
        responseType: 'blob' as 'json',
        observe: 'response' as 'response',
      });
  }



  resolvedError(id: number[]) {
    return this.httpClient.put(
      `/dbsync/synchronizers/error?errorIds=${[id]}`,
      {}
    );
  }

  resolvedAllError(topicName): Observable<CommonResponse<string>> {
    return this.httpClient.put<CommonResponse<string>>(
      `/dbsync/synchronizers/errorAll?topicName=${topicName}`,
      {}
    );
  }

  deleteError(ids: number[]) {
    return this.httpClient.post(
      `/dbsync/synchronizers/error/deleteList?errorIds=${[ids]}`,
      {}
    );
  }

  retryToResolveError(ids: number[], topic: string): Observable<CommonResponse<string>> {
    return this.httpClient.post<CommonResponse<string>>(
      `/dbsync/synchronizers/error/retries?errorIds=${[ids]}&topic=${topic}`,
      {}
    );
  }

  retryAllError(topicName: string): Observable<CommonResponse<string>> {
    return this.httpClient.post<CommonResponse<string>>(
      `/dbsync/synchronizers/error/retriesAll?topicName=${topicName}`,
      {}
    );
  }
  deleteAllError(topicName) {
    return this.httpClient.post(
      `/dbsync/synchronizers/error/deleteAll?topicName=${topicName}`,
      {}
    );
  }
  markProcessing() {
    return this.httpClient.post(
      `/dbsync/synchronizers/error/markProcessing`,
      {}
    );
  }

  reBatchSize(newSize: number) {
    return this.httpClient.post(
      `/dbsync/synchronizers/reBatchsize?newSize=${newSize}`,
      {}
    );
  }

  exportWithConditions(dateFrom: string,
                   dateTo: string,
                   topicName: any[],
                   stateValue?: string,
                   divisionValue?: string,
                   db?: any,
                   schema?: string) {
    const headers = {
      'Content-Length': 'application/json',
      'Accept': '*/*',
      'Cache-Control': 'no-cache',
    };
    return this.httpClient.get<any>(
      `/dbsync/synchronizers/export?topicNames=` +
      `${topicName}&state=${stateValue}&divisionValue=${divisionValue}&db=${db}&schema=${schema}&dateFrom=${dateFrom}&dateTo=${dateTo}`,
      {
        headers,
        responseType: 'blob' as 'json',
        observe: 'response' as 'response',
      }
    );
  }


  filterState(
    sortField: string,
    pageSize: number,
    pageNumber = 1,
    dateFrom: string,
    dateTo: string,
    topicName: any[],
    sortType: SortType,
    stateValue?: string,
    divisionValue?: string,
    db?: any,
    schema?: string
  ): Observable<SynchronizationParamsResponse> {
    return this.httpClient.get<SynchronizationParamsResponse>(
      `/dbsync/synchronizers/topics?pageNo=${pageNumber}&pageSize=${pageSize}&sortField=` +
      `${sortField}&dateFrom=${dateFrom}&dateTo=${dateTo}&sortType=${sortType}&topicNames=` +
      `${topicName}&state=${stateValue}&divisionValue=${divisionValue}&db=${db}&schema=${schema}`
    );
  }

  sortLastReceivedTime(
    pageSize: number,
    pageNumber = 1,
    sortType: SortType,
    dateFrom: string,
    dateTo: string,
    topicName: any[],
    stateValue?: string,
    divisionValue?: string,
    db?: any,
    schema?: string
  ): Observable<SynchronizationParamsResponse> {
    return this.httpClient.get<SynchronizationParamsResponse>(
      `/dbsync/synchronizers/sortLastReceivedTime?pageNo=${pageNumber}&pageSize=${pageSize}&type=${sortType}&dateFrom=${dateFrom}&dateTo=${dateTo}&topicNames=${topicName}&state=${stateValue}&divisionValue=${divisionValue}&db=${db}&schema=${schema}`
    );
  }

  start(topicIds: number[]): Observable<any> {
    return this.httpClient.post<any>('/dbsync/synchronizers/start', topicIds);
  }

  stop(topicIds: number[]): Observable<any> {
    return this.httpClient.post<any>('/dbsync/synchronizers/stop', topicIds);
  }

  updateOffset(topicName: string, synchronizerName: string, dateTime: any): Observable<CommonResponse<string>> {
    return this.httpClient.post<CommonResponse<string>>(
      `/dbsync/synchronizers/resetOffset?topicName=${topicName}&synchronizerName=${synchronizerName}&dateFrom=${dateTime}`, {});
  }
  // getOffsetByDateTime(topicName: string, dateTime: any) {
  //   return this.httpClient.get<CommonResponse<number>>(
  //     `/dbsync/synchronizers/convertDateTimeToOffset?topicName=${topicName}&dateFrom=${dateTime}`);
  // }

  // getCurrentOffsetByTopic(topicName: string): Observable<CommonResponse<string>> {
  //   return this.httpClient.post<CommonResponse<string>>(`/dbsync/synchronizers/getCurrentOffsetByTopic?topicName=${topicName}`, {});
  // }

  getOffsetByDate(topicName: string, dateTime: any): Observable<CommonResponse<string>> {
    return this.httpClient.post<CommonResponse<string>>(
      `/dbsync/synchronizers/getOffsetByDate?topicName=${topicName}&dateFrom=${dateTime}`, {});
  }

  hasAtLeastOneRunning(syncId: any): Observable<boolean> {
      return this.httpClient.get<boolean>(
        `/dbsync/synchronizers/hasAtLeastOneRunning?syncId=${syncId}`);
  }

  startAll(): Observable<any> {
    return this.httpClient.post<any>('/dbsync/synchronizers/startAll', {});
  }

  deleteAll(): Observable<any> {
    return this.httpClient.post<any>('/dbsync/synchronizers/deleteAll', {});
  }

  stopAll(): Observable<any> {
    return this.httpClient.post<any>('/dbsync/synchronizers/stopAll', {});
  }

  delete(topicIds: number[]): Observable<CommonResponse<string>> {
    return this.httpClient.post<CommonResponse<string>>(
      '/dbsync/synchronizers/delete',
      topicIds
    );
  }

  watchHistory(id: number): Observable<HistoryInfoDto> {
    return this.httpClient.get<HistoryInfoDto>(
      `/dbsync/synchronizers/watchHistory?syncId=${id}`, {});
  }
}
