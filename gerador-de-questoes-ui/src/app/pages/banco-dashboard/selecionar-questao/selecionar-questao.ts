import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { BancoQuestao } from '../../../models/banco-questao.model';
import { BancoQuestoesService } from '../../../services/banco-questoes/banco-questoes';
import { ProvaManualStateService } from '../../../services/prova-manual-state/prova-manual-state';
import { ProvaService } from '../../../services/prova/prova-service';

@Component({
  selector: 'app-selecionar-questao',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './selecionar-questao.html',
  styleUrls: ['./selecionar-questao.scss']
})
export class SelecionarQuestao implements OnInit {

  questoes: BancoQuestao[] = [];
  isLoading = false;
  objectKeys = Object.keys;

  constructor(
    private bancoService: BancoQuestoesService,
    private stateService: ProvaManualStateService,
    private provaService: ProvaService, 
    private router: Router,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.isLoading = true;
    this.bancoService.listarTodas().subscribe({ 
      next: (data) => {
        this.questoes = data;
        this.isLoading = false;
      },
      error: () => {
        this.toastr.error('Erro ao carregar questões.');
        this.isLoading = false;
      }
    });
  }

  onSelecionar(questao: BancoQuestao): void {
    const estado = this.stateService.lerEstado();

    if (estado && estado.idQuestaoEdicao && estado.returnUrl) {
      
      this.isLoading = true; 

      const questaoParaSalvar: any = {
        ...questao,
        id: estado.idQuestaoEdicao 
      };

      this.provaService.atualizarQuestaoExistente(estado.idQuestaoEdicao, questaoParaSalvar)
        .subscribe({
          next: () => {
            this.toastr.success('Questão substituída com sucesso!');
            
            this.stateService.limparEstado();
            
            this.router.navigateByUrl(estado.returnUrl!);
          },
          error: (err) => {
            console.error(err);
            this.toastr.error('Erro ao substituir a questão.');
            this.isLoading = false;
          }
        });

    } 
    else {
      this.stateService.definirQuestaoEscolhida(questao as any); 
      this.toastr.success('Questão selecionada!');
      
      if (estado && estado.returnUrl) {
        this.router.navigateByUrl(estado.returnUrl);
      } else {
        this.router.navigate(['/gerar-prova/manual']);
      }
    }
  }

  onCancelar(): void {
    const estado = this.stateService.lerEstado();
    
    if (estado && estado.returnUrl) {
      this.router.navigateByUrl(estado.returnUrl);
    } else {
      this.router.navigate(['/gerar-prova/manual']);
    }
  }
}