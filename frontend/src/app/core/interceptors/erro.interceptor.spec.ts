import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AlertaService } from '../../shared/alerta/alerta.service';
import { erroInterceptor } from './erro.interceptor';

describe('erroInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  let alertas: AlertaService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([erroInterceptor])),
        provideHttpClientTesting(),
      ],
    });

    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
    alertas = TestBed.inject(AlertaService);
  });

  afterEach(() => {
    backend.verify();
    alertas.fechar();
  });

  it('mostra a mensagem que vem do back', () => {
    let erroRecebido = false;
    http.post('/api/usuarios', {}).subscribe({ error: () => (erroRecebido = true) });

    backend
      .expectOne('/api/usuarios')
      .flush(
        { status: 409, erro: 'Conflict', mensagem: 'Email já cadastrado.' },
        { status: 409, statusText: 'Conflict' },
      );

    expect(alertas.mensagem()).toEqual({ tipo: 'erro', texto: 'Email já cadastrado.' });
    // A tela também precisa saber que deu erro.
    expect(erroRecebido).toBe(true);
  });

  it('avisa quando não consegue falar com o servidor', () => {
    http.get('/api/usuarios').subscribe({ error: () => undefined });

    backend.expectOne('/api/usuarios').error(new ProgressEvent('error'), { status: 0 });

    expect(alertas.mensagem()?.texto).toContain('Não foi possível conectar');
  });

  it('usa uma mensagem genérica quando o erro não tem corpo', () => {
    http.get('/api/cartoes').subscribe({ error: () => undefined });

    backend.expectOne('/api/cartoes').flush(null, { status: 500, statusText: 'Server Error' });

    expect(alertas.mensagem()?.texto).toBe('Erro inesperado (status 500). Tente novamente.');
  });

  it('não mostra nada quando a chamada dá certo', () => {
    http.get('/api/cartoes').subscribe();

    backend.expectOne('/api/cartoes').flush([]);

    expect(alertas.mensagem()).toBeNull();
  });
});
