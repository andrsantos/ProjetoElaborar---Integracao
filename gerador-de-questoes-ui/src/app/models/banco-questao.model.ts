export type TipoQuestao = 
  | 'MULTIPLA_ESCOLHA_5' 
  | 'MULTIPLA_ESCOLHA_4' 
  | 'VERDADEIRO_FALSO' 
  | 'DISCURSIVA';


export type NivelTecnico =
  | 'UNIVERSITARIO_INICIANTE'
  | 'UNIVERSITARIO_INTERMEDIARIO'
  | 'UNIVERSITARIO_AVANCADO';

export interface BancoQuestao {
  id?: string; 
    
  enunciado: string;
  
  tipo: TipoQuestao; 
  
  alternativas: { [key: string]: string }; 
  
  respostaCorreta: string;

  conceito: string;

  comentarioTecnico: string;

  competencia: string;

  dataCriacao?: string;

  nivel?: NivelTecnico;

  origem?: string;

  arquivoOrigem?: string;

  disciplinaId?: string;

  mostrarComentario?: boolean;

  isEditing?: boolean;
  
}