import {Injectable, OnInit} from '@angular/core';
import {Observable} from "rxjs";
import {SseClient} from 'ngx-sse-client';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import StorageService from "./storage.service";
import {CREDENTIAL_NAME} from "../constants";
import {BaseService} from "./base.service";

/**
 * https://github.com/rubinhos/ngx-sse-client/blob/master/lib/projects/ngx-sse-client/README.md
 */
@Injectable({
  providedIn: 'root'
})
export default class HeathCheckService extends BaseService implements OnInit {
  credential;

  constructor(private sseClient: SseClient, private storage: StorageService, protected httpClient: HttpClient) {
    super(httpClient);
  }

  ngOnInit(): void {
    this.credential = this.storage.get(CREDENTIAL_NAME);
  }

  startKafkaHeathCheck(url: string): Observable<Event> {
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.credential}`);
    return this.sseClient.stream(url, {
      keepAlive: true,
      reconnectionDelay: 10000,
      responseType: 'event'
    }, {headers}, 'GET');
  }

  stopKafkaHeathCheck(): Observable<any> {
    return this.httpClient.get<any>(`/heath/kafka/stop`);
  }
}
