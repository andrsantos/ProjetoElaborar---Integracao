import { Component, OnInit, OnDestroy, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Subject, timer } from 'rxjs';
import { switchMap, takeUntil } from 'rxjs/operators';
import { AlimentacaoService } from '../../../services/alimentacao/alimentacao-service';
import { BancoQuestoesService } from '../../../services/banco-questoes/banco-questoes';
import { BancoQuestao } from '../../../models/banco-questao.model';
import { DisciplinaContextService } from '../../../services/disciplina-context/disciplina-context-service';
import { ProvaService } from '../../../services/prova/prova-service';

export type CampoEdicao = 'enunciado' | 'resposta' | 'a' | 'b' | 'c' | 'd' | 'e';

export interface EstadoEdicaoExtraida {
  indexQuestao: number;
  campo: CampoEdicao;
}

@Component({
  selector: 'app-revisao',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './revisao.html', 
  styleUrls: ['./revisao.scss']  
})
export class Revisao implements OnInit, OnDestroy {
  public jobId: string = '';
  public caminhoArquivoTemporario: string = '';
  public statusJob: string = 'PENDING'; 
  public mensagemErro: string = '';
  public questoesExtraidas: any[] = []; 
  public questoesAprovadas = new Set<number>();
  public editando: EstadoEdicaoExtraida | null = null;
  public isSalvando = false;
  private destroy$ = new Subject<void>();
  public disciplinaAtivaId: string | null = null;
  public criarProvaRapida: boolean = false; 
  public modoExtracao: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private alimentacaoService: AlimentacaoService,
    private bancoQuestoesService: BancoQuestoesService,
    private toastr: ToastrService,
    private contextService: DisciplinaContextService,
    private provaService: ProvaService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    this.disciplinaAtivaId = this.contextService.getDisciplinaAtivaId();

    if (!this.disciplinaAtivaId) {
      if (isPlatformBrowser(this.platformId)) {
        this.toastr.error('Nenhuma disciplina selecionada. Retornando ao início.', 'Atenção');
        this.router.navigate(['/']);
      }
      return; 
    }

    this.jobId = this.route.snapshot.paramMap.get('id') || '';
    if (!this.jobId) {
      this.toastr.error('ID da extração não encontrado.');
      this.router.navigate(['/alimentacao']);
      return;
    }
    
    this.iniciarPolling();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private iniciarPolling(): void {
    timer(0, 3000).pipe(
      takeUntil(this.destroy$),
      switchMap(() => this.alimentacaoService.consultarStatusJob(this.jobId))
    ).subscribe({
      next: (job) => {
        this.statusJob = job.status;
        
        if (job.status === 'COMPLETED' || job.status === 'PARCIALMENTE_CONCLUIDO') {
          this.destroy$.next(); 
          
          this.questoesExtraidas = JSON.parse(job.resultadoJson);
          this.caminhoArquivoTemporario = job.caminhoArquivoTemporario;
          this.modoExtracao = job.modoExtracao || 'APENAS_ORIGINAIS';
          console.log("Questões extraídas", this.questoesExtraidas);
          
          this.prepararRevisao();
          
          if (job.status === 'COMPLETED') {
            this.toastr.success('Extração concluída com sucesso!');
          } else {
            this.toastr.warning(
              job.mensagemErro || 'O saldo esgotou durante o processamento. As questões geradas até o momento foram salvas com sucesso.', 
              'Processamento Parcial', 
              { timeOut: 8000 }
            );
          }
          
        } else if (job.status === 'ERROR') {
          this.destroy$.next(); 
          this.mensagemErro = job.mensagemErro || 'Ocorreu um erro no processamento da IA.';
          this.toastr.error(this.mensagemErro, 'Falha na extração');
        }
      },
      error: () => {
        this.destroy$.next();
        this.statusJob = 'ERROR';
        this.mensagemErro = 'Não foi possível conectar ao servidor para verificar o status.';
      }
    });
  }

  prepararRevisao(): void {
    this.questoesAprovadas.clear();
    this.questoesExtraidas.forEach((_, i) => this.questoesAprovadas.add(i));
  }

  isSugestao(q: any): boolean {
    return q.id?.startsWith('INS') || q.id?.startsWith('VAR');
  }

  toggleAprovacao(index: number): void {
    this.questoesAprovadas.has(index) ? this.questoesAprovadas.delete(index) : this.questoesAprovadas.add(index);
  }

