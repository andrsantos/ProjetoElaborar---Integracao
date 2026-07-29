import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CarteiraService {
  private apiUrl = `http://localhost:8082/api/carteira`;

  constructor(private http: HttpClient) {}

  getSaldo(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/saldo`);
  }
}