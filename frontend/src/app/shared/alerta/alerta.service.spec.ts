import { TestBed } from '@angular/core/testing';
import { AlertaService } from './alerta.service';

describe('AlertaService', () => {
  let service: AlertaService;

  beforeEach(() => {
    vi.useFakeTimers();
    service = TestBed.inject(AlertaService);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('começa sem mensagem', () => {
    expect(service.mensagem()).toBeNull();
  });

  it('mensagem de sucesso some sozinha depois de alguns segundos', () => {
    service.sucesso('Usuário salvo.');
    expect(service.mensagem()).toEqual({ tipo: 'sucesso', texto: 'Usuário salvo.' });

    vi.advanceTimersByTime(AlertaService.TEMPO_SUCESSO_MS);
    expect(service.mensagem()).toBeNull();
  });

  it('mensagem de erro fica até ser fechada', () => {
    service.erro('Email já cadastrado.');
    vi.advanceTimersByTime(AlertaService.TEMPO_SUCESSO_MS * 2);
    expect(service.mensagem()).toEqual({ tipo: 'erro', texto: 'Email já cadastrado.' });

    service.fechar();
    expect(service.mensagem()).toBeNull();
  });

  it('um erro logo depois de um sucesso não é apagado pelo timer do sucesso', () => {
    service.sucesso('Cartão removido.');
    service.erro('Falha ao carregar os cartões.');

    vi.advanceTimersByTime(AlertaService.TEMPO_SUCESSO_MS);
    expect(service.mensagem()?.tipo).toBe('erro');
  });
});
