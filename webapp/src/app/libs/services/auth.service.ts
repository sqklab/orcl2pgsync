import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { CREDENTIAL_NAME, USER_NAME_LOGGED } from '../constants';
import { BaseService } from './base.service';
import { Account, LoginResponse } from './interface';
import StorageService from './storage.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService extends BaseService {
  constructor(
    public router: Router,
    public storage: StorageService,

    httpClient: HttpClient
  ) {
    super(httpClient);
  }

  public isAuthenticated(): boolean {
    const token = this.storage.get(CREDENTIAL_NAME);
    return !!token;
  }

  public getUserLogged(): string {
    return this.storage.get(USER_NAME_LOGGED); 
  }

  login(account: Account): Observable<LoginResponse> {
    return this.httpClient.post<LoginResponse>(`/auth/login`, account);
  }

  public clearCache(): void {
    this.storage.clearAll();
    this.storage.clearItem(CREDENTIAL_NAME);
  }

  public logout(): void {
    this.clearCache();
    this.onAuth$.next(undefined);
    this.router.navigate(['/login']);
  }
}
