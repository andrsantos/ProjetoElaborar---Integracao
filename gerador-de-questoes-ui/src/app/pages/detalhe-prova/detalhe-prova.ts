import { Component, Inject, OnInit, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe, isPlatformBrowser } from '@angular/common'; 
import { FormsModule } from '@angular/forms'; 
import { ActivatedRoute, RouterLink, Router } from '@angular/router'; 
import { CdkDragDrop, moveItemInArray, DragDropModule } from '@angular/cdk/drag-drop';
import { firstValueFrom } from 'rxjs';
import { ProvaSalva } from '../../models/prova-entity.model'; 
import { Questao } from '../../models/questao.model';
import { ToastrService } from 'ngx-toastr';
import { ProvaService } from '../../services/prova/prova-service';
import { NotificationService } from '../../services/notification/notification-service';
import { ProvaManualStateService } from '../../services/prova-manual-state/prova-manual-state';
import { IntegracaoAvaliarService } from '../../services/integracao-avaliar/integracao-avaliar';
import { DisciplinaService } from '../../services/disciplina/disciplina-service';
import { DisciplinaContextService } from '../../services/disciplina-context/disciplina-context-service';

@Component({
  selector: 'app-detalhe-prova',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe, FormsModule, DragDropModule], 
  templateUrl: './detalhe-prova.html', 
  styleUrls: ['./detalhe-prova.scss']    
})
export class DetalheProva implements OnInit {

  public provaAtual: ProvaSalva | null = null; 
  private provaId: string | null = null; 
  
  public isSwapping: { [key: number]: boolean } = {};

  public questaoEmEdicaoId: string | null = null;
  public questaoRascunho: any = {}; 

  public isDeleteModalVisible = false;
  public isDeleting = false;
  public isDownloading = false;
  public isExporting = false;
  public isSavingFinal = false;
  public isGenerating = false; 

  public topicosDisponiveis: string[] = [];
  public disciplinaAtivaId: string | null = null;
  public nomeDisciplinaAtiva: string = '';
  public carregandoNomeDisciplina: boolean = true;
  
  objectKeys = Object.keys;

  public isCatalogoAberto = false;
  public isBuscandoCatalogo = false;
  public questoesCatalogo: Questao[] = [];
  public conceitoAtualCatalogo = '';
  public indexEmSubstituicaoManual: number | null = null;
  public enunciadoReferenciaIa = '';




  constructor(
    private provaService: ProvaService,
    private route: ActivatedRoute,
    private router: Router,
    private notificationService: NotificationService,
    private toastr: ToastrService,
    private stateService: ProvaManualStateService,
    private integracaoAvaliarService: IntegracaoAvaliarService,
    private contextService: DisciplinaContextService,
    private disciplinaService: DisciplinaService,
    private cdr: ChangeDetectorRef, 
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

    this.carregarProva();
    this.carregarTopicos(); 
  }

  carregarTopicos() {
    this.provaService.getTopicosDisponiveis(this.disciplinaAtivaId!).subscribe(topicos => {
      this.topicosDisponiveis = topicos;
    });
  }

  carregarProva() {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;
    this.provaId = id;

    this.provaService.getDetalheProva(id).subscribe({
      next: (prova) => {
        this.provaAtual = prova;
        this.verificarRetornoDoBanco(); 
        console.log("Prova", this.provaAtual);
      },
      error: () => this.toastr.error('Erro ao carregar a prova.')
    });
  }

  verificarRetornoDoBanco(): void {
    if (!this.provaAtual) return;
    
    const estado = this.stateService.lerEstado();
    const questaoDoBanco = this.stateService.getAndClearQuestaoSelecionada();

    if (estado && estado.provaId === this.provaId && estado.idQuestaoEdicao && questaoDoBanco) {
      const index = this.provaAtual.questoes.findIndex(q => q.id === estado.idQuestaoEdicao);
      
      if (index !== -1) {
        this.provaAtual.questoes[index] = { ...questaoDoBanco };
        this.toastr.info('Questão do banco carregada. Salve a prova para confirmar.', 'Atenção');
      }
      this.stateService.limparEstado();
    }
  }

  drop(event: CdkDragDrop<Questao[]>) {
    if (this.provaAtual && this.provaAtual.questoes) {
      moveItemInArray(this.provaAtual.questoes, event.previousIndex, event.currentIndex);
    }
  }

  habilitarEdicao(questao: Questao) {
    this.questaoEmEdicaoId = questao.id || null;
    this.questaoRascunho = JSON.parse(JSON.stringify(questao));
  }

  cancelarEdicao() {
    this.questaoEmEdicaoId = null;
    this.questaoRascunho = {};
  }

  confirmarEdicaoInLine(index: number) {
    if (!this.provaAtual) return;
    
    this.provaAtual.questoes[index] = { ...this.questaoRascunho };
    this.cancelarEdicao();
    this.toastr.success('Questão editada localmente. Salve a prova para confirmar.', 'Editada');
  }

