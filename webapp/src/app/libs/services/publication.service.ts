import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseService } from './base.service';
import {
  CommonResponse, PublicationDto
} from './interface';
import StorageService from './storage.service';

@Injectable({
  providedIn: 'root',
})
export class PublicationService extends BaseService {
  constructor(public storage: StorageService, httpClient: HttpClient) {
    super(httpClient);
  }

  getPublications(db): Observable<CommonResponse<string[]>> {
    return this.httpClient.get<CommonResponse<string[]>>(
      `/publication/getPublications?db=${db}`
    );
  }

  getPublicationTableByPublication(db, publicationName, tableSearch, pageNo, pageSize): Observable<CommonResponse<PublicationDto>> {
    return this.httpClient.get<CommonResponse<PublicationDto>>(
      `/publication/getPublicationTableByPublication?db=${db}&publicationName=${publicationName}&tableSearch=${tableSearch}&pageNo=${pageNo}&pageSize=${pageSize}`
    );
  }

  alterPublication(db, publicationName, tables, action): Observable<CommonResponse<number>> {
    return this.httpClient.get<CommonResponse<number>>(
      `/publication/alterPublication?db=${db}&publicationName=${publicationName}&tables=${tables}&action=${action}`
    );
  }
}
