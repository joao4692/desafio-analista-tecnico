import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { Usuario } from '../../../models/usuario';
import { AlertaService } from '../../../shared/alerta/alerta.service';
import { UsuarioService } from '../usuario.service';

@Component({
  imports: [RouterLink],
  selector: 'app-usuario-lista',
  styleUrl: './usuario-lista.scss',
  templateUrl: './usuario-lista.html',
})
export class UsuarioLista implements OnInit {
  private readonly usuarioService = inject(UsuarioService);
  private readonly alertas = inject(AlertaService);

  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly carregando = signal(true);

  ngOnInit(): void {
    this.usuarioService
      .listar()
      .pipe(finalize(() => this.carregando.set(false)))
      // Em caso de erro, o interceptor já mostra a mensagem.
      .subscribe({ next: (usuarios) => this.usuarios.set(usuarios), error: () => undefined });
  }

  protected remover(usuario: Usuario): void {
    if (!confirm(`Remover ${usuario.nome}?${this.avisoCartoes(usuario.cartoes.length)}`)) {
      return;
    }

    this.usuarioService.remover(usuario.id).subscribe({
      next: () => {
        this.usuarios.update((lista) => lista.filter((u) => u.id !== usuario.id));
        this.alertas.sucesso(`${usuario.nome} foi removido.`);
      },
      error: () => undefined,
    });
  }

  protected textoCartoes(quantidade: number): string {
    if (quantidade === 0) {
      return 'Nenhum cartão';
    }
    return quantidade === 1 ? '1 cartão' : `${quantidade} cartões`;
  }

  private avisoCartoes(quantidade: number): string {
    if (quantidade === 0) {
      return '';
    }
    return quantidade === 1
      ? ' O cartão dele também será removido.'
      : ` Os ${quantidade} cartões dele também serão removidos.`;
  }
}
