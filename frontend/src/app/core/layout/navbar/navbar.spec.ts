import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Navbar } from './navbar';

describe('Navbar', () => {
  let fixture: ComponentFixture<Navbar>;
  let elemento: HTMLElement;

  const menu = () => elemento.querySelector('#menu-principal')!;
  const botaoMenu = () => elemento.querySelector<HTMLButtonElement>('.navbar-toggler')!;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Navbar],
      providers: [
        provideRouter([
          { path: 'usuarios', children: [] },
          { path: 'cartoes', children: [] },
        ]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Navbar);
    elemento = fixture.nativeElement;
    await fixture.whenStable();
  });

  it('começa com o menu fechado', () => {
    expect(menu().classList).not.toContain('show');
    expect(botaoMenu().getAttribute('aria-expanded')).toBe('false');
  });

  it('abre e fecha o menu pelo botão', async () => {
    botaoMenu().click();
    await fixture.whenStable();
    expect(menu().classList).toContain('show');
    expect(botaoMenu().getAttribute('aria-expanded')).toBe('true');

    botaoMenu().click();
    await fixture.whenStable();
    expect(menu().classList).not.toContain('show');
  });

  it('fecha o menu ao escolher uma página', async () => {
    botaoMenu().click();
    await fixture.whenStable();

    elemento.querySelector<HTMLAnchorElement>('a.nav-link[href="/cartoes"]')!.click();
    await fixture.whenStable();

    expect(menu().classList).not.toContain('show');
  });

  it('tem os links de usuários e cartões', () => {
    const links = Array.from(elemento.querySelectorAll('.nav-link')).map((link) =>
      link.getAttribute('href'),
    );
    expect(links).toEqual(['/usuarios', '/cartoes']);
  });
});
