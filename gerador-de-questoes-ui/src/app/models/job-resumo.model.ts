export interface JobResumo {
  id: string;
  status: string;
  nomeOriginal: string;
  modoExtracao: string;
  mensagemErro: string;
  tipo: string; 
  dataCriacao: string;
  visualizado: boolean;
}