import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseService } from './base.service';
import {
    DataSourceInfoDTO,
    DataSourceResponse
} from './interface';
import StorageService from './storage.service';


@Injectable({
    providedIn: 'root'
})
export class DBConfigService extends BaseService {
    constructor(public storage: StorageService, httpClient: HttpClient) {
        super(httpClient);
    }

    addNewDBConfig(body: DataSourceInfoDTO): Observable<DataSourceInfoDTO> {
        return this.httpClient.post<DataSourceInfoDTO>('/datasource/add', body);
    }

    testConnection(body: DataSourceInfoDTO): Observable<boolean> {
        return this.httpClient.post<boolean>('/datasource/testConnection', body);
    }

    updateDatasource(body: DataSourceInfoDTO) {
        return this.httpClient.put<DataSourceInfoDTO>('/datasource/update', body);
    }
    getDatasourceById(id): Observable<DataSourceInfoDTO> {
        return this.httpClient.get<DataSourceInfoDTO>(`/datasource/view/${id}`);
    }

    deleteDatasource(ids: number []): Observable<boolean> {
        return this.httpClient.post<boolean>('/datasource/delete', ids);
    }

    getDBConfigs(pageNumber = 1, pageSize: number): Observable<DataSourceResponse> {
        return this.httpClient.get<DataSourceResponse>(`/datasource/all?pageNo=${pageNumber}&pageSize=${pageSize}`);
    }
}
