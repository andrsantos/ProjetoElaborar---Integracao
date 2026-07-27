import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router'; 
import { DisciplinaService } from '../../services/disciplina/disciplina-service';
import { HomeDisciplinas } from '../../pages/home-disciplinas/home-disciplinas';
import { Disciplina } from '../../models/disciplina.model';
import { DisciplinaContextService } from '../../services/disciplina-context/disciplina-context-service';
import { ToastrService } from 'ngx-toastr';


@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink], 
  templateUrl: './dashboard.html', 
  styleUrls: ['./dashboard.scss']   
})
export class Dashboard implements OnInit {
  
  public disciplinaId: string | null = null;
  public disciplinaAtual: Disciplina | null = null;
  public carregando: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private disciplinaService: DisciplinaService,
    private contextService: DisciplinaContextService,
    private toastr: ToastrService,
    private router: Router
  ) {}

    ngOnInit(): void {

      this.disciplinaId = this.route.snapshot.paramMap.get('idDisciplina');

      if (this.disciplinaId) {

        this.contextService.setDisciplinaAtiva(this.disciplinaId);
        this.carregarDadosDaDisciplina(this.disciplinaId);
        
      }
  }

  carregarDadosDaDisciplina(id: string): void {
    this.disciplinaService.buscarPorId(id).subscribe({
      next: (disciplina) => {
        this.disciplinaAtual = disciplina;
        this.carregando = false;
      },
      error: (error) => {
        console.error('Erro ao buscar detalhes da disciplina:', error);
        this.carregando = false;
      }
    });
  }


  excluirDisciplina(): void {
    const idDisciplina = this.disciplinaAtual?.id || this.disciplinaId; 

    if (!idDisciplina) {
      this.toastr.error('Nenhuma disciplina selecionada para exclusão.');
      return;
    }

    const mensagem = '⚠️ ATENÇÃO: Tem certeza absoluta que deseja excluir este ambiente de estudos?\n\n' +
                     'Todos os conceitos, questões, documentos e configurações vinculados a esta disciplina serão APAGADOS PERMANENTEMENTE.\n\n' +
                     'Esta ação NÃO pode ser desfeita. Deseja continuar?';

    const confirmacao = window.confirm(mensagem);

    if (confirmacao) {      
      this.disciplinaService.deletarDisciplina(idDisciplina).subscribe({
        next: () => {
          this.toastr.success('Disciplina e todos os seus dados foram excluídos com sucesso!', 'Exclusão Concluída');          
          this.router.navigate(['/']);
        },
        error: (erro) => {
          console.error('Erro ao excluir disciplina:', erro);
          this.toastr.error(
            erro.error?.erro || 'Ocorreu um erro ao tentar excluir a disciplina.', 
            'Falha na Exclusão'
          );
        }
      });
    }
  }



}