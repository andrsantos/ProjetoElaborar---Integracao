import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DisciplinaContextService {
  
  private disciplinaIdSubject: BehaviorSubject<string | null>;

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    let initialId = null;
    
    if (isPlatformBrowser(this.platformId)) {
      initialId = localStorage.getItem('disciplina_ativa_id');
    }
    
    this.disciplinaIdSubject = new BehaviorSubject<string | null>(initialId);
  }

  setDisciplinaAtiva(id: string): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('disciplina_ativa_id', id);
    }
    this.disciplinaIdSubject.next(id);
  }

  getDisciplinaAtivaId(): string | null {
    return this.disciplinaIdSubject.value;
  }

  getDisciplinaAtivaIdObservable(): Observable<string | null> {
    return this.disciplinaIdSubject.asObservable();
  }

  limparContexto(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('disciplina_ativa_id');
    }
    this.disciplinaIdSubject.next(null);
  }
}