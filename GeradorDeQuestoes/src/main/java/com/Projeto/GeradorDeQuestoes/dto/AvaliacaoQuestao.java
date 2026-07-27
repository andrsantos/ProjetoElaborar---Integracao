package com.Projeto.GeradorDeQuestoes.dto;

public class AvaliacaoQuestao {
    
    private String competencia;
    private String comentarioTecnico;

    public AvaliacaoQuestao(String competencia, String comentarioTecnico) {
        this.competencia = competencia;
        this.comentarioTecnico = comentarioTecnico;
    }
    
    public String getCompetencia() {
        return this.competencia;
    }

    public void setCompetencia(String competencia) {
        this.competencia = competencia;
    }

    public String getComentarioTecnico() {
        return this.comentarioTecnico;
    }

    public void setComentarioTecnico(String comentarioTecnico) {
        this.comentarioTecnico = comentarioTecnico;
    }

    
}
