import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { TopicoQuantidade } from '../../models/topico-quantidade.model';
import { firstValueFrom, Observable, shareReplay } from 'rxjs';
import { Prova } from '../../models/prova.model';
import { ProvaService } from '../../services/prova/prova-service';
import { ToastrService } from 'ngx-toastr';
import { BancoQuestoesService } from '../../services/banco-questoes/banco-questoes';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { QuestaoFormatoAvaliarDTO } from '../../models/questao-formato-avaliar.model';
import { IntegracaoAvaliarService } from '../../services/integracao-avaliar/integracao-avaliar';
import { ConceitoConfig } from '../../models/conceito-config.model';
import { Router } from '@angular/router';
import { NotificationService } from '../../services/notification/notification-service';
import { DisciplinaContextService } from '../../services/disciplina-context/disciplina-context-service';
import { DisciplinaService } from '../../services/disciplina/disciplina-service';

export type CampoEdicao = 'enunciado' | 'resposta' | 'a' | 'b' | 'c' | 'd' | 'e';

export interface EstadoEdicao {
  indexQuestao: number;
  campo: CampoEdicao;
}

export interface BlocoGeracao extends TopicoQuantidade {
  subtopicos?: ConceitoConfig[];
}

@Component({
  selector: 'app-prova-builder',
  imports: [CommonModule, FormsModule],
  templateUrl: './prova-builder.html',
  styleUrl: './prova-builder.scss',
})
export class ProvaBuilder implements OnInit {

  public conceitosDisponiveis: string[] = [];
  public conceitoSelecionado: string = '';

  public topicosSelecionados: BlocoGeracao[] = [];

  provaId: string | null = null;
  prova$: Observable<Prova> | null = null;
  isLoadingFinalizar = false;
  isLoadingAdicionar = false;
  isLoadingCriar = false;
  public isLoadingAssuntos: boolean = true; 
  public temQuestoesCadastradas: boolean = true; 

  public editando: EstadoEdicao | null = null;
  descartandoIndex: number | null = null;
  modalAberta = false;
  questaoSelecionada: any = null;
  provaFormatoAvaliar: QuestaoFormatoAvaliarDTO[] = [];
  formatoAvaliarResultado: string = '';

  public disciplinaAtivaId: string | null = null;
  public nomeDisciplinaAtiva: string = '';
  public carregandoNomeDisciplina: boolean = true;

  constructor(private provaService: ProvaService, private toastr: ToastrService,
    private bancoQuestoesService: BancoQuestoesService,
    private integracaoAvaliarService: IntegracaoAvaliarService,
    private router: Router,
    private notificationService: NotificationService,
    private contextService: DisciplinaContextService,
    private disciplinaService: DisciplinaService,
    @Inject(PLATFORM_ID) private platformId: Object

  ) { }

  ngOnInit(): void {

     this.disciplinaAtivaId = this.contextService.getDisciplinaAtivaId();

      if (!this.disciplinaAtivaId) {
      if (isPlatformBrowser(this.platformId)) {
        this.toastr.error('Nenhuma disciplina selecionada. Retornando ao início.', 'Atenção');
        this.router.navigate(['/']);
      }
      return; 
    }

    this.disciplinaService.buscarNomeDisciplina(this.disciplinaAtivaId).subscribe({
      next: (res) => {
        this.nomeDisciplinaAtiva = res.nome;
        this.carregandoNomeDisciplina = false;
      },
      error: () => {
        this.nomeDisciplinaAtiva = 'Ambiente de Trabalho';
        this.carregandoNomeDisciplina = false;
      }
    });
   
    this.onCriarProva();
    this.verificarBancoQuestoes();

    this.bancoQuestoesService.getConceitosPorDisciplina(this.disciplinaAtivaId).subscribe({
      next: (conceitos) => {
        this.conceitosDisponiveis = conceitos;
      },
      error: () => {
        this.toastr.error("Erro ao carregar conceitos.");
      }
    });

  }

