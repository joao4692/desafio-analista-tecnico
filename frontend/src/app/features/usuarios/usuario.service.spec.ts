import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Usuario } from '../../models/usuario';
import { UsuarioService } from './usuario.service';

describe('UsuarioService', () => {
  let service: UsuarioService;
  let backend: HttpTestingController;

  const ana: Usuario = {
    id: 1,
    nome: 'Ana',
    email: 'ana@email.com',
    criadoEm: '2026-11-02T10:00:00',
    cartoes: [],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UsuarioService);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    backend.verify();
  });

  it('lista os usuários', () => {
    let resposta: Usuario[] | undefined;
    service.listar().subscribe((usuarios) => (resposta = usuarios));

    const req = backend.expectOne('/api/usuarios');
    expect(req.request.method).toBe('GET');
    req.flush([ana]);

    expect(resposta).toEqual([ana]);
  });

  it('busca um usuário pelo id', () => {
    service.buscar(1).subscribe();

    const req = backend.expectOne('/api/usuarios/1');
    expect(req.request.method).toBe('GET');
    req.flush(ana);
  });

  it('inclui um usuário', () => {
    const dados = { nome: 'Ana', email: 'ana@email.com', senha: '123456' };
    service.incluir(dados).subscribe();

    const req = backend.expectOne('/api/usuarios');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dados);
    req.flush(ana);
  });

  it('altera um usuário', () => {
    const dados = { nome: 'Ana Souza', email: 'ana@email.com', senha: '' };
    service.alterar(1, dados).subscribe();

    const req = backend.expectOne('/api/usuarios/1');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(dados);
    req.flush({ ...ana, nome: 'Ana Souza' });
  });

  it('remove um usuário', () => {
    service.remover(1).subscribe();

    const req = backend.expectOne('/api/usuarios/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });
  });
});
