import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { BancoQuestao } from '../../../models/banco-questao.model';
import { BancoQuestoesService } from '../../../services/banco-questoes/banco-questoes';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-questoes-documento',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './questoes-documento.html',
  styleUrl: './questoes-documento.scss',
})
export class QuestoesDocumento implements OnInit {
  
  public provaId: string = '';
  public questoes: BancoQuestao[] = [];
  public isLoading: boolean = true;

  public paginaAtual: number = 1;
  public itensPorPagina: number = 5;

  private backupQuestao: { [key: string]: any } = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bancoQuestoesService: BancoQuestoesService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.provaId = this.route.snapshot.paramMap.get('id') || '';

    if (this.provaId) {
      this.carregarQuestoes();
    } else {
      this.toastr.error('Identificador da prova não encontrado.', 'Erro de Rota');
      this.voltar();
    }
  }

  carregarQuestoes(): void {
    this.isLoading = true;
    this.bancoQuestoesService.listarQuestoesDaProva(this.provaId).subscribe({
      next: (res) => {
        this.questoes = res || [];
        console.log("Questoes", this.questoes);
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar questões:', err);
        this.toastr.error('Não foi possível carregar as questões desta prova.', 'Erro');
        this.isLoading = false;
      }
    });
  }

  get totalPaginas(): number {
    return Math.ceil(this.questoes.length / this.itensPorPagina) || 1;
  }

  get questoesPaginadas(): BancoQuestao[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    const fim = inicio + this.itensPorPagina;
    return this.questoes.slice(inicio, fim);
  }

  mudarPagina(proxima: boolean): void {
    if (proxima && this.paginaAtual < this.totalPaginas) {
      this.paginaAtual++;
    } else if (!proxima && this.paginaAtual > 1) {
      this.paginaAtual--;
    }
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }


  habilitarEdicao(questao: BancoQuestao): void {
    this.backupQuestao[questao.id!] = JSON.parse(JSON.stringify(questao));
  
    questao['isEditing'] = true;
  }

  cancelarEdicao(questao: BancoQuestao): void {
    if (this.backupQuestao[questao.id!]) {
      const bkp = this.backupQuestao[questao.id!];
      questao.enunciado = bkp.enunciado;
      questao.alternativas = bkp.alternativas;
      questao.respostaCorreta = bkp.respostaCorreta;
      questao.comentarioTecnico = bkp.comentarioTecnico;
    }
    questao['isEditing'] = false;
  }

  salvarEdicao(questao: BancoQuestao): void {
    questao['isEditing'] = false;
    
    this.bancoQuestoesService.atualizarQuestao(questao.id!, questao).subscribe({
      next: () => {
        this.toastr.success('Questão atualizada com sucesso!', 'Salvo');
        delete this.backupQuestao[questao.id!];
      },
      error: (err) => {
        console.error('Erro ao atualizar:', err);
        this.toastr.error('Erro ao salvar as alterações. As mudanças foram revertidas.', 'Erro');
        this.cancelarEdicao(questao); 
      }
    });
  }

  excluirQuestao(questao: BancoQuestao): void {
    const confirmacao = window.confirm('Tem certeza que deseja excluir esta questão permanentemente?');
    
    if (confirmacao && questao.id) {
      this.bancoQuestoesService.excluirQuestao(questao.id).subscribe({
        next: () => {
          this.questoes = this.questoes.filter(q => q.id !== questao.id);
          
          if (this.questoesPaginadas.length === 0 && this.paginaAtual > 1) {
            this.paginaAtual--;
          }
          
          this.toastr.success('Questão excluída com sucesso!', 'Exclusão');
        },
        error: (err) => {
          console.error('Erro ao excluir:', err);
          this.toastr.error('Ocorreu um erro ao excluir a questão.', 'Erro');
        }
      });
    }
  }

  voltar(): void {
    this.router.navigate(['/gerenciamento']);
  }
}