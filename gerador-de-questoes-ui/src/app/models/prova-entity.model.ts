import { Questao } from "./questao.model";

export interface ProvaSalva {
  id: string;
  titulo: string;
  dataCriacao: string;
  questoes: Questao[]; 
}