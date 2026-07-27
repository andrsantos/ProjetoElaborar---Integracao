import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, DatePipe, isPlatformBrowser } from '@angular/common'; 
import { RouterLink, Router } from '@angular/router'; 
import { Observable } from 'rxjs';
import { ProvaInfo } from '../../models/prova-info.model';
import { ProvaService } from '../../services/prova/prova-service';
import { NotificationService } from '../../services/notification/notification-service';
import { ToastrService } from 'ngx-toastr';
import { DisciplinaContextService } from '../../services/disciplina-context/disciplina-context-service';
import { DisciplinaService } from '../../services/disciplina/disciplina-service';

@Component({
  selector: 'app-provas-salvas',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe], 
  templateUrl: './provas-salvas.html', 
  styleUrls: ['./provas-salvas.scss']    
})
export class ProvasSalvas implements OnInit {
  
  objectKeys = Object.keys;
  public provas$!: Observable<ProvaInfo[]>;
  
  public disciplinaAtivaId: string | null = null;
  public nomeDisciplinaAtiva: string = '';
  public carregandoNomeDisciplina: boolean = true;

  constructor(
    private provaService: ProvaService,
    private notificationService: NotificationService,
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

    this.showToastOnLoad();
    
    this.provas$ = this.provaService.getProvasSalvas(); 
  }

  private showToastOnLoad(): void {
    const message = this.notificationService.getAndClearMessage();
    if (message) {
      this.toastr.success(message, 'Sucesso!');
    }
  }
}