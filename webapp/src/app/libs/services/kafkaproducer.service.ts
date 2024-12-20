import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { KafkaMessage, KafkaPostgres } from 'src/app/kafka-send-message/kafka.model';
import { BaseService } from './base.service';
import {
  CommonResponse
} from './interface';
import StorageService from './storage.service';

@Injectable({
  providedIn: 'root',
})
export class KafkaProducerService extends BaseService {
  constructor(public storage: StorageService, httpClient: HttpClient) {
    super(httpClient);
  }

  send(body: KafkaMessage): Observable<CommonResponse<string>> {
    return this.httpClient.post<CommonResponse<string>>(
      `/kafka-producer/producer`, body
    );
  }

  getTopics(fetchBy=''): Observable<CommonResponse<string[]>> {
    return this.httpClient.get<CommonResponse<string[]>>(`/kafka-producer/getTopics?fetchBy=${fetchBy}`);
  }
}
