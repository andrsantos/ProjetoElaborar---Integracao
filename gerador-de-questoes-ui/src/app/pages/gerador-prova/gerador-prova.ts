import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, Inject, PLATFORM_ID } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { DisciplinaContextService } from '../../services/disciplina-context/disciplina-context-service';
import { DisciplinaService } from '../../services/disciplina/disciplina-service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-gerador-prova',
  imports: [CommonModule, RouterLink],
  templateUrl: './gerador-prova.html',
  styleUrl: './gerador-prova.scss',
})
export class GeradorProva {


  public disciplinaAtivaId: string | null = null;
  public nomeDisciplinaAtiva: string = '';
  public carregandoNomeDisciplina: boolean = true;

  constructor(
    private contextService: DisciplinaContextService,
    private disciplinaService: DisciplinaService,
    private router: Router,
    private toastr: ToastrService,
    @Inject(PLATFORM_ID) private platformId: Object

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
        this.nomeDisciplinaAtiva = res.nome;
        this.carregandoNomeDisciplina = false;
      },
      error: () => {
        this.nomeDisciplinaAtiva = 'Ambiente de Trabalho';
        this.carregandoNomeDisciplina = false;
      }
    });
  }

}
