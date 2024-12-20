import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {BaseService} from './base.service';
import {CommonResponse, SourceRecordInfoDto} from './interface';
import StorageService from './storage.service';

@Injectable({
  providedIn: 'root',
})
export class ConnectorService extends BaseService {
  constructor(public storage: StorageService, httpClient: HttpClient) {
    super(httpClient);
  }

  revert(revertId, currentId): Observable<CommonResponse<any[]>> {
    return this.httpClient.get<CommonResponse<any[]>>(
      `/connectors/revert?revertId=${revertId}&currentId=${currentId}`
    );
  }

  history(id, name, type): Observable<CommonResponse<any[]>> {
    return this.httpClient.post<CommonResponse<any[]>>(
      `/connectors/history`, {id, name, type}
    );
  }

  update(config: string, id): Observable<CommonResponse<number>> {
    return this.httpClient.post<CommonResponse<number>>(
      `/connectors/update?id=${id}`, config
    );
  }

  getById(id): Observable<CommonResponse<number>> {
    return this.httpClient.get<CommonResponse<number>>(
      `/connectors/getById?id=${id}`
    );
  }

  synchronize(): Observable<CommonResponse<SourceRecordInfoDto[]>> {
    return this.httpClient.post<CommonResponse<SourceRecordInfoDto[]>>(
      `/connectors/synchronize`, {}
    );
  }

  create(body): Observable<CommonResponse<number>> {
    return this.httpClient.post<CommonResponse<number>>(
      `/connectors/create`, body
    );
  }

  // createByJson(body): Observable<CommonResponse<number>> {
  //   return this.httpClient.post<CommonResponse<number>>(
  //     `/connectors/create`, body
  //   );
  // }

  pause(id): Observable<CommonResponse<number>> {
    return this.httpClient.post<CommonResponse<number>>(
      `/connectors/pause?id=${id}`, {}
    );
  }

  resume(id): Observable<CommonResponse<number>> {
    return this.httpClient.post<CommonResponse<number>>(
      `/connectors/resume?id=${id}`, {}
    );
  }

  restart(id, taskId): Observable<CommonResponse<number>> {
    return this.httpClient.post<CommonResponse<number>>(
      `/connectors/restart?id=${id}&taskId=${taskId}`, {}
    );
  }

  deleteHistory(ids = []): Observable<CommonResponse<string>> {
    return this.httpClient.post<CommonResponse<string>>(
      `/connectors/deleteHistory`,  ids
    );
  }

  delete(id, isDeleteKafkaConnectorAlso: boolean): Observable<CommonResponse<string>> {
    return this.httpClient.post<CommonResponse<string>>(
      `/connectors/delete?id=${id}&deleteKafkaConnectorAlso=${isDeleteKafkaConnectorAlso}`, {}
    );
  }

  deleteTopicName(topicName): Observable<CommonResponse<string>> {
    return this.httpClient.post<CommonResponse<string>>(
      `/connectors/deleteTopicName?topicName=${topicName}`, {}
    );
  }

  stopAll(): Observable<CommonResponse<string>> {
    return this.httpClient.post<CommonResponse<string>>(
      `/connectors/stopAll`, {}
    );
  }

  startAll(): Observable<CommonResponse<string>> {
    return this.httpClient.post<CommonResponse<string>>(
      `/connectors/startAll`, {}
    );
  }

  restartAll(): Observable<CommonResponse<string[]>> {
    return this.httpClient.post<CommonResponse<string[]>>(
      `/connectors/restartAll`, {}
    );
  }

  refresh(): Observable<CommonResponse<SourceRecordInfoDto[]>> {
    return this.httpClient.get<CommonResponse<SourceRecordInfoDto[]>>(
      `/connectors/refresh`
    );
  }

}
