import { Component, inject } from '@angular/core';
import { AlertaService } from './alerta.service';

@Component({
  imports: [],
  selector: 'app-alerta',
  styleUrl: './alerta.scss',
  templateUrl: './alerta.html',
})
export class Alerta {
  protected readonly alertas = inject(AlertaService);
}
