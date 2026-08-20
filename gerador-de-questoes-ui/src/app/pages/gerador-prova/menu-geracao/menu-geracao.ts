import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-menu-geracao',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './menu-geracao.html',
  styleUrl: './menu-geracao.scss',
})
export class MenuGeracao {


    constructor(
    private toastr: ToastrService
  ) {}

    bloquearFluxo(event: Event): void {
    event.preventDefault(); 
    this.toastr.warning(
      'Este fluxo está temporariamente fechado para manutenção. Aguarde atualizações.', 
      'Em Manutenção'
    );
  }

}
