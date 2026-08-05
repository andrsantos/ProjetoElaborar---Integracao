import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = `${environment.apiUrl}/api/auth`; 

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
    const token = this.getToken();
    
    if (!token) {
      return false;
    }

    try {
      const payloadBase64 = token.split('.')[1];
      
      const payloadDecoded = JSON.parse(atob(payloadBase64));
      
      const dataExpiracao = payloadDecoded.exp * 1000;
      const dataAtual = new Date().getTime();

      if (dataAtual > dataExpiracao) {
        this.logout(); 
        return false;
      }

      return true; 
      
    } catch (error) {
      console.error('Erro ao decodificar o token:', error);
      return false; 
    }
  }



  logout(): void {
    localStorage.removeItem('token_elaborar');
    this.router.navigate(['/login']);
  }
}