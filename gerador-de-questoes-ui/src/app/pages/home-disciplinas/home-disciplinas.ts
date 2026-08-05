import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core'; // Não esqueça do OnInit aqui
import { Disciplina } from '../../models/disciplina.model';
import { DisciplinaService } from '../../services/disciplina/disciplina-service';
import { Router } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DisciplinaContextService } from '../../services/disciplina-context/disciplina-context-service'; 

@Component({
  selector: 'app-home-disciplinas',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './home-disciplinas.html',
  styleUrl: './home-disciplinas.scss',
})
export class HomeDisciplinas implements OnInit { 

  disciplinas: Disciplina[] = [];
  carregando = true;

  exibirModal = false;
  disciplinaForm: FormGroup;
  salvando = false;

  constructor(
    private disciplinaService: DisciplinaService,
    private router: Router,
    private fb: FormBuilder,
    private contextService: DisciplinaContextService,
    @Inject(PLATFORM_ID) private platformId: Object 
  ) {

    this.disciplinaForm = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(3)]],
      descricao: [''] 
    });

  }

  ngOnInit(): void {
    this.contextService.limparContexto();
    if (isPlatformBrowser(this.platformId)) {
      this.carregarDisciplinas();
    }
  }

  carregarDisciplinas() {
    this.disciplinaService.listarDisciplinas().subscribe({
      next: (dados) => {
        this.disciplinas = dados;
        this.carregando = false;
      },
      error: (err) => {
        console.error('Erro ao buscar disciplinas', err);
        this.carregando = false;
      }
    });
  }

  entrarNaDisciplina(id: string) {
    this.contextService.setDisciplinaAtiva(id);
    this.router.navigate(['/painel', id]);
  }

  novaDisciplina() {
    const nome = prompt('Digite o nome da nova disciplina:');
    if (nome && nome.trim() !== '') {
      this.disciplinaService.criarDisciplina({ nome }).subscribe(() => {
        this.carregarDisciplinas(); 
      });
    }
  }

  abrirModalNovaDisciplina() {
    this.disciplinaForm.reset();
    this.exibirModal = true;
  }

  fecharModal() {
    this.exibirModal = false;
  }

  salvarDisciplina() {
    if (this.disciplinaForm.invalid) return;

    this.salvando = true;
    const novaDisciplina = this.disciplinaForm.value;

    this.disciplinaService.criarDisciplina(novaDisciplina).subscribe({
      next: () => {
        this.salvando = false;
        this.fecharModal();
        this.carregarDisciplinas(); 
      },
      error: (err) => {
        console.error('Erro ao salvar:', err);
        this.salvando = false;
      }
    });
  }

}