  private verificarBancoQuestoes(): void {
    this.bancoQuestoesService.listarTodasPorDisciplina(this.disciplinaAtivaId!).subscribe({
      next: (questoes) => {
        this.temQuestoesCadastradas = questoes && questoes.length > 0;
        this.isLoadingAssuntos = false;
      },
      error: () => {
        this.temQuestoesCadastradas = false;
        this.isLoadingAssuntos = false;
      }
    });
  }

  adicionarFiltroProva(): void {
    if (!this.conceitoSelecionado) return;
    
    if (this.topicosSelecionados.some(t => t.topico === this.conceitoSelecionado)) {
      this.toastr.warning("Este conceito já foi adicionado à prova.");
      return;
    }
    
    this.topicosSelecionados.push({
        topico: this.conceitoSelecionado, 
        subtopicos: [{ 
          conceito: this.conceitoSelecionado, 
          quantidadeFaceis: 0, 
          quantidadeMedias: 0, 
          quantidadeDificeis: 0, 
          quantidade: 0 
        }],
        quantidade: 0, quantidadeDificeis: 0, quantidadeFaceis: 0, quantidadeMedias: 0
    });

    this.conceitoSelecionado = '';
  }

  onRemoverTopicoPorIndex(index: number): void { 
    this.topicosSelecionados.splice(index, 1); 
  }

  get listaTopicosFormatada(): string {
    return this.topicosSelecionados.map(t => t.topico).join(' | ');
  }


  onGerarProvaBanco() {
    if (!this.provaId || this.topicosSelecionados.length === 0) return;
    this.isLoadingAdicionar = true;

    const payloadAgrupado = this.topicosSelecionados.map(itemTela => ({
      documentoId: "CONCEITO_DIRETO", 
      quantidadeFaceis: itemTela.subtopicos![0].quantidadeFaceis,
      quantidadeMedias: itemTela.subtopicos![0].quantidadeMedias,
      quantidadeDificeis: itemTela.subtopicos![0].quantidadeDificeis,
      quantidade: itemTela.quantidade,
      subtopicos: [{
        conceito: itemTela.topico, 
        quantidadeFaceis: itemTela.subtopicos![0].quantidadeFaceis,
        quantidadeMedias: itemTela.subtopicos![0].quantidadeMedias,
        quantidadeDificeis: itemTela.subtopicos![0].quantidadeDificeis,
        quantidade: itemTela.quantidade
      }]
    }));

    console.log("Payload Simplificado para o Java:", payloadAgrupado);

    this.prova$ = this.provaService.gerarProvaBanco(this.provaId, payloadAgrupado).pipe(shareReplay(1));
    
    this.prova$.subscribe({
      next: (prova) => {
        this.isLoadingAdicionar = false;
        this.toastr.success('As questões foram adicionadas à prova!', 'Sucesso');
      },
      error: (err) => {
        this.isLoadingAdicionar = false; 
        
        if (err.error && err.error.erro) {
          this.toastr.error(err.error.erro, 'Atenção');
        } else if (err.error && err.error.message) {
          this.toastr.error(err.error.message, 'Atenção');
        } else if (typeof err.error === 'string') {
          this.toastr.error(err.error, 'Atenção');
        } else {
          this.toastr.error('Ocorreu um erro ao tentar buscar as questões no banco.', 'Erro de Servidor');
        }
      }
    });
  }

  onCriarProva() {
      this.isLoadingCriar = true; 
      this.prova$ = this.provaService.criarProva(this.disciplinaAtivaId!).pipe(
        shareReplay(1) 
      );

      this.prova$.subscribe({
        next: p => {
          this.provaId = p.id;
          this.isLoadingCriar = false; 
        },
        error: () => this.isLoadingCriar = false 
      });
  }

  isTopicoSelecionado(topico: string): boolean {
    return this.topicosSelecionados.some(t => t.topico === topico);
  }

