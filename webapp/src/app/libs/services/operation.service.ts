import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseService } from './base.service';
import {
  CommonResponse,
  CorrectionBatchResult,
  CorrectionResult, OperationDto,
  CompareDiffItem, ViewComparisonInfoDto, OperationResponse, OperationSummary, DiffDeleteReq
} from './interface';
import StorageService from './storage.service';

@Injectable({
  providedIn: 'root',
})
export class OperationService extends BaseService {
  constructor(public storage: StorageService, httpClient: HttpClient) {
    super(httpClient);
  }

  loadSearchingData(sourceDatabase, sourceSchema, targetDatabase, targetSchema, table, session, whereStm, pageNo, pageSize, primaryKeys, uniqueKeys): Observable<CommonResponse<OperationResponse>> {
    return this.httpClient.get<CommonResponse<OperationResponse>>(
      `/operation/loadSearching?table=${table}&pageNo=${pageNo}&pageSize=${pageSize}&sourceDatabase=${sourceDatabase}&sourceSchema=${sourceSchema}&targetDatabase=${targetDatabase}&targetSchema=${targetSchema}&whereStm=${whereStm}&session=${session}&primaryKeys=${primaryKeys}&uniqueKeys=${uniqueKeys}`
    );
  }

  getViewInfoForSearching(): Observable<ViewComparisonInfoDto[]> {
    return this.httpClient.get<ViewComparisonInfoDto[]>(
      `/comparison/viewInfoForSearching`
    );
  }

  getOperationSummary(table, sessionId, whereCondition): Observable<CommonResponse<OperationSummary>> {
    return this.httpClient.get<CommonResponse<OperationSummary>>(
      `/operation/getOperationSummary?table=${table}&sessionId=${sessionId}&whereCondition=${whereCondition}`
    );
  }

  checkOperation(table, sessionId, whereCondition): Observable<CommonResponse<OperationSummary>> {
    return this.httpClient.get<CommonResponse<OperationSummary>>(
      `/operation/checkOperation?table=${table}&sessionId=${sessionId}&whereCondition=${whereCondition}`
    );
  }

  getColumnIdValue(body: OperationDto): Observable<CommonResponse<string[]>> {
    return this.httpClient.post<CommonResponse<string[]>>(`/operation/getColumnIdValue`, body);
  }

  getSuggestColumnIdValue(body: OperationDto): Observable<CommonResponse<string[]>> {
    return this.httpClient.post<CommonResponse<string[]>>(`/operation/getFirstCharacter`, body);
  }

  search(body: OperationDto): Observable<CommonResponse<CompareDiffItem[]>> {
    return this.httpClient.post<CommonResponse<CompareDiffItem[]>>(
      `/operation/compare`,
      body
    );
  }

  correction(body: CompareDiffItem[], targetDatabase, targetSchema, targetTable): Observable<CommonResponse<CorrectionResult[]>> {
    return this.httpClient.post<CommonResponse<CorrectionResult[]>>(
      `/operation/correct?targetDatabase=${targetDatabase}&targetSchema=${targetSchema}&targetTable=${targetTable}`,
      body
    );
  }

  correctionBatch(body: CompareDiffItem[], sourceDatabase, sourceSchema, targetDatabase, targetSchema, targetTable, sessionId, whereCondition): Observable<CommonResponse<CorrectionBatchResult>> {
    return this.httpClient.post<CommonResponse<CorrectionBatchResult>>(
      `/operation/correctBatch?targetDatabase=${targetDatabase}&targetSchema=${targetSchema}&targetTable=${targetTable}&sourceDatabase=${sourceDatabase}&sourceSchema=${sourceSchema}&session=${sessionId}&whereStm=${whereCondition}`,
      body
    );
  }
  cancelSearching(table, sessionId, whereCondition): Observable<CommonResponse<any>> {
    return this.httpClient.post<CommonResponse<any>>(
      `/operation/cancelSearching?session=${sessionId}&whereStm=${whereCondition}&table=${table}`,
      {}
    );
  }
  // deletes
  deleteProcess(body: DiffDeleteReq): Observable<CommonResponse<void>> {
    return this.httpClient.post<CommonResponse<void>>(`/operation/deleteProcess`, body);
  }
  deleteOperationTable(body: DiffDeleteReq): Observable<CommonResponse<void>> {
    return this.httpClient.post<CommonResponse<void>>(`/operation/deleteDiff`, body);
  }

}
