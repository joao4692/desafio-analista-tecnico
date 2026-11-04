import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { Usuario } from '../../../models/usuario';
import { AlertaService } from '../../../shared/alerta/alerta.service';
import { UsuarioService } from '../usuario.service';
import { UsuarioForm } from './usuario-form';

describe('UsuarioForm', () => {
  let fixture: ComponentFixture<UsuarioForm>;
  let elemento: HTMLElement;
  let alertas: AlertaService;
  let navegar: ReturnType<typeof vi.spyOn>;

  const ana: Usuario = {
    id: 3,
    nome: 'Ana',
    email: 'ana@email.com',
    criadoEm: '2026-11-02T10:00:00',
    cartoes: [],
  };

  const usuarioService = {
    buscar: vi.fn(),
    incluir: vi.fn(),
    alterar: vi.fn(),
  };

  async function criar(id?: string) {
    vi.clearAllMocks();
    usuarioService.buscar.mockReturnValue(of(ana));
    usuarioService.incluir.mockReturnValue(of(ana));
    usuarioService.alterar.mockReturnValue(of(ana));

    await TestBed.configureTestingModule({
      imports: [UsuarioForm],
      providers: [
        provideRouter([{ path: 'usuarios', children: [] }]),
        { provide: UsuarioService, useValue: usuarioService },
      ],
    }).compileComponents();

    navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    alertas = TestBed.inject(AlertaService);

    fixture = TestBed.createComponent(UsuarioForm);
    if (id) {
      fixture.componentRef.setInput('id', id);
    }
    elemento = fixture.nativeElement;
    await fixture.whenStable();
  }

  function preencher(campo: string, valor: string) {
    const input = elemento.querySelector<HTMLInputElement>(`#${campo}`)!;
    input.value = valor;
    input.dispatchEvent(new Event('input'));
  }

  async function enviar() {
    elemento.querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
    await fixture.whenStable();
  }

  afterEach(() => {
    alertas.fechar();
    vi.restoreAllMocks();
  });

  describe('incluir', () => {
    beforeEach(async () => {
      await criar();
    });

    it('não envia com campos vazios e mostra os erros', async () => {
      await enviar();

      expect(usuarioService.incluir).not.toHaveBeenCalled();
      expect(elemento.querySelectorAll('.is-invalid')).toHaveLength(3);
    });

    it('exige a senha', async () => {
      preencher('nome', 'Ana');
      preencher('email', 'ana@email.com');
      await enviar();

      expect(usuarioService.incluir).not.toHaveBeenCalled();
      expect(elemento.querySelector('#senha')!.classList).toContain('is-invalid');
    });

    it('não aceita email inválido', async () => {
      preencher('nome', 'Ana');
      preencher('email', 'ana.email.com');
      preencher('senha', '123456');
      await enviar();

      expect(usuarioService.incluir).not.toHaveBeenCalled();
      expect(elemento.querySelector('#email')!.classList).toContain('is-invalid');
    });

    it('cadastra e volta para a lista', async () => {
      preencher('nome', 'Ana');
      preencher('email', 'ana@email.com');
      preencher('senha', '123456');
      await enviar();

      expect(usuarioService.incluir).toHaveBeenCalledWith({
        nome: 'Ana',
        email: 'ana@email.com',
        senha: '123456',
      });
      expect(alertas.mensagem()?.texto).toBe('Usuário cadastrado.');
      expect(navegar).toHaveBeenCalledWith(['/usuarios']);
    });
  });

  describe('editar', () => {
    beforeEach(async () => {
      await criar('3');
    });

    it('carrega os dados do usuário', () => {
      expect(usuarioService.buscar).toHaveBeenCalledWith(3);
      expect(elemento.querySelector('h1')!.textContent).toContain('Editar usuário');
      expect(elemento.querySelector<HTMLInputElement>('#nome')!.value).toBe('Ana');
      expect(elemento.querySelector<HTMLInputElement>('#email')!.value).toBe('ana@email.com');
    });

    it('salva sem senha, mantendo a atual', async () => {
      preencher('nome', 'Ana Souza');
      await enviar();

      expect(usuarioService.alterar).toHaveBeenCalledWith(3, {
        nome: 'Ana Souza',
        email: 'ana@email.com',
        senha: '',
      });
      expect(alertas.mensagem()?.texto).toBe('Usuário atualizado.');
      expect(navegar).toHaveBeenCalledWith(['/usuarios']);
    });

    it('não aceita senha curta', async () => {
      preencher('senha', '123');
      await enviar();

      expect(usuarioService.alterar).not.toHaveBeenCalled();
    });
  });
});
