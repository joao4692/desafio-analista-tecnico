import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { routes } from './app.routes';
import { erroInterceptor } from './core/interceptors/erro.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    // Passa os parâmetros da rota (ex.: :id) direto para os inputs dos componentes.
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([erroInterceptor])),
  ],
};
