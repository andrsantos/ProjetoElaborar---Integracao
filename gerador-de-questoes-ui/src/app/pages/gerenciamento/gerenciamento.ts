import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { GerenciamentoService } from '../../services/gerenciamento/gerenciamento-service';
import { ExtracaoJob } from '../../models/extracao-job.model';
import { JobService } from '../../services/job/job-service';
import { DisciplinaContextService } from '../../services/disciplina-context/disciplina-context-service';
import { DisciplinaService } from '../../services/disciplina/disciplina-service';
import { PdfquestaoService } from '../../services/pdf-questao/pdfquestao-service';
import { PdfQuestaoResumo } from '../../models/pdfquestao-resumo.model';
import { PromptService } from '../../services/prompts/prompt-service';
import { Prompt } from '../../models/prompt.model';

@Component({
  selector: 'app-gerenciamento',
  imports: [FormsModule, ReactiveFormsModule, CommonModule, RouterModule],
  templateUrl: './gerenciamento.html',
  styleUrl: './gerenciamento.scss',
  standalone: true
})
export class Gerenciamento implements OnInit {

  public listaProvas: PdfQuestaoResumo[] = [];
  public listaPrompts: Prompt[] = []; 
  
  public managementForm!: FormGroup;
  public searchPerformed = false;
  public isSearching: boolean = false;
  public paginaAtual: number = 1;
  public itensPorPagina: number = 7;
  
  public filtroTermo: string = '';
  public listaMateriais: any[] = []; 

  public listaJobsCompleta: ExtracaoJob[] = [];
  public listaJobsFiltrada: ExtracaoJob[] = [];

  public disciplinaAtivaId: string | null = null; 
  public nomeDisciplinaAtiva: string = '';
  public carregandoNomeDisciplina: boolean = true;

  constructor(
    private fb: FormBuilder, 
    private gerenciamentoService: GerenciamentoService,
    private jobService: JobService,
    private router: Router,
    private route: ActivatedRoute, 
    private toastr: ToastrService,
    private contextService: DisciplinaContextService,
    private disciplinaService: DisciplinaService,
    private pdfQuestaoService: PdfquestaoService,
    private promptService: PromptService, 
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }



  ngOnInit(): void {
    const abaDesejada = this.route.snapshot.queryParamMap.get('tab') || 'documentation';

    this.managementForm = this.fb.group({
      tableType: [abaDesejada, Validators.required] 
    });

    this.managementForm.get('tableType')?.valueChanges.subscribe(() => {
      this.searchPerformed = false; 
      this.listaMateriais = [];
      this.listaProvas = [];
      this.listaJobsCompleta = [];
      this.listaPrompts = [];
      this.onSearch();
    });

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
    
    if (this.route.snapshot.queryParamMap.has('tab')) {
      setTimeout(() => {
        this.onSearch();
      });
    }
  }

  onSearch(): void {
    if (this.managementForm.invalid) return;

    this.isSearching = true;
    this.searchPerformed = true;
    this.paginaAtual = 1;
    this.limparFiltros();

    const tableType = this.managementForm.get('tableType')?.value;

    if (tableType === 'documentation') {
      this.buscarDocumentosBaseConhecimento();
    } else if (tableType === 'processamentos') {
      this.buscarHistoricoProcessamentos();
    } else if (tableType === 'provas') {
      this.buscarProvasBaseConhecimento(); 
    } else if (tableType === 'prompts') { 
      this.buscarPrompts();
    }
  }

  onFiltroChange(): void {
    this.paginaAtual = 1;
  }

  limparFiltros(): void {
    this.filtroTermo = '';
    this.paginaAtual = 1;
  }



  buscarPrompts(): void {
    if (!this.disciplinaAtivaId) {
      console.warn('Tentativa de buscar prompts sem uma disciplina ativa.');
      this.toastr.warning('Nenhuma disciplina selecionada.');
      this.isSearching = false;
      return; 
    }

    this.promptService.listarPorDisciplina(this.disciplinaAtivaId).subscribe({
      next: (prompts) => {
        this.listaPrompts = prompts;
      },
      error: (error) => {
        console.error('Erro ao buscar prompts:', error);
        this.toastr.error('Erro ao carregar a lista de Padrões de Banca.', 'Erro');
        this.isSearching = false;
      },
      complete: () => {
        this.isSearching = false;
      }
    });
  }

  get listaPromptsFiltrada(): Prompt[] {
    const busca = this.filtroTermo.toLowerCase();
    return this.listaPrompts.filter(prompt => 
      prompt.nome && prompt.nome.toLowerCase().includes(busca)
    );
  }

  get listaPromptsPaginada(): Prompt[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    const fim = inicio + this.itensPorPagina;
    return this.listaPromptsFiltrada.slice(inicio, fim);
  }

