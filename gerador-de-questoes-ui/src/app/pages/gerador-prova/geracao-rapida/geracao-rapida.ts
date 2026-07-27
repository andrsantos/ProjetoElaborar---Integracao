import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { ProvaService } from '../../../services/prova/prova-service';
import { DisciplinaContextService } from '../../../services/disciplina-context/disciplina-context-service';
import { BancoQuestoesService } from '../../../services/banco-questoes/banco-questoes';

@Component({
  selector: 'app-geracao-rapida',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './geracao-rapida.html',
  styleUrls: ['./geracao-rapida.scss']
})
export class GeracaoRapida implements OnInit {

  public opcoesQuantidade = [5, 10, 15, 20];
  public opcoesNivel = [
    { label: 'Iniciante', valor: 'UNIVERSITARIO_INICIANTE' },
    { label: 'Intermediário', valor: 'UNIVERSITARIO_INTERMEDIARIO' },
    { label: 'Avançado', valor: 'UNIVERSITARIO_AVANCADO' },
    { label: 'Mesclado (Todos)', valor: 'MESCLADO' }
  ];

  public quantidadeSelecionada: number | null = 10; 
  public nivelSelecionado: string | null = 'UNIVERSITARIO_INTERMEDIARIO';
  public topicosSelecionados: string[] = [];
  public diretrizPrompt: string = '';

  public isGerando: boolean = false;
  public carregandoTopicos: boolean = false;
  public topicosDisponiveis: string[] = [];
  
  private disciplinaAtivaId: string | null = null;

  constructor(
    private toastr: ToastrService,
    private router: Router,
    private provaService: ProvaService,
    private contextService: DisciplinaContextService,
    private bancoQuestoesService: BancoQuestoesService
  ) {}

  ngOnInit(): void {
    this.disciplinaAtivaId = this.contextService.getDisciplinaAtivaId();
    
    if (!this.disciplinaAtivaId) {
      this.toastr.error('Nenhuma disciplina selecionada.');
      this.router.navigate(['/']);
      return;
    }

    this.carregarTopicosDaDisciplina();
  }

  carregarTopicosDaDisciplina(): void {
    this.carregandoTopicos = true;
    this.bancoQuestoesService.getConceitosPorDisciplina(this.disciplinaAtivaId!).subscribe({
      next: (conceitos: any[]) => {
        this.topicosDisponiveis = conceitos.map(c => c.nome || c);
        this.carregandoTopicos = false;
      },
      error: () => {
        this.toastr.error('Erro ao carregar os tópicos da disciplina.');
        this.carregandoTopicos = false;
      }
    });
  }

  selecionarQuantidade(qtd: number): void { this.quantidadeSelecionada = qtd; }
  selecionarNivel(nivel: string): void { this.nivelSelecionado = nivel; }

  toggleTopico(topico: string): void {
    const index = this.topicosSelecionados.indexOf(topico);
    if (index > -1) {
      this.topicosSelecionados.splice(index, 1);
    } else {
      this.topicosSelecionados.push(topico);
    }
  }

  isFormularioValido(): boolean {
    return this.quantidadeSelecionada !== null && 
           this.nivelSelecionado !== null && 
           this.topicosSelecionados.length > 0;
  }


  iniciarGeracao(): void {
    if (!this.isFormularioValido()) {
      this.toastr.warning('Selecione a quantidade, o nível e pelo menos um tópico.');
      return;
    }

    this.isGerando = true;
    this.toastr.info('A IA está selecionando as questões...', 'Aguarde');

    const payload = {
      disciplinaId: this.disciplinaAtivaId,
      quantidade: this.quantidadeSelecionada,
      nivel: this.nivelSelecionado,
      topicos: this.topicosSelecionados,
      diretriz: this.diretrizPrompt
    };

    this.provaService.gerarProvaExpressa(payload).subscribe({
      next: (provaGerada) => {
        
        provaGerada.disciplinaId = this.disciplinaAtivaId!;
        console.log("Prova Gerada para ser salvar no banco", provaGerada);
        
        this.provaService.salvarProvaNoBanco(provaGerada).subscribe({
          next: () => {
            this.isGerando = false;
            this.toastr.success('Prova Expressa gerada e salva com sucesso!', 'Excelente!');
            this.router.navigate(['/provas-salvas']); 
          },
          error: (erroSalvar) => {
            this.isGerando = false;
            this.toastr.error('A prova foi gerada, mas falhou ao tentar salvar no banco.', 'Atenção');
            console.error(erroSalvar);
          }
        });
        
      },
      error: (erroGeracao) => {
        this.isGerando = false;
        const mensagemErro = erroGeracao.error?.erro || 'Ocorreu um erro ao gerar a prova.';
        this.toastr.error(mensagemErro, 'Atenção');
        console.error(erroGeracao);
      }
    });
  }


}