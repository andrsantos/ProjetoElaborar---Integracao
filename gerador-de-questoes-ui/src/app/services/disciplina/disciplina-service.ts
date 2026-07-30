import { Injectable } from '@angular/core';
import { Disciplina } from '../../models/disciplina.model';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class DisciplinaService {

  private apiUrl = '${environment.apiUrl}/api/disciplinas'; 

  constructor(private http: HttpClient) {}

  listarDisciplinas(): Observable<Disciplina[]> {
    return this.http.get<Disciplina[]>(this.apiUrl);
  }

  criarDisciplina(disciplina: Disciplina): Observable<Disciplina> {
    return this.http.post<Disciplina>(this.apiUrl, disciplina);
  }

  buscarPorId(id: string): Observable<Disciplina> {
    return this.http.get<Disciplina>(`${this.apiUrl}/${id}`);
  }

  buscarNomeDisciplina(id: string) {
    return this.http.get<{nome: string}>(`${this.apiUrl}/${id}/nome`);
  }

  deletarDisciplina(id: string): Observable<any> {
  return this.http.delete(`${this.apiUrl}/${id}`);
}
  
}
