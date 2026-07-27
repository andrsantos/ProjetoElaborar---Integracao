import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BancoQuestao } from '../../models/banco-questao.model';

@Injectable({
  providedIn: 'root',
})
export class BancoQuestoesService {
  private readonly API_URL = 'http://localhost:8080/api/banco-questoes';

  constructor(private http: HttpClient) {}

  salvarQuestao(questao: any): Observable<any> {
    return this.http.post(this.API_URL, questao);
  }
  listarTodas(): Observable<any[]> {
    return this.http.get<any[]>(this.API_URL);
  }

  listarTodasPorDisciplina(disciplinaId: string): Observable<any[]> {
    console.log("Disciplina ID", disciplinaId);
    return this.http.get<any[]>(`${this.API_URL}/listar/${disciplinaId}`);
  }


  buscarPorId(id: string): Observable<any> {
    return this.http.get<any>(`${this.API_URL}/${id}`);
  }
  atualizarQuestao(id: string, questao: any): Observable<any> {
    return this.http.put(`${this.API_URL}/${id}`, questao);
  }
  excluirQuestao(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
  cadastrarQuestao(questao: BancoQuestao): Observable<any> {
    console.log("Questão a ser cadastrada:", questao);
    return this.http.post(`${this.API_URL}/cadastrar`, questao);
  }

  cadastrarLote(payload: { jobId: string, questoes: BancoQuestao[] }): Observable<any> {
    console.log("Lote a ser cadastrado (JobID):", payload.jobId);
    return this.http.post(`${this.API_URL}/cadastrar-lote`, payload);
  }

  
  cadastrarPdfOrigem(file: File): Observable<any>{
    const formData: FormData = new FormData();
    formData.append('file', file, file.name);
    return this.http.post(`${this.API_URL}/cadastrar/pdf/upload`, formData);
  }

  getConceitosPorDisciplina(disciplinaId: string): Observable<string[]> {
    const url = `${this.API_URL}/disciplina/${disciplinaId}/conceitos`;
    return this.http.get<string[]>(url);
  }


}
  
