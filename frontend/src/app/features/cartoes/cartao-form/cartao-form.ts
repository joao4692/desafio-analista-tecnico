import { Component, OnInit, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { CartaoCreateRequest } from '../../../models/cartao';
import { ROTULO_TIPO_CARTAO, TIPOS_CARTAO, TipoCartao } from '../../../models/tipo-cartao';
import { Usuario } from '../../../models/usuario';
import { AlertaService } from '../../../shared/alerta/alerta.service';
import { UsuarioService } from '../../usuarios/usuario.service';
import { CartaoService } from '../cartao.service';

type Campo = 'usuarioId' | 'numeroCartao' | 'nome' | 'tipoCartao';

@Component({
  imports: [ReactiveFormsModule, RouterLink],
  selector: 'app-cartao-form',
  styleUrl: './cartao-form.scss',
  templateUrl: './cartao-form.html',
})
export class CartaoForm implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly cartaoService = inject(CartaoService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly alertas = inject(AlertaService);
  private readonly router = inject(Router);

  protected readonly tipos = TIPOS_CARTAO;
  protected readonly rotuloTipo = ROTULO_TIPO_CARTAO;
  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly salvando = signal(false);

  protected readonly form = this.fb.group({
    usuarioId: this.fb.control<number | null>(null, Validators.required),
    // Inteiro positivo, como o @Positive do back.
    numeroCartao: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.min(1),
      Validators.pattern(/^\d+$/),
    ]),
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    tipoCartao: this.fb.control<TipoCartao>('COMUM', Validators.required),
  });

  ngOnInit(): void {
    this.usuarioService.listar().subscribe({
      next: (usuarios) => this.usuarios.set(usuarios),
      error: () => undefined,
    });
  }

  protected salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    // Com o form válido, usuarioId e numeroCartao não são mais null.
    const dados = this.form.getRawValue() as CartaoCreateRequest;

    this.salvando.set(true);
    this.cartaoService
      .incluir(dados)
      .pipe(finalize(() => this.salvando.set(false)))
      .subscribe({
        next: () => {
          this.alertas.sucesso('Cartão cadastrado.');
          this.router.navigate(['/cartoes']);
        },
        error: () => undefined,
      });
  }

  protected invalido(campo: Campo): boolean {
    const controle = this.form.controls[campo];
    return controle.invalid && controle.touched;
  }
}
