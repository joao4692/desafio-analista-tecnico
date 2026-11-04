import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AlertaService } from '../../../shared/alerta/alerta.service';
import { UsuarioService } from '../usuario.service';

type Campo = 'nome' | 'email' | 'senha';

// Mesmo formulário para incluir (/usuarios/novo) e editar (/usuarios/:id).
// Os limites de tamanho são os mesmos das validações do back.
@Component({
  imports: [ReactiveFormsModule, RouterLink],
  selector: 'app-usuario-form',
  styleUrl: './usuario-form.scss',
  templateUrl: './usuario-form.html',
})
export class UsuarioForm implements OnInit {
  // Vem do parâmetro :id da rota (withComponentInputBinding).
  readonly id = input<string>();

  private readonly fb = inject(NonNullableFormBuilder);
  private readonly usuarioService = inject(UsuarioService);
  private readonly alertas = inject(AlertaService);
  private readonly router = inject(Router);

  protected readonly edicao = computed(() => !!this.id());
  protected readonly salvando = signal(false);

  protected readonly form = this.fb.group({
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(160)]],
    // Na edição a senha é opcional: vazia mantém a atual.
    senha: ['', [Validators.minLength(6), Validators.maxLength(72)]],
  });

  ngOnInit(): void {
    const id = this.id();

    if (!id) {
      this.form.controls.senha.addValidators(Validators.required);
      this.form.controls.senha.updateValueAndValidity();
      return;
    }

    this.usuarioService.buscar(Number(id)).subscribe({
      next: (usuario) => this.form.patchValue({ nome: usuario.nome, email: usuario.email }),
      error: () => undefined,
    });
  }

  protected salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const id = this.id();
    const dados = this.form.getRawValue();
    const requisicao = id
      ? this.usuarioService.alterar(Number(id), dados)
      : this.usuarioService.incluir(dados);

    this.salvando.set(true);
    requisicao.pipe(finalize(() => this.salvando.set(false))).subscribe({
      next: () => {
        this.alertas.sucesso(id ? 'Usuário atualizado.' : 'Usuário cadastrado.');
        this.router.navigate(['/usuarios']);
      },
      // Erro de validação do back (ex.: email repetido) aparece pelo interceptor.
      error: () => undefined,
    });
  }

  protected invalido(campo: Campo): boolean {
    const controle = this.form.controls[campo];
    return controle.invalid && controle.touched;
  }
}
