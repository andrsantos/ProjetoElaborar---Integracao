import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BancoQuestoesService } from '../../../../services/banco-questoes/banco-questoes';
import { DisciplinaContextService } from '../../../../services/disciplina-context/disciplina-context-service';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';
import { DisciplinaService } from '../../../../services/disciplina/disciplina-service';
import { ConceitoService } from '../../../../services/conceito/conceito-service';

interface Taxonomia {
  nomeDisciplina: string;
  palavrasChave: string[];
  topicos: string[];
}

@Component({
  selector: 'app-editar-taxonomia',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './editar-taxonomia.html',
  styleUrl: './editar-taxonomia.scss',
})
export class EditarTaxonomia implements OnInit {
  
  public quantidadeQuestoes: number = 0; 
  public jsonString: string = '';
  public jsonErro: string | null = null;
  public sincronizando: boolean = false;
  public carregando: boolean = true;
  public carregandoNomeDisciplina: boolean = true;

  public disciplinaAtivaId: string | null = null;
  conceitosDisponiveis: string[] = [];

  public taxonomia: Taxonomia = {
    nomeDisciplina: "Carregando...",
    palavrasChave: [],
    topicos: []
  };

  constructor(
    private bancoService: BancoQuestoesService,
    private contextService: DisciplinaContextService,
    @Inject(PLATFORM_ID) private platformId: Object,
    private toastr: ToastrService,
    private router: Router,
    private disciplinaService: DisciplinaService,
    private conceitoService: ConceitoService
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

    this.disciplinaService.buscarNomeDisciplina(this.disciplinaAtivaId).subscribe({
      next: (res) => {
        this.taxonomia.nomeDisciplina = res.nome;
        this.carregandoNomeDisciplina = false;
      },
      error: () => {
        this.taxonomia.nomeDisciplina = 'Ambiente de Trabalho';
        this.carregandoNomeDisciplina = false;
      }
    });

    this.buscarConceitosPorDisciplina(this.disciplinaAtivaId);
  }

  buscarConceitosPorDisciplina(disciplinaId: string) {
    this.carregando = true;
    this.bancoService.getConceitosPorDisciplina(disciplinaId).subscribe({
      next: (conceitos) => {
        this.conceitosDisponiveis = conceitos;
        this.taxonomia.topicos = [...conceitos]; 
        
        this.atualizarJsonView(); 
        this.carregando = false;
      },
      error: (err) => {
        console.error('Erro ao buscar conceitos para a disciplina:', err);
        this.toastr.error('Não foi possível carregar a árvore de conceitos.');
        this.carregando = false;
      }
    });
  }

  public onJsonChange(): void {
    try {
      const parsed = JSON.parse(this.jsonString);
      this.taxonomia = parsed;
      this.jsonErro = null; 
    } catch (e: any) {
      this.jsonErro = 'JSON Inválido: ' + e.message;
    }
  }

  public onVisualTreeChange(): void {
    this.atualizarJsonView();
  }

  private atualizarJsonView(): void {
    this.jsonString = JSON.stringify(this.taxonomia, null, 2);
    this.jsonErro = null;
  }

  public adicionarTopico(): void {
    this.taxonomia.topicos.unshift('Novo Tópico...');
    this.onVisualTreeChange();
  }

  public removerTopico(index: number): void {
    this.taxonomia.topicos.splice(index, 1);
    this.onVisualTreeChange();
  }

  public getIdentacaoNivel(topico: string): number {
    const match = topico.match(/^(\d+(\.\d+)*)/);
    if (match) {
      const niveis = match[0].split('.').length;
      return (niveis - 1) * 20; 
    }
    return 0;
  }

  public salvarERecatalogar(): void {
    if (this.jsonErro) {
      this.toastr.warning('Corrija os erros no JSON antes de salvar.');
      return;
    }

    if (!this.disciplinaAtivaId) {
      this.toastr.error('Erro: Nenhuma disciplina selecionada.');
      return;
    }

    this.sincronizando = true;
    console.log('Payload enviado para o backend:', this.taxonomia);

    this.conceitoService.sincronizarTaxonomia(this.disciplinaAtivaId, this.taxonomia).subscribe({
      next: () => {
        this.toastr.success('Taxonomia salva! O Agente iniciará a recatalogação em background. Acompanhe na aba Processamentos');
        this.sincronizando = false;
        
        this.router.navigate(['/banco-questoes/gerenciar']);
      },
      error: (err) => {
        console.error('Erro ao sincronizar taxonomia:', err);
        this.toastr.error('Falha ao salvar a taxonomia. Tente novamente mais tarde.');
        this.sincronizando = false;
      }
    });
    
  }

  public trackByFn(index: number, item: any): number {
    return index;
  }
}