import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, Inject, OnDestroy, OnInit, PLATFORM_ID } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Subscription, interval } from 'rxjs';
import { switchMap, startWith } from 'rxjs/operators';
import { JobService } from '../../services/job/job-service';
import { DisciplinaContextService } from '../../services/disciplina-context/disciplina-context-service';
import { JobResumo } from '../../models/job-resumo.model';

@Component({
  selector: 'app-processamentos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './processamentos.html',
  styleUrl: './processamentos.scss',
})
export class Processamentos implements OnInit, OnDestroy {

  public jobs: JobResumo[] = [];
  public isLoading: boolean = true;
  public disciplinaAtivaId: string | null = null;

  public autoRefreshAtivo: boolean = true;
  private pollingSubscription?: Subscription;
  private readonly INTERVALO_POLLING = 10000; 

  constructor(
    private jobService: JobService,
    private contextService: DisciplinaContextService,
    private toastr: ToastrService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    this.disciplinaAtivaId = this.contextService.getDisciplinaAtivaId();

    if (!this.disciplinaAtivaId) {
      if (isPlatformBrowser(this.platformId)) {
        this.toastr.error('Nenhuma disciplina selecionada.', 'Atenção');
        this.router.navigate(['/']);
      }
      return;
    }

    this.iniciarPolling();
  }

  ngOnDestroy(): void {
    this.pararPolling();
  }

  iniciarPolling(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    this.pararPolling(); 

    this.pollingSubscription = interval(this.INTERVALO_POLLING)
      .pipe(
        startWith(0), 
        switchMap(() => this.jobService.listarJobsPorDisciplina(this.disciplinaAtivaId!))
      )
      .subscribe({
        next: (res) => {
          this.jobs = res;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Erro ao buscar processamentos:', err);
          this.isLoading = false;
        }
      });
  }

  pararPolling(): void {
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
    }
  }

  toggleAutoRefresh(): void {
    this.autoRefreshAtivo = !this.autoRefreshAtivo;
    if (this.autoRefreshAtivo) {
      this.iniciarPolling();
      this.toastr.info('Atualização automática ativada.');
    } else {
      this.pararPolling();
      this.toastr.warning('Atualização automática pausada.');
    }
  }

  atualizarManualmente(): void {
    this.isLoading = true;
    this.jobService.listarJobsPorDisciplina(this.disciplinaAtivaId!).subscribe({
      next: (res) => {
        this.jobs = res;
        this.isLoading = false;
        this.toastr.success('Lista atualizada com sucesso!');
      },
      error: () => {
        this.toastr.error('Falha ao atualizar a lista.');
        this.isLoading = false;
      }
    });
  }

  verDetalhesErro(job: JobResumo): void {
    alert(`Detalhes da Falha (${job.nomeOriginal}):\n\n${job.mensagemErro || 'Erro desconhecido.'}`);
  }

  verQuestoes(job: JobResumo): void {
    this.router.navigate(['/gerenciamento/prova', job.id, 'questoes']);
  }

  deletarProcessamento(job: JobResumo): void {
    const confirmacao = window.confirm(`Tem certeza que deseja apagar o registro do processamento "${job.nomeOriginal}"? Essa ação não apaga o PDF original, mas limpará este histórico.`);
    
    if (confirmacao) {
      this.jobService.deletarJob(job.id).subscribe({
        next: () => {
          this.jobs = this.jobs.filter(j => j.id !== job.id);
          this.toastr.success('Processamento apagado com sucesso!');
        },
        error: (err) => {
          console.error('Erro ao deletar processamento:', err);
          if (err.status === 400) {
            this.toastr.warning('Não é possível apagar um processamento em andamento.', 'Aviso');
          } else {
            this.toastr.error('Ocorreu um erro ao tentar apagar o registro.', 'Erro');
          }
        }
      });
    }
  }



  
}