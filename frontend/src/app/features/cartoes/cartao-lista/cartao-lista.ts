import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { Cartao } from '../../../models/cartao';
import { ROTULO_TIPO_CARTAO } from '../../../models/tipo-cartao';
import { Usuario } from '../../../models/usuario';
import { AlertaService } from '../../../shared/alerta/alerta.service';
import { UsuarioService } from '../../usuarios/usuario.service';
import { CartaoService } from '../cartao.service';

@Component({
  imports: [RouterLink],
  selector: 'app-cartao-lista',
  styleUrl: './cartao-lista.scss',
  templateUrl: './cartao-lista.html',
})
export class CartaoLista implements OnInit {
  private readonly cartaoService = inject(CartaoService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly alertas = inject(AlertaService);

  protected readonly rotuloTipo = ROTULO_TIPO_CARTAO;
  protected readonly cartoes = signal<Cartao[]>([]);
  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly carregando = signal(true);

  // A API de cartões só devolve o usuarioId; o nome vem da lista de usuários.
  private readonly nomesPorId = computed(
    () => new Map(this.usuarios().map((usuario) => [usuario.id, usuario.nome])),
  );

  ngOnInit(): void {
    forkJoin({ cartoes: this.cartaoService.listar(), usuarios: this.usuarioService.listar() })
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: ({ cartoes, usuarios }) => {
          this.cartoes.set(cartoes);
          this.usuarios.set(usuarios);
        },
        error: () => undefined,
      });
  }

  protected filtrar(evento: Event): void {
    const valor = (evento.target as HTMLSelectElement).value;
    const usuarioId = valor ? Number(valor) : undefined;

    this.carregando.set(true);
    this.cartaoService
      .listar(usuarioId)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({ next: (cartoes) => this.cartoes.set(cartoes), error: () => undefined });
  }

  protected nomeDoDono(cartao: Cartao): string {
    return this.nomesPorId().get(cartao.usuarioId) ?? `Usuário #${cartao.usuarioId}`;
  }

  protected alternarStatus(cartao: Cartao, evento: Event): void {
    const checkbox = evento.target as HTMLInputElement;
    const novoStatus = !cartao.status;

    this.cartaoService.alterarStatus(cartao.id, novoStatus).subscribe({
      next: (atualizado) => {
        this.cartoes.update((lista) => lista.map((c) => (c.id === atualizado.id ? atualizado : c)));
        this.alertas.sucesso(`Cartão ${atualizado.status ? 'ativado' : 'inativado'}.`);
      },
      // Se a API recusar, o switch volta para a posição anterior.
      error: () => (checkbox.checked = cartao.status),
    });
  }

  protected remover(cartao: Cartao): void {
    if (!confirm(`Remover o cartão ${cartao.numeroCartao} (${cartao.nome})?`)) {
      return;
    }

    this.cartaoService.remover(cartao.id).subscribe({
      next: () => {
        this.cartoes.update((lista) => lista.filter((c) => c.id !== cartao.id));
        this.alertas.sucesso('Cartão removido.');
      },
      error: () => undefined,
    });
  }
}