  getTipoQuestaoEdicao(): 'M5' | 'M4' | 'VF' | 'DISC' {
    if (!this.questaoRascunho) return 'DISC';

    const alts = this.questaoRascunho.alternativas || {};
    const has = (key: string) => alts[key] && alts[key].trim() !== '';

    if (has('a') && has('b') && has('c') && has('d') && has('e')) return 'M5';
    if (has('a') && has('b') && has('c') && has('d')) return 'M4';

    const resp = (this.questaoRascunho.respostaCorreta || '').toUpperCase().trim();
    if (resp === 'V' || resp === 'F') return 'VF';

    return 'DISC';
  }

  removerQuestao(index: number) {
    if (!this.provaAtual) return;
    this.provaAtual.questoes.splice(index, 1);
    this.toastr.info('Questão removida da lista.', 'Removida');
  }



  substituirQuestao(questaoAntiga: Questao, index: number) {
  if (!this.provaAtual || !this.disciplinaAtivaId) return;

  const conceitoAlvo = questaoAntiga.conceito;
  
  if (!conceitoAlvo) {
    this.toastr.warning('Esta questão não possui um conceito definido para buscar semelhantes.');
    return;
  }

  this.isSwapping[index] = true;

  const idsNaProva = this.provaAtual.questoes
    .map(q => q.id)
    .filter(id => id !== undefined) as string[];


    this.provaService.substituirQuestaoPorAleatoria(this.disciplinaAtivaId, conceitoAlvo, idsNaProva)
      .subscribe({
        next: (novaQuestao) => {
          if (this.provaAtual) {
            this.provaAtual.questoes[index] = novaQuestao;
            this.cdr.detectChanges(); 
          }

          this.toastr.success('Questão substituída com sucesso!', 'Trocada!');
          this.isSwapping[index] = false;
        },
        error: (err) => {
          if (err.status === 404) {
            this.toastr.info('Banco sem questões inéditas. A IA está gerando uma nova...', 'Criando...');
            
            this.provaService.gerarQuestaoSubstitutaIa(this.disciplinaAtivaId!, conceitoAlvo, questaoAntiga.enunciado)
              .subscribe({
                next: (questaoGeradaIa) => {
                  if (this.provaAtual) {
                    this.provaAtual.questoes[index] = questaoGeradaIa;
                    this.cdr.detectChanges();
                  }
                  this.toastr.success('Questão inédita gerada pela IA!', 'Mágica Feita!');
                  this.isSwapping[index] = false;
                },
                error: (errIa) => {
                  this.isSwapping[index] = false;
                  this.toastr.error('A IA falhou ao gerar a questão inédita.', 'Erro');
                }
              });

          } else {
            this.isSwapping[index] = false;
            this.toastr.error('Erro ao tentar substituir a questão.');
          }
        }
      });


}


  abrirModalBuscaAvulsa() {
    this.toastr.info('Em breve: Painel de inclusão de novas questões!', 'Adicionar');
  }

  salvarProvaFinal() {
    if (!this.provaAtual || !this.provaId) return;
    this.isSavingFinal = true;
    
    // Aqui você conectará com o endpoint de PUT do Spring Boot:
    // Ex: this.provaService.atualizarProva(this.provaId, this.provaAtual).subscribe(...)
    setTimeout(() => {
      this.isSavingFinal = false;
      this.toastr.success('Prova atualizada com sucesso no banco!', 'Salvo!');
    }, 1000); 
  }

  temAlternativasValidas(alternativas: any): boolean {
    if (!alternativas) return false;
    return Object.values(alternativas).some((v: any) => v && v.trim() !== '');
  }
  

  openDeleteModal(): void { 
    this.isDeleteModalVisible = true; 
  }
  
  closeDeleteModal(): void { 
    this.isDeleteModalVisible = false; 
  }
  
  onConfirmDelete(): void {
     if (!this.provaId) return;
     
     this.isDeleting = true;
     
     this.provaService.deleteProva(this.provaId).subscribe({
        next: () => {
           this.isDeleting = false;
           this.toastr.success('Prova excluída com sucesso!', 'Excluída'); 
           this.router.navigate(['/provas-salvas']); 
        },
        error: () => { 
          this.isDeleting = false; 
          this.isDeleteModalVisible = false; 
          this.toastr.error('Erro ao tentar excluir a prova.', 'Falha');
        }
     });
  }

  onDownloadPdf(): void {
     if (!this.provaId) return;
     this.isDownloading = true;
     this.provaService.downloadProvaSalvaPdf(this.provaId).subscribe({
        next: (blob) => {
           const url = window.URL.createObjectURL(blob);
           const a = document.createElement('a');
           a.href = url;
           a.download = `prova_${this.provaId}.pdf`;
           a.click();
           this.isDownloading = false;
        },
        error: () => this.isDownloading = false
     });
  }

