import { BehaviorSubject } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { IModalProps } from 'src/app/components/modal/interface';

@Injectable()
export class BaseService {
    onAuth$ = new BehaviorSubject<boolean>(false);
    isSubmit = new BehaviorSubject<boolean>(false);
    onLoading$ = new BehaviorSubject<boolean>(false);
    onShowModal$ = new BehaviorSubject<IModalProps>({} as IModalProps);
    onInternalServerError$ = new BehaviorSubject<boolean>(false);
    constructor(protected httpClient: HttpClient) {
    }
}