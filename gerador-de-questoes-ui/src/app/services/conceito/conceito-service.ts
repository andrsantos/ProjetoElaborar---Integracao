import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ConceitoEntity {
  id?: string;
  nome: string;
  descricao?: string;
  disciplina?: string;
  tipoOrigem?: string;
  origemId?: string;
}

export interface TaxonomiaDTO {
  nomeDisciplina: string;
  palavrasChave: string[];
  topicos: string[];
}

@Injectable({
  providedIn: 'root',
})
export class ConceitoService {
  
  private readonly API_URL = `${environment.apiUrl}/api/conceitos`;

  constructor(private http: HttpClient) {}

  criarConceito(conceito: ConceitoEntity): Observable<ConceitoEntity> {
    return this.http.post<ConceitoEntity>(this.API_URL, conceito);
  }

  listarConceitos(): Observable<ConceitoEntity[]> {
    return this.http.get<ConceitoEntity[]>(this.API_URL);
  }

  buscarPorId(id: string): Observable<ConceitoEntity> {
    return this.http.get<ConceitoEntity>(`${this.API_URL}/${id}`);
  }

  buscarPorNome(nome: string): Observable<ConceitoEntity> {
    const params = new HttpParams().set('nome', nome);
    return this.http.get<ConceitoEntity>(`${this.API_URL}/buscar`, { params });
  }

  atualizarConceito(id: string, conceito: ConceitoEntity): Observable<ConceitoEntity> {
    return this.http.put<ConceitoEntity>(`${this.API_URL}/${id}`, conceito);
  }


  sincronizarTaxonomia(disciplinaId: string, taxonomiaDTO: TaxonomiaDTO): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/disciplina/${disciplinaId}/sincronizar`, taxonomiaDTO);
  }

  deletarConceito(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}