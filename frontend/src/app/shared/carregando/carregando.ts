import { Component, input } from '@angular/core';

@Component({
  imports: [],
  selector: 'app-carregando',
  styleUrl: './carregando.scss',
  templateUrl: './carregando.html',
})
export class Carregando {
  readonly texto = input('Carregando...');
}
