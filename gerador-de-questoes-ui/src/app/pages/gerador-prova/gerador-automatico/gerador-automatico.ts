
import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { Prova } from '../../../models/prova.model';
import { Observable } from 'rxjs/internal/Observable';
import { ProvaService } from '../../../services/prova/prova-service';
import { FormsModule } from '@angular/forms';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { TopicoQuantidade } from '../../../models/topico-quantidade.model';
import { shareReplay } from 'rxjs/operators';
import { BancoQuestoesService } from '../../../services/banco-questoes/banco-questoes';
import { BancoQuestao } from '../../../models/banco-questao.model';
import { ConceitoConfig } from '../../../models/conceito-config.model'; 
import { DisciplinaContextService } from '../../../services/disciplina-context/disciplina-context-service';
import { DisciplinaService } from '../../../services/disciplina/disciplina-service';
import { GerenciamentoService } from '../../../services/gerenciamento/gerenciamento-service';

export type CampoEdicao = 'enunciado' | 'resposta' | 'a' | 'b' | 'c' | 'd' | 'e';

export interface EstadoEdicao {
  indexQuestao: number;
  campo: CampoEdicao;
}


export interface BlocoGeracao extends TopicoQuantidade {
  documentoId: string; 
  documentoTitulo: string; 
  subtopicos?: ConceitoConfig[]; 
  diretrizCustomizada?: string; 
  mostrarPrompt?: boolean;
}

export interface FonteReferencia {
  id: string;
  titulo: string;
  tipo: 'DOCUMENTO' | 'PROVA';
}



@Component({
  selector: 'app-gerador-prova',
  imports: [CommonModule, FormsModule],
  templateUrl: './gerador-automatico.html',
  styleUrl: './gerador-automatico.scss',
  standalone: true
})
export class GeradorAutomatico implements OnInit {

  public fontesDisponiveis: FonteReferencia[] = [];
  public documentoSelecionadoId: string = '';
  public isDocumentosLoaded: boolean = false; 
  
  public conceitosDisponiveisCombo: string[] = [];
  public conceitosSelecionadosCombo: string[] = [];
  public isConceitosDropdownOpen = false;

  public blocosSelecionados: BlocoGeracao[] = []; 
  prova$: Observable<Prova> | null = null;
  provaId: string | null = null;
  
  isLoadingCriar = false;
  isLoadingAdicionar = false;
  isLoadingFinalizar = false;
  descartandoIndex: number | null = null;
  public editando: EstadoEdicao | null = null;
  modalAberta = false;
  questaoSelecionada: any = null;
  questoesCadastradas = new Set<number>();

  public disciplinaAtivaId: string | null = null;
  public nomeDisciplinaAtiva: string = '';
  public carregandoNomeDisciplina: boolean = true;

