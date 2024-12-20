import { Component, OnInit, HostListener } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subscription, Subject } from 'rxjs';
import { AuthService } from './libs/services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  private authSubs: Subscription;
  isAuth = false;
  timeout = 0;
  expiresTime = 0;
  userActivity;
  userInactive: Subject<any> = new Subject();

  constructor(private authService: AuthService, public translate: TranslateService) {
    this.translate.addLangs(['en', 'kr']);
    this.translate.setDefaultLang('en');
  }

  ngOnInit(): void {
    this.isAuth = this.authService.isAuthenticated();
    if (this.isAuth) {
      this.initIdle();
    }
    this.authSubs = this.authService.onAuth$.subscribe(() => {
      this.isAuth = this.authService.isAuthenticated();
      if (this.isAuth) {
        this.initIdle();
      }
    });
  }

  initIdle() {
    const adminTimeOut = 1440; // one day to logout
    this.timeout = +adminTimeOut * 60 * 1000;
    this.setTimeoutUserInactive();
    this.userInactive.subscribe(() => {
      if (this.expiresTime < Date.now()) {
        clearTimeout(this.userActivity);
        this.authService.logout();
        window.location.reload();
      }
    });
  }

  setTimeoutUserInactive() {
    clearTimeout(this.userActivity);
    this.expiresTime = Date.now() + this.timeout;
    // this.authService.storage.save(EXPIRED_IDLE_TIME_NAME, Date.now() + this.timeout);
    this.userActivity = setTimeout(
      () => this.userInactive.next(undefined),
      this.timeout
    );
  }

  @HostListener('window:click') refreshUserStateMousemove() {
    clearTimeout(this.userActivity);
    this.userActivity = setTimeout(() => this.setTimeoutUserInactive(), 200);
  }

  @HostListener('window:scroll') refreshUserStateScroll() {
    clearTimeout(this.userActivity);
    this.userActivity = setTimeout(() => this.setTimeoutUserInactive(), 200);
  }

  @HostListener('window:keydown') refreshUserStateKeydown() {
    clearTimeout(this.userActivity);
    this.userActivity = setTimeout(() => this.setTimeoutUserInactive(), 200);
  }

  ngOnDestroy(): void {
    if (this.authSubs) this.authSubs.unsubscribe();
    clearTimeout(this.userActivity);
    if (this.userInactive) this.userInactive.unsubscribe();
  }
}
