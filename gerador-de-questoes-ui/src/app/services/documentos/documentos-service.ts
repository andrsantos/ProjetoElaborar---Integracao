import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PdfQuestaoDTO {
  id: string;
  nomeOriginal: string;
  contentType: string;
  tamanhoBytes: number;
  dataUpload: string;
  promptId?: string;
}

@Injectable({
  providedIn: 'root',
})
export class DocumentosService {

  private readonly API_URL = `${environment.apiUrl}/api/documentos`;

  constructor(private http: HttpClient) {}

  listarConceitosPorDocumento(documentoId: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.API_URL}/${documentoId}/conceitos`);
  }

  buscarArquivoPorId(arquivoId: string): Observable<PdfQuestaoDTO> {
    return this.http.get<PdfQuestaoDTO>(`${this.API_URL}/arquivo/${arquivoId}`);
  }
  
}