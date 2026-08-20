import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ExtracaoJob } from '../../models/extracao-job.model';
import { environment } from '../../../environments/environment';
import { JobResumo } from '../../models/job-resumo.model';

@Injectable({
  providedIn: 'root',
})
export class JobService {
  private apiUrl = `${environment.apiUrl}/api/admin/jobs`; 
  
  constructor(private http: HttpClient) {}

  consultarStatusJob(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  listarJobs(): Observable<ExtracaoJob[]> {
    return this.http.get<ExtracaoJob[]>(this.apiUrl);
  }

  listarJobsPorDisciplina(disciplinaId: string): Observable<JobResumo[]> {
    return this.http.get<JobResumo[]>(`${this.apiUrl}/disciplina/${disciplinaId}`);
  }

  deletarJob(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  marcarVisualizados(disciplinaId: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/disciplina/${disciplinaId}/marcar-vistos`, {});
  }
  

  marcarVisualizadoIndividual(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/marcar-visto`, {});
  }

  consolidarJob(id: string): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/consolidar`, {});
  }
}