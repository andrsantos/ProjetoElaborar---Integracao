import { Component, Inject, OnInit, PLATFORM_ID, HostListener, ElementRef, ViewChild } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { BancoQuestoesService } from '../../../services/banco-questoes/banco-questoes';
import { BancoQuestao } from '../../../models/banco-questao.model';
import { ProvaService } from '../../../services/prova/prova-service';
import { DisciplinaContextService } from '../../../services/disciplina-context/disciplina-context-service';
import { DisciplinaService } from '../../../services/disciplina/disciplina-service';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-gerenciar-banco',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './gerenciar-banco.html',
  styleUrls: ['./gerenciar-banco.scss']
})
export class GerenciarBanco implements OnInit {

  questoes: BancoQuestao[] = [];
  questoesExibidas: BancoQuestao[] = [];

  // Listas para os Dropdowns
  topicosDisponiveis: string[] = [];
  conceitosDisponiveis: string[] = [];

  // Variáveis de Filtro
  topicoSelecionado: string = '';
  conceitoSelecionado: string = '';
  nivelSelecionado: string = '';
  ordemSelecionada: 'asc' | 'desc' = 'desc';
  searchTerm: string = '';
  dataFiltro: string = '';

  // Variáveis de Controle
  isLoading = false;
  isEditModalOpen = false;
  isComentarioModalOpen = false;
  isCadastroModalOpen = false;
  isModoEdicao = false;
  questaoEmEdicao: BancoQuestao | null = null;
  questaoComentario: BancoQuestao | null = null;
  novaQuestao: BancoQuestao = this.criarNovaQuestao();
  editStates: { [key: string]: boolean } = {};
  objectKeys = Object.keys;

  public disciplinaAtivaId: string | null = null;
  public nomeDisciplinaAtiva: string = '';
  public carregandoNomeDisciplina: boolean = true;
  dropdownConceitoAberto: boolean = false;

  paginaAtual: number = 1;
  itensPorPagina: number = 10;


  constructor(
    private bancoService: BancoQuestoesService,
    private provaService: ProvaService,
    private toastr: ToastrService,
    private contextService: DisciplinaContextService,
    private disciplinaService: DisciplinaService,
    private router: Router,
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
    
    this.carregarQuestoes(this.disciplinaAtivaId);
    this.buscarTopicos();
    this.buscarConceitosPorDisciplina(this.disciplinaAtivaId);
  }









  buscarTopicos() {
    this.provaService.getTopicosDisponiveis(this.disciplinaAtivaId!).subscribe({
      next: (topicos) => {
        this.topicosDisponiveis = topicos;
      },
      error: (err) => {
        console.error("Erro ao buscar tópicos:", err);
      }
    });
  }

  buscarConceitosPorDisciplina(disciplinaId: string) {
    this.bancoService.getConceitosPorDisciplina(disciplinaId).subscribe({
      next: (conceitos) => {
        this.conceitosDisponiveis = conceitos;
        console.log("Conceitos ", this.conceitosDisponiveis);
      },
      error: (err) => console.error('Erro ao buscar conceitos para a disciplina:', err)
    });
  }

  carregarQuestoes(disciplinaId: string): void {
    this.isLoading = true;
    this.bancoService.listarTodasPorDisciplina(disciplinaId).subscribe({
      next: (data) => {
        this.questoes = data;
        console.log("Questões", this.questoes);
        this.aplicarFiltros();
        this.isLoading = false;
      },
      error: () => {
        this.toastr.error('Erro ao carregar questões.');
        this.isLoading = false;
      }
    });
  }

  aplicarFiltros() {
    let resultado = [...this.questoes];

    // if (this.topicoSelecionado) {
    //   resultado = resultado.filter(q => q.topico === this.topicoSelecionado);
    // }

    if (this.conceitoSelecionado) {
      resultado = resultado.filter(q => q.conceito === this.conceitoSelecionado);
    }

    if (this.nivelSelecionado) {
      resultado = resultado.filter(q => q.nivel === this.nivelSelecionado);
    }

    if (this.searchTerm) {
      const termo = this.searchTerm.toLowerCase();
      resultado = resultado.filter(q =>
        q.enunciado.toLowerCase().includes(termo) ||
        // q.topico.toLowerCase().includes(termo) ||
        (q.conceito && q.conceito.toLowerCase().includes(termo))
      );
    }

    if (this.dataFiltro) {
      resultado = resultado.filter(q => {
        if (!q.dataCriacao) return false;
        return q.dataCriacao.split('T')[0] === this.dataFiltro;
      });
    }

    resultado.sort((a, b) => {
      const dataA = a.dataCriacao ? new Date(a.dataCriacao).getTime() : 0;
      const dataB = b.dataCriacao ? new Date(b.dataCriacao).getTime() : 0;
      return this.ordemSelecionada === 'asc'
        ? dataA - dataB
        : dataB - dataA;
    });

    this.questoesExibidas = resultado;
    this.paginaAtual = 1;
  }

  onDataChange() {
    this.aplicarFiltros();
  }

