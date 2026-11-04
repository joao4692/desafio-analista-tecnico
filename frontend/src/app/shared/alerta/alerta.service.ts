import { Injectable, signal } from '@angular/core';

export type TipoAlerta = 'sucesso' | 'erro';

export interface MensagemAlerta {
  tipo: TipoAlerta;
  texto: string;
}

// Mostra uma mensagem por vez. Sucesso some sozinho; erro fica até o usuário fechar,
// para dar tempo de ler.
@Injectable({ providedIn: 'root' })
export class AlertaService {
  static readonly TEMPO_SUCESSO_MS = 4000;

  private readonly mensagemAtual = signal<MensagemAlerta | null>(null);
  readonly mensagem = this.mensagemAtual.asReadonly();

  private temporizador?: ReturnType<typeof setTimeout>;

  sucesso(texto: string): void {
    this.mostrar({ tipo: 'sucesso', texto });
    this.temporizador = setTimeout(() => this.fechar(), AlertaService.TEMPO_SUCESSO_MS);
  }

  erro(texto: string): void {
    this.mostrar({ tipo: 'erro', texto });
  }

  fechar(): void {
    clearTimeout(this.temporizador);
    this.mensagemAtual.set(null);
  }

  private mostrar(mensagem: MensagemAlerta): void {
    // Evita que o timer de uma mensagem antiga feche a nova.
    clearTimeout(this.temporizador);
    this.mensagemAtual.set(mensagem);
  }
}
