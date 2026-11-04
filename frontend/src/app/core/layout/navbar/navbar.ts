import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

// O menu do celular é controlado aqui, sem o JS do Bootstrap.
// A partir de lg, a classe navbar-expand-lg mostra os links direto na barra.
@Component({
  imports: [RouterLink, RouterLinkActive],
  selector: 'app-navbar',
  styleUrl: './navbar.scss',
  templateUrl: './navbar.html',
})
export class Navbar {
  protected readonly menuAberto = signal(false);

  protected alternarMenu(): void {
    this.menuAberto.update((aberto) => !aberto);
  }

  protected fecharMenu(): void {
    this.menuAberto.set(false);
  }
}
