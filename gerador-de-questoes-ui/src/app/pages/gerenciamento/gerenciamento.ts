import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { GerenciamentoService } from '../../services/gerenciamento/gerenciamento-service';
import { ExtracaoJob } from '../../models/extracao-job.model';
import { JobService } from '../../services/job/job-service';
import { DisciplinaContextService } from '../../services/disciplina-context/disciplina-context-service';
import { DisciplinaService } from '../../services/disciplina/disciplina-service';

@Component({
  selector: 'app-gerenciamento',
  imports: [FormsModule, ReactiveFormsModule, CommonModule, RouterModule],
  templateUrl: './gerenciamento.html',
  styleUrl: './gerenciamento.scss',
  standalone: true
})
export class Gerenciamento implements OnInit {
  
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
    private toastr: ToastrService,
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

    this.managementForm = this.fb.group({
      tableType: ['', Validators.required]
    });
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
    }
  }

  onFiltroChange(): void {
    this.paginaAtual = 1;
  }

  limparFiltros(): void {
    this.filtroTermo = '';
    this.paginaAtual = 1;
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
}