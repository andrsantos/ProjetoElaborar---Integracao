import { Questao } from "./questao.model";

export interface Prova {
    id: string;
    questoes: Questao[];
    disciplinaId: string;
}