export type StatusJob = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'ERROR';

export interface ExtracaoJob {
  id: string;
  status: StatusJob; 
  nomeArquivo: string;
  tipo: string;
  dataCriacao: string; 
  resultadoJson?: string; 
  mensagemErro?: string;  
}