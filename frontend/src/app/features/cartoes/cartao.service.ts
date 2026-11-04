import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Cartao, CartaoCreateRequest } from '../../models/cartao';

@Injectable({ providedIn: 'root' })
export class CartaoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/cartoes`;

  // Sem usuarioId, lista os cartões de todos os usuários.
  listar(usuarioId?: number): Observable<Cartao[]> {
    const params = usuarioId ? new HttpParams().set('usuarioId', usuarioId) : undefined;
    return this.http.get<Cartao[]>(this.url, { params });
  }

  incluir(dados: CartaoCreateRequest): Observable<Cartao> {
    return this.http.post<Cartao>(this.url, dados);
  }

  alterarStatus(id: number, status: boolean): Observable<Cartao> {
    return this.http.patch<Cartao>(`${this.url}/${id}/status`, { status });
  }

  remover(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