  irParaNovoPrompt(): void {
    this.router.navigate(['gerenciamento/detalhe-prompt']);
  }

  verDetalhesPrompt(id: string): void {
    this.router.navigate(['gerenciamento/detalhe-prompt'], { queryParams: { promptId: id } });
  }

  excluirPrompt(prompt: Prompt): void {
    const confirmacao = window.confirm(
      `⚠️ Tem certeza que deseja excluir o Padrão "${prompt.nome}"?\n\nEle não estará mais disponível na hora da extração. Esta ação é IRREVERSÍVEL.`
    );

    if (confirmacao) {
      this.promptService.deletarPrompt(prompt.id).subscribe({
        next: () => {
          this.listaPrompts = this.listaPrompts.filter(p => p.id !== prompt.id);
          
          if (this.listaPromptsPaginada.length === 0 && this.paginaAtual > 1) {
            this.paginaAtual--;
          }
          this.toastr.success(`O padrão "${prompt.nome}" foi excluído com sucesso!`, 'Exclusão Concluída');
        },
        error: (error) => {
          console.error('Erro ao excluir prompt:', error);
          this.toastr.error('Ocorreu um erro ao tentar excluir o padrão.', 'Erro');
        }
      });
    }
  }


  buscarHistoricoProcessamentos(): void {
    this.jobService.listarJobs().subscribe({
      next: (jobs: ExtracaoJob[]) => {
        this.listaJobsCompleta = jobs;
        this.listaJobsFiltrada = [...this.listaJobsCompleta];
        this.isSearching = false;
      },
      error: (error) => {
        console.error('Erro ao buscar jobs:', error);
        this.toastr.error('Falha ao carregar o histórico de extrações.', 'Erro');
        this.isSearching = false;
      }
    });
  }

  get listaJobsPaginada(): ExtracaoJob[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    const fim = inicio + this.itensPorPagina;
    return this.listaJobsFiltrada.slice(inicio, fim);
  }

  abrirModalErroJob(job: ExtracaoJob): void {
    const mensagem = job.mensagemErro ? job.mensagemErro : 'Ocorreu um erro desconhecido no processamento da IA.';
    alert(`Falha na extração de: ${job.nomeArquivo}\n\nDetalhes do Servidor:\n${mensagem}`);
  }

  buscarDocumentosBaseConhecimento(): void {
    this.gerenciamentoService.listarDocumentosFiltrados(this.disciplinaAtivaId!).subscribe({
      next: (documentos) => {
        this.listaMateriais = documentos;
      },
      error: (error) => {
        console.error('Erro ao listar documentos:', error);
        this.toastr.error('Erro ao buscar a base de conhecimento.', 'Erro');
        this.isSearching = false;
      },
      complete: () => {
        this.isSearching = false;  
      }
    });
  }

  get listaMateriaisFiltrada(): any[] {
    return this.listaMateriais.filter(doc => {
      const titulo = doc.titulo || '';
      const arquivo = doc.nomeArquivo || '';
      const busca = this.filtroTermo.toLowerCase();
      
      return titulo.toLowerCase().includes(busca) || arquivo.toLowerCase().includes(busca);
    });
  }

  get listaMateriaisPaginada(): any[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    const fim = inicio + this.itensPorPagina;
    return this.listaMateriaisFiltrada.slice(inicio, fim);
  }

  irParaCadastroDocumento(): void {
    this.router.navigate(['/cadastro-documento']);
  }

  irParaGerenciamentoPrompts(documento: any): void {
    this.router.navigate(['/detalhe-prompt'], { 
      queryParams: { documentoId: documento.id } 
    });
  }