  exportarFormatoAvaliar() {
    this.isExporting = true;
    
    try {
      if (!this.provaAtual || !this.provaAtual.questoes || this.provaAtual.questoes.length === 0) {
        this.toastr.warning("A prova está vazia.", "Aviso");
        this.isExporting = false;
        return;
      }

      let numero = 1;
      const payloadFormatado = this.provaAtual.questoes.map(q => ({
        numeroQuestao: numero++,
        enunciado: q.enunciado,
        alternativas: q.alternativas,
        respostaCorreta: q.respostaCorreta
      }));

      this.integracaoAvaliarService.exportarFormatoAvaliar(payloadFormatado).subscribe({
        next: (data: string) => {
          this.toastr.success("Prova exportada para avaliação com sucesso!", 'Sucesso!');
          if (data && data.trim().length > 0) {
            this.baixarArquivoTxt(data);
          } else {
            this.toastr.warning("O servidor retornou uma prova vazia.", "Aviso");
          }
          this.isExporting = false;
        },
        error: (err: any) => {
          console.error("Erro ao exportar prova para avaliação:", err);
          this.toastr.error("Falha ao exportar prova para avaliação.", 'Erro');
          this.isExporting = false;
        }
      });
      
    } catch (error) {
       console.error("Erro ao processar prova para exportação:", error);
       this.toastr.error("Falha ao ler os dados da prova.", 'Erro');
       this.isExporting = false;
    }
  }

  private baixarArquivoTxt(conteudo: string) {
    const blob = new Blob([conteudo], { type: 'text/plain;charset=utf-8' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    
    const dataAtual = new Date().toLocaleDateString('pt-BR').replace(/\//g, '-');
    link.download = `prova-avaliacao-${dataAtual}.txt`;
    
    link.click();
    window.URL.revokeObjectURL(url);
  }


  abrirCatalogoManual(questaoAtual: Questao, index: number) {
    if (!this.provaAtual || !this.disciplinaAtivaId) return;
    
    this.conceitoAtualCatalogo = questaoAtual.conceito || 'Geral';
    this.indexEmSubstituicaoManual = index;
    this.enunciadoReferenciaIa = questaoAtual.enunciado;
    
    this.isCatalogoAberto = true;
    this.isBuscandoCatalogo = true;
    this.questoesCatalogo = [];

    const idsNaProva = this.provaAtual.questoes
      .map(q => q.id)
      .filter(id => id !== undefined) as string[];

    this.provaService.buscarCatalogoSubstituicao(this.disciplinaAtivaId, this.conceitoAtualCatalogo, idsNaProva)
      .subscribe({
        next: (questoes) => {
          this.questoesCatalogo = questoes.filter(qCatalogo => 
            qCatalogo.id && !idsNaProva.includes(qCatalogo.id)
          );
          this.isBuscandoCatalogo = false;
        },
        error: () => {
          this.toastr.error('Erro ao buscar o catálogo.');
          this.isBuscandoCatalogo = false;
          this.fecharCatalogo();
        }
      });
  }

  fecharCatalogo() {
    this.isCatalogoAberto = false;
    this.questoesCatalogo = [];
    this.indexEmSubstituicaoManual = null;
    this.conceitoAtualCatalogo = '';
    this.enunciadoReferenciaIa = '';
  }

  selecionarQuestaoDoCatalogo(novaQuestao: Questao) {
    if (this.indexEmSubstituicaoManual === null || !this.provaAtual) return;

    this.provaAtual.questoes[this.indexEmSubstituicaoManual] = novaQuestao;
    this.cdr.detectChanges(); 

    this.toastr.success('Questão selecionada do catálogo!', 'Atualizado');
    this.fecharCatalogo();
  }

  fecharCatalogoEChamarIa() {
    if (this.indexEmSubstituicaoManual === null || !this.disciplinaAtivaId) return;

    const index = this.indexEmSubstituicaoManual;
    const conceito = this.conceitoAtualCatalogo;
    const enunciadoParaEvitar = this.enunciadoReferenciaIa;
    
    this.fecharCatalogo();
    
    this.isSwapping[index] = true;
    this.toastr.info('A IA está gerando uma questão inédita...', 'Criando...');

    this.provaService.gerarQuestaoSubstitutaIa(this.disciplinaAtivaId, conceito, enunciadoParaEvitar)
      .subscribe({
        next: (questaoGeradaIa) => {
          if (this.provaAtual) {
            this.provaAtual.questoes[index] = questaoGeradaIa;
            this.cdr.detectChanges();
          }
          this.toastr.success('Questão inédita gerada pela IA!', 'Mágica Feita!');
          this.isSwapping[index] = false;
        },
        error: () => {
          this.isSwapping[index] = false;
          this.toastr.error('A IA falhou ao gerar a questão inédita.', 'Erro');
        }
      });
  }


  



}