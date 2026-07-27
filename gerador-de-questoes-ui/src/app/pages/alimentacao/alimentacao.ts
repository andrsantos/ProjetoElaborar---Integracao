import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AlimentacaoService } from '../../services/alimentacao/alimentacao-service';
import { DisciplinaContextService } from '../../services/disciplina-context/disciplina-context-service';
import { DisciplinaService } from '../../services/disciplina/disciplina-service';

@Component({
  selector: 'app-alimentacao',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './alimentacao.html',
  styleUrls: ['./alimentacao.scss']
})
export class Alimentacao implements OnInit {
  
  public arquivoQuestoes: File | null = null;
  public isHovering = false;
  public isUploading = false;

  public disciplinaAtivaId: string | null = null;
  public nomeDisciplinaAtiva: string = '';
  public carregandoNomeDisciplina: boolean = true;

  public isExtracaoModalOpen = false;
  public usarPromptPersonalizado = false;
  public promptPersonalizado = '';
  
  public modoExtracao: 'APENAS_ORIGINAIS' | 'ORIGINAIS_E_VARIACOES' | 'APENAS_VARIACOES' = 'APENAS_VARIACOES';

  constructor(
    private alimentacaoService: AlimentacaoService,
    private toastr: ToastrService,
    private router: Router,
    private contextService: DisciplinaContextService, 
    private disciplinaService: DisciplinaService,
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

  abrirModalExtracao(): void {
    this.isExtracaoModalOpen = true;
  }

  fecharModalExtracao(): void {
    if (this.isUploading) return; 
    
    this.isExtracaoModalOpen = false;
    this.removerArquivo();
    this.usarPromptPersonalizado = false;
    this.promptPersonalizado = '';
    this.modoExtracao = 'APENAS_VARIACOES'; 
  }

  onFileSelected(event: any): void {
    const file = event.target?.files?.[0];
    this.validarEAtribuirArquivo(file);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isHovering = false;
    const file = event.dataTransfer?.files[0];
    this.validarEAtribuirArquivo(file);
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isHovering = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.isHovering = false;
  }

  private validarEAtribuirArquivo(file: File | undefined): void {
    if (file?.type === 'application/pdf') {
      this.arquivoQuestoes = file;
    } else if (file) {
      this.toastr.error('Por favor, selecione apenas arquivos PDF.', 'Formato Inválido');
    }
  }

  removerArquivo(): void {
    this.arquivoQuestoes = null;
    this.isUploading = false;
  }

  iniciarProcessamento(): void {
    if (!this.arquivoQuestoes || !this.disciplinaAtivaId) return;

    this.isUploading = true;

    const promptParaEnviar = this.usarPromptPersonalizado ? this.promptPersonalizado : '';

    this.alimentacaoService.uploadQuestoesAsync(
      this.arquivoQuestoes, 
      this.disciplinaAtivaId, 
      promptParaEnviar, 
      this.modoExtracao
    ).subscribe({
      next: (response) => {
        this.isUploading = false;
        this.fecharModalExtracao(); 
        const jobId = response.jobId;
        this.toastr.success('Arquivo e instruções enviadas! Iniciando extração...');
        this.router.navigate(['/revisao', jobId]);
      },
      error: () => {
        this.toastr.error('Erro ao enviar o arquivo para processamento.');
        this.isUploading = false;
      }
    });
  }
}