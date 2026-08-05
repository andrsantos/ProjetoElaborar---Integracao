import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router'; 
import { DisciplinaContextService } from '../../services/disciplina-context/disciplina-context-service';
import { DisciplinaService } from '../../services/disciplina/disciplina-service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.scss']
})
export class Sidebar implements OnInit {
  
  temDisciplinaAtiva = false;
  nomeDisciplinaAtiva = ''; 

  linksInativos = [
    { path: '/', label: 'Início', exact: true }
  ];

  linksExibidos: any[] = [];

  constructor(
    private contextService: DisciplinaContextService,
    private disciplinaService: DisciplinaService,
    private router: Router 
  ) {}

  ngOnInit() {
    this.linksExibidos = this.linksInativos;

    this.contextService.getDisciplinaAtivaIdObservable().subscribe(id => {
      this.temDisciplinaAtiva = !!id; 
      
      if (id) {
        this.linksExibidos = [
          { path: '/', label: '⬅ Voltar para o início', exact: true, isExit: true },
          { path: `/painel/${id}`, label: 'Painel da Disciplina', exact: true }, 
          { path: '/gerenciamento', label: 'Gerenciamento', exact: false },
          { path: '/gerar-prova', label: 'Geração', exact: false },
          { path: '/banco-questoes', label: 'Banco de Questões', exact: false },
          { path: '/provas-salvas', label: 'Provas Salvas', exact: false },
          { path: '/alimentacao', label: 'Extração de Questões', exact: false },
        ];

        this.disciplinaService.buscarNomeDisciplina(id).subscribe({
          next: (resposta) => this.nomeDisciplinaAtiva = resposta.nome,
          error: () => this.nomeDisciplinaAtiva = 'Ambiente de Trabalho'
        });
      } else {
        this.nomeDisciplinaAtiva = ''; 
        this.linksExibidos = this.linksInativos;
      }
    });
  }

  lidarComClique(link: any, event: Event) {
    if (link.isExit) {
      event.preventDefault(); 
      this.contextService.limparContexto(); 
      this.router.navigate(['/']); 
    }
  }

  sairDoSistema(event: Event) {
    event.preventDefault();
    
    this.contextService.limparContexto();
    
    if (typeof window !== 'undefined' && window.localStorage) {
      localStorage.removeItem('token_elaborar');
    }
    
    this.router.navigate(['/login']);
  }
}