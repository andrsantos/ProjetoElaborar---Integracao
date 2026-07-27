import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';
import { BancoQuestoesService } from '../../../services/banco-questoes/banco-questoes';

@Component({
  selector: 'app-banco-questoes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './banco-questoes.html',
  styleUrls: ['./banco-questoes.scss']
})
export class BancoQuestoes {

  tipos = [
    { valor: 'MULTIPLA_ESCOLHA_4', label: 'Múltipla Escolha (4 Alternativas)' },
    { valor: 'MULTIPLA_ESCOLHA_5', label: 'Múltipla Escolha (5 Alternativas)' },
    { valor: 'VERDADEIRO_FALSO', label: 'Verdadeiro ou Falso' },
    { valor: 'DISCURSIVA', label: 'Discursiva' }
  ];

  tipoSelecionado: string = '';

  novaQuestao = {
    topico: '',
    enunciado: '',
    alternativaA: '',
    alternativaB: '',
    alternativaC: '',
    alternativaD: '',
    alternativaE: '',
    respostaCorreta: ''
  };

  isSaving = false;

  constructor(
    private bancoService: BancoQuestoesService,
    private toastr: ToastrService,
    private router: Router
  ) {}

  onSalvar(): void {
    if (!this.validarFormulario()) return;

    this.isSaving = true;


    const payload = {
      topico: this.novaQuestao.topico,
      enunciado: this.novaQuestao.enunciado,
      tipo: this.tipoSelecionado,
      respostaCorreta: this.novaQuestao.respostaCorreta,
      alternativas: {
        'a': this.novaQuestao.alternativaA,
        'b': this.novaQuestao.alternativaB,
        'c': this.novaQuestao.alternativaC,
        'd': this.novaQuestao.alternativaD,
        'e': this.novaQuestao.alternativaE
      }
    };

    this.bancoService.salvarQuestao(payload).subscribe({
      next: () => {
        this.router.navigate(['/banco-questoes']);
        this.limparFormulario();
        this.toastr.success("Questão cadastrada com sucesso!",'Sucesso!');
        this.isSaving = false;
      },
      error: () => {
        this.toastr.error('Erro ao cadastrar questão.');
        this.isSaving = false;
      }
    });
  }

  validarFormulario(): boolean {
    if (!this.tipoSelecionado) {
      this.toastr.warning('Selecione um tipo de questão.');
      return false;
    }
    if (!this.novaQuestao.topico || !this.novaQuestao.enunciado) {
      this.toastr.warning('Preencha o tópico e o enunciado.');
      return false;
    }
    if (this.tipoSelecionado === 'MULTIPLA_ESCOLHA_4') {
      if (!this.novaQuestao.alternativaA || !this.novaQuestao.alternativaB || 
          !this.novaQuestao.alternativaC || !this.novaQuestao.alternativaD) {
        this.toastr.warning('Preencha todas as 4 alternativas.');
        return false;
      }
      if (!this.novaQuestao.respostaCorreta) {
        this.toastr.warning('Selecione a alternativa correta (gabarito).');
        return false;
      }
    }
    if(this.tipoSelecionado === 'MULTIPLA_ESCOLHA_5') {
      if (!this.novaQuestao.alternativaA || !this.novaQuestao.alternativaB || 
          !this.novaQuestao.alternativaC || !this.novaQuestao.alternativaD || !this.novaQuestao.alternativaE) {
        this.toastr.warning('Preencha todas as 5 alternativas.');
        return false;
      }
      if(!this.novaQuestao.respostaCorreta) {
        this.toastr.warning('Selecione a alternativa correta (gabarito).');
        return false;
      }
  }
      return true;
  }

  limparFormulario(): void {
    this.novaQuestao = {
      topico: '',
      enunciado: '',
      alternativaA: '',
      alternativaB: '',
      alternativaC: '',
      alternativaD: '',
      alternativaE: '',
      respostaCorreta: ''
    };
  }
}