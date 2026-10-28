import { ApplicationConfig, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding, withRouterConfig } from '@angular/router';
import { appRoutes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { LOCAL_STORAGE } from './core/persistence/local-storage.token';
import { apiInterceptor } from './core/interceptors/api.interceptor';
import { MAT_TOOLTIP_DEFAULT_OPTIONS } from '@angular/material/tooltip';
import { MAT_ICON_DEFAULT_OPTIONS } from '@angular/material/icon';
import { unauthorizedInterceptor } from './core/interceptors/unauthorized.interceptor';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { FullscreenOverlayContainer, OverlayContainer } from '@angular/cdk/overlay';
import { TRANSLATION_CONTEXT } from './core/translate/translate.types';
import { DataModelTranslationContextProvider } from './data-models/services/data-model-translation-context.provider';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZonelessChangeDetection(),
    provideRouter(appRoutes,
      withComponentInputBinding(),
      withRouterConfig({ paramsInheritanceStrategy: 'always' }),
    ),
    provideHttpClient(
      withInterceptors([apiInterceptor, jwtInterceptor, unauthorizedInterceptor]),
    ),
    { provide: LOCAL_STORAGE, useFactory: () => window.localStorage },
    { provide: MAT_TOOLTIP_DEFAULT_OPTIONS, useValue: { position: 'above' } },
    { provide: MAT_ICON_DEFAULT_OPTIONS, useValue: { fontSet: 'material-symbols-outlined' } },
    { provide: OverlayContainer, useClass: FullscreenOverlayContainer },
    { provide: TRANSLATION_CONTEXT, useExisting: DataModelTranslationContextProvider, multi: true },
  ],
};
