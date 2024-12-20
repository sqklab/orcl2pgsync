import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {BaseService} from './base.service';
import {
  CommonResponse,
  ComparisonResultDto,
  ComparisonSummary,
  CustomDateTime,
  DataAnalysisDtoResponse,
  DbComparisonScheduleDto,
  QuickRunComparisonQuery,
  ViewComparisonInfoDto,
  ViewComparisonResultDto,
  ViewComparisonResultDtoResponse,
  ViewKafkaConsumerGroup
} from './interface';
import StorageService from './storage.service';

@Injectable({
  providedIn: 'root',
})
export class ComparisonService extends BaseService {
  constructor(public storage: StorageService, httpClient: HttpClient) {
    super(httpClient);
  }

  getSchedules(): Observable<DbComparisonScheduleDto[]> {
    return this.httpClient.get<DbComparisonScheduleDto[]>(
      `/comparison/schedules`
    );
  }

  getDivisions(): Observable<string[]> {
    return this.httpClient.get<string[]>(
      `/dbsync/synchronizers/division`
    );
  }

  getTimeOnServer(): Observable<CommonResponse<CustomDateTime>> {
    return this.httpClient.get<CommonResponse<CustomDateTime>>(
      `/comparison/getTimeOnServer`
    );
  }

  getViewInfoForSearching(): Observable<ViewComparisonInfoDto[]> {
    return this.httpClient.get<ViewComparisonInfoDto[]>(
      `/comparison/viewInfoForSearching`
    );
  }

  getResultByDate(date): Observable<string[]> {
    return this.httpClient.get<string[]>(
      `/comparison/getResultByDate?date=${date}`
    );
  }

  viewKafkaConsumerGroup(filter, reverse, topN): Observable<ViewKafkaConsumerGroup[]> {
    return this.httpClient.get<ViewKafkaConsumerGroup[]>(
      `/comparison/viewKafkaConsumerGroup?topN=${topN}&reverse=${reverse}&filter=${filter}`
    );
  }

  runningCount(): Observable<number> {
    return this.httpClient.get<number>(`/comparison/runningCount`);
  }
  startSchedule(): Observable<number> {
    return this.httpClient.get<number>(`/comparison/startSchedule`);
  }
  stopSchedule(): Observable<number> {
    return this.httpClient.get<number>(`/comparison/stopSchedule`);
  }

  getSummaryByDateAndTime(date, time): Observable<ComparisonSummary> {
    return this.httpClient.get<ComparisonSummary>(
      `/comparison/getSummaryByDateAndTime?date=${date}&time=${time}`
    );
  }

  // getKafkaMessageBehind(): Observable<MessageBehind> {
  //   return this.httpClient.get<MessageBehind>(
  //     `/comparison/getKafkaMessageBehind`
  //   );
  // }

  deleteSummaryByDateAndTime(date, time): Observable<boolean> {
    return this.httpClient.post<boolean>(
      `/comparison/deleteSummaryByDateAndTime?date=${date}&time=${time}`,
      {}
    );
  }

  saveSchedules(
    body: DbComparisonScheduleDto[]
  ): Observable<CommonResponse<string>> {
    return this.httpClient.put<CommonResponse<string>>(
      '/comparison/schedules/update',
      body
    );
  }

  exportAllByDate(date): Observable<ComparisonResultDto> {
    return this.httpClient.post<ComparisonResultDto>(
      `/comparison/exportAllByDate?date=${date}`,
      {}
    );
  }

  quickRunQuery(body: QuickRunComparisonQuery): Observable<any> {
    return this.httpClient.post<any>(
      `/comparison/quickRunQuery`, body
    );
  }

  exportByDateAndTime(
    date,
    time
  ): Observable<Map<string, ViewComparisonResultDto[]>> {
    return this.httpClient.post<Map<string, ViewComparisonResultDto[]>>(
      `/comparison/exportByDateAndTime?date=${date}&time=${time}`,
      {}
    );
  }

  compareAll(): Observable<any> {
    return this.httpClient.get<any>('/comparison/compareAll');
  }
  compareSelectedItems(id: number[]): Observable<CommonResponse<string>> {
    return this.httpClient.post<CommonResponse<string>>(
      `/comparison/compareSelected`,
      id
    );
  }

  filter(
    dateFrom,
    time,
    sourceDB,
    sourceSchema,
    targetDB,
    targetSchema,
    sourceTable,
    targetTable,
    state,
    pageNo,
    pageSize,
    sortField,
    sortType,
    division
  ): Observable<ViewComparisonResultDtoResponse> {
    return this.httpClient.get<ViewComparisonResultDtoResponse>(
      `/comparison/filter?dateFrom=${dateFrom}&time=${time}&sourceDB=${sourceDB}&sourceSchema=${sourceSchema}&targetDB=${targetDB}&targetSchema=${targetSchema}&sourceTable=${sourceTable}&targetTable=${targetTable}&state=${state}&pageNo=${pageNo}&pageSize=${pageSize}&sortField=${sortField}&sortType=${sortType}&division=${division}`
    );
  }

  viewDailyGraph(
    dbName,
    schmName,
    division,
    topic,
    year,
    month,
    date,
    chartType
  ): Observable<DataAnalysisDtoResponse> {
    return this.httpClient.get<DataAnalysisDtoResponse>(
      `/dbsync/analysis/viewDailyGraph?dbName=${dbName}&schmName=${schmName}&division=${division}&topic=${topic}&year=${year}&month=${month}&date=${date}&chartType=${chartType}`
    );
  }

  viewHourlyGraph(
    dbName,
    schmName,
    division,
    topic,
    year,
    month,
    date,
    chartType
  ): Observable<DataAnalysisDtoResponse> {
    return this.httpClient.get<DataAnalysisDtoResponse>(
      `/dbsync/analysis/viewHourlyGraph?dbName=${dbName}&schmName=${schmName}&division=${division}&topic=${topic}&year=${year}&month=${month}&date=${date}&chartType=${chartType}`
    );
  }

  viewMinutelyGraph(
    dbName: string[],
    schmName: string[],
    division,
    topic,
    year,
    month,
    date,
    fromHour,
    toHour,
    type,
    chartType
  ): Observable<DataAnalysisDtoResponse> {
    return this.httpClient.get<DataAnalysisDtoResponse>(
      `/dbsync/analysis/viewMinutelyGraph?dbName=${dbName}&schmName=${schmName}&division=${division}&topic=${topic}&year=${year}&month=${month}&date=${date}&fromHour=${fromHour}&toHour=${toHour}&type=${type}&chartType=${chartType}`
    );
  }

  viewTopicsHourly(
      topicNames,
      year,
      month,
      date
    ): Observable<DataAnalysisDtoResponse> {
      return this.httpClient.get<DataAnalysisDtoResponse>(
        `/dbsync/analysis/viewTopicHourly?topicNames=${topicNames}&year=${year}&month=${month}&date=${date}`
      );
    }
}
