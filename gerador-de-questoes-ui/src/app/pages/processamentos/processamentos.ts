import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, Inject, OnDestroy, OnInit, PLATFORM_ID } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Subscription, interval } from 'rxjs';
import { switchMap, startWith, take } from 'rxjs/operators';
import { JobService } from '../../services/job/job-service';
import { DisciplinaContextService } from '../../services/disciplina-context/disciplina-context-service';
import { JobResumo } from '../../models/job-resumo.model';
import { DisciplinaService } from '../../services/disciplina/disciplina-service';

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
  public nomeDisciplina: string = 'Carregando...';

  public autoRefreshAtivo: boolean = true;
  private pollingSubscription?: Subscription;
  private readonly INTERVALO_POLLING = 10000; 

  public isModalTaxonomiaOpen: boolean = false;
  public jobSelecionado: JobResumo | null = null;
  public detalhesJobCompleto: any = null;
  public isLoadingDetalhes: boolean = false;

  // Variáveis de controle para filtros da tabela e paginação
  public filtroTermo: string = '';
  public statusFiltro: string = 'TODOS';
  public ordenacao: string = 'dataCriacao_desc';
  public paginaAtual: number = 1;
  public itensPorPagina: number = 7;



  constructor(
    private jobService: JobService,
    private contextService: DisciplinaContextService,
    private disciplinaService: DisciplinaService,
    private toastr: ToastrService,
    private router: Router,
    private route: ActivatedRoute,
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

    this.jobService.marcarVisualizados(this.disciplinaAtivaId).subscribe({
      error: (err) => console.error('Erro ao marcar jobs como visualizados:', err)
    });

    this.disciplinaService.buscarNomeDisciplina(this.disciplinaAtivaId).subscribe({
      next: (res) => {
        this.nomeDisciplina = res.nome;
      },
      error: (err) => {
        console.error('Erro ao buscar o nome da disciplina:', err);
        this.nomeDisciplina = 'Nome Indisponível';
      }
    });

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

          this.route.queryParams.pipe(take(1)).subscribe(params => {
            const jobIdParaAbrir = params['abrirModal'];
            if (jobIdParaAbrir && this.jobs.length > 0) {
              const jobEncontrado = this.jobs.find(j => j.id === jobIdParaAbrir);
              if (jobEncontrado && !this.isModalTaxonomiaOpen) {
                this.abrirModalTaxonomia(jobEncontrado);
                
                this.router.navigate([], {
                  relativeTo: this.route,
                  queryParams: { abrirModal: null },
                  queryParamsHandling: 'merge'
                });
              }
            }
          });
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
    this.router.navigate(['/revisao', job.id]);
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


  abrirModalTaxonomia(job: JobResumo): void {
    this.jobSelecionado = job;
    this.isModalTaxonomiaOpen = true;
    this.isLoadingDetalhes = true;
    this.detalhesJobCompleto = null;

    this.jobService.consultarStatusJob(job.id).subscribe({
      next: (jobCompleto) => {
        this.detalhesJobCompleto = jobCompleto;
        this.isLoadingDetalhes = false;
      },
      error: (err) => {
        console.error('Erro ao buscar detalhes do job', err);
        this.toastr.error('Não foi possível carregar os detalhes.');
        this.isLoadingDetalhes = false;
      }
    });
  }

  fecharModalTaxonomia(): void {
    this.isModalTaxonomiaOpen = false;
    this.jobSelecionado = null;
    this.detalhesJobCompleto = null;
  }

// LÓGICA DE FILTROS DA TABELA
  resetarPagina(): void {
    this.paginaAtual = 1;
  }

  get jobsFiltradosEOrdenados(): JobResumo[] {
    let resultado = this.jobs; 

    if (this.filtroTermo.trim()) {
      const termo = this.filtroTermo.toLowerCase();
      resultado = resultado.filter(j => 
        (j.nomeOriginal && j.nomeOriginal.toLowerCase().includes(termo)) ||
        (j.tipo && j.tipo.toLowerCase().includes(termo))
      );
    }

    if (this.statusFiltro !== 'TODOS') {
      resultado = resultado.filter(j => j.status === this.statusFiltro);
    }

    resultado = resultado.sort((a, b) => {
      switch (this.ordenacao) {
        case 'dataCriacao_desc':
          return new Date(b.dataCriacao).getTime() - new Date(a.dataCriacao).getTime();
        case 'dataCriacao_asc':
          return new Date(a.dataCriacao).getTime() - new Date(b.dataCriacao).getTime();
        case 'nome_asc':
          const nomeA = a.nomeOriginal || a.tipo || '';
          const nomeB = b.nomeOriginal || b.tipo || '';
          return nomeA.localeCompare(nomeB);
        default:
          return 0;
      }
    });

    return resultado;
  }

  get jobsPaginados(): JobResumo[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    const fim = inicio + this.itensPorPagina;
    return this.jobsFiltradosEOrdenados.slice(inicio, fim);
  }

  get totalPaginas(): number {
    return Math.ceil(this.jobsFiltradosEOrdenados.length / this.itensPorPagina) || 1;
  }

  mudarPagina(proxima: boolean): void {
    if (proxima && this.paginaAtual < this.totalPaginas) {
      this.paginaAtual++;
    } else if (!proxima && this.paginaAtual > 1) {
      this.paginaAtual--;
    }
  }
////////////////




  
}