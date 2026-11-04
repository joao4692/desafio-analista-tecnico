import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { Cartao } from '../../../models/cartao';
import { Usuario } from '../../../models/usuario';
import { AlertaService } from '../../../shared/alerta/alerta.service';
import { UsuarioService } from '../../usuarios/usuario.service';
import { CartaoService } from '../cartao.service';
import { CartaoForm } from './cartao-form';

describe('CartaoForm', () => {
  let fixture: ComponentFixture<CartaoForm>;
  let elemento: HTMLElement;
  let alertas: AlertaService;
  let navegar: ReturnType<typeof vi.spyOn>;

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

  const criado: Cartao = {
    id: 10,
    numeroCartao: 123456,
    nome: 'Escola',
    status: true,
    tipoCartao: 'ESTUDANTE',
    usuarioId: 2,
  };

  const cartaoService = { incluir: vi.fn() };
  const usuarioService = { listar: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();
    cartaoService.incluir.mockReturnValue(of(criado));
    usuarioService.listar.mockReturnValue(of(usuarios));

    await TestBed.configureTestingModule({
      imports: [CartaoForm],
      providers: [
        provideRouter([{ path: 'cartoes', children: [] }]),
        { provide: CartaoService, useValue: cartaoService },
        { provide: UsuarioService, useValue: usuarioService },
      ],
    }).compileComponents();

    navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    alertas = TestBed.inject(AlertaService);

    fixture = TestBed.createComponent(CartaoForm);
    elemento = fixture.nativeElement;
    await fixture.whenStable();
  });

  afterEach(() => {
    alertas.fechar();
    vi.restoreAllMocks();
  });

  function escolher(campo: string, indice: number) {
    const select = elemento.querySelector<HTMLSelectElement>(`#${campo}`)!;
    select.selectedIndex = indice;
    select.dispatchEvent(new Event('change'));
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

  it('lista os usuários para escolher o dono', () => {
    const opcoes = Array.from(elemento.querySelectorAll('#usuarioId option')).map((opcao) =>
      opcao.textContent?.trim(),
    );
    expect(opcoes).toEqual(['Selecione', 'Ana', 'Bruno']);
  });

  it('começa com o tipo Comum', () => {
    const tipo = elemento.querySelector<HTMLSelectElement>('#tipoCartao')!;
    expect(tipo.options[tipo.selectedIndex].textContent?.trim()).toBe('Comum');
  });

  it('não envia com campos vazios', async () => {
    await enviar();

    expect(cartaoService.incluir).not.toHaveBeenCalled();
    expect(elemento.querySelectorAll('.is-invalid')).toHaveLength(3);
  });

  it('não aceita número zero ou com casas decimais', async () => {
    escolher('usuarioId', 1);
    preencher('nome', 'Principal');

    preencher('numeroCartao', '0');
    await enviar();
    expect(cartaoService.incluir).not.toHaveBeenCalled();

    preencher('numeroCartao', '12.5');
    await enviar();
    expect(cartaoService.incluir).not.toHaveBeenCalled();
  });

  it('cadastra o cartão e volta para a lista', async () => {
    escolher('usuarioId', 2);
    preencher('numeroCartao', '123456');
    preencher('nome', 'Escola');
    escolher('tipoCartao', 1);
    await enviar();

    expect(cartaoService.incluir).toHaveBeenCalledWith({
      usuarioId: 2,
      numeroCartao: 123456,
      nome: 'Escola',
      tipoCartao: 'ESTUDANTE',
    });
    expect(alertas.mensagem()?.texto).toBe('Cartão cadastrado.');
    expect(navegar).toHaveBeenCalledWith(['/cartoes']);
  });
});
