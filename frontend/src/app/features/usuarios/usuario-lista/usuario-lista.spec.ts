import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { Usuario } from '../../../models/usuario';
import { AlertaService } from '../../../shared/alerta/alerta.service';
import { UsuarioService } from '../usuario.service';
import { UsuarioLista } from './usuario-lista';

describe('UsuarioLista', () => {
  let fixture: ComponentFixture<UsuarioLista>;
  let elemento: HTMLElement;
  let alertas: AlertaService;

  const usuarios: Usuario[] = [
    { id: 1, nome: 'Ana', email: 'ana@email.com', criadoEm: '2026-11-02T10:00:00', cartoes: [] },
    {
      id: 2,
      nome: 'Bruno',
      email: 'bruno@email.com',
      criadoEm: '2026-11-02T11:00:00',
      cartoes: [
        { id: 10, numeroCartao: 111, nome: 'Principal', status: true, tipoCartao: 'COMUM' },
        { id: 11, numeroCartao: 222, nome: 'Escola', status: false, tipoCartao: 'ESTUDANTE' },
      ],
    },
  ];

  const usuarioService = {
    listar: vi.fn(),
    remover: vi.fn(),
  };

  async function criar(lista: Usuario[]) {
    vi.clearAllMocks();
    usuarioService.listar.mockReturnValue(of(lista));
    usuarioService.remover.mockReturnValue(of(undefined));

    await TestBed.configureTestingModule({
      imports: [UsuarioLista],
      providers: [provideRouter([]), { provide: UsuarioService, useValue: usuarioService }],
    }).compileComponents();

    fixture = TestBed.createComponent(UsuarioLista);
    elemento = fixture.nativeElement;
    alertas = TestBed.inject(AlertaService);
    await fixture.whenStable();
  }

  const cards = () => Array.from(elemento.querySelectorAll('.card'));

  afterEach(() => {
    alertas.fechar();
    vi.restoreAllMocks();
  });

  it('mostra um card por usuário, com a quantidade de cartões', async () => {
    await criar(usuarios);

    expect(cards()).toHaveLength(2);
    expect(cards()[0].textContent).toContain('Ana');
    expect(cards()[0].textContent).toContain('Nenhum cartão');
    expect(cards()[1].textContent).toContain('2 cartões');
  });

  it('avisa quando não há usuários', async () => {
    await criar([]);

    expect(cards()).toHaveLength(0);
    expect(elemento.textContent).toContain('Nenhum usuário cadastrado.');
  });

  it('remove o usuário depois de confirmar', async () => {
    await criar(usuarios);
    const confirmar = vi.spyOn(window, 'confirm').mockReturnValue(true);

    cards()[1].querySelector<HTMLButtonElement>('.btn-outline-danger')!.click();
    await fixture.whenStable();

    expect(confirmar).toHaveBeenCalledWith(
      'Remover Bruno? Os 2 cartões dele também serão removidos.',
    );
    expect(usuarioService.remover).toHaveBeenCalledWith(2);
    expect(cards()).toHaveLength(1);
    expect(alertas.mensagem()).toEqual({ tipo: 'sucesso', texto: 'Bruno foi removido.' });
  });

  it('não remove quando o usuário cancela', async () => {
    await criar(usuarios);
    vi.spyOn(window, 'confirm').mockReturnValue(false);

    cards()[0].querySelector<HTMLButtonElement>('.btn-outline-danger')!.click();
    await fixture.whenStable();

    expect(usuarioService.remover).not.toHaveBeenCalled();
    expect(cards()).toHaveLength(2);
  });
});
