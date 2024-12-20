import { Component, OnDestroy, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { IUser } from 'src/app/libs/services/interface';
import StorageService from 'src/app/libs/services/storage.service';
import { AuthService } from '../../libs/services/auth.service';
import HeathCheckService from '../../libs/services/HeathCheck.service';
import { IModalProps } from '../modal/interface';
import { IRouter } from './interface';

const routes: IRouter[] = [
  {
    id: 'idSynchronizer',
    name: 'Synchronizer',
    path: '/synchronize',
    icon: 'bi bi-arrow-repeat',
    children: [],
  },
  {
    id: 'isManageUser',
    name: 'DB Configuration',
    path: '/dbconfig',
    icon: 'bi-gear-fill',
    children: [],
  },
  {
    id: 'comparison',
    name: 'Comparison',
    path: '/comparison',
    icon: 'bi bi-view-stacked',
    children: [],
  },
  {
    id: 'execute',
    name: 'DB Scheduler',
    path: '/db-scheduler',
    icon: 'bi bi-play-circle',
    children: [],
  },
  {
    id: 'connector',
    name: 'Connector',
    path: '/db-connector',
    icon: 'bi bi-wifi',
    children: [],
  },
  {
    id: 'analysis',
    name: 'Analysis',
    path: '/analysis',
    icon: 'bi bi-bar-chart-line',
    children: [],
  },
  {
    id: 'operation',
    name: 'Operation',
    path: '/operation',
    icon: 'bi bi-sliders',
    children: [],
  },
  {
    id: 'kafka-producer',
    name: 'Kafka Producer',
    path: '/kafka-producer',
    icon: 'bi bi-envelope',
    children: [],
  },
];

@Component({
  selector: 'app-layout',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit, OnDestroy {
  private internalSubs: Subscription;
  private loadingSubs: Subscription;
  private modalSubs: Subscription;
  // private authSubs: Subscription;

  routes = routes;
  isShow = false;
  isShowModal = false;
  isLoading = false;
  modalProp = {} as IModalProps;
  credential: IUser = {} as IUser;

  kafkaStatus;
  kafkaEvent: Subscription;

  language = 'en';

  sideBarShow = true;

  constructor(
    private authService: AuthService,
    public translate: TranslateService,
    private storage: StorageService,
    private heathCheckService: HeathCheckService
  ) {}

  ngOnInit(): void {
    const lang: string = this.storage.get('lang');
    if (lang) {
      this.translate.setDefaultLang(lang);
    }
    // this.credential = this.storage.get(CREDENTIAL_NAME)
    //   ? JSON.parse(this.storage.get(CREDENTIAL_NAME))
    //   : ({} as IUser);

    // this.authSubs = this.authService.onAuth$.subscribe(() => {
    //   this.credential = this.storage.get(CREDENTIAL_NAME)
    //     ? JSON.parse(this.storage.get(CREDENTIAL_NAME))
    //     : {};
    // });

    this.heathCheck();
    this.internalSubs = this.authService.onInternalServerError$.subscribe(
      (status) => {
        if (status) {
          this.modalProp = {
            type: 'warning',
            isShow: true,
            title: 'Thông báo',
            message:
              'Chúng tôi xin lỗi vì hệ thống đang không thể xử lý tại thời điểm hiện tại',
            confirm: () => {
              this.authService.logout();
            },
          };
        } else {
          this.modalProp = {
            isShow: false,
            title: '',
            message: '',
            confirm: () => {},
          };
        }
      }
    );

    this.loadingSubs = this.authService.onLoading$.subscribe((rs) => {
      this.isLoading = rs;
    });

    this.modalSubs = this.authService.onShowModal$.subscribe(
      (rs: IModalProps) => {
        this.isShowModal = rs.isShow;
        const cancelAct = rs.cancel || rs.closeModal ? true : false;
        this.modalProp = {
          ...rs,
          confirm: () => this.resetModal(rs.confirm, cancelAct),
          closeModal: () => this.resetModal(rs.closeModal, cancelAct),
          cancel: cancelAct,
        };
      }
    );
  }

  resetModal(func = () => {}, cancel = false): void {
    this.authService.onShowModal$.next({
      isShow: false,
      title: '',
      message: '',
      confirm: () => {},
      cancel,
    });
    func();
  }

  handleLogout(): void {
    this.stopHeathCheck();
    this.authService.onAuth$.next(false);
  }

  changeLang(lang): void {
    this.language = lang;
    this.storage.save('lang', lang);
    this.translate.use(lang);
  }

  ngOnDestroy(): void {
    if (this.kafkaEvent) {
      this.kafkaEvent.unsubscribe();
    }
    // if (this.authSubs) this.authSubs.unsubscribe();
    if (this.internalSubs) this.internalSubs.unsubscribe();
    if (this.loadingSubs) this.loadingSubs.unsubscribe();
    if (this.modalSubs) this.modalSubs.unsubscribe();
  }

  heathCheck(): void {
    this.kafkaEvent = this.heathCheckService
      .startKafkaHeathCheck('/heath/kafka/start')
      .subscribe((event) => {
        if (event.type === 'error') {
        } else {
          const messageEvent = event as MessageEvent;
          console.info(
            `SSE request with type "${messageEvent.type}" and data "${messageEvent.data}"`
          );
          this.kafkaStatus = messageEvent.data;
        }
      });
  }

  stopHeathCheck(): void {
    this.heathCheckService.stopKafkaHeathCheck().subscribe((_) => {
      this.authService.logout();
    });
  }

  toggle() {
    this.sideBarShow = !this.sideBarShow;
    this.storage.toggleLeftBar.next(this.sideBarShow);
  }
}
