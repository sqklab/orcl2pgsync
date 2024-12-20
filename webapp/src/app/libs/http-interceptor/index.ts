import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { AuthInterceptor } from './auth.interceptor';
import {BlobErrorHttpInterceptor} from "./blob.interceptor";

export const HttpInterceptProviders = [
    {
        provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true
    },
    {
        provide: HTTP_INTERCEPTORS, useClass: BlobErrorHttpInterceptor, multi: true
    }
];
