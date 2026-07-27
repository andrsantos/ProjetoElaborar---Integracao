import { HttpClient, HttpEvent } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AlimentacaoService {

  private readonly API_URL = 'http://localhost:8082/api/alimentacao';

  private readonly API_URL_2 = 'http://localhost:8082/api/admin/material/upload/questoes';

  private readonly API_URL_3 = 'http://localhost:8082/api/admin/material/upload';

  private readonly API_URL_4 = 'http://localhost:8082/api/documentacao';

  private readonly API_URL_5 = 'http://localhost:8082/api/admin/jobs';

  private readonly API_URL_6 = 'http://localhost:8082/api/gerenciamento';






  constructor(private http: HttpClient) { }



  uploadPdf(file: File, titulo: string, disciplinaId: string): Observable<HttpEvent<any>> {
    const formData: FormData = new FormData();
    formData.append('file', file, file.name);
    formData.append('titulo', titulo);
    formData.append('disciplinaId', disciplinaId);
    
    console.log("Form Data enviando:", { arquivo: file.name, titulo, disciplinaId });
    
    return this.http.post(`${this.API_URL_6}/upload`, formData, {
      reportProgress: true,
      observe: 'events',
      responseType: 'text'
    });
  }

  uploadQuestoes(file: File): Observable<HttpEvent<any>> {
    const formData: FormData = new FormData();
    formData.append('file', file, file.name);

    return this.http.post<any[]>(this.API_URL_2, formData, {
      reportProgress: true,
      observe: 'events',
    });
  }


  uploadQuestoesAsync(file: File, disciplinaId: string, prompt: string, modoExtracao: string): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('disciplinaId', disciplinaId);
    
    formData.append('modoExtracao', modoExtracao);
    
    if (prompt && prompt.trim() !== '') {
      formData.append('prompt', prompt.trim());
    }

    return this.http.post(`${this.API_URL_2}/async`, formData);
  }

  consultarStatusJob(jobId: string): Observable<any> {
    return this.http.get(`${this.API_URL_5}/${jobId}`);
  }
  
}

