import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { PromptService } from '../../../services/prompts/prompt-service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DisciplinaContextService } from '../../../services/disciplina-context/disciplina-context-service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-detalhe-prompt',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './detalhe-prompt.html',
  styleUrls: ['./detalhe-prompt.scss']
})
export class DetalhePrompt implements OnInit {
  
  isLoading: boolean = true; 
  isSaving: boolean = false;
  promptForm!: FormGroup;
  promptId: string | null = null; 
  public disciplinaAtivaId: string | null = null; 
  
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private promptService: PromptService,
    private fb: FormBuilder,
   private toastr: ToastrService,
    private contextService: DisciplinaContextService,
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

    this.promptForm = this.fb.group({
      nome: ['', Validators.required],
      nivel: [''],
      instrucao: ['', [Validators.required, Validators.minLength(20)]],
      ativo: [true] ,
      isPadrao: [false],
      disciplinaId: this.disciplinaAtivaId
    });

    this.route.queryParamMap.subscribe(params => {
      this.promptId = params.get('promptId');
      
      if (this.promptId) {
        this.carregarPromptPorId(this.promptId);
      } else {
        this.isLoading = false;
      }
    });
  }

  carregarPromptPorId(id: string): void {
      this.isLoading = true;
      this.promptService.buscarPorId(id)
        .pipe(finalize(() => this.isLoading = false))
        .subscribe({
          next: (prompt) => {
            this.promptForm.patchValue({
              nome: prompt.nome,
              nivel: prompt.nivel,
              instrucao: prompt.instrucao,
              ativo: prompt.ativo
            });
          },
          error: (err) => {
            console.error('Erro ao buscar o prompt:', err);
            alert('Não foi possível carregar os detalhes do padrão.');
            this.voltarParaPainel();
          }
        });
  }
    
  voltarParaPainel(): void {
    this.router.navigate(['/gerenciamento']);
  }

  salvarPrompt(): void {
    if (this.promptForm.invalid) {
      alert('Preencha os campos obrigatórios corretamente.');
      return;
    }

    this.isSaving = true;
    const dadosFormulario = this.promptForm.value;

    if (this.promptId) {
      console.log("Dados Formulário", dadosFormulario);
      this.promptService.editarPrompt(this.promptId, dadosFormulario)
        .pipe(finalize(() => this.isSaving = false))
        .subscribe({
          next: () => {
            this.voltarParaPainel();
          },
          error: (err) => {
            console.error('Erro ao editar prompt:', err);
            alert('Erro ao salvar as alterações.');
          }
        });
    } else {
      console.log("Dados Formulário", dadosFormulario);
      this.promptService.cadastrarPrompt(dadosFormulario)
        .pipe(finalize(() => this.isSaving = false))
        .subscribe({
          next: () => {
            this.voltarParaPainel();
          },
          error: (err) => {
            console.error('Erro ao salvar prompt:', err);
            alert('Erro ao criar o padrão.');
          }
        });
    }
  }

}