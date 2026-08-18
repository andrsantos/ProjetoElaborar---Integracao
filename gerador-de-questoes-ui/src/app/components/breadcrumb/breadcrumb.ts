import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { Router, NavigationEnd, RouterLink } from '@angular/router';
import { filter } from 'rxjs/operators';
import { CarteiraService } from '../../services/carteira/carteira-service';

interface BreadcrumbModel {
  label: string;
  url: string;
}

@Component({
  selector: 'app-breadcrumb',
  standalone: true,
  imports: [CommonModule, RouterLink], 
  templateUrl: './breadcrumb.html',
  styleUrls: ['./breadcrumb.scss']
})
export class Breadcrumb implements OnInit {
  breadcrumbs: BreadcrumbModel[] = [];
  public saldoAtual: number = 0;

  constructor(
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object ,
    private carteiraService: CarteiraService
  ) {}

  ngOnInit(): void {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.buildHierarchy(event.urlAfterRedirects);
      });
    this.buildHierarchy(this.router.url);
    if (isPlatformBrowser(this.platformId)) {
      this.carregarSaldo();
    }
  }

  carregarSaldo(): void {
    this.carteiraService.getSaldo().subscribe({
      next: (saldo) => {
        this.saldoAtual = saldo;
      },
      error: (err) => {
        console.error('Erro ao buscar o saldo da carteira', err);
      }
    });
  }

  private buildHierarchy(url: string): void {
    if (url.includes('/login')) {
      this.breadcrumbs = [];
      return;
    }

    const crumbs: BreadcrumbModel[] = [];

    crumbs.push({ label: 'Início', url: '/' });

    if (url === '/' || url === '/inicio') {
      this.breadcrumbs = crumbs;
      return;
    }

    const subPaginasDisciplina = [
      '/gerenciamento', '/banco-questoes', '/gerar-prova', 
      '/provas-salvas', '/alimentacao'
    ];
    
    const isRotaDisciplina = subPaginasDisciplina.some(rota => url.includes(rota)) || url.includes('/painel');

    if (isRotaDisciplina) {
      let disciplinaId = this.extrairIdDaUrl(url);

      if (isPlatformBrowser(this.platformId)) {
        if (!disciplinaId) {
          disciplinaId = sessionStorage.getItem('lastDisciplinaId');
        } else {
          sessionStorage.setItem('lastDisciplinaId', disciplinaId);
        }
      }

      const painelUrl = disciplinaId ? `/painel/${disciplinaId}` : '/';
      
      if (!url.endsWith(painelUrl)) {
        crumbs.push({ label: 'Painel da Disciplina', url: painelUrl });
      }
    }

    const labelAtual = this.getLabelForUrl(url);
    if (labelAtual && labelAtual !== 'Início' && labelAtual !== 'Painel da Disciplina') {
      crumbs.push({ label: labelAtual, url: url });
    } else if (labelAtual === 'Painel da Disciplina') {
      crumbs.push({ label: 'Painel da Disciplina', url: url });
    }

    this.breadcrumbs = crumbs;
  }

  private extrairIdDaUrl(url: string): string | null {
    const match = url.match(/\/painel\/([a-zA-Z0-9_-]+)/);
    return match ? match[1] : null;
  }

  private getLabelForUrl(url: string): string {

    const urlLimpa = url.split('?')[0];

    if (urlLimpa === '/' || urlLimpa === '/inicio') return 'Início';
    if (urlLimpa.includes('/painel')) return 'Painel da Disciplina';
    if (urlLimpa.includes('/gerenciamento')) return 'Gerenciamento RAG';
    if (urlLimpa.includes('/banco-questoes')) return 'Banco de Questões';
    if (urlLimpa.includes('/gerar-prova/automatico')) return 'Automático';
    if (urlLimpa.includes('/gerar-prova/rapida')) return 'Rápida';
    if (urlLimpa.includes('/gerar-prova/manual')) return 'Manual';
    if (urlLimpa.includes('/gerar-prova')) return 'Gerador de Prova';
    if (urlLimpa.includes('/provas-salvas')) return 'Provas Salvas';
    if (urlLimpa.includes('/alimentacao')) return 'Alimentação RAG';
    if (urlLimpa.includes('/detalhe-prompt')) return 'Detalhes de Prompt'; 
    
    const segments = urlLimpa.split('/').filter(seg => seg !== '');
    const lastSegment = segments[segments.length - 1];
    if (lastSegment) {
      return lastSegment.charAt(0).toUpperCase() + lastSegment.slice(1).replace(/-/g, ' ');
    }
    
    return '';
    
  }


}