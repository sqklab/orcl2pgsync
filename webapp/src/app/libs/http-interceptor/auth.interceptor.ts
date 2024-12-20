import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';
import StorageService from '../services/storage.service';
import { CREDENTIAL_NAME } from '../constants';
import {SweetAlert} from "../utils/sweetalert";
@Injectable({
  providedIn: 'root',
})
export class AuthInterceptor implements HttpInterceptor {
  constructor(
    private storage: StorageService,
    private authService: AuthService
  ) {}

  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    const contenType = request.headers.get('Content-Type');
    const headers = request.headers;
    // headers.append('Content-Type', contenType ? contenType : 'application/json');

    // let headers: any = {
    //     'Cache-Control': 'no-cache',
    //     Pragma: 'no-cache',
    //     'X-Frame-Options': 'DENY',
    //     'x-frontend-json': 'true',
    //     // 'Content-Type': contenType ? contenType : 'application/json',
    //     // 'Access-Control-Allow-Origin': '*',
    //     // 'Access-Control-Allow-Methods': 'POST, GET, OPTIONS, PUT, DELETE',
    //     // 'Access-Control-Allow-Headers': 'Origin, Content-Type, Accept, Authorization, X-Request-With'
    // };
    // const rs = cookie.get(CREDENTIAL_NAME);
    // const credential: IUser = this.authService.getInfo();
    // if (credential && credential.token) {
    //     headers.Authorization = `Bearer ${credential.token}`;
    // }
    const credential = this.storage.get(CREDENTIAL_NAME);

    if (!credential) {
      this.handleLogout();
    }
    // let newHeader = { ...headers.getAll };
    // if (credential) {
    //     newHeader = { ...headers.getAll, 'Authorization': `Bearer ${credential}` };
    // }
    const isNotFromI18n = request.url.indexOf('assets/i18n') < 0;
    if (isNotFromI18n) {
      const isFromConnectors = request.url.indexOf('/connectors') >= 0;
      let customerHeader = {
        Authorization: `Bearer ${credential}`
      } as any;
      if (isFromConnectors) {
        customerHeader = {
          Authorization: `Bearer ${credential}`,
          'Content-Type': 'application/json'
        } as any;
      }
      if (credential) {
        request = request.clone({
          url: `${environment.baseUrl}${request.url}`,
          setHeaders: customerHeader,
        });
      } else {
        request = request.clone({
          url: `${environment.baseUrl}${request.url}`
        });
      }
    }

    return next.handle(request).pipe(
      catchError((res) => {
        this.authService.onLoading$.next(false);
        console.log('res', res);
        if (res.status === 401 && res.error && res.error.message == "RESOURCE_ACCESS_DENY") {
          alert('You do not have permission');
          return throwError("You do not have permission.");
        }
        if (res.status === 401) {
          // auto logout if 401 response returned from api
          this.handleLogout();
        }
        if (!res.error) {
          return throwError(res);
        } else {
          const error = res.error && res.error.message || res.statusText;
          return throwError(error);
        }
      })
    );
  }

  handleLogout(): void {
    this.authService.logout();
    this.authService.onAuth$.next(false);
  }
}
