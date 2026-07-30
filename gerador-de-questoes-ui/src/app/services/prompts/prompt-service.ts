import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Prompt } from '../../models/prompt.model'; 

@Injectable({
  providedIn: 'root',
})
export class PromptService {
  
  private apiUrl = '${environment.apiUrl}/api/prompts';

  constructor(private http: HttpClient) {}

  cadastrarPrompt(prompt: Prompt): Observable<Prompt> {
    return this.http.post<Prompt>(this.apiUrl, prompt);
  }

  listarPromptsPorDocumento(documentoId: string): Observable<Prompt[]> {
    return this.http.get<Prompt[]>(`${this.apiUrl}/documento/${documentoId}`);
  }

  listarTodos(): Observable<Prompt[]> {
    return this.http.get<Prompt[]>(this.apiUrl);
  }

  atualizarPrompt(id: string, prompt: Prompt): Observable<Prompt> {
    return this.http.put<Prompt>(`${this.apiUrl}/${id}`, prompt);
  }

  deletarPrompt(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  alternarStatusPrompt(id: string, novoStatus: boolean): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/status?ativo=${novoStatus}`, null);
  }

  editarPrompt(id: string, promptData: any): Observable<Prompt> {
    return this.http.put<Prompt>(`${this.apiUrl}/${id}`, promptData);
  }

}