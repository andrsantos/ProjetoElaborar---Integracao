import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrls: ['./login.scss']
})
export class Login {
  public credenciais = {
    email: '',
    senha: ''
  };
  
  public isLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  fazerLogin(): void {
    if (!this.credenciais.email || !this.credenciais.senha) {
      this.toastr.warning('Preencha o e-mail e a senha para continuar.', 'Atenção');
      return;
    }

    this.isLoading = true;

    this.authService.login(this.credenciais).subscribe({
      next: () => {
        this.isLoading = false;
        this.toastr.success('Login realizado com sucesso!', 'Bem-vindo(a)');
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 403 || err.status === 401) {
          this.toastr.error('E-mail ou senha incorretos.', 'Falha na Autenticação');
        } else {
          this.toastr.error('Não foi possível conectar ao servidor.', 'Erro');
        }
      }
    });
  }
}