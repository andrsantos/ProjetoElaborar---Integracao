import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://187.77.240.149:8082/api/auth'; 

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  login(credenciais: any): Observable<any> {
    return this.http.post<{ token: string }>(`${this.apiUrl}/login`, credenciais).pipe(
      tap(response => {
        if (response && response.token) {
          this.setToken(response.token);
        }
      })
    );
  }

  registrar(dados: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/registrar`, dados, { responseType: 'text' });
  }

  setToken(token: string): void {
    localStorage.setItem('token_elaborar', token);
  }


  getToken(): string | null {
    if (typeof window !== 'undefined' && window.localStorage) {
      return localStorage.getItem('token_elaborar');
    }
    return null;
  }

  isAutenticado(): boolean {
    return !!this.getToken();
  }

  logout(): void {
    localStorage.removeItem('token_elaborar');
    this.router.navigate(['/login']);
  }
}