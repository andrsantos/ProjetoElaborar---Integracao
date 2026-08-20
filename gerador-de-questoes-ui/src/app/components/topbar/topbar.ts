import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { JobNotificationService } from '../../services/job-notification/job-notification-service';
import { DisciplinaContextService } from '../../services/disciplina-context/disciplina-context-service';
import { JobResumo } from '../../models/job-resumo.model';
import { JobService } from '../../services/job/job-service';
import { UsuarioService } from '../../services/usuario/usuario-service';
import { NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './topbar.html',
  styleUrls: ['./topbar.scss']
})
export class Topbar implements OnInit, OnDestroy {
  
  public temNotificacao: boolean = false;
  public mostrarDropdown: boolean = false;
  private notificationSub?: Subscription;
  public notificacoes: JobResumo[] = [];
  private unreadJobsSub?: Subscription;
  public nomeUsuario: string = 'Carregando...';
  public mostrarNotificacoes: boolean = false;

  constructor(
    private notificationService: JobNotificationService,
    private contextService: DisciplinaContextService,
    private usuarioService: UsuarioService, 
    private jobService: JobService,
    private router: Router
  ) {}

  ngOnInit() {
    this.notificationSub = this.notificationService.hasNotification$.subscribe(status => {
      this.temNotificacao = status;
    });

    this.unreadJobsSub = this.notificationService.unreadJobs$.subscribe(jobs => {
      this.notificacoes = jobs;
    });

    this.usuarioService.obterMeuPerfil().subscribe({
      next: (perfil) => {
        this.nomeUsuario = perfil.nome || 'Professor(a)';
      },
      error: (err) => {
        console.error('Erro ao buscar o perfil do usuário:', err);
        this.nomeUsuario = 'Professor(a)';
      }
    });

    this.verificarVisibilidadeNotificacoes(this.router.url);

    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.verificarVisibilidadeNotificacoes(event.urlAfterRedirects);
      });
  }

  private verificarVisibilidadeNotificacoes(url: string): void {
    const urlLimpa = url.split('?')[0];
    
    if (urlLimpa === '/' || urlLimpa === '/inicio' || urlLimpa === '/login') {
      this.mostrarNotificacoes = false;
      this.fecharDropdown(); 
    } else {
      this.mostrarNotificacoes = true;
    }
  }

  ngOnDestroy() {
    if (this.notificationSub) {
      this.notificationSub.unsubscribe();
    }
    if (this.unreadJobsSub) {
      this.unreadJobsSub.unsubscribe();
    }
  }

  marcarTodasComoLidas() {
    const disciplinaId = this.contextService.getDisciplinaAtivaId();
    if (disciplinaId) {
      this.jobService.marcarVisualizados(disciplinaId).subscribe({
        next: () => {
          this.notificacoes = [];
          this.temNotificacao = false;
          this.fecharDropdown();
        }
      });
    }
  }

  toggleDropdown() {
    this.mostrarDropdown = !this.mostrarDropdown;
  }

  fecharDropdown() {
    this.mostrarDropdown = false;
  }

  sairDoSistema(event: Event) {
    event.preventDefault();
    this.contextService.limparContexto();
    if (typeof window !== 'undefined' && window.localStorage) {
      localStorage.removeItem('token_elaborar');
    }
    this.router.navigate(['/login']);
  }

  clicarNotificacao(aviso: JobResumo) {
    this.fecharDropdown();

    this.jobService.marcarVisualizadoIndividual(aviso.id).subscribe({
      next: () => {
        this.notificacoes = this.notificacoes.filter(n => n.id !== aviso.id);
        
        if (this.notificacoes.length === 0) {
          this.temNotificacao = false;
        }
      },
      error: (err) => console.error('Falha ao marcar notificação individual como lida:', err)
    });

    if (aviso.tipo === 'EDICAO_TAXONOMIA') {
      this.router.navigate(['/processamentos'], { queryParams: { abrirModal: aviso.id } });
    } else {
      this.router.navigate(['/revisao', aviso.id]);
    }
  }
}