  atualizarTotal(item: BlocoGeracao): void {
    if (item.quantidadeDificeis < 0) item.quantidadeDificeis = 0;
    if (item.quantidadeMedias < 0) item.quantidadeMedias = 0;
    if (item.quantidadeFaceis < 0) item.quantidadeFaceis = 0;
    item.quantidade = item.quantidadeDificeis + item.quantidadeMedias + item.quantidadeFaceis;

    if (item.subtopicos && item.subtopicos.length > 0) {
      item.subtopicos[0].quantidadeFaceis = item.quantidadeFaceis;
      item.subtopicos[0].quantidadeMedias = item.quantidadeMedias;
      item.subtopicos[0].quantidadeDificeis = item.quantidadeDificeis;
      item.subtopicos[0].quantidade = item.quantidade;
    }
  }

  exportarFormatoAvaliar(){
    this.provaFormatoAvaliar = [];
    let numero = 1;
    this.prova$?.forEach(questao => {
      questao.questoes.forEach(q => {
        const formatoAvaliar = {
          numeroQuestao: numero++,
          enunciado: q.enunciado,
          alternativas: q.alternativas,
          respostaCorreta: q.respostaCorreta
        };
        this.provaFormatoAvaliar.push(formatoAvaliar);
      });
    });

    this.integracaoAvaliarService.exportarFormatoAvaliar(this.provaFormatoAvaliar).subscribe({
      next: (data: string) => {
        this.formatoAvaliarResultado = data;
        this.toastr.success("Prova exportada para avaliação com sucesso!", 'Sucesso!');

      if (data && data.trim().length > 0) {
        this.baixarArquivoTxt(data);
      } else {
        this.toastr.warning("O servidor retornou uma prova vazia.", "Aviso");
      }
      },
      error: (err: any) => {
        console.error("Erro ao exportar prova para avaliação:", err);
        this.toastr.error("Falha ao exportar prova para avaliação.", 'Erro');
      }
    });
  }

  isEditando(index: number, campo: string): boolean {
    return this.editando?.indexQuestao === index && this.editando?.campo === campo;
  }

  ativarEdicao(index: number, campo: string): void {
      this.editando = { indexQuestao: index, campo: campo as CampoEdicao };
  }
  
  salvarEdicao(): void {
    this.editando = null;
    this.toastr.info("Alteração salva localmente.");
  }

  onDescartarQuestao(indice: number) {
        if (!this.provaId) return;
        this.descartandoIndex = indice; 

        this.prova$ = this.provaService.descartarQuestao(this.provaId, indice);
        this.prova$.subscribe({
          next: () => this.descartandoIndex = null, 
          error: () => this.descartandoIndex = null 
        });
  }

  abrirComentarios(questao: any) {
    this.questaoSelecionada = questao;
    this.modalAberta = true;
  }

  fecharModal() {
    this.modalAberta = false;
    this.questaoSelecionada = null;
  }
  
  async onFinalizarProva() {
    if (!this.provaId || !this.prova$) {
      alert("Nenhuma prova ativa.");
      return;
    }
    
    this.isLoadingFinalizar = true;

    try {
      const provaAtual = await firstValueFrom(this.prova$);
      provaAtual.disciplinaId = this.disciplinaAtivaId!;
      await firstValueFrom(this.provaService.salvarProvaNoBanco(provaAtual));
      console.log("Prova salva no banco de dados com sucesso!");
      this.prova$ = null;
      this.provaId = null;
      this.isLoadingFinalizar = false;
      this.notificationService.setMessage("Prova salva no banco de dados com sucesso!");
      this.router.navigate(['/provas-salvas']);

    } catch (error) {
      console.error("Erro ao salvar a prova:", error);
      this.toastr.error("Falha ao salvar a prova no banco de dados.", 'Erro');
      this.isLoadingFinalizar = false;
    }
  }


  private baixarArquivoTxt(conteudo: string) {
    const blob = new Blob([conteudo], { type: 'text/plain;charset=utf-8' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    
    const data = new Date().toLocaleDateString('pt-BR').replace(/\//g, '-');
    link.download = `prova-avaliacao-${data}.txt`;
    
    link.click();
    window.URL.revokeObjectURL(url);
  }

  async executarAcoesFinais() {
    this.exportarFormatoAvaliar();
    await this.onFinalizarProva();
  }

}