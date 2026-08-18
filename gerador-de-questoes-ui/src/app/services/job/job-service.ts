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

  listarJobs(): Observable<ExtracaoJob[]> {
    return this.http.get<ExtracaoJob[]>(this.apiUrl);
  }

  listarJobsPorDisciplina(disciplinaId: string): Observable<JobResumo[]> {
  return this.http.get<JobResumo[]>(`${this.apiUrl}/disciplina/${disciplinaId}`);
  }

  deletarJob(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  
}
