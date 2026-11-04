import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Cartao } from '../../models/cartao';
import { CartaoService } from './cartao.service';

describe('CartaoService', () => {
  let service: CartaoService;
  let backend: HttpTestingController;

  const cartao: Cartao = {
    id: 10,
    numeroCartao: 123456,
    nome: 'Principal',
    status: true,
    tipoCartao: 'COMUM',
    usuarioId: 1,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CartaoService);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    backend.verify();
  });

  it('lista todos os cartões', () => {
    let resposta: Cartao[] | undefined;
    service.listar().subscribe((cartoes) => (resposta = cartoes));

    const req = backend.expectOne('/api/cartoes');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.has('usuarioId')).toBe(false);
    req.flush([cartao]);

    expect(resposta).toEqual([cartao]);
  });

  it('filtra os cartões por usuário', () => {
    service.listar(1).subscribe();

    const req = backend.expectOne((r) => r.url === '/api/cartoes');
    expect(req.request.params.get('usuarioId')).toBe('1');
    req.flush([cartao]);
  });

  it('adiciona um cartão a um usuário', () => {
    const dados = {
      numeroCartao: 123456,
      nome: 'Principal',
      tipoCartao: 'COMUM' as const,
      usuarioId: 1,
    };
    service.incluir(dados).subscribe();

    const req = backend.expectOne('/api/cartoes');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dados);
    req.flush(cartao);
  });

  it('ativa ou inativa um cartão', () => {
    service.alterarStatus(10, false).subscribe();

    const req = backend.expectOne('/api/cartoes/10/status');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: false });
    req.flush({ ...cartao, status: false });
  });

  it('remove um cartão', () => {
    service.remover(10).subscribe();

    const req = backend.expectOne('/api/cartoes/10');
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });
  });
});
