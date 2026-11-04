import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Alerta } from './alerta';
import { AlertaService } from './alerta.service';

describe('Alerta', () => {
  let fixture: ComponentFixture<Alerta>;
  let elemento: HTMLElement;
  let alertas: AlertaService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Alerta],
    }).compileComponents();

    fixture = TestBed.createComponent(Alerta);
    elemento = fixture.nativeElement;
    alertas = TestBed.inject(AlertaService);
    await fixture.whenStable();
  });

  afterEach(() => {
    alertas.fechar();
  });

  it('não mostra nada sem mensagem', () => {
    expect(elemento.querySelector('[role="alert"]')).toBeNull();
  });

  it('mostra o erro em vermelho', async () => {
    alertas.erro('Email já cadastrado.');
    await fixture.whenStable();

    const alerta = elemento.querySelector('[role="alert"]')!;
    expect(alerta.textContent).toContain('Email já cadastrado.');
    expect(alerta.classList).toContain('alert-danger');
  });

  it('mostra o sucesso em verde', async () => {
    alertas.sucesso('Usuário salvo.');
    await fixture.whenStable();

    expect(elemento.querySelector('[role="alert"]')!.classList).toContain('alert-success');
  });

  it('fecha pelo botão', async () => {
    alertas.erro('Falha.');
    await fixture.whenStable();

    elemento.querySelector<HTMLButtonElement>('.btn-close')!.click();
    await fixture.whenStable();

    expect(elemento.querySelector('[role="alert"]')).toBeNull();
  });
});
