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
      '/provas-salvas', '/alimentacao', '/processamentos'
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

    if (url.includes('/gerar-prova/')) {
      crumbs.push({ label: 'Gerador de Prova', url: '/gerar-prova' });
      
      if (url.includes('/gerar-prova/escolher-modelo-prova/')) {
        crumbs.push({ label: 'Escolher Modelo Prova', url: '/gerar-prova/escolher-modelo-prova' });
      }
    }

    if (url.includes('/gerenciamento/')) {
      crumbs.push({ label: 'Gerenciamento RAG', url: '/gerenciamento' });
    }




    if (url.includes('/banco-questoes/')) {
      crumbs.push({ label: 'Banco de Questões', url: '/banco-questoes' });

      if (url.includes('/banco-questoes/gerenciar/')) {
        crumbs.push({ label: 'Gerenciar', url: '/banco-questoes/gerenciar' });
      }


    }

    if (url.includes('/provas-salvas/')) {
      crumbs.push({ label: 'Provas Salvas', url: '/provas-salvas' });
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


    // === ROTAS ESTÁTICAS BÁSICAS ===
    if (urlLimpa === '/' || urlLimpa === '/inicio') return 'Início';
    if (urlLimpa.includes('/painel')) return 'Painel da Disciplina';
    if (urlLimpa.includes('/alimentacao')) return 'Alimentação RAG';
    if (urlLimpa.includes('/detalhe-prompt')) return 'Detalhes de Prompt'; 
    if (urlLimpa.includes('/processamentos')) return 'Processamentos'; 

     // === ROTAS FILHAS DO PROVAS SALVAS ===
    if (urlLimpa.includes('/provas-salvas/')) return 'Detalhamento de Prova';
    if (urlLimpa.endsWith('/provas-salvas')) return 'Provas Salvas';


    // === ROTAS FILHAS DO BANCO DE QUESTÕES ===
    if (urlLimpa.includes('/banco-questoes/gerenciar/taxonomia')) return 'Taxonomia';
    if (urlLimpa.includes('/banco-questoes/gerenciar')) return 'Gerenciar';
    if (urlLimpa.includes('/banco-questoes/novo')) return 'Nova Questão';
    if (urlLimpa.includes('/banco-questoes/selecionar-questao')) return 'Selecionar Questão';
    if (urlLimpa.endsWith('/banco-questoes')) return 'Banco de Questões';


    // === ROTAS FILHAS DE GERAR PROVA ===
    if (urlLimpa.includes('/gerar-prova/escolher-modelo-prova/rapida')) return 'Prova Rápida';
    if (urlLimpa.includes('/gerar-prova/escolher-modelo-prova')) return 'Escolher Modelo Prova';
    if (urlLimpa.includes('/gerar-prova/automatico')) return 'Geração Automática';
    if (urlLimpa.includes('/gerar-prova/rapida')) return 'Geração Rápida';
    if (urlLimpa.includes('/gerar-prova/manual')) return 'Geração Manual';
    if (urlLimpa.includes('/gerar-prova/prova-builder')) return 'Prova Builder';
    if (urlLimpa.endsWith('/gerar-prova')) return 'Gerador de Prova';

    // === ROTAS DE GERENCIAMENTO RAG ===
    if (urlLimpa.includes('/gerenciamento/prova/') && urlLimpa.includes('/questoes')) return 'Auditoria de Questões';
    if (urlLimpa.includes('/gerenciamento/detalhe-prompt')) return 'Detalhes de Prompt';
    if (urlLimpa.endsWith('/gerenciamento')) return 'Gerenciamento RAG';


    
    const segments = urlLimpa.split('/').filter(seg => seg !== '');
    const lastSegment = segments[segments.length - 1];
    if (lastSegment) {
      return lastSegment.charAt(0).toUpperCase() + lastSegment.slice(1).replace(/-/g, ' ');
    }
    
    return '';
  }


}