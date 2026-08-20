import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router'; 
import { DisciplinaContextService } from '../../services/disciplina-context/disciplina-context-service';
import { DisciplinaService } from '../../services/disciplina/disciplina-service';
import { JobNotificationService } from '../../services/job-notification/job-notification-service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.scss']
})
export class Sidebar implements OnInit, OnDestroy {
  
  temDisciplinaAtiva = false;
  nomeDisciplinaAtiva = '';
  private notificationSub?: Subscription; 

  linksInativos = [
    { path: '/', label: 'Início', exact: true, icon: 'fas fa-home' }
  ];

  linksExibidos: any[] = [];

  constructor(
    private contextService: DisciplinaContextService,
    private disciplinaService: DisciplinaService,
    private router: Router,
    private notificationService: JobNotificationService
  ) {}


  ngOnInit() {
    this.linksExibidos = this.linksInativos;

    this.notificationService.iniciarMonitoramento();

    this.contextService.getDisciplinaAtivaIdObservable().subscribe(id => {
      this.temDisciplinaAtiva = !!id; 
      
      if (id) {
        this.linksExibidos = [
          { path: '/', label: 'Voltar para o início', exact: true, isExit: true, icon: 'fas fa-arrow-left' },
          { path: `/painel/${id}`, label: 'Painel da Disciplina', exact: true, icon: 'fas fa-chart-line' }, 
          { path: '/gerenciamento', label: 'Gerenciamento', exact: false, icon: 'fas fa-sliders-h' },
          { path: '/gerar-prova', label: 'Geração', exact: false, icon: 'fas fa-magic' },
          { path: '/banco-questoes', label: 'Banco de Questões', exact: false, icon: 'fas fa-database' },
          { path: '/provas-salvas', label: 'Provas Salvas', exact: false, icon: 'fas fa-file-pdf' },
          { path: '/alimentacao', label: 'Extração de Questões', exact: false, icon: 'fas fa-robot' },
          { path: '/processamentos', label: 'Processamentos', exact: false, icon: 'fas fa-tasks', hasNotification: false }
        ];

        this.disciplinaService.buscarNomeDisciplina(id).subscribe({
          next: (resposta) => this.nomeDisciplinaAtiva = resposta.nome,
          error: () => this.nomeDisciplinaAtiva = 'Ambiente de Trabalho'
        });
      } else {
        this.nomeDisciplinaAtiva = ''; 
        this.linksExibidos = this.linksInativos;
      }
    });

    this.notificationSub = this.notificationService.hasNotification$.subscribe(temNotificacao => {
      const linkProcessamento = this.linksExibidos.find(l => l.path === '/processamentos');
      if (linkProcessamento) {
        linkProcessamento.hasNotification = temNotificacao;
      }
    });
  }

  ngOnDestroy(): void {
    this.notificationService.pararMonitoramento();
    if (this.notificationSub) {
      this.notificationSub.unsubscribe();
    }
  }

  lidarComClique(link: any, event: Event) {
    if (link.isExit) {
      event.preventDefault(); 
      this.contextService.limparContexto(); 
      this.router.navigate(['/']); 
    }
  }

  sairDoSistema(event: Event) {
    event.preventDefault();
    
    this.contextService.limparContexto();
    
    if (typeof window !== 'undefined' && window.localStorage) {
      localStorage.removeItem('token_elaborar');
    }
    
    this.router.navigate(['/login']);
  }
}