import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { DisciplinaContextService } from '../../services/disciplina-context/disciplina-context-service';

@Injectable({
  providedIn: 'root'
})
export class DisciplinaGuard implements CanActivate {

  constructor(
    private contextService: DisciplinaContextService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object 
  ) {}

  canActivate(): boolean | UrlTree {
    
    if (!isPlatformBrowser(this.platformId)) {
      return true; 
    }

    const idDisciplina = this.contextService.getDisciplinaAtivaId();

    if (idDisciplina) {
      return true;
    }

    return this.router.createUrlTree(['/']);
  }
}