  constructor(
    private provaService: ProvaService, 
    private toastr: ToastrService,
    private bancoQuestoesService: BancoQuestoesService,
    private router: Router, 
    private contextService: DisciplinaContextService, 
    private disciplinaService: DisciplinaService,
    private gerenciamentoService: GerenciamentoService,
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
      next: (res) => { this.nomeDisciplinaAtiva = res.nome; this.carregandoNomeDisciplina = false; },
      error: () => { this.nomeDisciplinaAtiva = 'Ambiente de Trabalho'; this.carregandoNomeDisciplina = false; }
    });

    this.onCriarProva();

    this.gerenciamentoService.listarFontesReferencia(this.disciplinaAtivaId).subscribe({
      next: (fontes: FonteReferencia[]) => {
        this.fontesDisponiveis = fontes;
        this.isDocumentosLoaded = true;
      },
      error: (err) => { this.isDocumentosLoaded = true; }
    });

  }


  get documentosTeoria(): FonteReferencia[] {
    return this.fontesDisponiveis.filter(f => f.tipo === 'DOCUMENTO');
  }

  get provasCadastradas(): FonteReferencia[] {
    return this.fontesDisponiveis.filter(f => f.tipo === 'PROVA');
  }

  onDocumentoChange(): void {
    this.conceitosSelecionadosCombo = [];
    this.isConceitosDropdownOpen = false;
    if (this.documentoSelecionadoId) {
      this.provaService.getConceitosPorDocumento(this.documentoSelecionadoId).subscribe({
        next: (conceitos) => this.conceitosDisponiveisCombo = conceitos,
        error: () => this.conceitosDisponiveisCombo = []
      });
    }
    console.log("conceitos", this.conceitosDisponiveisCombo);
  }

  toggleConceitosDropdown(): void {
    if (this.documentoSelecionadoId) this.isConceitosDropdownOpen = !this.isConceitosDropdownOpen;
  }

  onToggleConceitoCombo(conceito: string, event: any): void {
    if (event.target.checked) this.conceitosSelecionadosCombo.push(conceito);
    else this.conceitosSelecionadosCombo = this.conceitosSelecionadosCombo.filter(c => c !== conceito);
  }

  adicionarFiltroProva(): void {
    const doc = this.fontesDisponiveis.find(d => d.id === this.documentoSelecionadoId);
    if (!doc) return;

    if (this.conceitosSelecionadosCombo.length === 0) {
      this.blocosSelecionados.push({
        topico: doc.titulo,
        documentoId: doc.id,
        documentoTitulo: doc.titulo,
        subtopicos: [],
        quantidade: 5, quantidadeDificeis: 0, quantidadeFaceis: 0, quantidadeMedias: 0
      });
    } else {
      this.conceitosSelecionadosCombo.forEach(conceito => {
        this.blocosSelecionados.push({
          topico: doc.titulo,
          documentoId: doc.id,
          documentoTitulo: doc.titulo,
          subtopicos: [{
            conceito: conceito,
            quantidade: 5, quantidadeFaceis: 0, quantidadeMedias: 0, quantidadeDificeis: 0
          }],
          quantidade: 5, quantidadeDificeis: 0, quantidadeFaceis: 0, quantidadeMedias: 0
        });
      });
    }

    this.documentoSelecionadoId = '';
    this.conceitosSelecionadosCombo = [];
  }

  onRemoverBlocoPorIndex(index: number): void {
    this.blocosSelecionados.splice(index, 1);
  }

  get listaDocumentosFormatada(): string {
    return this.blocosSelecionados.map(b => b.documentoTitulo).join(' | ');
  }

  onCriarProva() {
    this.isLoadingCriar = true; 
    this.prova$ = this.provaService.criarProva(this.disciplinaAtivaId!).pipe(shareReplay(1));
    this.prova$.subscribe({ next: p => { this.provaId = p.id; this.isLoadingCriar = false; }, error: () => this.isLoadingCriar = false });
  }

  onGerarProvaAutomatica() {
    if (!this.provaId || this.blocosSelecionados.length === 0) return;
    this.isLoadingAdicionar = true; 
    
    this.prova$ = this.provaService.adicionarQuestoesAutomatico(this.provaId, this.blocosSelecionados).pipe(shareReplay(1));
    this.prova$.subscribe({
      next: () => { this.isLoadingAdicionar = false; this.toastr.success('Questões geradas!'); },
      error: () => this.isLoadingAdicionar = false
    });
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

  onFinalizarProva() {
    if (!this.provaId) return;
    this.isLoadingFinalizar = true;
    this.provaService.finalizarProvaPdf(this.provaId).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `prova_${this.provaId}.pdf`; 
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        a.remove();
        this.prova$ = null;
        this.provaId = null;
        this.isLoadingFinalizar = false;
        this.toastr.success("Prova gerada com sucesso!", 'Sucesso!');
      },
      error: (err) => {
        alert("Falha ao gerar o PDF da prova.");
        this.isLoadingFinalizar = false;
      }
    });
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

  ativarEdicao(index: number, campo: string): void {
    this.editando = { indexQuestao: index, campo: campo as CampoEdicao };
  }

  isEditando(index: number, campo: string): boolean {
    return this.editando?.indexQuestao === index && this.editando?.campo === campo;
  }

  salvarEdicao(): void {
    this.editando = null;
    this.toastr.info("Alteração salva localmente.");
  }

  abrirComentarios(questao: any) {
    this.questaoSelecionada = questao;
    this.modalAberta = true;
  }

  fecharModal() {
    this.modalAberta = false;
    this.questaoSelecionada = null;
  }

  cadastrarQuestao(questao: any, index: number) {
    const bancoQuestao = this.converterParaBancoQuestao(questao);
    this.bancoQuestoesService.cadastrarQuestao(bancoQuestao).subscribe({
      next: () => {
        this.questoesCadastradas.add(index);
        this.toastr.success("Questão cadastrada no banco!", "Sucesso");
      },
      error: (err) => this.toastr.error("Erro ao cadastrar questão", "Erro")
    });
  }

  converterParaBancoQuestao(questao: any): BancoQuestao {
    return {
      topico: questao.topico || "Geral",
      enunciado: questao.enunciado,
      tipo: "MULTIPLA_ESCOLHA_5",
      alternativas: {
        a: questao.alternativas?.a,
        b: questao.alternativas?.b,
        c: questao.alternativas?.c,
        d: questao.alternativas?.d,
        e: questao.alternativas?.e
      },
      respostaCorreta: questao.respostaCorreta,
      conceito: questao.conceito || "",
      comentarioTecnico: questao.comentarioTecnico || "",
      competencia: questao.competencia || "",
      nivel: questao.nivel,
      dataCriacao: new Date().toISOString().split('.')[0],
      origem: "GERADO_POR_DOCUMENTO",
      arquivoOrigem: undefined,
      disciplinaId: this.disciplinaAtivaId! 
    };
  }

  public isConceitoComboSelecionado(conceito: string): boolean {
    return this.conceitosSelecionadosCombo.includes(conceito);
  }


}