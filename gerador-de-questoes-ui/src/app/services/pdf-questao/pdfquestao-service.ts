import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment'; 

export interface PdfQuestaoResumo {
  id: string;
  nomeOriginal: string;
  tamanhoBytes: number;
  dataUpload: string;
  quantidadeQuestoes: number;
}

@Injectable({
  providedIn: 'root',
})
export class PdfquestaoService {
  
  private readonly API_URL = `${environment.apiUrl}/api/gerenciamento`;

  constructor(private http: HttpClient) { }


  listarProvas(): Observable<PdfQuestaoResumo[]> {
    return this.http.get<PdfQuestaoResumo[]>(`${this.API_URL}/listar/provas`);
  }

  listarProvasResumo(disciplinaId: string): Observable<any> {
    return this.http.get(`${this.API_URL}/listar/provas/${disciplinaId}`);
  }

  baixarProva(id: string): Observable<Blob> {
    return this.http.get(`${this.API_URL}/download-prova/${id}`, { responseType: 'blob' });
  }

  excluirProva(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/deletar-prova/${id}`);
  }
  
}