import { TestBed } from '@angular/core/testing';
import { Carregando } from './carregando';

describe('Carregando', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Carregando],
    }).compileComponents();
  });

  it('mostra o texto padrão para leitores de tela', async () => {
    const fixture = TestBed.createComponent(Carregando);
    await fixture.whenStable();

    const status = (fixture.nativeElement as HTMLElement).querySelector('[role="status"]')!;
    expect(status.textContent).toContain('Carregando...');
  });

  it('aceita outro texto', async () => {
    const fixture = TestBed.createComponent(Carregando);
    fixture.componentRef.setInput('texto', 'Buscando cartões...');
    await fixture.whenStable();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Buscando cartões...');
  });
});
