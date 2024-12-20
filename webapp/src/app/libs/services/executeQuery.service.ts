import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseService } from './base.service';
import {
  CommonResponse,
  DBScheduleDto,
  DbScheduleProcedureResponse,
  DbScheduleProcedureResultResponse,
  DBScheduleResultDto,
  ResultSummaryDto,
  ScheduleAllDto,
} from './interface';
import StorageService from './storage.service';

@Injectable({
  providedIn: 'root',
})
export class ExecuteQueryService extends BaseService {
  constructor(public storage: StorageService, httpClient: HttpClient) {
    super(httpClient);
  }

  deleteRegistrationByIds(ids: number[]): Observable<string> {
    return this.httpClient.post<string>(
      `/dbschedule/deleteRegistrationByIds?ids=${[ids]}`,
      {}
    );
  }

  filter(
    sql,
    targetDB,
    targetSchema,
    date,
    table,
    status,
    pageNo,
    pageSize,
    sortField,
    sortType
  ): Observable<DbScheduleProcedureResponse> {
    return this.httpClient.get<DbScheduleProcedureResponse>(
      `/dbschedule/filter?sql=${sql}&targetDB=${targetDB}&targetSchema=${targetSchema}&date=${date}&table=${table}&status=${status}&pageNo=${pageNo}&pageSize=${pageSize}&sortField=${sortField}&sortType=${sortType}`
    );
  }

  filterResult(
    sql,
    targetDB,
    targetSchema,
    date,
    time,
    table,
    status,
    pageNo,
    pageSize,
    sortField,
    sortType
  ): Observable<DbScheduleProcedureResultResponse> {
    return this.httpClient.get<DbScheduleProcedureResultResponse>(
      `/dbschedule/filterResult?sql=${sql}&targetDB=${targetDB}&targetSchema=${targetSchema}&date=${date}&time=${time}&table=${table}&status=${status}&pageNo=${pageNo}&pageSize=${pageSize}&sortField=${sortField}&sortType=${sortType}`
    );
  }

  getScheduleByDate(date): Observable<CommonResponse<string[]>> {
    return this.httpClient.get<CommonResponse<string[]>>(
      `/dbschedule/getScheduleByDate?date=${date}`
    );
  }

  getResultSummary(date, time): Observable<CommonResponse<ResultSummaryDto>> {
    return this.httpClient.get<CommonResponse<ResultSummaryDto>>(
      `/dbschedule/getResultSummary?date=${date}&time=${time}`
    );
  }

  countAll(): Observable<CommonResponse<number>> {
    return this.httpClient.get<CommonResponse<number>>(
      `/dbschedule/countAll`
    );
  }

  deleteResultByDateAndTime(date, time): Observable<string> {
    return this.httpClient.put<string>(
      `/dbschedule/deleteResult?date=${date}&time=${time}`,
      {}
    );
  }

  deleteResultByIds(ids: number[]): Observable<string> {
    return this.httpClient.post<string>(
      `/dbschedule/deleteResultByIds?ids=${[ids]}`,
      {}
    );
  }

  scheduleAll(): Observable<CommonResponse<ScheduleAllDto>> {
    return this.httpClient.post<CommonResponse<ScheduleAllDto>>(
      `/dbschedule/scheduleAll`,
      {}
    );
  }

  exportAllByDate(date): Observable<Map<string, DBScheduleResultDto[]>> {
    return this.httpClient.post<Map<string, DBScheduleResultDto[]>>(
      `/dbschedule/exportAllByDate?date=${date}`,
      {}
    );
  }

  exportByDateAndTime(
    date,
    time
  ): Observable<Map<string, DBScheduleResultDto[]>> {
    return this.httpClient.post<Map<string, DBScheduleResultDto[]>>(
      `/dbschedule/exportByDateAndTime?date=${date}&time=${time}`,
      {}
    );
  }

  view(id: number): Observable<CommonResponse<DBScheduleDto>> {
    return this.httpClient.get<CommonResponse<DBScheduleDto>>(
      `/dbschedule/view?id=${id}`
    );
  }

  retry(doc: DBScheduleResultDto): Observable<CommonResponse<string>> {
    return this.httpClient.post<CommonResponse<string>>(
      `/dbschedule/retry`,
      doc
    );
  }

  test(doc: DBScheduleDto): Observable<CommonResponse<string>> {
    return this.httpClient.post<CommonResponse<string>>(
      `/dbschedule/test`,
      doc
    );
  }

  add(newSchedule: DBScheduleDto): Observable<any> {
    return this.httpClient.post<any>('/dbschedule/add', newSchedule);
  }
}
