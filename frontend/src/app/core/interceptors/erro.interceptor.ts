import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ApiErro } from '../../models/api-erro';
import { AlertaService } from '../../shared/alerta/alerta.service';

// Centraliza a mensagem de erro das chamadas à API, para as telas não repetirem isso.
// O erro continua sendo propagado: a tela ainda pode reagir (parar o loading, por exemplo).
export const erroInterceptor: HttpInterceptorFn = (req, next) => {
  const alertas = inject(AlertaService);

  return next(req).pipe(
    catchError((erro: HttpErrorResponse) => {
      alertas.erro(mensagemDoErro(erro));
      return throwError(() => erro);
    }),
  );
};

export function mensagemDoErro(erro: HttpErrorResponse): string {
  if (erro.status === 0) {
    return 'Não foi possível conectar ao servidor. Verifique se a API está no ar.';
  }

  // O back já manda a mensagem em português no campo "mensagem".
  const corpo = erro.error as Partial<ApiErro> | null;
  if (corpo?.mensagem) {
    return corpo.mensagem;
  }

  return `Erro inesperado (status ${erro.status}). Tente novamente.`;
}
