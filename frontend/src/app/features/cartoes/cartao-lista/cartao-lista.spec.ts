import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Cartao } from '../../../models/cartao';
import { Usuario } from '../../../models/usuario';
import { AlertaService } from '../../../shared/alerta/alerta.service';
import { UsuarioService } from '../../usuarios/usuario.service';
import { CartaoService } from '../cartao.service';
import { CartaoLista } from './cartao-lista';

describe('CartaoLista', () => {
  let fixture: ComponentFixture<CartaoLista>;
  let elemento: HTMLElement;
  let alertas: AlertaService;

  const usuarios: Usuario[] = [
    { id: 1, nome: 'Ana', email: 'ana@email.com', criadoEm: '2026-11-02T10:00:00', cartoes: [] },
    {
      id: 2,
      nome: 'Bruno',
      email: 'bruno@email.com',
      criadoEm: '2026-11-02T11:00:00',
      cartoes: [],
    },
  ];

  const cartoes: Cartao[] = [
    {
      id: 10,
      numeroCartao: 111,
      nome: 'Principal',
      status: true,
      tipoCartao: 'COMUM',
      usuarioId: 1,
    },
    {
      id: 11,
      numeroCartao: 222,
      nome: 'Escola',
      status: false,
      tipoCartao: 'ESTUDANTE',
      usuarioId: 2,
    },
  ];

  const cartaoService = { listar: vi.fn(), alterarStatus: vi.fn(), remover: vi.fn() };
  const usuarioService = { listar: vi.fn() };

  async function criar(lista: Cartao[] = cartoes) {
    vi.clearAllMocks();
    cartaoService.listar.mockReturnValue(of(lista));
    cartaoService.remover.mockReturnValue(of(undefined));
    usuarioService.listar.mockReturnValue(of(usuarios));

    await TestBed.configureTestingModule({
      imports: [CartaoLista],
      providers: [
        provideRouter([]),
        { provide: CartaoService, useValue: cartaoService },
        { provide: UsuarioService, useValue: usuarioService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CartaoLista);
    elemento = fixture.nativeElement;
    alertas = TestBed.inject(AlertaService);
    await fixture.whenStable();
  }

  const cards = () => Array.from(elemento.querySelectorAll('.card'));
  const switchDo = (card: Element) => card.querySelector<HTMLInputElement>('[role="switch"]')!;

  afterEach(() => {
    alertas.fechar();
    vi.restoreAllMocks();
  });

  it('mostra cada cartão com o nome do dono, o tipo e o status', async () => {
    await criar();

    expect(cards()).toHaveLength(2);
    expect(cards()[0].textContent).toContain('Principal');
    expect(cards()[0].textContent).toContain('Ana');
    expect(cards()[1].textContent).toContain('Estudante');
    expect(switchDo(cards()[0]).checked).toBe(true);
    expect(switchDo(cards()[1]).checked).toBe(false);
  });

  it('monta o filtro com os usuários', async () => {
    await criar();

    const opcoes = Array.from(elemento.querySelectorAll('#filtro-usuario option')).map((opcao) =>
      opcao.textContent?.trim(),
    );
    expect(opcoes).toEqual(['Todos', 'Ana', 'Bruno']);
  });

  it('filtra pelos cartões do usuário escolhido', async () => {
    await criar();
    cartaoService.listar.mockReturnValue(of([cartoes[1]]));

    const filtro = elemento.querySelector<HTMLSelectElement>('#filtro-usuario')!;
    filtro.value = '2';
    filtro.dispatchEvent(new Event('change'));
    await fixture.whenStable();

    expect(cartaoService.listar).toHaveBeenLastCalledWith(2);
    expect(cards()).toHaveLength(1);
  });

  it('inativa um cartão pelo switch', async () => {
    await criar();
    cartaoService.alterarStatus.mockReturnValue(of({ ...cartoes[0], status: false }));

    switchDo(cards()[0]).click();
    await fixture.whenStable();

    expect(cartaoService.alterarStatus).toHaveBeenCalledWith(10, false);
    expect(cards()[0].textContent).toContain('Inativo');
    expect(alertas.mensagem()?.texto).toBe('Cartão inativado.');
  });

  it('volta o switch se a API recusar a mudança', async () => {
    await criar();
    cartaoService.alterarStatus.mockReturnValue(throwError(() => new Error('falhou')));

    switchDo(cards()[0]).click();
    await fixture.whenStable();

    expect(switchDo(cards()[0]).checked).toBe(true);
  });

  it('remove o cartão depois de confirmar', async () => {
    await criar();
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    cards()[0].querySelector<HTMLButtonElement>('.btn-outline-danger')!.click();
    await fixture.whenStable();

    expect(cartaoService.remover).toHaveBeenCalledWith(10);
    expect(cards()).toHaveLength(1);
  });

  it('não remove quando o usuário cancela', async () => {
    await criar();
    vi.spyOn(window, 'confirm').mockReturnValue(false);

    cards()[0].querySelector<HTMLButtonElement>('.btn-outline-danger')!.click();
    await fixture.whenStable();

    expect(cartaoService.remover).not.toHaveBeenCalled();
  });

  it('avisa quando não há cartões', async () => {
    await criar([]);

    expect(elemento.textContent).toContain('Nenhum cartão encontrado.');
  });
});
