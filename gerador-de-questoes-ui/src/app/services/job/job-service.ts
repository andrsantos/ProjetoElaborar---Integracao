import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ExtracaoJob } from '../../models/extracao-job.model';

@Injectable({
  providedIn: 'root',
})
export class JobService {
  private apiUrl = '${environment.apiUrl}/api/admin/jobs'; 
  
  constructor(private http: HttpClient) {}

  listarJobs(): Observable<ExtracaoJob[]> {
    return this.http.get<ExtracaoJob[]>(this.apiUrl);
  }
}
