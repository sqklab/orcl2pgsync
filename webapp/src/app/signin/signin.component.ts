import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { CREDENTIAL_NAME, USER_NAME_LOGGED } from '../libs/constants';
import { AuthService } from '../libs/services/auth.service';
import { Account } from '../libs/services/interface';
import StorageService from '../libs/services/storage.service';

@Component({
  selector: 'app-signin',
  templateUrl: './signin.component.html',
  styleUrls: ['./signin.component.scss'],
})
export class SignInComponent implements OnInit {
  private loadingSubs: Subscription;
  errorRequired = false;
  errorInvalid = false;
  isLoading = false;
  account = {
    username: '',
    password: '',
  } as Account;

  constructor(
    private authService: AuthService,
    private router: Router,
    private storageService: StorageService
  ) {
    if (this.authService.isAuthenticated()) {
      this.authService.router.navigate(['/']);
    }
  }

  ngOnInit(): void {
    this.loadingSubs = this.authService.onLoading$.subscribe((rs) => {
      this.isLoading = rs;
    });
  }

  get isInvalid(): boolean {
    this.account.username = this.account.username.trim();
    this.account.password = this.account.password.trim();
    if (!this.account.username.length || !this.account.password.length) {
      this.errorRequired = true;
      return true;
    }
    this.errorRequired = false;
    return false;
  }

  handleLogin(): Promise<void> {
    if (this.isInvalid) {
      return;
    }
    this.errorInvalid = false;
    this.authService.onLoading$.next(true);
    this.authService.login(this.account).subscribe(
      (res) => {
        // cookies.set(CREDENTIAL_NAME, JSON.stringify(this.account));
        this.storageService.save(CREDENTIAL_NAME, res.token);
        this.storageService.save(USER_NAME_LOGGED, this.account.username);
        this.authService.onAuth$.next(true);
        this.authService.onLoading$.next(false);
        this.errorInvalid = false;
        this.router.navigate(['/synchronize']);
      },
      () => {
        this.errorInvalid = true;
        this.authService.onLoading$.next(false);
      }
    );
  }

  ngOnDestroy(): void {
    this.loadingSubs && this.loadingSubs.unsubscribe();
  }
}