  aprovarTodas(): void {
    this.questoesAprovadas.clear();
    this.questoesExtraidas.forEach((_, i) => this.questoesAprovadas.add(i));
  }

  marcarTodasOriginais(): void {
    this.questoesExtraidas.forEach((q, i) => {
      if (!this.isSugestao(q)) this.questoesAprovadas.add(i);
    });
  }

  marcarTodasSugeridas(): void {
    this.questoesExtraidas.forEach((q, i) => {
      if (this.isSugestao(q)) this.questoesAprovadas.add(i);
    });
  }

  desmarcarTodas(): void {
    this.questoesAprovadas.clear();
  }

  ativarEdicao(index: number, campo: string): void {
    this.editando = { indexQuestao: index, campo: campo as CampoEdicao };
  }

  isEditando(index: number, campo: string): boolean {
    return this.editando?.indexQuestao === index && this.editando?.campo === campo;
  }

  salvarEdicao(): void {
    this.editando = null;
  }

  confirmarESalvarNoBanco(): void {
    const questoesParaSalvar: BancoQuestao[] = [];

    this.questoesAprovadas.forEach(i => {
      questoesParaSalvar.push(this.converterParaBancoQuestao(this.questoesExtraidas[i]));
    });

    if (questoesParaSalvar.length === 0) {
      this.toastr.warning("Nenhuma questão selecionada.");
      return;
    }

    this.isSalvando = true;

    const payload = {
      jobId: this.jobId,
      questoes: questoesParaSalvar
    };

    this.bancoQuestoesService.cadastrarLote(payload).subscribe({
      next: (questoesSalvasNoBanco) => {
        if (this.criarProvaRapida) {
          this.gerarEFinalizarProvaRapida(questoesSalvasNoBanco);
        } else {
          this.toastr.success(`${questoesParaSalvar.length} questões cadastradas com sucesso!`);
          this.isSalvando = false;
          this.router.navigate(['/banco-questoes']); 
        }
      },
      error: () => {
        this.toastr.error('Erro ao salvar o lote de questões.');
        this.isSalvando = false;
      }
    });
  }

  private gerarEFinalizarProvaRapida(questoes: BancoQuestao[]): void {
    this.toastr.info('Montando a prova rápida...', 'Aguarde');

    this.provaService.criarProva(this.disciplinaAtivaId!).subscribe({
      next: (provaCriada) => {
        provaCriada.questoes = questoes.map(q => ({
          ...q,
          nivel: q.nivel || 'UNIVERSITARIO_INTERMEDIARIO'
        })) as any[];

        provaCriada.disciplinaId = this.disciplinaAtivaId!;
        
        this.provaService.salvarProvaNoBanco(provaCriada).subscribe({
          next: () => {
            this.isSalvando = false;
            this.toastr.success('Questões adicionadas e Prova Rápida gerada com sucesso!', 'Excelente!');
            this.router.navigate(['/provas-salvas']); 
          },
          error: () => {
            this.isSalvando = false;
            this.toastr.error('As questões foram para o banco, mas falhou ao gerar a prova.', 'Atenção');
            this.router.navigate(['/banco-questoes']);
          }
        });
      },
      error: () => {
        this.isSalvando = false;
        this.toastr.error('As questões foram salvas, mas não foi possível inicializar a prova rápida.', 'Atenção');
        this.router.navigate(['/banco-questoes']);
      }
    });
  }

  private converterParaBancoQuestao(q: any): BancoQuestao {
    return {
      enunciado: q.enunciado,
      tipo: "MULTIPLA_ESCOLHA_5",
      alternativas: {
        a: q.alternativas?.A || q.alternativas?.a,
        b: q.alternativas?.B || q.alternativas?.b,
        c: q.alternativas?.C || q.alternativas?.c,
        d: q.alternativas?.D || q.alternativas?.d,
        e: q.alternativas?.E || q.alternativas?.e
      },
      respostaCorreta: q.respostaCorreta || q.gabarito,
      conceito: q.conceito || "",
      comentarioTecnico: q.comentarioTecnico || "",
      competencia: q.competencia || "",
      nivel: q.nivel || "UNIVERSITARIO_INTERMEDIARIO",
      dataCriacao: new Date().toISOString().split('.')[0],
      origem: "GERADO_POR_PROVA",
      disciplinaId: this.disciplinaAtivaId!
    };
  }
}