  baixarPDF(idBinario: string, nomeArquivo: string): void {
    this.gerenciamentoService.baixarMaterialBinario(idBinario).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const linkHTML = document.createElement('a');
        linkHTML.href = url;
        linkHTML.download = nomeArquivo || 'documento.pdf'; 
        document.body.appendChild(linkHTML);
        linkHTML.click();
        document.body.removeChild(linkHTML);
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Erro ao fazer o download do PDF:', error);
        alert('Não foi possível baixar o arquivo. Ele pode estar indisponível no servidor.');
      }
    });
  }

  excluirDocumento(documento: any) { 
    const confirmacao = window.confirm(
      `⚠️ CUIDADO: Tem certeza que deseja excluir o documento "${documento.titulo}"?\n\nIsso apagará o PDF, removerá as instruções de IA e os fragmentos de contexto associados. Ação IRREVERSÍVEL.`
    );

    if (confirmacao) {
      this.gerenciamentoService.deletarDocumento(documento.id).subscribe({
        next: () => {
          this.listaMateriais = this.listaMateriais.filter(d => d.id !== documento.id);
          if (this.listaMateriaisPaginada.length === 0 && this.paginaAtual > 1) {
            this.paginaAtual--;
          }
          this.toastr.success(`O documento "${documento.titulo}" foi apagado do banco de dados!`, 'Exclusão Concluída');
        },
        error: (error) => {
          console.error('Erro ao excluir documento:', error);
          this.toastr.error('Ocorreu um erro ao tentar excluir o documento.', 'Erro na Exclusão');
        }
      });
    }
  }

  // --- PAGINAÇÃO GERAL ---

  get totalPaginas(): number {
    const tipoAtivo = this.managementForm.get('tableType')?.value;
    let totalRegistros = 0;
    
    if (tipoAtivo === 'documentation') {
      totalRegistros = this.listaMateriaisFiltrada.length;
    } else if (tipoAtivo === 'processamentos') {
      totalRegistros = this.listaJobsFiltrada.length;
    } else if (tipoAtivo === 'provas') {
      totalRegistros = this.listaProvasFiltrada.length;
    } else if (tipoAtivo === 'prompts') { 
      totalRegistros = this.listaPromptsFiltrada.length;
    }
      
    return Math.ceil(totalRegistros / this.itensPorPagina) || 1;
  }

  mudarPagina(proxima: boolean): void {
    if (proxima && this.paginaAtual < this.totalPaginas) {
      this.paginaAtual++;
    } else if (!proxima && this.paginaAtual > 1) {
      this.paginaAtual--;
    }
  }

  buscarProvasBaseConhecimento(): void {

    if (!this.disciplinaAtivaId) {
      console.warn('Tentativa de buscar provas sem uma disciplina ativa.');
      this.toastr.warning('Nenhuma disciplina selecionada.');
      this.isSearching = false;
      return; 
    }
    
    this.pdfQuestaoService.listarProvasResumo(this.disciplinaAtivaId).subscribe({
      next: (provas) => {
        this.listaProvas = provas;
      },
      error: (error) => {
        console.error('Erro ao buscar provas:', error);
        this.toastr.error('Erro ao carregar a lista de provas.', 'Erro');
        this.isSearching = false;
      },
      complete: () => {
        this.isSearching = false;
      }
    });
  }

  get listaProvasFiltrada(): PdfQuestaoResumo[] {
    const busca = this.filtroTermo.toLowerCase();
    return this.listaProvas.filter(prova => 
      prova.nomeOriginal && prova.nomeOriginal.toLowerCase().includes(busca)
    );
  }

  get listaProvasPaginada(): PdfQuestaoResumo[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    const fim = inicio + this.itensPorPagina;
    return this.listaProvasFiltrada.slice(inicio, fim);
  }

  baixarProva(id: string, nomeArquivo: string): void {
    this.pdfQuestaoService.baixarProva(id).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const linkHTML = document.createElement('a');
        linkHTML.href = url;
        linkHTML.download = nomeArquivo || 'prova.pdf';
        document.body.appendChild(linkHTML);
        linkHTML.click();
        document.body.removeChild(linkHTML);
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Erro ao baixar prova:', error);
        this.toastr.error('Não foi possível baixar o arquivo da prova.', 'Erro');
      }
    });
  }

  excluirProva(prova: PdfQuestaoResumo): void {
    const confirmacao = window.confirm(
      `⚠️ ATENÇÃO: Tem certeza que deseja excluir a prova "${prova.nomeOriginal}"?\n\nIsso apagará permanentemente as ${prova.quantidadeQuestoes} questões extraídas a partir dela. Esta ação é IRREVERSÍVEL.`
    );

    if (confirmacao) {
      this.pdfQuestaoService.excluirProva(prova.id).subscribe({
        next: () => {
          this.listaProvas = this.listaProvas.filter(p => p.id !== prova.id);
          
          if (this.listaProvasPaginada.length === 0 && this.paginaAtual > 1) {
            this.paginaAtual--;
          }
          this.toastr.success(`Prova e questões excluídas com sucesso!`, 'Exclusão Concluída');
        },
        error: (error) => {
          console.error('Erro ao excluir prova:', error);
          this.toastr.error('Ocorreu um erro ao tentar excluir a prova e suas questões.', 'Erro');
        }
      });
    }
  }

  verQuestoesDaProva(provaId: string): void {
    this.router.navigate(['/gerenciamento/prova', provaId, 'questoes']);
  }

    bloquearFluxo(event: Event): void {
    event.preventDefault(); 
    this.toastr.warning(
      'Este fluxo está temporariamente fechado para manutenção. Aguarde atualizações.', 
      'Em Manutenção'
    );
  }

  
}