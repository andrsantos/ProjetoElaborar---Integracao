import { Injectable, PLATFORM_ID, Inject, NgZone } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject, Subscription } from 'rxjs'; 
import { DisciplinaContextService } from '../disciplina-context/disciplina-context-service';
import { JobResumo } from '../../models/job-resumo.model';
import { JobService } from '../job/job-service';
import { environment } from '../../../environments/environment'; 

@Injectable({
  providedIn: 'root' 
})
export class JobNotificationService {
  
  private hasNotificationSubject = new BehaviorSubject<boolean>(false);
  public hasNotification$ = this.hasNotificationSubject.asObservable();

  private unreadJobsSubject = new BehaviorSubject<JobResumo[]>([]);
  public unreadJobs$ = this.unreadJobsSubject.asObservable();

  private contextSubscription?: Subscription;
  private disciplinaAtualId: string | null = null;
  
  private eventSource?: EventSource; 

  constructor(
    private jobService: JobService,
    private contextService: DisciplinaContextService,
    private ngZone: NgZone, 
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  iniciarMonitoramento(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    this.contextSubscription = this.contextService.getDisciplinaAtivaIdObservable().subscribe(id => {
      this.disciplinaAtualId = id;
      if (id) {
        this.reiniciarConexaoSse();
      } else {
        this.pararConexaoSse();
        this.limparNotificacoes();
      }
    });
  }

  private reiniciarConexaoSse(): void {
    this.pararConexaoSse();
    
    if (!this.disciplinaAtualId) return;

    this.buscarEAtualizarJobs();

    const url = `${environment.apiUrl}/api/admin/jobs/disciplina/${this.disciplinaAtualId}/stream`;
    
    this.eventSource = new EventSource(url);

    this.eventSource.addEventListener('job-update', (event: MessageEvent) => {
      
      this.ngZone.run(() => {
        console.log('[SSE] Atualização detectada no servidor! Recarregando lista...');
        this.buscarEAtualizarJobs();
      });

    });

    this.eventSource.onerror = (error) => {
      console.warn('[SSE] Oscilação na conexão em tempo real. Tentando reconectar...');
    };
  }

  private buscarEAtualizarJobs(): void {
    if (!this.disciplinaAtualId) return;

    this.jobService.listarJobsPorDisciplina(this.disciplinaAtualId).subscribe({
      next: (jobs: JobResumo[]) => {
        
        const notificacoesNaoLidas = jobs.filter(job => 
          (job.status === 'COMPLETED' || 
           job.status === 'PARCIALMENTE_CONCLUIDO' || 
           job.status === 'ERROR') 
           && !job.visualizado
        );

        this.unreadJobsSubject.next(notificacoesNaoLidas);
        this.hasNotificationSubject.next(notificacoesNaoLidas.length > 0);
        
      },
      error: (err) => console.error('Erro ao buscar jobs para notificação:', err)
    });
  }

  pararMonitoramento(): void {
    this.pararConexaoSse();
    if (this.contextSubscription) {
      this.contextSubscription.unsubscribe();
    }
  }

  private pararConexaoSse(): void {
    if (this.eventSource) {
      this.eventSource.close(); 
      this.eventSource = undefined;
    }
  }

  private limparNotificacoes(): void {
    this.hasNotificationSubject.next(false);
    this.unreadJobsSubject.next([]);
  }
}