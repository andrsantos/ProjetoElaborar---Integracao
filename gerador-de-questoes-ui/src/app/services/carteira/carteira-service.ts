import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CarteiraService {
  private apiUrl = `${environment.apiUrl}/api/carteira`;

  constructor(private http: HttpClient) {}

  getSaldo(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/saldo`);
  }
}