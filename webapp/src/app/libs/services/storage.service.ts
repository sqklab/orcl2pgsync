import { Injectable } from '@angular/core';
import * as Cookies from 'js-cookie';
import { ReplaySubject } from 'rxjs';
import { CREDENTIAL_NAME, USER_NAME_LOGGED } from '../constants';

@Injectable({
    providedIn: 'root'
})
export default class StorageService {

    toggleLeftBar = new ReplaySubject();

    isExist(name: string): boolean {
        return !!this.get(name);
    }
    save(name: string, value: any): void {
        if (name === CREDENTIAL_NAME || name === USER_NAME_LOGGED) {
            const oneDay = 1;
            Cookies.set(name, value, { expires: oneDay });
        } else {
            window.sessionStorage.setItem(name, JSON.stringify(value));
        }
    }

    get<T>(name: string): T {
        if (name === CREDENTIAL_NAME || name === USER_NAME_LOGGED) {
            return Cookies.get(name) as any;
        }
        return JSON.parse(window.sessionStorage.getItem(name));
    }

    clearItem(name: string): void {
        window.sessionStorage.removeItem(name);
        Cookies.remove(name);
    }

    clearAll(): void {
        window.sessionStorage.clear();
        Cookies.remove(CREDENTIAL_NAME);
    }
}