  onConceitoChange() {
    this.aplicarFiltros();
  }

  onNivelChange() {
    this.aplicarFiltros();
  }

  limparFiltros() {
    this.searchTerm = '';
    this.topicoSelecionado = '';
    this.conceitoSelecionado = '';
    this.nivelSelecionado = '';
    this.dataFiltro = '';
    this.aplicarFiltros();
  }

  onSearch() {
    this.aplicarFiltros();
  }

  onTopicoChange(event: any) {
    this.topicoSelecionado = event.target.value;
    this.aplicarFiltros();
  }

  onOrderChange(event: any) {
    this.ordemSelecionada = event.target.value as 'asc' | 'desc';
    this.aplicarFiltros();
  }

  onExcluir(id: string | undefined): void {
    if (!id) return;
    if (confirm('Tem certeza que deseja excluir esta questão?')) {
      this.bancoService.excluirQuestao(id).subscribe({
        next: () => {
          this.toastr.success('Questão excluída.');
          this.carregarQuestoes(this.disciplinaAtivaId!);
          this.buscarConceitosPorDisciplina(this.disciplinaAtivaId!);
        },
        error: () => this.toastr.error('Erro ao excluir.')
      });
    }
  }

  // --- LÓGICA DE EDIÇÃO INLINE ---
  isEditando(id: string | undefined, campo: string): boolean {
    if (!id) return false;
    return !!this.editStates[`${id}_${campo}`];
  }

  ativarEdicao(id: string | undefined, campo: string): void {
    if (!id) return;
    this.editStates[`${id}_${campo}`] = true;
  }

  salvarEdicaoItem(questao: BancoQuestao, campo: string): void {
    if (!questao.id) return;
    this.editStates[`${questao.id}_${campo}`] = false;

    if (campo === 'resposta' && questao.respostaCorreta) {
      questao.respostaCorreta = questao.respostaCorreta.toLowerCase();
    }

    this.bancoService.atualizarQuestao(questao.id, questao).subscribe({
      next: () => this.toastr.success('Alteração salva com sucesso!'),
      error: () => this.toastr.error('Erro ao salvar alteração. Verifique a conexão.')
    });
  }

  salvarQuestao() {
    this.bancoService.cadastrarQuestao(this.novaQuestao).subscribe({
      next: () => {
        this.toastr.success("Questão cadastrada com sucesso!");
        this.fecharCadastro();
        
        this.carregarQuestoes(this.disciplinaAtivaId!);
        this.buscarConceitosPorDisciplina(this.disciplinaAtivaId!);
      },
      error: () => this.toastr.error("Erro ao cadastrar questão")
    });
  }

  abrirComentarios(questao: BancoQuestao) {
    this.questaoComentario = questao;
    this.isComentarioModalOpen = true;
  }

  fecharComentarios() {
    this.isComentarioModalOpen = false;
    this.questaoComentario = null;
  }

  abrirCadastro() {
    this.novaQuestao = this.criarNovaQuestao();
    this.isCadastroModalOpen = true;
  }

  fecharCadastro() {
    this.isCadastroModalOpen = false;
    this.isModoEdicao = false;
  }

  criarNovaQuestao(): BancoQuestao {
    return {
      tipo: "MULTIPLA_ESCOLHA_5",
      topico: "",
      enunciado: "",
      alternativas: { a: "", b: "", c: "", d: "", e: "" },
      respostaCorreta: "",
      competencia: "",
      conceito: "",
      comentarioTecnico: "",
      nivel: "UNIVERSITARIO_INTERMEDIARIO",
      dataCriacao: new Date().toISOString().split('.')[0],
    } as BancoQuestao;
  }


  toggleDropdownConceito() {
    this.dropdownConceitoAberto = !this.dropdownConceitoAberto;
  }

  selecionarConceito(conceito: string) {
    this.conceitoSelecionado = conceito;
    this.dropdownConceitoAberto = false; 
    this.onConceitoChange(); 
  }

  @ViewChild('dropdownConceito') dropdownConceitoRef!: ElementRef;

  @HostListener('document:click', ['$event'])
  cliqueForaDoDropdown(event: Event) {
    if (this.dropdownConceitoAberto && this.dropdownConceitoRef) {
      const clicouDentro = this.dropdownConceitoRef.nativeElement.contains(event.target);
      if (!clicouDentro) {
        this.dropdownConceitoAberto = false;
      }
    }
  }

// LÓGICA DE PAGINAÇÃO
  get totalPaginas(): number {
    return Math.ceil(this.questoesExibidas.length / this.itensPorPagina) || 1;
  }

  get questoesPaginadas(): BancoQuestao[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    const fim = inicio + this.itensPorPagina;
    return this.questoesExibidas.slice(inicio, fim);
  }

  mudarPagina(proxima: boolean): void {
    if (proxima && this.paginaAtual < this.totalPaginas) {
      this.paginaAtual++;
      window.scrollTo({ top: 0, behavior: 'smooth' }); 
    }
    else if (!proxima && this.paginaAtual > 1) {
      this.paginaAtual--;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }


}