import { Injectable } from '@angular/core';
import { Questao } from '../../models/questao.model';

export interface EstadoProvaManual {
  provaId: string | null;
  quantidadeDesejada?: number | null; 
  cards?: any[]; 
  indiceEdicao?: number | null; 
  idQuestaoEdicao?: string | null; 
  returnUrl?: string; 
}

@Injectable({
  providedIn: 'root'
})
export class ProvaManualStateService {

  private estado: EstadoProvaManual | null = null;
  
  private questaoTemp: Questao | null = null;

  constructor() { }

  salvarEstado(dados: EstadoProvaManual): void {
    this.estado = dados;
  }

  lerEstado(): EstadoProvaManual | null {
    return this.estado;
  }

  definirQuestaoEscolhida(questao: Questao): void {
    this.questaoTemp = questao;
    if (this.estado && this.estado.cards && this.estado.indiceEdicao !== null && this.estado.indiceEdicao !== undefined) {
      this.estado.cards[this.estado.indiceEdicao].questaoPreenchida = questao;
      this.estado.cards[this.estado.indiceEdicao].usarBanco = 'S'; 
      this.estado.cards[this.estado.indiceEdicao].usarIA = 'N';
      this.estado.indiceEdicao = null; 
    }
  }

  getAndClearQuestaoSelecionada(): Questao | null {
    const q = this.questaoTemp;
    this.questaoTemp = null;
    return q;
  }

  limparEstado(): void {
    this.estado = null;
    this.questaoTemp = null;
  }

  temEstadoSalvo(): boolean {
    return this.estado !== null;
  }
}